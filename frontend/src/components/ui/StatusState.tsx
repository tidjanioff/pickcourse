import type { ReactNode } from 'react'

interface StatusStateProps {
  title: string
  detail?: string
  loading?: boolean
  action?: ReactNode
  className?: string
}

export function StatusState({ title, detail, loading = false, action, className = 'py-16' }: StatusStateProps) {
  return (
    <div className={`text-center ${className}`} role="status">
      {loading && <span className="mx-auto block h-4 w-4 animate-spin rounded-full border-2 border-primary/10 border-t-accent" aria-hidden="true" />}
      <h2 className={`${loading ? 'mt-4' : ''} text-2xl font-semibold tracking-[-0.03em] text-primary`}>{title}</h2>
      {detail && <p className="mt-3 text-sm leading-6 text-secondary">{detail}</p>}
      {action && <div className="mt-6">{action}</div>}
    </div>
  )
}

export function InlineLoading({ message }: { message: string }) {
  return <div className="flex items-center justify-center gap-3 py-10 text-sm text-secondary" role="status"><span className="h-4 w-4 animate-spin rounded-full border-2 border-primary/10 border-t-accent" aria-hidden="true" />{message}</div>
}
