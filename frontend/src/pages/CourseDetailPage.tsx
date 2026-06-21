import { useEffect, useState, type FormEvent } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import {
  checkEligibility,
  createReview,
  getCourseDifficulty,
  getCoursePopularity,
  getCourseReviews,
  type CourseReview,
  type EligibilityResult,
} from '../api/courseDetails'
import { getCourse } from '../api/courses'
import { AppHeader } from '../components/ui/AppHeader'
import { InlineLoading, StatusState } from '../components/ui/StatusState'
import { SectionHeading } from '../components/ui/Typography'
import type { Course, CourseDetailNavigationState, ScheduleDraft, ScheduleNavigationState } from '../types/course'

const inputClass = 'field mt-2 w-full'

export function CourseDetailPage() {
  const location = useLocation()
  const { id = '' } = useParams()
  const courseId = decodeURIComponent(id).toUpperCase()
  const [course, setCourse] = useState<Course | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [courseError, setCourseError] = useState(false)
  const [difficulty, setDifficulty] = useState<string | null>(null)
  const [popularity, setPopularity] = useState<string | null>(null)
  const [statsError, setStatsError] = useState(false)
  const [reviews, setReviews] = useState<CourseReview[]>([])
  const [reviewsLoading, setReviewsLoading] = useState(true)
  const [reviewsError, setReviewsError] = useState(false)

  useEffect(() => {
    const controller = new AbortController()
    setIsLoading(true)
    setCourseError(false)
    setStatsError(false)
    setReviewsLoading(true)
    setReviewsError(false)

    getCourse(courseId, controller.signal)
      .then((result) => {
        setCourse(result)
        setCourseError(!result)
      })
      .catch((error) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) setCourseError(true)
      })
      .finally(() => {
        if (!controller.signal.aborted) setIsLoading(false)
      })

    Promise.all([
      getCourseDifficulty(courseId, controller.signal),
      getCoursePopularity(courseId, controller.signal),
    ])
      .then(([difficultyResult, popularityResult]) => {
        setDifficulty(difficultyResult)
        setPopularity(popularityResult)
      })
      .catch((error) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) setStatsError(true)
      })

    getCourseReviews(courseId, controller.signal)
      .then(setReviews)
      .catch((error) => {
        if (!(error instanceof DOMException && error.name === 'AbortError')) setReviewsError(true)
      })
      .finally(() => {
        if (!controller.signal.aborted) setReviewsLoading(false)
      })

    return () => controller.abort()
  }, [courseId])

  if (isLoading) return <FullPageStatus loading title="Loading course…" />

  if (courseError || !course) {
    return (
      <FullPageStatus
        title="We couldn't find that course."
        detail="Check the course code or return to search."
        action={<Link className="font-semibold text-accent" to="/">Back to search</Link>}
      />
    )
  }

  const scheduleDraft = getScheduleDraft(location.state)
  const selectedCourses = mergeCourses(scheduleDraft?.selectedCourses ?? [], course)
  const scheduleState = {
    selectedCourses,
    semester: scheduleDraft?.semester ?? 'A25',
  } satisfies ScheduleNavigationState

  return (
    <main className="page-shell">
      <AppHeader action={
        <Link
          className="rounded-full px-3 py-2 text-sm font-medium text-secondary outline-none transition hover:text-primary focus-visible:ring-4 focus-visible:ring-accent/20"
          state={scheduleDraft ? { selectedCourses: scheduleDraft.selectedCourses, semester: scheduleDraft.semester } satisfies ScheduleNavigationState : undefined}
          to={scheduleDraft ? '/schedule' : '/'}
        >
          <span className="sm:hidden">← Back</span>
          <span className="hidden sm:inline">{scheduleDraft ? '← Back to schedule' : '← All courses'}</span>
        </Link>
      } />

      <header className="reveal mx-auto max-w-5xl pb-20 pt-24 sm:pb-24 sm:pt-32">
        <div className="flex flex-col items-start justify-between gap-8 sm:flex-row sm:items-end">
          <div>
            <p className="font-mono text-xl font-semibold tracking-[0.08em] text-accent sm:text-2xl">{course.id}</p>
            <h1 className="mt-5 max-w-4xl text-5xl font-semibold leading-[0.97] tracking-[-0.055em] text-primary sm:text-7xl lg:text-[5.25rem]">{course.name}</h1>
          </div>
          <span className="shrink-0 rounded-full bg-primary/[0.05] px-4 py-2 font-mono text-sm text-primary">{course.credits} credits</span>
        </div>
        <p className="mt-9 max-w-3xl text-lg leading-8 text-secondary sm:text-xl sm:leading-9">
          {course.description || 'A course description is not available yet.'}
        </p>
        <Link
          className="button-primary mt-10 inline-flex items-center"
          state={scheduleState}
          to="/schedule"
        >
          Add to schedule <span className="ml-2" aria-hidden="true">→</span>
        </Link>
      </header>

      <div className="mx-auto max-w-5xl space-y-24 sm:space-y-32">
        <EligibilitySection courseId={course.id} />
        <StatsSection difficulty={difficulty} popularity={popularity} hasError={statsError} />
        <ReviewsSection courseId={course.id} reviews={reviews} setReviews={setReviews} isLoading={reviewsLoading} hasError={reviewsError} />
      </div>
    </main>
  )
}

