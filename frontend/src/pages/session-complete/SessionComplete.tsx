import { useNavigate, useParams, useLocation } from 'react-router-dom'
import './SessionComplete.css'

type SessionResult = {
  totalQuestions: number
  correctAnswers: number
  topicName: string
}

export default function SessionComplete() {
  const { id } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const result = location.state as SessionResult | null

  if (!result) {
    return (
      <div className="session-complete">
        <p className="loading-text">No session data available.</p>
        <button className="complete-btn secondary" onClick={() => navigate('/')}>
          Back to Dashboard
        </button>
      </div>
    )
  }

  const accuracy = Math.round((result.correctAnswers / result.totalQuestions) * 100)

  const getMessage = () => {
    if (accuracy === 100) return 'Perfect score! Outstanding work!'
    if (accuracy >= 80) return 'Great job! Keep up the momentum!'
    if (accuracy >= 60) return 'Good effort! Review the tricky ones.'
    if (accuracy >= 40) return 'Keep practicing — you\'re getting there!'
    return 'Don\'t give up! Every attempt helps you learn.'
  }

  const getAccuracyColor = () => {
    if (accuracy >= 80) return 'var(--color-success)'
    if (accuracy >= 60) return 'var(--color-warning)'
    return 'var(--color-danger)'
  }

  return (
    <div className="session-complete">
      <div className="complete-card">
        <div
          className="score-ring"
          style={{
            background: `conic-gradient(${getAccuracyColor()} ${accuracy * 3.6}deg, var(--color-border) 0deg)`,
          }}
        >
          <div className="score-ring-inner">
            <span className="score-value">{accuracy}%</span>
          </div>
        </div>

        <h1 className="complete-title">Session complete</h1>
        <p className="complete-topic">{result.topicName}</p>
        <p className="complete-message">{getMessage()}</p>

        <div className="complete-stats">
          <div className="complete-stat">
            <span className="complete-stat-value">
              {result.correctAnswers}/{result.totalQuestions}
            </span>
            <span className="complete-stat-label">Correct</span>
          </div>
          <div className="stat-divider"></div>
          <div className="complete-stat">
            <span className="complete-stat-value">{accuracy}%</span>
            <span className="complete-stat-label">Accuracy</span>
          </div>
          <div className="stat-divider"></div>
          <div className="complete-stat">
            <span className="complete-stat-value">
              {result.totalQuestions - result.correctAnswers}
            </span>
            <span className="complete-stat-label">Missed</span>
          </div>
        </div>

        <div className="complete-actions">
          <button className="complete-btn primary" onClick={() => navigate('/')}>
            Back to Dashboard
          </button>
          <button className="complete-btn secondary" onClick={() => navigate('/topics')}>
            Start another quiz
          </button>
        </div>
      </div>
    </div>
  )
}