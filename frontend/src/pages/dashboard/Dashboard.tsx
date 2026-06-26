import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getStreak, getWeakAreas, getActivity, getDueReviews } from '../../api/analytics'
import type { StreakDto, WeakAreaDto, DailyActivityDto, DueReviewDto } from '../../api/analytics'
import './Dashboard.css'

function getStreakMessage(current: number, longest: number): string {
  if (current === 0) return 'Start a quiz to begin your streak!'
  if (current >= longest) return "You're at your all-time best — don't stop now!"
  const gap = longest - current
  if (gap <= 3) return `${gap} more day${gap > 1 ? 's' : ''} to beat your record!`
  return `${current} days strong — keep it going!`
}

function getAccuracyColor(accuracy: number): string {
  if (accuracy < 0.6) return 'var(--color-danger)'
  if (accuracy < 0.75) return 'var(--color-warning)'
  return 'var(--color-primary)'
}

export default function Dashboard() {
  const navigate = useNavigate()
  const [streak, setStreak] = useState<StreakDto | null>(null)
  const [dueReviews, setDueReviews] = useState<DueReviewDto[]>([])
  const [activity, setActivity] = useState<DailyActivityDto[]>([])
  const [weakAreas, setWeakAreas] = useState<WeakAreaDto[]>([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([
      getStreak().catch(() => null),
      getDueReviews().catch(() => []),
      getActivity().catch(() => []),
      getWeakAreas().catch(() => []),
    ]).then(([streakData, reviewsData, activityData, weakData]) => {
      setStreak(streakData)
      setDueReviews(reviewsData)
      setActivity(activityData)
      setWeakAreas(weakData)
      setLoading(false)
    })
  }, [])

  const totalQuestions = activity.reduce((sum, a) => sum + a.questionsAnswered, 0)
  const totalCorrect = activity.reduce((sum, a) => sum + a.correctAnswers, 0)
  const weeklyAccuracy = totalQuestions > 0
    ? Math.round((totalCorrect / totalQuestions) * 100)
    : 0

  const todayActivity = activity.find(
    a => a.date === new Date().toISOString().split('T')[0]
  )

  const daysOverdue = (dateStr: string) => {
    const diff = Math.floor(
      (Date.now() - new Date(dateStr).getTime()) / (1000 * 60 * 60 * 24)
    )
    return diff > 0 ? `${diff}d overdue` : 'Due today'
  }

  if (loading) {
    return <div className="dashboard"><p className="loading-text">Loading...</p></div>
  }

  return (
    <div className="dashboard">
      <h1 className="page-title">Good morning</h1>

      <div className="quick-actions">
        <button className="quick-action-btn primary" onClick={() => navigate('/topics')}>
          ▶ Start a quiz
        </button>
        {dueReviews.length > 0 && (
          <button className="quick-action-btn secondary" onClick={() => navigate('/reviews')}>
            ↻ Review due topics ({dueReviews.length})
          </button>
        )}
      </div>

      <div className="stat-cards">
        <div className="stat-card accent">
          <span className="stat-card-label">Current streak</span>
          <span className="stat-card-value">🔥 {streak?.currentStreak ?? 0}</span>
          <span className="stat-card-sub">
            {getStreakMessage(streak?.currentStreak ?? 0, streak?.longestStreak ?? 0)}
          </span>
        </div>
        <div className="stat-card">
          <span className="stat-card-label">Reviews due</span>
          <span className="stat-card-value">{dueReviews.length}</span>
          <span className="stat-card-sub clickable" onClick={() => navigate('/reviews')}>
            View all →
          </span>
        </div>
        <div className="stat-card">
          <span className="stat-card-label">7-day accuracy</span>
          <span className="stat-card-value">{weeklyAccuracy}%</span>
          <span className="stat-card-sub clickable" onClick={() => navigate('/progress')}>
            See progress →
          </span>
        </div>
      </div>

      {todayActivity && (
        <div className="today-summary">
          Today you answered <strong>{todayActivity.questionsAnswered}</strong> questions
          with <strong>{todayActivity.questionsAnswered > 0
            ? Math.round((todayActivity.correctAnswers / todayActivity.questionsAnswered) * 100)
            : 0}%</strong> accuracy
          across <strong>{todayActivity.sessionsCompleted}</strong> sessions.
        </div>
      )}

      <section className="dashboard-section">
        <div className="section-header">
          <h2>Due for review</h2>
          <span className="section-link" onClick={() => navigate('/reviews')}>
            See all →
          </span>
        </div>
        {dueReviews.length === 0 ? (
          <p className="empty-state">No reviews due — you're all caught up!</p>
        ) : (
          <div className="review-chips">
            {dueReviews.map(r => (
              <div key={r.topicId} className="review-chip">
                <div className="chip-info">
                  <span className="chip-name">{r.topicName}</span>
                  <span className="chip-meta">
                    {r.parentTopicName} · {daysOverdue(r.lastReviewDate)}
                  </span>
                </div>
                <button
                  className="chip-action"
                  onClick={() => navigate('/reviews')}
                >
                  Review
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

      <section className="dashboard-section">
        <div className="section-header">
          <h2>Weak areas</h2>
          <span className="section-link" onClick={() => navigate('/progress')}>
            See all →
          </span>
        </div>
        {weakAreas.length === 0 ? (
          <p className="empty-state">No weak areas detected yet — keep practicing!</p>
        ) : (
          <div className="weak-areas-list">
            {weakAreas.map(w => (
              <div key={w.topicId} className="weak-area-row">
                <div className="weak-area-info">
                  <span className="weak-area-name">{w.topicName}</span>
                  <span className="weak-area-meta">
                    {w.parentTopicName} · {w.totalAnswered} questions
                  </span>
                </div>
                <div className="weak-area-right">
                  <span className="weak-area-acc">{Math.round(w.accuracy * 100)}%</span>
                  <div className="weak-area-bar">
                    <div
                      className="weak-area-bar-fill"
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

      <section className="dashboard-section">
        <div className="section-header">
          <h2>Recent activity</h2>
        </div>
        {activity.length === 0 ? (
          <p className="empty-state">No activity yet — start your first quiz!</p>
        ) : (
          <div className="activity-list">
            {activity.map(a => {
              const acc = a.questionsAnswered > 0
                ? Math.round((a.correctAnswers / a.questionsAnswered) * 100)
                : 0
              return (
                <div key={a.date} className="activity-row">
                  <div className="activity-info">
                    <span className="activity-date">{a.date}</span>
                    <span className="activity-meta">
                      {a.questionsAnswered} questions · {a.sessionsCompleted} sessions
                    </span>
                  </div>
                  <div className="activity-right">
                    <span className="activity-acc">{acc}%</span>
                    <div className="activity-bar">
                      <div
                        className="activity-bar-fill"
                        style={{ width: `${acc}%` }}
                      ></div>
                    </div>
                  </div>
                </div>
              )
            })}
          </div>
        )}
      </section>
    </div>
  )
}