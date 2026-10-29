import { useEffect, useMemo, useState } from 'react';
import * as api from '../api';
import type { LogSetResponse, Movement, RegressionCheck, Reason, Session, SetType, Split, WeightSystem } from '../types';
import { Card, Delta, Segmented, MUSCLE_COLORS } from '../ui';

const SPLITS: Split[] = ['PUSH', 'PULL', 'LEGS', 'UPPER', 'LOWER'];
const SET_TYPES: { value: SetType; label: string }[] = [
  { value: 'WARMUP', label: 'Warm-up' }, { value: 'WORKING', label: 'Working' },
  { value: 'DROPSET', label: 'Drop' }, { value: 'FAILURE', label: 'Failure' },
];
const REASONS: Record<Reason, { label: string; hint: string }> = {
  PURPOSEFUL_DELOAD: { label: 'Planned deload', hint: "Won't count against your strength trend" },
  FATIGUE_POOR_SLEEP: { label: 'Tired or slept badly', hint: 'Stays in your trend' },
  INJURY_DISCOMFORT: { label: 'Injury or discomfort', hint: "Won't count against your strength trend" },
  EQUIPMENT_CHANGE: { label: 'Different equipment', hint: 'Stays in your trend' },
};
const today = () => new Date().toLocaleDateString('en-CA');
const KEY = 'overload.session';

