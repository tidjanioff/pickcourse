import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'

export function AppHeader({ action }: { action?: ReactNode }) {
  return (
    <nav className="mx-auto flex max-w-6xl items-center justify-between gap-4" aria-label="Primary navigation">
      <Link className="rounded-md text-xl font-semibold tracking-[-0.03em] text-primary outline-none focus-visible:ring-4 focus-visible:ring-accent/20" to="/">
        Cadence<span className="text-accent">.</span>
      </Link>
      <div className="flex items-center gap-3 sm:gap-5">
        <span className="text-xs font-medium text-secondary sm:text-sm">by PickCourse</span>
        {action}
      </div>
    </nav>
  )
}
