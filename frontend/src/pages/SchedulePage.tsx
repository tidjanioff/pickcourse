import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { generateScheduleOptions } from '../api/schedule'
import { CalendarGrid } from '../components/schedule/CalendarGrid'
import { CourseSelector } from '../components/schedule/CourseSelector'
import { courseColor } from '../components/schedule/colors'
import { AppHeader } from '../components/ui/AppHeader'
import { InlineLoading, StatusState } from '../components/ui/StatusState'
import { Eyebrow, PageIntro } from '../components/ui/Typography'
import type { Course, ScheduleDraft, ScheduleNavigationState } from '../types/course'
import type { ScheduleOption } from '../types/schedule'

export function SchedulePage() {
  const location = useLocation()
  const navigate = useNavigate()
  const [selectedCourses, setSelectedCourses] = useState<Course[]>(() => readScheduleDraft().selectedCourses)
  const [semester, setSemester] = useState(() => readScheduleDraft().semester)
  const [options, setOptions] = useState<ScheduleOption[]>([])
  const [optionIndex, setOptionIndex] = useState(0)
  const [isGenerating, setIsGenerating] = useState(false)
  const [hasGenerated, setHasGenerated] = useState(false)
  const [hasError, setHasError] = useState(false)

  useEffect(() => {
    const incomingDraft = getIncomingDraft(location.state)
    if (!incomingDraft) return

    setSelectedCourses((current) => {
      const merged = [...current]
      for (const course of incomingDraft.selectedCourses) {
        if (merged.length >= 6) break
        if (!merged.some((selected) => selected.id.toUpperCase() === course.id.toUpperCase())) merged.push(course)
      }
      return merged
    })
    setSemester(incomingDraft.semester)
    setOptions([])
    setOptionIndex(0)
    setHasGenerated(false)
    setHasError(false)

    navigate(
      { pathname: location.pathname, search: location.search, hash: location.hash },
      { replace: true, state: null },
    )
  }, [location.hash, location.pathname, location.search, location.state, navigate])

  useEffect(() => {
    window.sessionStorage.setItem(SCHEDULE_DRAFT_KEY, JSON.stringify({ selectedCourses, semester } satisfies ScheduleDraft))
  }, [selectedCourses, semester])

  function resetSchedule() {
    setOptions([])
    setOptionIndex(0)
    setHasGenerated(false)
    setHasError(false)
  }

  function addCourse(course: Course) {
    setSelectedCourses((current) => {
      if (current.length >= 6 || current.some((selected) => selected.id.toUpperCase() === course.id.toUpperCase())) return current
      return [...current, course]
    })
    resetSchedule()
  }

  function removeCourse(courseId: string) {
    setSelectedCourses((current) => current.filter((course) => course.id !== courseId))
    resetSchedule()
  }

  async function generate() {
    if (selectedCourses.length === 0 || !semester.trim()) return
    setIsGenerating(true)
    setHasError(false)
    setHasGenerated(false)
    setOptions([])
    setOptionIndex(0)
    try {
      const generated = await generateScheduleOptions(selectedCourses.map((course) => course.id), semester.trim().toUpperCase())
      setOptions(generated)
      setHasGenerated(true)
    } catch {
      setHasError(true)
    } finally {
      setIsGenerating(false)
    }
  }

  const currentOption = options[optionIndex]

  return (
    <main className="page-shell">
      <AppHeader action={<Link className="rounded-full px-3 py-2 text-sm font-medium text-secondary outline-none transition hover:text-primary focus-visible:ring-4 focus-visible:ring-accent/20" to="/"><span className="sm:hidden">← Search</span><span className="hidden sm:inline">← Course search</span></Link>} />

      <header className="reveal mx-auto flex max-w-5xl flex-col items-center pb-20 pt-24 text-center sm:pt-32">
        <PageIntro eyebrow="Schedule builder" title="See your week before it happens." description="Add up to six courses, choose a semester, and explore available section combinations." />
      </header>

      <section className="surface-card mx-auto max-w-5xl p-6 sm:p-8" aria-labelledby="build-heading">
        <h2 className="text-2xl font-semibold tracking-[-0.03em] text-primary" id="build-heading">Build your course list</h2>
        <p className="mt-2 text-sm text-secondary">Search by course code or name. The backend supports a maximum of six courses.</p>
        <div className="mt-7"><CourseSelector onAdd={addCourse} onRemove={removeCourse} selectedCourses={selectedCourses} semester={semester} /></div>
        <div className="mt-8 flex flex-col gap-4 border-t border-primary/[0.07] pt-7 sm:flex-row sm:items-end sm:justify-between">
          <label className="text-sm font-medium text-primary">Target semester
            <input className="field mt-2 block w-36 py-3 font-mono text-sm uppercase" maxLength={3} onChange={(event) => { setSemester(event.target.value.toUpperCase()); resetSchedule() }} placeholder="A25" value={semester} />
          </label>
          <button className="button-primary" disabled={selectedCourses.length === 0 || !semester.trim() || isGenerating} onClick={generate} type="button">
            {isGenerating ? 'Generating schedule…' : 'Generate schedule'}
          </button>
        </div>
      </section>

      <section className="mx-auto mt-24 max-w-7xl sm:mt-32" aria-busy={isGenerating} aria-live="polite">
        {selectedCourses.length === 0 && !isGenerating && <StatusState title="Your week starts here." detail="Add a course above to begin building your schedule." />}
        {selectedCourses.length > 0 && !hasGenerated && !isGenerating && !hasError && <StatusState title="Ready when you are." detail="Generate a schedule to see available section combinations." />}
        {isGenerating && <InlineLoading message="Finding every valid option…" />}
        {hasError && <StatusState title="Schedules are temporarily unavailable." detail="Check the semester and try again in a moment." />}
        {hasGenerated && options.length === 0 && <StatusState title="No schedule options found." detail="One or more courses may not have sections in this semester. Try another semester or course selection." />}
        {currentOption && (
          <div className="results-enter">
            <div className="mb-8 flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
              <div><Eyebrow>Your weekly calendar</Eyebrow><h2 className="mt-4 text-4xl font-semibold tracking-[-0.045em] text-primary sm:text-5xl">Option {optionIndex + 1} of {options.length}</h2><p className="mt-3 text-sm text-secondary">{currentOption.conflits.length === 0 ? 'No conflicts in this option.' : `${currentOption.conflits.length} time conflict${currentOption.conflits.length === 1 ? '' : 's'} detected.`}</p></div>
              <div className="flex gap-2">
                <button aria-label="Previous schedule option" className="rounded-full border border-primary/10 bg-surface px-5 py-3 text-sm font-semibold text-primary outline-none hover:bg-primary/[0.03] focus-visible:ring-4 focus-visible:ring-accent/20 disabled:opacity-35" disabled={optionIndex === 0} onClick={() => setOptionIndex((current) => current - 1)} type="button">← Previous</button>
                <button aria-label="Next schedule option" className="rounded-full border border-primary/10 bg-surface px-5 py-3 text-sm font-semibold text-primary outline-none hover:bg-primary/[0.03] focus-visible:ring-4 focus-visible:ring-accent/20 disabled:opacity-35" disabled={optionIndex === options.length - 1} onClick={() => setOptionIndex((current) => current + 1)} type="button">Next →</button>
              </div>
            </div>
            <div className="surface-card p-4 sm:p-6"><CalendarGrid courses={selectedCourses} option={currentOption} /></div>
            <div className="mt-6 flex flex-wrap gap-x-7 gap-y-3 rounded-card bg-primary/[0.03] px-5 py-4" aria-label="Course color legend">
              {selectedCourses.map((course, index) => { const color = courseColor(index); return <div className="flex min-w-0 items-center gap-2.5" key={course.id}><span className="h-3 w-3 shrink-0 rounded-full" style={{ backgroundColor: color.color }} /><span className="font-mono text-xs font-semibold text-primary">{course.id}</span><span className="max-w-52 truncate text-xs text-secondary">{course.name}</span></div> })}
            </div>
          </div>
        )}
      </section>
    </main>
  )
}

const SCHEDULE_DRAFT_KEY = 'cadence.scheduleDraft'

function getIncomingDraft(state: unknown): ScheduleDraft | null {
  if (!state || typeof state !== 'object' || !('selectedCourses' in state)) return null
  const draft = state as ScheduleNavigationState
  if (!Array.isArray(draft.selectedCourses) || typeof draft.semester !== 'string') return null
  return { selectedCourses: draft.selectedCourses.filter(isCourse).slice(0, 6), semester: draft.semester }
}

function readScheduleDraft(): ScheduleDraft {
  try {
    const stored = window.sessionStorage.getItem(SCHEDULE_DRAFT_KEY)
    if (!stored) return { selectedCourses: [], semester: 'A25' }
    const parsed = JSON.parse(stored) as unknown
    return getIncomingDraft(parsed) ?? { selectedCourses: [], semester: 'A25' }
  } catch {
    return { selectedCourses: [], semester: 'A25' }
  }
}

function isCourse(value: unknown): value is Course {
  if (!value || typeof value !== 'object') return false
  const course = value as Course
  return typeof course.id === 'string' && typeof course.name === 'string' && typeof course.credits === 'number'
}
