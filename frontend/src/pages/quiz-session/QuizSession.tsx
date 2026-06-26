import { useState, useEffect } from 'react'
import { useLocation, useNavigate, useParams} from 'react-router-dom'
import { submitAnswer } from '../../api/sessions'
import type { QuestionDto, AnswerResponse } from '../../api/sessions'
import './QuizSession.css'

const bloomLabels: Record<number, string> = {
  1: 'Remember',
  2: 'Understand',
  3: 'Apply',
  4: 'Analyze',
  5: 'Evaluate',
  6: 'Create',
}

export default function QuizSession() {
    const { id } = useParams<{id: string}>()
    const navigate = useNavigate()
    const location = useLocation()
    const routerState = location.state as {
        topicName?: string
        firstQuestion?: QuestionDto
    } | null

    const [question, setQuestion] = useState<QuestionDto | null>(null)
    const [selectedOptionId, setSelectedOptionId] = useState<string | null>(null)
    const [confidence, setConfidence] = useState<number | null>(null)
    const [answerResult, setAnswerResult] = useState<AnswerResponse | null>(null)
    const [correctCount, setCorrectCount] = useState(0)
    const [loading, setLoading] = useState(true)
    const [submitting, setSubmitting] = useState(false)
    const [topicName, setTopicName] = useState('')

    useEffect(() => {
        if (routerState?.firstQuestion) {
            setQuestion(routerState.firstQuestion)
            setTopicName(routerState.topicName ?? '')
            sessionStorage.setItem(
                `session-${id}-question`,
                JSON.stringify(routerState.firstQuestion)
            )
            setLoading(false)
            return
        }

        const stored = sessionStorage.getItem(`session-${id}-question`)
        if (stored) {
            setQuestion(JSON.parse(stored))
            setLoading(false)
        } else {
            setLoading(false)
        }
    }, [id])

    const handleSelectOption = (optionId: string) => {
        if (answerResult) return
        setSelectedOptionId(optionId)
    }

    const handleSubmitAnswer = async () => {
        if (!id || !question || !selectedOptionId || !confidence) return
        if (submitting) return

        setSubmitting(true)

        try{
            const result = await submitAnswer(id, {
                questionId: question.id,
                selectedOption: selectedOptionId,
                confidenceRating: confidence,
            })

            setAnswerResult(result);
            if (result.isCorrect) {
                setCorrectCount(prev => prev + 1)
            }
        } catch (err) {
            console.error('Failed to submit answer: ', err)
        } finally {
            setSubmitting(false)
        }
    }

    const handleNext = () => {
        if (!answerResult || !id) return

        if (answerResult.sessionStatus === 'COMPLETED' || !answerResult.nextQuestion) {
            sessionStorage.removeItem(`session-${id}-question`);
            navigate(`/sessions/${id}/complete`, {
                state: {
                    totalQuestions: question?.totalQuestions,
                    correctAnswers: correctCount,
                    topicName: routerState?.topicName
                }
            })
            return
        }

        setQuestion(answerResult.nextQuestion)
        sessionStorage.setItem(
            `session-${id}-question`,
            JSON.stringify(answerResult.nextQuestion)
        )
        setSelectedOptionId(null)
        setConfidence(null)
        setAnswerResult(null)

    }
    
    const getOptionClass = (optionId: string) => {
        if (!answerResult) {
        return selectedOptionId === optionId ? 'selected' : ''
        }
        if (answerResult.correctAnswer === optionId) return 'correct'
        if (selectedOptionId === optionId && !answerResult.isCorrect) return 'wrong'
        return 'dimmed'
    }

    const progress = question
        ? ((question.questionIndex + (answerResult ? 1 : 0)) / question.totalQuestions) * 100
        : 0

    if (loading) {
        return <div className="quiz-session"><p className="loading-text">Loading question...</p></div>
    }

    if (!question) {
        return (
        <div className="quiz-session">
            <p className="loading-text">No question available. The session may have already ended.</p>
            <button className="quiz-back-btn" onClick={() => navigate('/')}>
            Back to Dashboard
            </button>
        </div>
        )
    }

    return (
        <div className="quiz-session">
        <div className="quiz-top-bar">
            <span className="quiz-counter">
            Q {question.questionIndex} / {question.totalQuestions}
            </span>
            <div className="quiz-progress-bar">
            <div className="quiz-progress-fill" style={{ width: `${progress}%` }}></div>
            </div>
            <span className="bloom-badge">
            <span className="bloom-dot"></span>
            {bloomLabels[question.bloomLevel] ?? `Level ${question.bloomLevel}`}
            </span>
        </div>

        <div className="quiz-question-card">
            <p className="quiz-question-text">{question.text}</p>

            <div className="quiz-options">
            {question.options.map(option => (
                <button
                key={option.id}
                className={`quiz-option ${getOptionClass(option.id)}`}
                onClick={() => handleSelectOption(option.id)}
                disabled={answerResult !== null}
                >
                <span className="option-letter">
                    {option.id.toUpperCase()}
                </span>
                <span className="option-text">{option.text}</span>
                </button>
            ))}
            </div>

            {answerResult && (
            <div className={`answer-feedback ${answerResult.isCorrect ? 'correct' : 'wrong'}`}>
                <span className="feedback-icon">{answerResult.isCorrect ? '✓' : '✗'}</span>
                <span className="feedback-text">
                {answerResult.isCorrect ? 'Correct!' : 'Incorrect'}
                </span>
                {answerResult.explanation && (
                <p className="feedback-explanation">{answerResult.explanation}</p>
                )}
            </div>
            )}

            {!answerResult && selectedOptionId && (
            <div className="confidence-section">
                <p className="confidence-label">How confident are you?</p>
                <div className="confidence-options">
                {[
                    { value: 1, label: 'Not sure' },
                    { value: 2, label: 'Fairly sure' },
                    { value: 3, label: 'Very sure' },
                ].map(c => (
                    <button
                    key={c.value}
                    className={`confidence-btn ${confidence === c.value ? 'active' : ''}`}
                    onClick={() => setConfidence(c.value)}
                    >
                    {c.label}
                    </button>
                ))}
                </div>
            </div>
            )}
        </div>

        {!answerResult ? (
            <button
            className="quiz-action-btn"
            onClick={handleSubmitAnswer}
            disabled={!selectedOptionId || !confidence || submitting}
            >
            {submitting ? 'Submitting...' : 'Submit answer'}
            </button>
        ) : (
            <button className="quiz-action-btn" onClick={handleNext}>
            {answerResult.sessionStatus === 'COMPLETED' || !answerResult.nextQuestion
                ? 'See results'
                : 'Next question'}
            </button>
        )}
        </div>
    )

}