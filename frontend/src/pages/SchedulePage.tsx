import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { generateScheduleOptions } from '../api/schedule'
import { CalendarGrid } from '../components/schedule/CalendarGrid'
import { CourseSelector } from '../components/schedule/CourseSelector'
import { courseColor } from '../components/schedule/colors'
import { AppFooter } from '../components/ui/AppFooter'
import { AppHeader } from '../components/ui/AppHeader'
import { InlineLoading, StatusState } from '../components/ui/StatusState'
import { Eyebrow, PageIntro } from '../components/ui/Typography'
import type { Course, ScheduleDraft, ScheduleNavigationState } from '../types/course'
import type { ScheduleOption } from '../types/schedule'

export function SchedulePage() {
  const { t } = useTranslation()
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
    <main className="page-shell flex flex-col">
      <AppHeader action={<Link className="rounded-full px-3 py-2 text-sm font-medium text-secondary outline-none transition hover:text-primary focus-visible:ring-4 focus-visible:ring-accent/20" to="/"><span className="sm:hidden">{t('schedule.nav.backToSearch')}</span><span className="hidden sm:inline">{t('schedule.nav.backToSearch')}</span></Link>} />

      <div className="flex-1">
        <header className="reveal mx-auto flex max-w-5xl flex-col items-center pb-20 pt-24 text-center sm:pt-32">
          <PageIntro eyebrow={t('schedule.hero.eyebrow')} title={t('schedule.hero.title')} description={t('schedule.hero.description')} />
        </header>

        <section className="surface-card mx-auto max-w-5xl p-6 sm:p-8" aria-labelledby="build-heading">
          <h2 className="text-2xl font-semibold tracking-[-0.03em] text-primary" id="build-heading">{t('schedule.builder.title')}</h2>
          <p className="mt-2 text-sm text-secondary">{t('schedule.builder.description')}</p>
          <div className="mt-7"><CourseSelector onAdd={addCourse} onRemove={removeCourse} selectedCourses={selectedCourses} semester={semester} /></div>
          <div className="mt-8 flex flex-col gap-4 border-t border-primary/[0.07] pt-7 sm:flex-row sm:items-end sm:justify-between">
            <label className="text-sm font-medium text-primary">{t('schedule.builder.semesterLabel')}
              <input className="field mt-2 block w-36 py-3 font-mono text-sm uppercase" maxLength={3} onChange={(event) => { setSemester(event.target.value.toUpperCase()); resetSchedule() }} placeholder={t('schedule.builder.semesterPlaceholder')} value={semester} />
            </label>
            <button className="button-primary" disabled={selectedCourses.length === 0 || !semester.trim() || isGenerating} onClick={generate} type="button">
              {isGenerating ? t('schedule.builder.generating') : t('schedule.builder.generateButton')}
            </button>
          </div>
        </section>

        <section className="mx-auto mt-24 max-w-7xl sm:mt-32" aria-busy={isGenerating} aria-live="polite">
          {selectedCourses.length === 0 && !isGenerating && <StatusState title={t('schedule.states.empty.title')} detail={t('schedule.states.empty.detail')} />}
          {selectedCourses.length > 0 && !hasGenerated && !isGenerating && !hasError && <StatusState title={t('schedule.states.ready.title')} detail={t('schedule.states.ready.detail')} />}
          {isGenerating && <InlineLoading message={t('schedule.states.generating.message')} />}
          {hasError && <StatusState title={t('schedule.states.error.title')} detail={t('schedule.states.error.detail')} />}
          {hasGenerated && options.length === 0 && <StatusState title={t('schedule.states.noOptions.title')} detail={t('schedule.states.noOptions.detail')} />}
          {currentOption && (
            <div className="results-enter">
              <div className="mb-8 flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
                <div><Eyebrow>{t('schedule.calendar.headingEyebrow')}</Eyebrow><h2 className="mt-4 text-4xl font-semibold tracking-[-0.045em] text-primary sm:text-5xl">{t('schedule.calendar.option', { current: optionIndex + 1, total: options.length })}</h2><p className="mt-3 text-sm text-secondary">{currentOption.conflits.length === 0 ? t('schedule.calendar.noConflicts') : t('schedule.calendar.conflicts', { count: currentOption.conflits.length })}</p></div>
                <div className="flex gap-2">
                  <button aria-label={t('schedule.calendar.previous')} className="rounded-full border border-primary/10 bg-surface px-5 py-3 text-sm font-semibold text-primary outline-none hover:bg-primary/[0.03] focus-visible:ring-4 focus-visible:ring-accent/20 disabled:opacity-35" disabled={optionIndex === 0} onClick={() => setOptionIndex((current) => current - 1)} type="button">{t('schedule.calendar.previous')}</button>
                  <button aria-label={t('schedule.calendar.next')} className="rounded-full border border-primary/10 bg-surface px-5 py-3 text-sm font-semibold text-primary outline-none hover:bg-primary/[0.03] focus-visible:ring-4 focus-visible:ring-accent/20 disabled:opacity-35" disabled={optionIndex === options.length - 1} onClick={() => setOptionIndex((current) => current + 1)} type="button">{t('schedule.calendar.next')}</button>
                </div>
              </div>
              <div className="surface-card p-4 sm:p-6"><CalendarGrid courses={selectedCourses} option={currentOption} /></div>
              <div className="mt-6 flex flex-wrap gap-x-7 gap-y-3 rounded-card bg-primary/[0.03] px-5 py-4" aria-label={t('schedule.calendar.legend')}>
                {selectedCourses.map((course, index) => { const color = courseColor(index); return <div className="flex min-w-0 items-center gap-2.5" key={course.id}><span className="h-3 w-3 shrink-0 rounded-full" style={{ backgroundColor: color.color }} /><span className="font-mono text-xs font-semibold text-primary">{course.id}</span><span className="max-w-52 truncate text-xs text-secondary">{course.name}</span></div> })}
              </div>
            </div>
          )}
        </section>
      </div>

      <AppFooter />
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
