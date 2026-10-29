import type * as T from './types';

const BASE = (import.meta.env.VITE_API_URL as string | undefined) ?? '';

async function req<R>(path: string, init?: RequestInit): Promise<R> {
  const res = await fetch(BASE + path, { headers: { 'Content-Type': 'application/json' }, ...init });
  if (!res.ok) {
    const p = await res.json().catch(() => ({}));
    const fields = p.errors ? Object.values(p.errors).join(', ') : '';
    throw new Error(fields || p.detail || p.title || 'Something went wrong');
  }
  return res.status === 204 ? (undefined as R) : res.json();
}
const post = (body: unknown): RequestInit => ({ method: 'POST', body: JSON.stringify(body) });

/** Taxonomy is cached locally so the logger still works with no signal. */
export async function getMovements(): Promise<T.Movement[]> {
  try {
    const movements = await req<T.Movement[]>('/api/exercises');
    localStorage.setItem('overload.taxonomy.v2', JSON.stringify(movements));
    return movements;
  } catch (e) {
    const cachedMovements = localStorage.getItem('overload.taxonomy.v2');
    if (cachedMovements) return JSON.parse(cachedMovements);
    const cachedVariants = localStorage.getItem('overload.taxonomy');
    if (cachedVariants) {
      const variants = JSON.parse(cachedVariants) as {
        id: number; variantName: string; baseName: string; muscleGroup: string;
        equipmentType: string; muscles: Record<string, number>;
      }[];
      const movements = new Map<string, T.Movement>();
      for (const variant of variants) {
        const key = JSON.stringify([variant.baseName, variant.muscleGroup]);
        let movement = movements.get(key);
        if (!movement) {
          movement = {
            id: variant.id,
            movementName: variant.baseName,
            muscleGroup: variant.muscleGroup,
            weightSystems: [],
          };
          movements.set(key, movement);
        }
        movement.weightSystems.push({
          variantId: variant.id,
          variantName: variant.variantName,
          equipmentType: variant.equipmentType,
          muscles: variant.muscles,
        });
      }
      return [...movements.values()];
    }
    throw e;
  }
}
export const createSession = (sessionDate: string, splitType: T.Split) => req<T.Session>('/api/sessions', post({ sessionDate, splitType }));
export const getSession = (id: string) => req<T.Session>(`/api/sessions/${id}`);
export const listSessions = () => req<T.Session[]>('/api/sessions');
export const deleteSession = (id: string) => req<void>(`/api/sessions/${id}`, { method: 'DELETE' });
export const logSet = (id: string, body: { variantId: number; setType: T.SetType; weight: number; reps: number; bodyweight: boolean; addedWeight: number }) =>
  req<T.LogSetResponse>(`/api/sessions/${id}/sets`, post(body));
export const checkRegression = (id: string, variantId: number) => req<T.RegressionCheck>(`/api/sessions/${id}/regression?variantId=${variantId}`);
export const sendFeedback = (id: string, reason: T.Reason) => req<T.Session>(`/api/sessions/${id}/feedback`, post({ reason }));
export const getVolume = (window: T.Window) => req<T.VolumeDistribution>(`/api/analytics/volume?window=${window}`);
export const getProgress = (variantId: number) => req<T.ProgressTrend>(`/api/analytics/progress/${variantId}`);
