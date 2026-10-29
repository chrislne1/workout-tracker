import { ReactNode } from 'react';

export const Card = ({ children, className = '' }: { children: ReactNode; className?: string }) => (
  <div className={`rounded-3xl bg-card border border-line p-4 ${className}`}>{children}</div>
);

export function Segmented<V extends string>({ options, value, onChange }: { options: { value: V; label: string }[]; value: V; onChange: (v: V) => void }) {
  return (
    <div className="flex rounded-2xl bg-card border border-line p-1 gap-1">
      {options.map((o) => (
        <button key={o.value} onClick={() => onChange(o.value)}
          className={`tap flex-1 rounded-xl text-sm font-semibold transition-colors ${value === o.value ? 'bg-brand text-white' : 'text-mute'}`}>
          {o.label}
        </button>
      ))}
    </div>
  );
}

/** Circular progress ring (like MacroFactor's macro rings). */
export function Ring({ value, color, size = 84, children }: { value: number; color: string; size?: number; children?: ReactNode }) {
  const r = (size - 12) / 2, c = 2 * Math.PI * r, pct = Math.min(Math.max(value, 0), 100);
  return (
    <div className="relative" style={{ width: size, height: size }}>
      <svg width={size} height={size} className="-rotate-90">
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="#272736" strokeWidth={9} />
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={color} strokeWidth={9} strokeLinecap="round"
          strokeDasharray={c} strokeDashoffset={c - (pct / 100) * c} />
      </svg>
      <div className="absolute inset-0 flex flex-col items-center justify-center">{children}</div>
    </div>
  );
}

export const Delta = ({ value, suffix }: { value: number; suffix: string }) => {
  const sign = value > 0 ? '+' : '';
  const tone = value > 0 ? 'bg-up/15 text-up' : value < 0 ? 'bg-down/15 text-down' : 'bg-raised text-mute';
  return <span className={`rounded-full px-2.5 py-1 text-xs font-bold ${tone}`}>{sign}{Math.round(value * 10) / 10}{suffix}</span>;
};

export const MUSCLE_COLORS: Record<string, string> = {
  Chest: '#7C5CFF', Back: '#34D399', Legs: '#FBBF24', Shoulders: '#38BDF8', Arms: '#FB7185',
};
