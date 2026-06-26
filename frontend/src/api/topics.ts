import client from './client'

export type TopicDto = {
    id: string
    name: string
    subtopics: SubtopicDto[]
}

export type SubtopicDto = {
    id: string
    name: string
}

export async function getAllTopics(): Promise<TopicDto[]> {
    const response = await client.get('api/topics')
    return response.data
}

export async function getSubtopics(parentId: string): Promise<SubtopicDto[]> {
    const response = await client.get(`/api/topics/${parentId}/subtopics`)
    return response.data
}

export async function searchParentTopics(query: string): Promise<SubtopicDto[]> {
  const response = await client.get('/api/topics/search-parents', {
    params: { q: query },
  })
  return response.data
}

export async function searchSubtopics(query: string, parentId: string): Promise<SubtopicDto[]> {
  const response = await client.get('/api/topics/search', {
    params: { q: query, parentId },
  })
  return response.data
}