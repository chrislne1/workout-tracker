/** Dark, card-based palette in the spirit of MacroFactor. */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        bg: '#0B0B12', card: '#15151F', raised: '#1E1E2B', line: '#272736',
        ink: '#F4F4F8', mute: '#8B8BA3', brand: '#7C5CFF', brand2: '#A58BFF',
        up: '#34D399', down: '#FB7185', gold: '#FBBF24',
      },
      fontFamily: { sans: ['Inter', 'system-ui', '-apple-system', 'Segoe UI', 'sans-serif'] },
      borderRadius: { '4xl': '2rem' },
    },
  },
  plugins: [],
};