function getScheduleDraft(state: unknown): ScheduleDraft | null {
  if (!state || typeof state !== 'object' || !('scheduleDraft' in state)) return null
  const draft = (state as CourseDetailNavigationState).scheduleDraft
  if (!draft || !Array.isArray(draft.selectedCourses) || typeof draft.semester !== 'string') return null
  return draft
}

function mergeCourses(courses: Course[], courseToAdd: Course) {
  if (courses.some((course) => course.id.toUpperCase() === courseToAdd.id.toUpperCase()) || courses.length >= 6) return courses
  return [...courses, courseToAdd]
}

function EligibilitySection({ courseId }: { courseId: string }) {
  const [completedCourses, setCompletedCourses] = useState('')
  const [cycle, setCycle] = useState('1')
  const [result, setResult] = useState<EligibilityResult | null>(null)
  const [isChecking, setIsChecking] = useState(false)
  const [hasError, setHasError] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const completed = completedCourses.split(',').map((code) => code.trim().toUpperCase()).filter(Boolean)
    setIsChecking(true)
    setHasError(false)
    setResult(null)
    try {
      setResult(await checkEligibility(courseId, completed, Number(cycle)))
    } catch {
      setHasError(true)
    } finally {
      setIsChecking(false)
    }
  }

  return (
    <section aria-labelledby="eligibility-heading">
      <SectionHeading eyebrow="Plan with confidence" title="Check your eligibility." description="Enter the courses you have completed and your current study cycle." id="eligibility-heading" />
      <div className="surface-card mt-10 p-6 sm:p-8">
        <form className="grid gap-5 sm:grid-cols-[1fr_10rem_auto] sm:items-end" onSubmit={handleSubmit}>
          <label className="text-sm font-medium text-primary">
            Completed courses
            <input className={inputClass} value={completedCourses} onChange={(event) => setCompletedCourses(event.target.value)} placeholder="IFT1015, MAT1400" />
          </label>
          <label className="text-sm font-medium text-primary">
            Study cycle
            <select className={inputClass} value={cycle} onChange={(event) => setCycle(event.target.value)}>
              {[1, 2, 3, 4].map((value) => <option key={value} value={value}>Cycle {value}</option>)}
            </select>
          </label>
          <button className="button-secondary py-4" disabled={isChecking} type="submit">
            {isChecking ? 'Checking…' : 'Check eligibility'}
          </button>
        </form>
        {result && (
          <div className={`mt-6 rounded-card border px-5 py-4 text-sm leading-6 text-primary ${result.eligible ? 'border-accent/20 bg-accent/[0.06]' : 'border-conflict/20 bg-conflict/[0.05]'}`} role="status">
            <p className="font-semibold">{result.eligible ? 'You are eligible.' : 'Not eligible yet.'}</p>
            <p className="mt-1 opacity-80">{result.message}</p>
          </div>
        )}
        {hasError && <InlineError message="Eligibility could not be checked right now. Please try again." />}
      </div>
    </section>
  )
}

function StatsSection({ difficulty, popularity, hasError }: { difficulty: string | null; popularity: string | null; hasError: boolean }) {
  return (
    <section aria-labelledby="stats-heading">
      <SectionHeading eyebrow="At a glance" title="Know what to expect." description="Historical performance and enrollment, stated simply." id="stats-heading" />
      {hasError ? <InlineError message="Course stats are temporarily unavailable." /> : (
        <div className="mt-10 grid gap-4 sm:grid-cols-2">
          <StatCard label="Difficulty" value={difficultyLabel(difficulty)} detail={difficulty} />
          <StatCard label="Popularity" value={popularityLabel(popularity)} detail={popularity} />
        </div>
      )}
    </section>
  )
}

