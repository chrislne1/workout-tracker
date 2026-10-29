import { useEffect, useState } from 'react';
import * as api from '../api';
import type { Movement, ProgressTrend, VolumeDistribution, Window } from '../types';
import { Card, MUSCLE_COLORS, Ring, Segmented } from '../ui';

const WINDOWS: { value: Window; label: string }[] = [
  { value: 'D30', label: '1M' }, { value: 'D90', label: '3M' }, { value: 'M6', label: '6M' }, { value: 'Y1', label: '1Y' }, { value: 'AOT', label: 'All' },
];

export default function Insights() {
  const [win, setWin] = useState<Window>('D30');
  const [data, setData] = useState<VolumeDistribution | null>(null);
  const [movements, setMovements] = useState<Movement[]>([]);
  const [vid, setVid] = useState<number | null>(null);
  const [trend, setTrend] = useState<ProgressTrend | null>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.getMovements().then((items) => {
      setMovements(items);
      setVid(items[0]?.weightSystems[0]?.variantId ?? null);
    }).catch(() => undefined);
  }, []);
  useEffect(() => { setError(''); api.getVolume(win).then(setData).catch((e) => setError(e.message)); }, [win]);
  useEffect(() => { if (vid) api.getProgress(vid).then(setTrend).catch(() => setTrend(null)); }, [vid]);

  const empty = data && data.muscles.every((m) => m.volume === 0);
  return (
    <div className="space-y-4">
      <h1 className="text-3xl font-extrabold">Insights</h1>
      <Segmented options={WINDOWS} value={win} onChange={setWin} />
      {error && <p className="text-sm text-down">{error}</p>}
      {empty && <Card><p className="font-semibold">No working sets in this window yet</p><p className="text-sm text-mute">Log a workout and your weekly sets per muscle will show up here.</p></Card>}

      {data && !empty && (
        <>
          <Card>
            <h2 className="mb-3 font-bold">Weekly hard sets vs. target</h2>
            <div className="grid grid-cols-3 gap-y-5">
              {data.muscles.map((m) => (
                <div key={m.muscle} className="flex flex-col items-center gap-1.5">
                  <Ring value={m.percentOfOptimal} color={MUSCLE_COLORS[m.muscle] ?? '#7C5CFF'}>
                    <span className="text-base font-extrabold leading-none">{m.weeklySets}</span><span className="text-[10px] text-mute">/ {m.optimalSetsPerWeek}</span>
                  </Ring>
                  <span className="text-xs font-semibold text-mute">{m.muscle}</span>
                </div>
              ))}
            </div>
          </Card>

          <Card>
            <h2 className="mb-3 font-bold">Volume share</h2>
            <div className="flex h-3 overflow-hidden rounded-full bg-raised">
              {data.muscles.filter((m) => m.sharePercent > 0).map((m) => <div key={m.muscle} style={{ width: `${m.sharePercent}%`, background: MUSCLE_COLORS[m.muscle] }} />)}
            </div>
            <ul className="mt-3 space-y-1.5">
              {data.muscles.filter((m) => m.volume > 0).map((m) => (
                <li key={m.muscle} className="flex items-center justify-between text-sm">
                  <span className="flex items-center gap-2"><span className="h-2.5 w-2.5 rounded-full" style={{ background: MUSCLE_COLORS[m.muscle] }} />{m.muscle}</span>
                  <span className="font-semibold">{Math.round(m.volume).toLocaleString()} lbs <span className="text-mute">· {m.sharePercent}%</span></span>
                </li>
              ))}
            </ul>
          </Card>

          <div className="grid grid-cols-2 gap-3">
            <Card><p className="text-xs text-mute">Push : Pull</p><p className="text-3xl font-extrabold">{data.pushToPullRatio ?? '–'}</p><p className="text-xs text-mute">chest + shoulders vs back</p></Card>
            {data.pairs.map((p) => (
              <Card key={p.agonist}><p className="text-xs text-mute">{p.agonist} : {p.antagonist}</p><p className="text-3xl font-extrabold">{p.ratio ?? '–'}</p><p className="text-xs text-mute">agonist to antagonist</p></Card>
            ))}
          </div>
        </>
      )}

      <Card>
        <div className="mb-3 flex items-center justify-between gap-3">
          <h2 className="font-bold">Strength trend</h2>
          <select value={vid ?? ''} onChange={(e) => setVid(Number(e.target.value))} className="tap max-w-[55%] rounded-xl bg-raised px-3 text-sm font-semibold">
            {movements.flatMap((movement) => movement.weightSystems.map((system) => (
              <option key={system.variantId} value={system.variantId}>
                {movement.movementName} · {system.equipmentType.toLowerCase()}
              </option>
            )))}
          </select>
        </div>
        {trend && trend.points.length > 0 ? <TrendChart trend={trend} /> : <p className="text-sm text-mute">Log this lift in a couple of sessions to see your estimated 1RM trend.</p>}
      </Card>
    </div>
  );
}

function TrendChart({ trend }: { trend: ProgressTrend }) {
  const pts = trend.points, w = 320, h = 140, pad = 12;
  const vals = pts.map((p) => p.estimatedOneRepMax), min = Math.min(...vals), max = Math.max(...vals), span = max - min || 1;
  const x = (i: number) => pad + (pts.length === 1 ? (w - 2 * pad) / 2 : (i / (pts.length - 1)) * (w - 2 * pad));
  const y = (v: number) => h - pad - ((v - min) / span) * (h - 2 * pad);
  const kept = pts.map((p, i) => ({ p, i })).filter((o) => !o.p.excluded);
  const line = kept.map((o, k) => `${k ? 'L' : 'M'}${x(o.i)},${y(o.p.estimatedOneRepMax)}`).join(' ');
  const v = trend.velocityPerWeek;
  return (
    <>
      <div className="mb-2 flex items-baseline gap-2">
        <span className="text-3xl font-extrabold">{vals[vals.length - 1]}</span><span className="text-sm text-mute">lbs e1RM</span>
        {v !== null && <span className={`ml-auto text-sm font-bold ${v >= 0 ? 'text-up' : 'text-down'}`}>{v > 0 ? '+' : ''}{v} lbs / week</span>}
      </div>
      <svg viewBox={`0 0 ${w} ${h}`} className="w-full" role="img" aria-label="Estimated 1RM over time">
        <path d={line} fill="none" stroke="#A58BFF" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
        {pts.map((p, i) => <circle key={i} cx={x(i)} cy={y(p.estimatedOneRepMax)} r={p.excluded ? 4 : 3.5} fill={p.excluded ? 'none' : '#A58BFF'} stroke={p.excluded ? '#8B8BA3' : 'none'} strokeWidth="2" />)}
      </svg>
      {pts.some((p) => p.excluded) && <p className="mt-1 text-xs text-mute">Hollow dots are deload or injury sessions, left out of the trend.</p>}
    </>
  );
}
