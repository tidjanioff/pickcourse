import { useState, type FormEvent } from 'react'
import { Link } from 'react-router-dom'
import { getCourseDifficulty, getCoursePopularity } from '../../api/courseDetails'
import { searchCourses } from '../../api/courses'
import type { Course, CourseDetailNavigationState } from '../../types/course'

interface CourseSelectorProps {
  selectedCourses: Course[]
  semester: string
  onAdd: (course: Course) => void
  onRemove: (courseId: string) => void
}

interface PreviewStats {
  difficulty: string | null
  popularity: string | null
  isLoading: boolean
  hasError: boolean
}

export function CourseSelector({ selectedCourses, semester, onAdd, onRemove }: CourseSelectorProps) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState<Course[]>([])
  const [isSearching, setIsSearching] = useState(false)
  const [hasSearched, setHasSearched] = useState(false)
  const [hasError, setHasError] = useState(false)
  const [expandedCourseId, setExpandedCourseId] = useState<string | null>(null)
  const [previewStats, setPreviewStats] = useState<Record<string, PreviewStats>>({})

  async function handleSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!query.trim()) return
    setIsSearching(true)
    setHasSearched(true)
    setHasError(false)
    try {
      setResults(await searchCourses(query))
    } catch {
      setHasError(true)
      setResults([])
    } finally {
      setIsSearching(false)
    }
  }

  function addCourse(course: Course) {
    onAdd(course)
    setQuery('')
    setResults([])
    setHasSearched(false)
  }

  async function togglePreview(course: Course) {
    if (expandedCourseId === course.id) {
      setExpandedCourseId(null)
      return
    }

    setExpandedCourseId(course.id)
    if (previewStats[course.id]) return
    setPreviewStats((current) => ({ ...current, [course.id]: { difficulty: null, popularity: null, isLoading: true, hasError: false } }))
    try {
      const [difficulty, popularity] = await Promise.all([
        getCourseDifficulty(course.id),
        getCoursePopularity(course.id),
      ])
      setPreviewStats((current) => ({ ...current, [course.id]: { difficulty, popularity, isLoading: false, hasError: false } }))
    } catch {
      setPreviewStats((current) => ({ ...current, [course.id]: { difficulty: null, popularity: null, isLoading: false, hasError: true } }))
    }
  }

  return (
    <div>
      <form className="flex gap-2" onSubmit={handleSearch} role="search">
        <label className="sr-only" htmlFor="schedule-course-search">Search courses to add</label>
        <input
          className="field min-w-0 flex-1"
          id="schedule-course-search"
          onChange={(event) => setQuery(event.target.value)}
          placeholder="Search IFT1025 or programming"
          type="search"
          value={query}
        />
        <button className="button-secondary px-5" disabled={!query.trim() || isSearching} type="submit">
          {isSearching ? 'Searching…' : 'Find'}
        </button>
      </form>

      {(results.length > 0 || (hasSearched && !isSearching)) && (
        <div className="mt-3 overflow-hidden rounded-card border border-primary/[0.08] bg-surface shadow-soft">
          {results.slice(0, 6).map((course) => {
            const isSelected = selectedCourses.some((selected) => selected.id === course.id)
            const isExpanded = expandedCourseId === course.id
            const stats = previewStats[course.id]
            return (
              <div className="border-b border-primary/[0.06] last:border-0" key={course.id}>
                <div className="flex items-center gap-3 px-4 py-3">
                  <button aria-controls={`preview-${course.id}`} aria-expanded={isExpanded} className="flex min-w-0 flex-1 items-center gap-4 rounded-md text-left outline-none hover:opacity-75 focus-visible:ring-4 focus-visible:ring-accent/15" onClick={() => togglePreview(course)} type="button">
                    <span className="w-20 shrink-0 font-mono text-xs font-semibold text-accent">{course.id}</span>
                    <span className="min-w-0 flex-1 truncate text-sm text-primary">{course.name}</span>
                    <span className="text-xs text-secondary">{isExpanded ? 'Hide' : 'Preview'}</span>
                  </button>
                  <button className="shrink-0 rounded-full bg-accent/[0.08] px-3.5 py-2 text-xs font-semibold text-accent outline-none transition hover:bg-accent/15 focus-visible:ring-4 focus-visible:ring-accent/20 disabled:opacity-45" disabled={isSelected || selectedCourses.length >= 6} onClick={() => addCourse(course)} type="button">
                    {isSelected ? 'Added' : 'Add'}
                  </button>
                </div>
                {isExpanded && (
                  <div className="bg-primary/[0.025] px-4 py-5 sm:pl-28" id={`preview-${course.id}`}>
                    <div className="flex flex-wrap items-center gap-x-5 gap-y-2 font-mono text-xs text-secondary">
                      <span>{course.credits} credits</span>
                      {stats?.isLoading && <span>Loading stats…</span>}
                      {!stats?.isLoading && !stats?.hasError && <><span>Difficulty: {difficultyLabel(stats?.difficulty)}</span><span>Popularity: {popularityLabel(stats?.popularity)}</span></>}
                      {stats?.hasError && <span>Stats unavailable</span>}
                    </div>
                    <p className="mt-3 max-h-[4.75rem] overflow-hidden text-sm leading-6 text-secondary">{course.description || 'No course description is available yet.'}</p>
                    <div className="mt-4 flex items-center gap-5">
                      <Link
                        className="rounded-md text-sm font-semibold text-accent outline-none hover:text-accent/80 focus-visible:ring-4 focus-visible:ring-accent/20"
                        state={{ scheduleDraft: { selectedCourses, semester } } satisfies CourseDetailNavigationState}
                        to={`/courses/${encodeURIComponent(course.id)}`}
                      >
                        View full details →
                      </Link>
                      <button className="text-xs text-secondary outline-none hover:text-primary focus-visible:ring-2 focus-visible:ring-accent/20" onClick={() => setExpandedCourseId(null)} type="button">Dismiss</button>
                    </div>
                  </div>
                )}
              </div>
            )
          })}
          {results.length === 0 && !hasError && <p className="px-4 py-5 text-center text-sm text-secondary">No courses found.</p>}
          {hasError && <p className="px-4 py-5 text-center text-sm text-secondary">Search is temporarily unavailable.</p>}
        </div>
      )}

      {selectedCourses.length > 0 && (
        <div className="mt-5 flex flex-wrap gap-2" aria-label="Selected courses">
          {selectedCourses.map((course) => (
            <span className="inline-flex items-center gap-2 rounded-full bg-accent/[0.08] py-2 pl-3.5 pr-2 font-mono text-xs font-semibold text-accent" key={course.id}>
              {course.id}
              <button aria-label={`Remove ${course.id}`} className="grid h-6 w-6 place-items-center rounded-full text-base leading-none outline-none hover:bg-accent/10 focus-visible:ring-2 focus-visible:ring-accent/30" onClick={() => onRemove(course.id)} type="button">×</button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}

function difficultyLabel(message?: string | null) {
  if (!message) return 'Unavailable'
  if (message.includes('difficulté moyenne')) return 'Moderate'
  if (message.includes('facile')) return 'Approachable'
  if (message.includes('difficile')) return 'Challenging'
  return 'Unavailable'
}

function popularityLabel(message?: string | null) {
  if (!message) return 'Unavailable'
  if (message.includes('très populaire')) return 'Very popular'
  if (message.includes('modérément populaire')) return 'Popular'
  if (message.includes('peu populaire')) return 'Smaller cohort'
  return 'Unavailable'
}
