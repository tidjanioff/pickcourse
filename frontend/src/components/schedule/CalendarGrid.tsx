import type { CSSProperties } from 'react'
import type { Course } from '../../types/course'
import type { ScheduleBlock, ScheduleConflict, ScheduleOption } from '../../types/schedule'
import { COURSE_COLORS } from './colors'

const HOUR_HEIGHT = 64
const DAY_NAMES: Record<string, string> = { Lu: 'Monday', Ma: 'Tuesday', Me: 'Wednesday', Je: 'Thursday', Ve: 'Friday', Sa: 'Saturday', Di: 'Sunday' }
const WEEKDAY_BASELINE = ['Lu', 'Ma', 'Me', 'Je', 'Ve']
const DAY_ORDER = [...WEEKDAY_BASELINE, 'Sa', 'Di']
const DAY_ALIASES: Record<string, string> = {
  lu: 'Lu', lun: 'Lu', lundi: 'Lu', mon: 'Lu', monday: 'Lu',
  ma: 'Ma', mar: 'Ma', mardi: 'Ma', tue: 'Ma', tuesday: 'Ma',
  me: 'Me', mer: 'Me', mercredi: 'Me', wed: 'Me', wednesday: 'Me',
  je: 'Je', jeu: 'Je', jeudi: 'Je', thu: 'Je', thursday: 'Je',
  ve: 'Ve', ven: 'Ve', vendredi: 'Ve', fri: 'Ve', friday: 'Ve',
  sa: 'Sa', sam: 'Sa', samedi: 'Sa', sat: 'Sa', saturday: 'Sa',
  di: 'Di', dim: 'Di', dimanche: 'Di', sun: 'Di', sunday: 'Di',
}
interface CalendarEvent {
  courseId: string
  day: string
  start: number
  end: number
  time: string
}

