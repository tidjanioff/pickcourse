import { useRef, useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { searchCourses } from '../api/courses'
import { AppFooter } from '../components/ui/AppFooter'
import { AppHeader } from '../components/ui/AppHeader'
import { StatusState } from '../components/ui/StatusState'
import { PageIntro } from '../components/ui/Typography'
import type { Course } from '../types/course'

export function LandingPage() {
  const { t } = useTranslation()
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Course[] | null>(null)
  const [searchedFor, setSearchedFor] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [hasError, setHasError] = useState(false)
  const activeRequest = useRef<AbortController | null>(null)

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const searchTerm = query.trim()
    if (!searchTerm) return

    activeRequest.current?.abort()
    const controller = new AbortController()
    activeRequest.current = controller
    setSearchedFor(searchTerm)
    setIsLoading(true)
    setHasError(false)
    setResults(null)

    try {
      setResults(await searchCourses(searchTerm, controller.signal))
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') return
      setHasError(true)
      setResults([])
    } finally {
      if (activeRequest.current === controller) setIsLoading(false)
    }
  }

  return (
    <main className="page-shell flex flex-col">
      <AppHeader />

      <div className="flex-1">
        <section className="mx-auto flex max-w-5xl flex-col items-center pb-16 pt-24 text-center sm:pb-20 sm:pt-32">
          <div className="reveal flex flex-col items-center">
            <PageIntro eyebrow={t('landing.eyebrow')} title={t('landing.title')} description={t('landing.description')} />
          </div>

          <form className="reveal reveal-delay-2 mt-10 w-full max-w-3xl sm:mt-12" onSubmit={handleSearch} role="search">
            <label className="sr-only" htmlFor="course-search">{t('landing.search.label')}</label>
            <div className="flex items-center gap-2 rounded-panel border border-primary/[0.08] bg-surface p-2.5 shadow-card transition focus-within:border-accent/40 focus-within:ring-4 focus-within:ring-accent/10 sm:gap-3 sm:p-3">
              <svg className="ml-2 h-5 w-5 shrink-0 text-secondary sm:ml-3" aria-hidden="true" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.8">
                <circle cx="11" cy="11" r="7" />
                <path strokeLinecap="round" d="m16.25 16.25 4 4" />
              </svg>
              <input
                id="course-search"
                className="min-w-0 flex-1 bg-transparent px-1 py-3 text-base text-primary outline-none placeholder:text-secondary/80 sm:text-lg"
                type="search"
                value={query}
                onChange={(event) => setQuery(event.target.value)}
                placeholder={t('landing.search.placeholder')}
                autoComplete="off"
              />
              <button
                className="button-primary shrink-0 px-5 py-3.5 sm:px-7"
                type="submit"
                disabled={!query.trim() || isLoading}
              >
                <span className="hidden sm:inline">{t('landing.search.button')}</span>
                <span className="sm:hidden">{t('landing.search.buttonShort')}</span>
              </button>
            </div>
          </form>
        </section>

        <section className="mx-auto max-w-4xl" aria-live="polite" aria-busy={isLoading}>
          {isLoading && <StatusState loading title={t('landing.states.searching.title')} />}
          {!isLoading && hasError && (
            <StatusState title={t('landing.states.error.title')} detail={t('landing.states.error.detail')} />
          )}
          {!isLoading && !hasError && results?.length === 0 && (
            <StatusState title={t('landing.states.empty.title')} detail={t('landing.states.empty.detail')} />
          )}
          {!isLoading && results && results.length > 0 && (
            <div className="results-enter">
              <div className="mb-5 flex items-end justify-between gap-4 px-1">
                <div>
                  <p className="text-sm text-secondary">{t('landing.resultsFor', { query: searchedFor })}</p>
                  <h2 className="mt-1 text-2xl font-semibold tracking-[-0.03em] text-primary">{t('landing.resultsCount', { count: results.length })}</h2>
                </div>
              </div>
              <ul className="space-y-3">
                {results.map((course) => (
                  <li key={course.id}>
                    <Link
                      className="surface-card group flex items-center gap-4 px-5 py-5 outline-none transition hover:-translate-y-0.5 hover:border-primary/10 hover:shadow-card focus-visible:ring-4 focus-visible:ring-accent/25 sm:px-6 sm:py-6"
                      to={`/courses/${encodeURIComponent(course.id)}`}
                    >
                      <div className="min-w-0 flex-1 sm:flex sm:items-center sm:gap-8">
                        <p className="shrink-0 font-mono text-sm font-semibold tracking-wide text-accent sm:w-24">{course.id}</p>
                        <p className="mt-1 truncate text-base font-medium text-primary sm:mt-0">{course.name}</p>
                      </div>
                      <p className="shrink-0 text-sm text-secondary">{t('landing.credits', { count: course.credits })}</p>
                      <svg className="h-5 w-5 shrink-0 text-secondary transition group-hover:translate-x-0.5 group-hover:text-primary" aria-hidden="true" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth="1.8">
                        <path strokeLinecap="round" strokeLinejoin="round" d="m9 5 7 7-7 7" />
                      </svg>
                    </Link>
                  </li>
                ))}
              </ul>
            </div>
          )}
        </section>
      </div>

      <AppFooter />
    </main>
  )
}
