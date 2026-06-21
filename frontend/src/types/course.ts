export interface Course {
  id: string
  name: string
  description?: string
  credits: number
}

export interface ScheduleDraft {
  selectedCourses: Course[]
  semester: string
}

export type ScheduleNavigationState = ScheduleDraft

export interface CourseDetailNavigationState {
  scheduleDraft: ScheduleDraft
}
