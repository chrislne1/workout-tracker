export type Split = 'UPPER' | 'LOWER' | 'PUSH' | 'PULL' | 'LEGS';
export type SetType = 'WARMUP' | 'WORKING' | 'DROPSET' | 'FAILURE';
export type Reason = 'PURPOSEFUL_DELOAD' | 'FATIGUE_POOR_SLEEP' | 'INJURY_DISCOMFORT' | 'EQUIPMENT_CHANGE';
export type Window = 'D30' | 'D90' | 'M6' | 'Y1' | 'AOT';

export interface Movement { id: number; movementName: string; muscleGroup: string; weightSystems: WeightSystem[]; }
export interface WeightSystem { variantId: number; variantName: string; equipmentType: string; muscles: Record<string, number>; }
export interface SetDto { id: string; variantId: number; variantName: string; setNumber: number; weight: number; reps: number; setType: SetType; bodyweight: boolean; addedWeight: number; estimatedOneRepMax: number; }
export interface Session { id: string; sessionDate: string; splitType: Split; notes?: string; sets: SetDto[]; workingVolume: number; regressionReason: Reason | null; }
export interface SetDelta { hasPrevious: boolean; weightDelta: number; repsDelta: number; volumeChangePercent: number; }
export interface LogSetResponse { set: SetDto; delta: SetDelta; achievements: string[]; }
export interface RegressionCheck { regression: boolean; todayVolume: number; baselineVolume: number; dropPercent: number; reasons: Reason[]; }
export interface MuscleVolume { muscle: string; volume: number; hardSets: number; weeklySets: number; optimalSetsPerWeek: number; percentOfOptimal: number; sharePercent: number; }
export interface VolumeDistribution { window: Window; muscles: MuscleVolume[]; pushVolume: number; pullVolume: number; pushToPullRatio: number | null; pairs: { agonist: string; antagonist: string; agonistVolume: number; antagonistVolume: number; ratio: number | null }[]; }
export interface ProgressTrend { variantId: number; points: { date: string; estimatedOneRepMax: number; excluded: boolean }[]; velocityPerWeek: number | null; }
