import { useState } from 'react';
import Log from './screens/Log';
import Insights from './screens/Insights';
import History from './screens/History';

type Tab = 'log' | 'insights' | 'history';
const TABS: { id: Tab; label: string; icon: string }[] = [
  { id: 'log', label: 'Log', icon: 'M12 5v14M5 12h14' },
  { id: 'insights', label: 'Insights', icon: 'M4 19V9m6 10V5m6 14v-7m4 7H2' },
  { id: 'history', label: 'History', icon: 'M12 8v4l3 2M21 12a9 9 0 11-18 0 9 9 0 0118 0z' },
];

export default function App() {
  const [tab, setTab] = useState<Tab>('log');
  return (
    <div className="mx-auto flex h-full max-w-md flex-col">
      <main className="flex-1 overflow-y-auto px-4 pb-28 pt-4">
        {/* Screens stay mounted so an in-progress workout is never lost when switching tabs */}
        <div hidden={tab !== 'log'}><Log /></div>
        {tab === 'insights' && <Insights />}
        {tab === 'history' && <History />}
      </main>
      <nav className="fixed inset-x-0 bottom-0 mx-auto flex max-w-md border-t border-line bg-bg/95 backdrop-blur"
        style={{ paddingBottom: 'env(safe-area-inset-bottom)' }}>
        {TABS.map((t) => (
          <button key={t.id} onClick={() => setTab(t.id)} aria-current={tab === t.id}
            className={`tap flex flex-1 flex-col items-center gap-1 py-2.5 text-xs font-semibold ${tab === t.id ? 'text-brand2' : 'text-mute'}`}>
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d={t.icon} /></svg>
            {t.label}
          </button>
        ))}
      </nav>
    </div>
  );
}