export default function Log() {
  const [movements, setMovements] = useState<Movement[]>([]);
  const [session, setSession] = useState<Session | null>(null);
  const [split, setSplit] = useState<Split>('PUSH');
  const [movementId, setMovementId] = useState<number | null>(null);
  const [variantId, setVariantId] = useState<number | null>(null);
  const [weight, setWeight] = useState('');
  const [reps, setReps] = useState('');
  const [added, setAdded] = useState('');
  const [bodyweight, setBodyweight] = useState(false);
  const [setType, setSetType] = useState<SetType>('WORKING');
  const [last, setLast] = useState<LogSetResponse | null>(null);
  const [check, setCheck] = useState<RegressionCheck | null>(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.getMovements().then(setMovements).catch((e) => setError(e.message));
    const id = localStorage.getItem(KEY);
    if (id) api.getSession(id).then(setSession).catch(() => localStorage.removeItem(KEY));
  }, []);

  useEffect(() => { if (last?.achievements.length) { const t = setTimeout(() => setLast((l) => l && { ...l, achievements: [] }), 5000); return () => clearTimeout(t); } }, [last]);

  const grouped = useMemo(() => {
    const g: Record<string, Movement[]> = {};
    movements.forEach((movement) => (g[movement.muscleGroup] ||= []).push(movement));
    return g;
  }, [movements]);
  const movement = movements.find((item) => item.id === movementId);
  const variant = movement?.weightSystems.find((system) => system.variantId === variantId);
  const mySets = session?.sets.filter((s) => s.variantId === variantId) ?? [];

  const guard = async (fn: () => Promise<void>) => { setBusy(true); setError(''); try { await fn(); } catch (e) { setError((e as Error).message); } finally { setBusy(false); } };

  const start = () => guard(async () => {
    const s = await api.createSession(today(), split);
    localStorage.setItem(KEY, s.id); setSession(s);
  });

  const pick = (system: WeightSystem) => {
    setVariantId(system.variantId); setLast(null);
    const prev = session?.sets.filter((s) => s.variantId === system.variantId && s.setType !== 'WARMUP').slice(-1)[0];
    setWeight(prev ? String(prev.weight) : ''); setReps(prev ? String(prev.reps) : '');
    setBodyweight(system.equipmentType === 'BODYWEIGHT');
  };

  const log = () => guard(async () => {
    if (!session || !variantId) return;
    const res = await api.logSet(session.id, { variantId, setType, weight: Number(weight), reps: Number(reps), bodyweight, addedWeight: Number(added) || 0 });
    setLast(res);
    setSession(await api.getSession(session.id));
  });

  const step = (setter: (v: string) => void, cur: string, by: number) => setter(String(Math.max(0, Math.round(((Number(cur) || 0) + by) * 10) / 10)));

  const finishExercise = () => guard(async () => {
    if (!session || !variantId) return;
    const r = await api.checkRegression(session.id, variantId);
    if (r.regression) setCheck(r); else { setMovementId(null); setVariantId(null); setLast(null); }
  });

  const answer = (reason: Reason) => guard(async () => {
    if (!session) return;
    setSession(await api.sendFeedback(session.id, reason));
    setCheck(null); setMovementId(null); setVariantId(null); setLast(null);
  });

  const finishWorkout = () => { localStorage.removeItem(KEY); setSession(null); setMovementId(null); setVariantId(null); setLast(null); };

  /* ---------- no active workout ---------- */
  if (!session) {
    return (
      <div className="space-y-5">
        <header>
          <p className="text-sm text-mute">{new Date().toLocaleDateString(undefined, { weekday: 'long', month: 'long', day: 'numeric' })}</p>
          <h1 className="text-3xl font-extrabold">What are we training?</h1>
        </header>
        <Segmented options={SPLITS.map((s) => ({ value: s, label: s[0] + s.slice(1).toLowerCase() }))} value={split} onChange={setSplit} />
        <button onClick={start} disabled={busy} className="tap w-full rounded-2xl bg-gradient-to-r from-brand to-brand2 py-4 text-lg font-bold">Start workout</button>
        {error && <p className="text-sm text-down">{error}</p>}
      </div>
    );
  }

  /* ---------- active workout ---------- */
  return (
    <div className="space-y-4">
      <header className="flex items-end justify-between">
        <div>
          <h1 className="text-3xl font-extrabold">{session.splitType[0] + session.splitType.slice(1).toLowerCase()} day</h1>
        </div>
        <button onClick={finishWorkout} className="tap rounded-xl px-3 text-sm font-semibold text-brand2">Finish workout</button>
      </header>

      {last && last.achievements.length > 0 && (
        <div role="status" className="rounded-3xl bg-gradient-to-r from-gold/90 to-brand2 p-4 text-bg">
          <p className="text-lg font-extrabold">New personal record 🏆</p>
          <p className="text-sm font-semibold">
            {last.achievements.includes('ESTIMATED_1RM_PR') && `Estimated 1RM ${last.set.estimatedOneRepMax} lbs. `}
            {last.achievements.includes('WEIGHT_PR') && 'Heaviest weight yet. '}
            {last.achievements.includes('REP_PR') && 'Most reps at this weight.'}
          </p>
        </div>
      )}

      {!movement ? (
        <div className="space-y-4">
          <h2 className="text-lg font-bold">Pick a movement</h2>
          {Object.entries(grouped).map(([group, list]) => (
            <div key={group} className="space-y-2">
              <p className="flex items-center gap-2 text-sm font-semibold text-mute"><span className="h-2.5 w-2.5 rounded-full" style={{ background: MUSCLE_COLORS[group] }} />{group}</p>
              {list.map((item) => (
                <button key={item.id} onClick={() => { setMovementId(item.id); setVariantId(null); }} className="tap flex w-full items-center justify-between rounded-2xl border border-line bg-card p-4 text-left">
                  <span className="font-semibold">{item.movementName}</span>
                  <span className="text-mute">›</span>
                </button>
              ))}
            </div>
          ))}
          {movements.length === 0 && <p className="text-sm text-mute">Loading movements…</p>}
        </div>
      ) : !variant ? (
        <div className="space-y-3">
          <div className="flex items-center justify-between">
            <div><h2 className="text-lg font-bold">{movement.movementName}</h2><p className="text-xs text-mute">Choose a weight system</p></div>
            <button onClick={() => setMovementId(null)} className="tap px-2 text-sm font-semibold text-mute">Change movement</button>
          </div>
          {movement.weightSystems.map((system) => (
            <button key={system.variantId} onClick={() => pick(system)} className="tap flex w-full items-center justify-between rounded-2xl border border-line bg-card p-4 text-left">
              <span className="font-semibold">{system.equipmentType.toLowerCase()}</span>
              <span className="text-mute">›</span>
            </button>
          ))}
        </div>
      ) : (
        <>
          <Card>
            <div className="flex items-center justify-between">
              <div><h2 className="text-lg font-bold">{movement.movementName}</h2><p className="text-xs text-mute">Weight system · {variant.equipmentType.toLowerCase()}</p></div>
              <button onClick={() => setVariantId(null)} className="tap px-2 text-sm font-semibold text-mute">Change</button>
            </div>

            <div className="mt-4 grid grid-cols-2 gap-3">
              {[{ label: 'Weight (lbs)', v: weight, set: setWeight, by: 5 }, { label: 'Reps', v: reps, set: setReps, by: 1 }].map((f) => (
                <label key={f.label} className="block">
                  <span className="text-xs font-semibold text-mute">{f.label}</span>
                  <div className="mt-1 flex items-center rounded-2xl bg-raised">
                    <button type="button" aria-label={`Decrease ${f.label}`} onClick={() => step(f.set, f.v, -f.by)} className="tap text-xl text-mute">−</button>
                    <input type="number" inputMode="decimal" step="any" min="0" value={f.v} onChange={(e) => f.set(e.target.value)} placeholder="0"
                      className="h-14 w-full min-w-0 bg-transparent text-center text-2xl font-extrabold outline-none" />
                    <button type="button" aria-label={`Increase ${f.label}`} onClick={() => step(f.set, f.v, f.by)} className="tap text-xl text-mute">+</button>
                  </div>
                </label>
              ))}
            </div>

            <div className="mt-3 flex items-center gap-3">
              <button type="button" onClick={() => setBodyweight(!bodyweight)} aria-pressed={bodyweight}
                className={`tap rounded-xl px-3 text-sm font-semibold ${bodyweight ? 'bg-brand text-white' : 'bg-raised text-mute'}`}>Bodyweight</button>
              {bodyweight && (
                <input type="number" inputMode="decimal" step="any" min="0" value={added} onChange={(e) => setAdded(e.target.value)} placeholder="+ added lbs"
                  className="h-11 w-full rounded-xl bg-raised px-3 text-sm font-semibold outline-none" />
              )}
            </div>

            <div className="mt-3"><Segmented options={SET_TYPES} value={setType} onChange={setSetType} /></div>

            <button onClick={log} disabled={busy || !weight || !reps}
              className="tap mt-4 w-full rounded-2xl bg-gradient-to-r from-brand to-brand2 py-4 text-lg font-bold disabled:opacity-40">Log set</button>
            {error && <p className="mt-2 text-sm text-down">{error}</p>}
          </Card>

          {last && last.set.variantId === variantId && (
            <Card className="space-y-2">
              <p className="text-sm text-mute">Estimated 1RM</p>
              <p className="text-3xl font-extrabold">{last.set.estimatedOneRepMax} <span className="text-base text-mute">lbs</span></p>
              {last.set.setType === 'WARMUP' ? <p className="text-sm text-mute">Warm-ups don't count toward progress.</p> :
                last.delta.hasPrevious ? (
                  <div className="flex flex-wrap items-center gap-2"><span className="text-xs text-mute">vs last session</span>
                    <Delta value={last.delta.weightDelta} suffix=" lbs" /><Delta value={last.delta.repsDelta} suffix=" reps" /><Delta value={last.delta.volumeChangePercent} suffix="% vol" />
                  </div>) : <p className="text-sm text-mute">First time logging this lift. This sets your baseline.</p>}
            </Card>
          )}

          {mySets.length > 0 && (
            <Card className="divide-y divide-line p-0">
              {mySets.map((s) => (
                <div key={s.id} className="flex items-center justify-between px-4 py-3">
                  <span className="text-sm font-semibold text-mute">Set {s.setNumber}</span>
                  <span className="font-bold">{s.bodyweight ? `BW${s.addedWeight ? ` +${s.addedWeight}` : ''}` : s.weight} × {s.reps}</span>
                  <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${s.setType === 'WARMUP' ? 'bg-raised text-mute' : 'bg-brand/20 text-brand2'}`}>{s.setType.toLowerCase()}</span>
                </div>
              ))}
            </Card>
          )}
          <button onClick={finishExercise} className="tap w-full rounded-2xl border border-line bg-card py-3 font-semibold">Finish exercise</button>
        </>
      )}

      {check && (
        <div className="fixed inset-0 z-20 flex items-end bg-black/60" role="dialog" aria-modal="true">
          <div className="sheet mx-auto w-full max-w-md rounded-t-4xl bg-card p-5" style={{ paddingBottom: 'calc(1.25rem + env(safe-area-inset-bottom))' }}>
            <h2 className="text-xl font-extrabold">Volume was down {check.dropPercent}%</h2>
            <p className="mt-1 text-sm text-mute">{Math.round(check.todayVolume).toLocaleString()} lbs today vs a {Math.round(check.baselineVolume).toLocaleString()} lbs 30-day average. What was behind it?</p>
            <div className="mt-4 space-y-2">
              {check.reasons.map((r) => (
                <button key={r} onClick={() => answer(r)} className="tap w-full rounded-2xl bg-raised p-4 text-left">
                  <span className="block font-semibold">{REASONS[r].label}</span><span className="text-xs text-mute">{REASONS[r].hint}</span>
                </button>
              ))}
            </div>
            <button onClick={() => { setCheck(null); setMovementId(null); setVariantId(null); }} className="tap mt-3 w-full text-sm font-semibold text-mute">Skip</button>
          </div>
        </div>
      )}
    </div>
  );
}
