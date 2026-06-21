export const COURSE_COLORS = [
  { background: 'var(--color-course-1-bg)', color: 'var(--color-course-1-text)' },
  { background: 'var(--color-course-2-bg)', color: 'var(--color-course-2-text)' },
  { background: 'var(--color-course-3-bg)', color: 'var(--color-course-3-text)' },
  { background: 'var(--color-course-4-bg)', color: 'var(--color-course-4-text)' },
  { background: 'var(--color-course-5-bg)', color: 'var(--color-course-5-text)' },
  { background: 'var(--color-course-6-bg)', color: 'var(--color-course-6-text)' },
]

export function courseColor(index: number) {
  return COURSE_COLORS[index % COURSE_COLORS.length]
}
