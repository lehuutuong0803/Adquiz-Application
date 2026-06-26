import client from './client'

export type StreakDto = {
  currentStreak: number
  longestStreak: number
  lastActiveDate: string | null
}

export type TopicAccuracyDto = {
  topicId: string
  topicName: string
  parentTopicName: string | null
  totalAnswered: number
  totalCorrect: number
  accuracy: number
}

export type WeakAreaDto = {
  topicId: string
  topicName: string
  parentTopicName: string | null
  totalAnswered: number
  accuracy: number
}

export type DailyActivityDto = {
  date: string
  questionsAnswered: number
  correctAnswers: number
  sessionsCompleted: number
}

export type DueReviewDto = {
  topicId: string
  topicName: string
  parentTopicName: string | null
  lastReviewDate: string
  intervalDays: number
}

export async function getStreak(): Promise<StreakDto> {
  const response = await client.get('/api/analytics/streak')
  return response.data
}

export async function getAccuracy(): Promise<TopicAccuracyDto[]> {
  const response = await client.get('/api/analytics/accuracy')
  return response.data
}

export async function getWeakAreas(): Promise<WeakAreaDto[]> {
  const response = await client.get('/api/analytics/weak-areas')
  return response.data
}

export async function getActivity(): Promise<DailyActivityDto[]> {
  const response = await client.get('/api/analytics/activity')
  return response.data
}

export async function getDueReviews(): Promise<DueReviewDto[]> {
  const response = await client.get('/api/reviews/due')
  return response.data
}