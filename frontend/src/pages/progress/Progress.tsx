import { useState, useEffect } from 'react'
import { getAccuracy, getWeakAreas, getActivity } from '../../api/analytics'
import type { TopicAccuracyDto, WeakAreaDto, DailyActivityDto } from '../../api/analytics'
import './Progress.css'

function getAccuracyColor(accuracy: number): string {
  if (accuracy < 0.6) return 'var(--color-danger)'
  if (accuracy < 0.75) return 'var(--color-warning)'
  return 'var(--color-primary)'
}

function getHeatmapColor(count: number, max: number): string {
  if (count === 0) return 'var(--color-border)'
  const ratio = count / max
  if (ratio > 0.75) return '#7c3aed'
  if (ratio > 0.5) return '#a78bfa'
  if (ratio > 0.25) return '#c4b5fd'
  return '#ddd6fe'
}

function buildHeatmapData(activity: DailyActivityDto[]): { date: string; count: number }[] {
  const map = new Map<string, number>()
  for (const a of activity) {
    map.set(a.date, a.questionsAnswered)
  }

  const days: { date: string; count: number }[] = []
  const today = new Date()

  for (let i = 29; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    const key = d.toISOString().split('T')[0]
    days.push({ date: key, count: map.get(key) ?? 0 })
  }

  return days
}

function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
}

export default function Progress() {
  const [accuracy, setAccuracy] = useState<TopicAccuracyDto[]>([])
  const [weakAreas, setWeakAreas] = useState<WeakAreaDto[]>([])
  const [activity, setActivity] = useState<DailyActivityDto[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      getAccuracy().catch(() => []),
      getWeakAreas().catch(() => []),
      getActivity().catch(() => []),
    ]).then(([accData, weakData, actData]) => {
      setAccuracy(accData)
      setWeakAreas(weakData)
      setActivity(actData)
      setLoading(false)
    })
  }, [])

  const heatmapDays = buildHeatmapData(activity)
  const maxCount = Math.max(...heatmapDays.map(d => d.count), 1)

  const totalQuestions = activity.reduce((sum, a) => sum + a.questionsAnswered, 0)
  const totalCorrect = activity.reduce((sum, a) => sum + a.correctAnswers, 0)
  const totalSessions = activity.reduce((sum, a) => sum + a.sessionsCompleted, 0)
  const overallAccuracy = totalQuestions > 0
    ? Math.round((totalCorrect / totalQuestions) * 100)
    : 0

  if (loading) {
    return <div className="progress-page"><p className="loading-text">Loading progress...</p></div>
  }

  return (
    <div className="progress-page">
      <h1 className="page-title">Progress</h1>

      <div className="progress-overview">
        <div className="overview-stat">
          <span className="overview-value">{totalQuestions}</span>
          <span className="overview-label">Questions answered</span>
        </div>
        <div className="overview-stat">
          <span className="overview-value">{overallAccuracy}%</span>
          <span className="overview-label">Overall accuracy</span>
        </div>
        <div className="overview-stat">
          <span className="overview-value">{totalSessions}</span>
          <span className="overview-label">Sessions completed</span>
        </div>
      </div>

      <section className="progress-section">
        <h2 className="section-heading">Activity (last 30 days)</h2>
        <div className="heatmap-card">
          <div className="heatmap-grid">
            {heatmapDays.map(day => (
              <div
                key={day.date}
                className="heatmap-cell"
                style={{ backgroundColor: getHeatmapColor(day.count, maxCount) }}
                title={`${formatDate(day.date)}: ${day.count} questions`}
              >
              </div>
            ))}
          </div>
          <div className="heatmap-legend">
            <span className="heatmap-legend-label">Less</span>
            <div className="heatmap-cell legend" style={{ backgroundColor: 'var(--color-border)' }}></div>
            <div className="heatmap-cell legend" style={{ backgroundColor: '#ddd6fe' }}></div>
            <div className="heatmap-cell legend" style={{ backgroundColor: '#c4b5fd' }}></div>
            <div className="heatmap-cell legend" style={{ backgroundColor: '#a78bfa' }}></div>
            <div className="heatmap-cell legend" style={{ backgroundColor: '#7c3aed' }}></div>
            <span className="heatmap-legend-label">More</span>
          </div>
        </div>
      </section>

      <section className="progress-section">
        <h2 className="section-heading">Weak areas</h2>
        {weakAreas.length === 0 ? (
          <p className="empty-state">No weak areas detected yet — keep practicing!</p>
        ) : (
          <div className="topic-accuracy-list">
            {weakAreas.map(w => (
              <div key={w.topicId} className="accuracy-row">
                <div className="accuracy-info">
                  <span className="accuracy-name">{w.topicName}</span>
                  <span className="accuracy-parent">{w.parentTopicName}</span>
                </div>
                <div className="accuracy-right">
                  <span className="accuracy-value">{Math.round(w.accuracy * 100)}%</span>
                  <div className="accuracy-bar">
                    <div
                      className="accuracy-bar-fill"
                      style={{
                        width: `${Math.round(w.accuracy * 100)}%`,
                        backgroundColor: getAccuracyColor(w.accuracy),
                      }}
                    ></div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="progress-section">
        <h2 className="section-heading">Accuracy by topic</h2>
        {accuracy.length === 0 ? (
          <p className="empty-state">No topics practiced yet.</p>
        ) : (
          <div className="topic-accuracy-list">
            {accuracy.map(a => (
              <div key={a.topicId} className="accuracy-row">
                <div className="accuracy-info">
                  <span className="accuracy-name">{a.topicName}</span>
                  <span className="accuracy-parent">
                    {a.parentTopicName} · {a.totalAnswered} questions
                  </span>
                </div>
                <div className="accuracy-right">
                  <span className="accuracy-value">{Math.round(a.accuracy * 100)}%</span>
                  <div className="accuracy-bar">
                    <div
                      className="accuracy-bar-fill"
                      style={{
                        width: `${Math.round(a.accuracy * 100)}%`,
                        backgroundColor: getAccuracyColor(a.accuracy),
                      }}
                    ></div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </section>
    </div>
  )
}