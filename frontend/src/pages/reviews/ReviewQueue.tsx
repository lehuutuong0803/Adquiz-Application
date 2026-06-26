import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getDueReviews } from '../../api/analytics'
import { createSession } from '../../api/sessions'
import type { DueReviewDto } from '../../api/analytics'
import './ReviewQueue.css'
import SessionConfigDialog from '../../components/session-config/SessionConfigDialog'

export default function ReviewQueue() {
  const navigate = useNavigate()
  const [reviews, setReviews] = useState<DueReviewDto[]>([])
  const [loading, setLoading] = useState(true)
  const [starting, setStarting] = useState<string | null>(null)
  const [configReview, setConfigReview] = useState<DueReviewDto | null>(null)

  useEffect(() => {
    getDueReviews()
      .then(data => setReviews(data))
      .catch(() => setReviews([]))
      .finally(() => setLoading(false))
  }, [])

  const handleStartReview = async (review: DueReviewDto, totalQuestions: number) => {
    if (starting) return
    setStarting(review.topicId)
    setConfigReview(null)

    try {
      const session = await createSession({
        topicId: review.topicId,
        mode: 'REVIEW',
        totalQuestions,
        targetAudience: 'UNIVERSITY_STUDENT',
      })
      navigate(`/sessions/${session.sessionId}`, {
        state: {
          topicName: review.topicName,
          firstQuestion: session.firstQuestion,
        },
      })
    } catch (err) {
      console.error('Failed to create review session:', err)
      setStarting(null)
    }
  }

  const daysSinceReview = (dateStr: string) => {
    const diff = Math.floor(
      (Date.now() - new Date(dateStr).getTime()) / (1000 * 60 * 60 * 24)
    )
    if (diff === 0) return 'Today'
    if (diff === 1) return '1 day ago'
    return `${diff} days ago`
  }

  if (loading) {
    return <div className="reviews-page"><p className="loading-text">Loading reviews...</p></div>
  }

  return (
    <div className="reviews-page">
      <div className="reviews-header">
        <div>
          <h1 className="page-title">Review Queue</h1>
          <p className="reviews-subtitle">
            {reviews.length === 0
              ? 'No topics due for review — you\'re all caught up!'
              : `${reviews.length} topic${reviews.length !== 1 ? 's' : ''} due for review`}
          </p>
        </div>
      </div>

      {starting && (
        <div className="session-loading-overlay">
          <div className="session-loading-dialog">
            <div className="loading-spinner"></div>
            <p className="loading-title">Preparing your review</p>
            <p className="loading-subtitle">Loading review questions...</p>
          </div>
        </div>
      )}

      {configReview && (
        <SessionConfigDialog
          topicName={configReview.topicName}
          mode="REVIEW"
          onClose={() => setConfigReview(null)}
          onConfirm={totalQuestions => handleStartReview(configReview, totalQuestions)}
        />
      )}

      {reviews.length === 0 ? (
        <div className="reviews-empty">
          <div className="empty-icon">✓</div>
          <p className="empty-title">All caught up!</p>
          <p className="empty-subtitle">
            Complete more quizzes to build your review schedule.
            The SM-2 algorithm will tell you when it's time to review.
          </p>
          <button className="empty-btn" onClick={() => navigate('/topics')}>
            Start a quiz
          </button>
        </div>
      ) : (
        <div className="reviews-list">
          {reviews.map(review => (
            <div key={review.topicId} className="review-card">
              <div className="review-card-info">
                <span className="review-card-name">{review.topicName}</span>
                <span className="review-card-parent">{review.parentTopicName}</span>
                <div className="review-card-meta">
                  <span>Last reviewed: {daysSinceReview(review.lastReviewDate)}</span>
                  <span>·</span>
                  <span>Interval: {review.intervalDays} days</span>
                </div>
              </div>
              <button
                className="review-start-btn"
                onClick={() => setConfigReview(review)}
                disabled={starting !== null}
              >
                {starting === review.topicId ? 'Starting...' : 'Start review'}
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}