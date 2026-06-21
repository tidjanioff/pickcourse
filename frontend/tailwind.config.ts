import type { Config } from 'tailwindcss'

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        background: 'var(--color-background)',
        surface: 'var(--color-surface)',
        primary: 'var(--color-text-primary)',
        secondary: 'var(--color-text-secondary)',
        accent: 'var(--color-accent)',
        'accent-hover': 'var(--color-accent-hover)',
        'accent-soft': 'var(--color-accent-soft)',
        'accent-ring': 'var(--color-accent-ring)',
        conflict: 'var(--color-conflict)',
        line: 'var(--color-border)',
        'primary-hover': 'var(--color-primary-hover)',
        'primary-ring': 'var(--color-primary-ring)',
      },
      fontFamily: {
        sans: ['-apple-system', 'SF Pro Display', 'Inter', 'sans-serif'],
        mono: ['SF Mono', 'JetBrains Mono', 'monospace'],
      },
      borderRadius: {
        card: 'var(--radius-card)',
        panel: 'var(--radius-panel)',
      },
      spacing: {
        18: '4.5rem',
        22: '5.5rem',
        30: '7.5rem',
      },
      boxShadow: {
        card: '0 24px 80px rgba(29, 29, 31, 0.08)',
        soft: '0 12px 40px rgba(29, 29, 31, 0.06)',
      },
    },
  },
  plugins: [],
} satisfies Config
