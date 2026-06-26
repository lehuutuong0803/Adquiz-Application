import { useState } from 'react'
import './SessionConfigDialog.css'

type SessionConfigProps = {
  topicName: string
  mode: 'ADAPTIVE' | 'REVIEW'
  onConfirm: (totalQuestions: number) => void
  onClose: () => void
}

export default function SessionConfigDialog({
  topicName,
  mode,
  onConfirm,
  onClose,
}: SessionConfigProps) {
  const [totalQuestions, setTotalQuestions] = useState(5)

  const handleChange = (value: string) => {
    const num = parseInt(value)
    if (isNaN(num)) return
    if (num < 2) setTotalQuestions(2)
    else if (num > 20) setTotalQuestions(20)
    else setTotalQuestions(num)
  }

  return (
    <div className="config-overlay" onClick={onClose}>
      <div className="config-dialog" onClick={e => e.stopPropagation()}>
        <button className="config-close" onClick={onClose}>✕</button>

        <p className="config-title">
          {mode === 'ADAPTIVE' ? 'Start practice' : 'Start review'}
        </p>
        <p className="config-topic">{topicName}</p>

        <div className="config-field">
          <label className="config-label">Number of questions</label>
          <div className="config-input-row">
            <button
              className="config-step-btn"
              onClick={() => setTotalQuestions(prev => Math.max(2, prev - 1))}
              disabled={totalQuestions <= 2}
            >
              −
            </button>
            <input
              type="number"
              className="config-input"
              value={totalQuestions}
              min={2}
              max={20}
              onChange={e => handleChange(e.target.value)}
            />
            <button
              className="config-step-btn"
              onClick={() => setTotalQuestions(prev => Math.min(20, prev + 1))}
              disabled={totalQuestions >= 20}
            >
              +
            </button>
          </div>
          <span className="config-hint">Min 2 · Max 20</span>
        </div>

        <button
          className="config-confirm-btn"
          onClick={() => onConfirm(totalQuestions)}
        >
          {mode === 'ADAPTIVE' ? 'Start practice' : 'Start review'}
        </button>
      </div>
    </div>
  )
}