export function CalendarGrid({ option, courses }: { option: ScheduleOption; courses: Course[] }) {
  const events = toEvents(option.horaire)
  const observedDays = [...new Set(events.map((event) => event.day))]
  const additionalDays = observedDays
    .filter((day) => !WEEKDAY_BASELINE.includes(day))
    .sort((left, right) => daySortIndex(left) - daySortIndex(right))
  const days = [...WEEKDAY_BASELINE, ...additionalDays]
  const minMinutes = events.length > 0 ? Math.floor(Math.min(...events.map((event) => event.start)) / 60) * 60 : 8 * 60
  const maxMinutes = events.length > 0 ? Math.ceil(Math.max(...events.map((event) => event.end)) / 60) * 60 : 17 * 60
  const totalHeight = ((maxMinutes - minMinutes) / 60) * HOUR_HEIGHT
  const hours = Array.from({ length: (maxMinutes - minMinutes) / 60 + 1 }, (_, index) => minMinutes + index * 60)

  return (
    <div className="overflow-x-auto pb-3">
      <div className="min-w-[760px]">
        <div className="grid border-b border-primary/[0.08]" style={{ gridTemplateColumns: `4.5rem repeat(${days.length}, minmax(0, 1fr))` }}>
          <div />
          {days.map((day) => <div className="pb-4 text-center text-xs font-semibold text-secondary" key={day}>{DAY_NAMES[day] ?? day}</div>)}
        </div>
        <div className="grid" style={{ gridTemplateColumns: `4.5rem repeat(${days.length}, minmax(0, 1fr))` }}>
          <div className="relative" style={{ height: totalHeight }}>
            {hours.map((minutes, index) => <span className="absolute right-3 -translate-y-1/2 font-mono text-[10px] text-secondary" key={minutes} style={{ top: index * HOUR_HEIGHT }}>{formatTime(minutes)}</span>)}
          </div>
          {days.map((day) => (
            <div className="relative border-l border-primary/[0.07]" key={day} style={{ height: totalHeight, backgroundImage: `repeating-linear-gradient(to bottom, transparent 0, transparent ${HOUR_HEIGHT - 1}px, var(--color-grid-line) ${HOUR_HEIGHT - 1}px, var(--color-grid-line) ${HOUR_HEIGHT}px)` }}>
              {events.filter((event) => event.day === day).map((event, index) => (
                <CalendarBlock conflict={matchingConflict(event, option.conflits)} course={courses.find((course) => course.id === event.courseId)} courseIndex={courses.findIndex((course) => course.id === event.courseId)} event={event} key={`${event.courseId}-${event.time}-${index}`} minMinutes={minMinutes} option={option} />
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}

function CalendarBlock({ event, course, courseIndex, minMinutes, option, conflict }: { event: CalendarEvent; course?: Course; courseIndex: number; minMinutes: number; option: ScheduleOption; conflict?: ScheduleConflict }) {
  const color = COURSE_COLORS[Math.max(courseIndex, 0) % COURSE_COLORS.length]
  const conflictCount = conflict?.cours.length ?? 1
  const conflictIndex = conflict ? conflict.cours.indexOf(event.courseId) : 0
  const position: CSSProperties = {
    top: ((event.start - minMinutes) / 60) * HOUR_HEIGHT + 2,
    height: Math.max(((event.end - event.start) / 60) * HOUR_HEIGHT - 4, 28),
    left: conflict ? `calc(${(conflictIndex / conflictCount) * 100}% + 3px)` : '3px',
    width: conflict ? `calc(${100 / conflictCount}% - 6px)` : 'calc(100% - 6px)',
    backgroundColor: color.background,
    color: color.color,
  }
  const choice = option.choices[event.courseId]
  const sections = choice ? Object.values(choice).join(' / ') : ''

  return (
    <article className={`absolute z-10 overflow-hidden rounded-lg p-2 shadow-sm ${conflict ? 'border border-conflict' : 'border border-transparent'}`} style={position} title={`${course?.name ?? event.courseId} · ${event.time}`}>
      <div className="flex items-center justify-between gap-1">
        <p className="truncate font-mono text-[11px] font-bold">{event.courseId}</p>
        {conflict && <ConflictBadge />}
      </div>
      <p className="mt-0.5 truncate font-mono text-[9px] opacity-75">{event.time}</p>
      {sections && <p className="mt-1 truncate text-[9px] font-medium opacity-75">Section {sections}</p>}
    </article>
  )
}

function ConflictBadge() {
  return <span aria-label="Schedule conflict" className="grid h-4 w-4 shrink-0 place-items-center rounded-full bg-conflict text-[10px] font-bold text-white" title="Schedule conflict">!</span>
}

function toEvents(schedule: Record<string, ScheduleBlock[]>): CalendarEvent[] {
  return Object.entries(schedule).flatMap(([courseId, blocks]) => blocks.flatMap(([rawDays, time]) => {
    const [startText, endText] = time.split('-')
    const start = parseTime(startText)
    const end = parseTime(endText)
    if (start === null || end === null || end <= start) return []
    const days = parseDays(rawDays)
    return days.map((day) => ({ courseId, day, start, end, time }))
  }))
}

function matchingConflict(event: CalendarEvent, conflicts: ScheduleConflict[]) {
  return conflicts.find((conflict) => {
    const days = parseDays(conflict.jour)
    const [start, end] = conflict.intervalle.split('-').map(parseTime)
    return days.includes(event.day) && conflict.cours.includes(event.courseId) && start !== null && end !== null && event.start < end && start < event.end
  })
}

function parseDays(rawDays: string) {
  return rawDays
    .replaceAll('[', '')
    .replaceAll(']', '')
    .split(',')
    .map(normalizeDay)
    .filter(Boolean)
}

function normalizeDay(value: string) {
  const trimmed = value.trim()
  if (!trimmed) return ''
  const key = trimmed.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().replaceAll('.', '')
  return DAY_ALIASES[key] ?? trimmed
}

function daySortIndex(day: string) {
  const index = DAY_ORDER.indexOf(day)
  return index === -1 ? DAY_ORDER.length : index
}

function parseTime(value: string): number | null {
  if (!/^\d{2}:\d{2}$/.test(value)) return null
  const [hours, minutes] = value.split(':').map(Number)
  return hours * 60 + minutes
}

function formatTime(minutes: number) {
  const hours = Math.floor(minutes / 60)
  const suffix = hours >= 12 ? 'PM' : 'AM'
  const displayHours = hours % 12 || 12
  return `${displayHours} ${suffix}`
}
