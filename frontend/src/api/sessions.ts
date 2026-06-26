import client from './client'

export type OptionDto = {
  id: string
  text: string
}

export type QuestionDto = {
  id: string
  text: string
  options: OptionDto[]
  bloomLevel: number
  questionIndex: number
  totalQuestions: number
}

export type SessionResponse = {
  sessionId: string
  status: string
  firstQuestion: QuestionDto
}

export type AnswerResponse = {
  isCorrect: boolean
  correctAnswer: string
  explanation: string
  nextQuestion: QuestionDto | null
  sessionStatus: string
}

export type SubmitAnswerRequest = {
  questionId: string
  selectedOption: string
  confidenceRating: number
}

export type CreateSessionRequest = {
  topicId?: string
  topicName?: string
  parentTopicId?: string
  parentTopicName?: string
  mode: 'ADAPTIVE' | 'REVIEW'
  totalQuestions: number
  targetAudience: 'UNIVERSITY_STUDENT' | 'INTERVIEW_PREP'
}

export type SessionStateDto = {
  sessionId: string
  status: string
  currentQuestionIndex: number
  totalQuestions: number
  currentDifficulty: number
  topicName: string
}

export type SessionSummaryDto = {
  sessionId: string
  topicId: string
  topicName: string
  mode: string
  status: string
  totalQuestions: number
  startedAt: string
  endedAt: string | null
}

export async function createSession(request: CreateSessionRequest): Promise<SessionResponse> {
  const response = await client.post('/api/sessions', request)
  return response.data
}

export async function submitAnswer(sessionId: string, request: SubmitAnswerRequest): Promise<AnswerResponse> {
  const response = await client.post(`/api/sessions/${sessionId}/answer`, request)
  return response.data
}

export async function getSessionState(sessionId: string): Promise<SessionStateDto> {
  const response = await client.get(`/api/sessions/${sessionId}`)
  return response.data
}

export async function getSessionHistory(): Promise<SessionSummaryDto[]> {
  const response = await client.get('/api/sessions')
  return response.data
}

export async function abandonSession(sessionId: string): Promise<void> {
  await client.post(`/api/sessions/${sessionId}/complete`)
}