// Service worker: app shell cache + cached exercise taxonomy so sets can be logged with poor signal.
const SHELL = 'overload-shell-v1';
const DATA = 'overload-data-v1';

self.addEventListener('install', (e) => {
  e.waitUntil(caches.open(SHELL).then((c) => c.addAll(['/', '/manifest.json', '/icon-192.png'])).then(() => self.skipWaiting()));
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) => Promise.all(keys.filter((k) => ![SHELL, DATA].includes(k)).map((k) => caches.delete(k))))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (e) => {
  const req = e.request;
  if (req.method !== 'GET') return;
  const url = new URL(req.url);

  // Static taxonomy: network first, fall back to last known copy.
  if (url.pathname === '/api/exercises') {
    e.respondWith(
      fetch(req).then((res) => { const copy = res.clone(); caches.open(DATA).then((c) => c.put(req, copy)); return res; })
        .catch(() => caches.match(req))
    );
    return;
  }
  if (url.pathname.startsWith('/api/')) return; // everything else is live data

  // App shell: stale-while-revalidate, SPA fallback when offline.
  e.respondWith(
    caches.match(req).then((hit) => {
      const net = fetch(req).then((res) => { const copy = res.clone(); caches.open(SHELL).then((c) => c.put(req, copy)); return res; })
        .catch(() => hit || caches.match('/'));
      return hit || net;
    })
  );
});