function ReviewsSection({ courseId, reviews, setReviews, isLoading, hasError }: { courseId: string; reviews: CourseReview[]; setReviews: React.Dispatch<React.SetStateAction<CourseReview[]>>; isLoading: boolean; hasError: boolean }) {
  const [professor, setProfessor] = useState('')
  const [difficulty, setDifficulty] = useState('3')
  const [workload, setWorkload] = useState('3')
  const [comment, setComment] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [submitError, setSubmitError] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSubmitting(true)
    setSubmitError(false)
    setSubmitted(false)
    try {
      await createReview({ sigleCours: courseId, professeur: professor.trim(), noteDifficulte: Number(difficulty), noteCharge: Number(workload), commentaire: comment.trim() })
      setReviews((current) => [{ sigleCours: courseId, nomProfesseur: professor.trim() || null, noteDifficulte: Number(difficulty), noteChargeTravail: Number(workload), commentaire: comment.trim(), valide: true }, ...current])
      setProfessor('')
      setDifficulty('3')
      setWorkload('3')
      setComment('')
      setSubmitted(true)
    } catch {
      setSubmitError(true)
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section aria-labelledby="reviews-heading">
      <SectionHeading eyebrow="Student perspective" title="Reviews from the classroom." description="Experiences shared by students who have taken this course." id="reviews-heading" />
      <div className="mt-10 space-y-4">
        {isLoading && <InlineLoading message="Loading reviews…" />}
        {hasError && <InlineError message="Reviews are temporarily unavailable." />}
        {!isLoading && !hasError && reviews.length === 0 && <p className="rounded-panel bg-primary/[0.035] px-6 py-8 text-center text-sm text-secondary">No reviews yet. Be the first to share your experience.</p>}
        {reviews.map((review, index) => (
          <article className="surface-card p-6 sm:p-8" key={`${review.nomProfesseur}-${index}`}>
            <div className="flex flex-wrap items-center justify-between gap-3">
              <h3 className="font-semibold text-primary">{review.nomProfesseur || 'Professor not specified'}</h3>
              <div className="flex gap-4 font-mono text-xs text-secondary">
                <span>Difficulty {review.noteDifficulte}/5</span>
                <span>Workload {review.noteChargeTravail}/5</span>
              </div>
            </div>
            <p className="mt-5 leading-7 text-secondary">{review.commentaire}</p>
          </article>
        ))}
      </div>

      <div className="mt-14 border-t border-primary/[0.08] pt-14">
        <h3 className="text-3xl font-semibold tracking-[-0.04em] text-primary">Leave a review.</h3>
        <p className="mt-3 text-secondary">Share a concise, useful note for the next student.</p>
        <form className="mt-8 grid gap-5 sm:grid-cols-2" onSubmit={handleSubmit}>
          <label className="text-sm font-medium text-primary sm:col-span-2">Professor name <span className="font-normal text-secondary">(optional)</span>
            <input className={inputClass} value={professor} onChange={(event) => setProfessor(event.target.value)} placeholder="Professor name" />
          </label>
          <RatingSelect label="Difficulty" value={difficulty} onChange={setDifficulty} />
          <RatingSelect label="Workload" value={workload} onChange={setWorkload} />
          <label className="text-sm font-medium text-primary sm:col-span-2">Comment
            <textarea className={`${inputClass} min-h-32 resize-y`} required value={comment} onChange={(event) => setComment(event.target.value)} placeholder="What should other students know?" />
          </label>
          <div className="flex flex-wrap items-center gap-4 sm:col-span-2">
            <button className="button-primary" disabled={isSubmitting || !comment.trim()} type="submit">{isSubmitting ? 'Submitting…' : 'Submit review'}</button>
            {submitted && <p className="text-sm font-medium text-accent" role="status">Thank you. Your review was submitted.</p>}
            {submitError && <p className="text-sm text-conflict" role="alert">Your review could not be submitted. Please try again.</p>}
          </div>
        </form>
      </div>
    </section>
  )
}

function StatCard({ label, value, detail }: { label: string; value: string; detail: string | null }) {
  return <article className="rounded-panel bg-primary/[0.035] p-6 sm:p-8"><p className="font-mono text-xs font-semibold uppercase tracking-[0.18em] text-secondary">{label}</p><p className="mt-5 text-3xl font-semibold tracking-[-0.04em] text-primary">{value}</p><p className="mt-4 text-sm leading-6 text-secondary">{detail || 'Loading…'}</p></article>
}

function RatingSelect({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return <label className="text-sm font-medium text-primary">{label}<select className={inputClass} value={value} onChange={(event) => onChange(event.target.value)}>{[1, 2, 3, 4, 5].map((rating) => <option key={rating} value={rating}>{rating} / 5</option>)}</select></label>
}

function InlineError({ message }: { message: string }) {
  return <p className="mt-6 rounded-card bg-primary/[0.035] px-5 py-4 text-sm text-secondary" role="status">{message}</p>
}

function FullPageStatus({ title, detail, action, loading = false }: { title: string; detail?: string; action?: React.ReactNode; loading?: boolean }) {
  return <main className="page-shell"><AppHeader /><StatusState action={action} className="grid min-h-[70vh] place-content-center" detail={detail} loading={loading} title={title} /></main>
}

function difficultyLabel(message: string | null) {
  if (!message) return 'Loading…'
  if (message.includes('difficulté moyenne')) return 'Moderate'
  if (message.includes('facile')) return 'Approachable'
  if (message.includes('difficile')) return 'Challenging'
  return 'Not available'
}

function popularityLabel(message: string | null) {
  if (!message) return 'Loading…'
  if (message.includes('très populaire')) return 'Very popular'
  if (message.includes('modérément populaire')) return 'Popular'
  if (message.includes('peu populaire')) return 'Smaller cohort'
  return 'Not available'
}
