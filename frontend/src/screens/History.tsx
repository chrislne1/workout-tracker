import { useEffect, useState } from 'react';
import * as api from '../api';
import type { Session } from '../types';
import { Card } from '../ui';

export default function History() {
  const [list, setList] = useState<Session[] | null>(null);
  const [error, setError] = useState('');
  const load = () => api.listSessions().then(setList).catch((e) => setError(e.message));
  useEffect(() => { load(); }, []);

  const remove = async (id: string) => {
    if (!confirm('Delete this workout? It will be removed from your analytics.')) return;
    await api.deleteSession(id); load();
  };

  return (
    <div className="space-y-3">
      <h1 className="text-3xl font-extrabold">History</h1>
      {error && <p className="text-sm text-down">{error}</p>}
      {list?.length === 0 && <Card><p className="font-semibold">No workouts yet</p><p className="text-sm text-mute">Start one from the Log tab.</p></Card>}
      {list?.map((s) => (
        <Card key={s.id}>
          <div className="flex items-start justify-between">
            <div>
              <p className="font-bold">{s.splitType[0] + s.splitType.slice(1).toLowerCase()} day</p>
              <p className="text-xs text-mute">{new Date(s.sessionDate + 'T00:00').toLocaleDateString(undefined, { weekday: 'short', month: 'short', day: 'numeric' })}</p>
            </div>
            <button onClick={() => remove(s.id)} className="tap -mr-2 -mt-2 px-3 text-sm font-semibold text-down">Delete</button>
          </div>
          <p className="mt-2 text-sm"><span className="font-bold">{Math.round(s.workingVolume).toLocaleString()}</span> lbs · {s.sets.filter((x) => x.setType !== 'WARMUP').length} working sets</p>
          {s.regressionReason && <span className="mt-2 inline-block rounded-full bg-raised px-2.5 py-1 text-xs font-semibold text-mute">{s.regressionReason.replace(/_/g, ' ').toLowerCase()}</span>}
        </Card>
      ))}
    </div>
  );
}
