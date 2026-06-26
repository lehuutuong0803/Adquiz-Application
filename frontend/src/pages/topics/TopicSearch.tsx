import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { getAllTopics, searchParentTopics, searchSubtopics } from '../../api/topics'
import { getAccuracy } from '../../api/analytics'
import { createSession } from '../../api/sessions'
import type { TopicDto, SubtopicDto } from '../../api/topics'
import type { TopicAccuracyDto } from '../../api/analytics'
import { useDebounce } from '../../hook/useDebounce'
import './TopicSearch.css'
import SessionConfigDialog  from '../../components/session-config/SessionConfigDialog'

type ParentTopic = {
  id: string
  name: string
}

type ChildTopic = {
  id: string
  name: string
  parentId: string
  parentName: string
  accuracy: number | null
}
type ConfigTopic = { id: string; name: string; parentName: string } 

function buildData(topics: TopicDto[], accuracyMap: Map<string, number>) {
  const parents: ParentTopic[] = []
  const children: ChildTopic[] = []

  for (const parent of topics) {
    parents.push({ id: parent.id, name: parent.name })
    for (const sub of parent.subtopics) {
      children.push({
        id: sub.id,
        name: sub.name,
        parentId: parent.id,
        parentName: parent.name,
        accuracy: accuracyMap.get(sub.id) ?? null,
      })
    }
  }
  return { parents, children }
}

const PAGE_SIZE = 6

export default function TopicSearch() {
  const navigate = useNavigate()

  const [allParents, setAllParents] = useState<ParentTopic[]>([])
  const [allChildren, setAllChildren] = useState<ChildTopic[]>([])
  const [loading, setLoading] = useState(true)

  const [parentQuery, setParentQuery] = useState('')
  const [selectedParentId, setSelectedParentId] = useState<string | null>(null)
  const [selectedParentName, setSelectedParentName] = useState<string | null>(null)
  const [showParentDropdown, setShowParentDropdown] = useState(false)
  const [parentSuggestions, setParentSuggestions] = useState<SubtopicDto[]>([])

  const [childQuery, setChildQuery] = useState('')
  const [selectedChildId, setSelectedChildId] = useState<string | null>(null)
  const [showChildDropdown, setShowChildDropdown] = useState(false)
  const [childSuggestions, setChildSuggestions] = useState<SubtopicDto[]>([])

  const [showConfirm, setShowConfirm] = useState(false)
  const [confirmTopic, setConfirmTopic] = useState<SubtopicDto | null>(null)
  const [pendingMode, setPendingMode] = useState<'ADAPTIVE' | 'REVIEW'>('ADAPTIVE')
  const [pendingQuestions, setPendingQuestions] = useState(5)

  const [visibleCount, setVisibleCount] = useState(PAGE_SIZE)
  const [starting, setStarting] = useState<string | null>(null)

  const debouncedParent = useDebounce(parentQuery, 300)
  const debouncedChild = useDebounce(childQuery, 300)

  const [configTopic, setConfigTopic] = useState<ConfigTopic | null>(null)
  const [configMode, setConfigMode] = useState<'ADAPTIVE' | 'REVIEW'>('ADAPTIVE')

  

  useEffect(() => {
    Promise.all([
      getAllTopics().catch(() => []),
      getAccuracy().catch(() => []),
    ]).then(([topics, accuracy]) => {
      const accuracyMap = new Map<string, number>()
      for (const a of accuracy as TopicAccuracyDto[]) {
        accuracyMap.set(a.topicId, a.accuracy)
      }
      const { parents, children } = buildData(topics, accuracyMap)
      setAllParents(parents)
      setAllChildren(children)
      setLoading(false)
    })
  }, [])

  useEffect(() => {
    if (debouncedParent.length === 0) {
      setParentSuggestions([])
      return
    }
    if (selectedParentId) return

    searchParentTopics(debouncedParent)
      .then(results => setParentSuggestions(results))
      .catch(() => setParentSuggestions([]))
  }, [debouncedParent, selectedParentId])

  useEffect(() => {
    if (debouncedChild.length === 0 || !selectedParentId) {
      setChildSuggestions([])
      return
    }
    if (selectedChildId) return

    searchSubtopics(debouncedChild, selectedParentId)
      .then(results => setChildSuggestions(results))
      .catch(() => setChildSuggestions([]))
  }, [debouncedChild, selectedParentId, selectedChildId])

  const handleParentSelect = (suggestion: SubtopicDto) => {
    setParentQuery(suggestion.name)
    setSelectedParentId(suggestion.id)
    setSelectedParentName(suggestion.name)
    setShowParentDropdown(false)
    setParentSuggestions([])
    setChildQuery('')
    setSelectedChildId(null)
    setChildSuggestions([])
  }

  const handleParentBlur = () => {
    setTimeout(() => {
      setShowParentDropdown(false)
      if (!selectedParentId && parentQuery.trim()) {
        const exact = allParents.find(
          p => p.name.toLowerCase() === parentQuery.trim().toLowerCase()
        )
        if (exact) {
          setSelectedParentId(exact.id)
          setSelectedParentName(exact.name)
          setParentQuery(exact.name)
        }
      }
    }, 200)
  }

  const handleChildSelect = (suggestion: SubtopicDto) => {
    setChildQuery(suggestion.name)
    setSelectedChildId(suggestion.id)
    setShowChildDropdown(false)
    setChildSuggestions([])
  }

  const handleChildBlur = () => {
    setTimeout(() => {
      setShowChildDropdown(false)
      if (!selectedChildId && childQuery.trim() && selectedParentId) {
        const exact = allChildren.find(
          c => c.parentId === selectedParentId
            && c.name.toLowerCase() === childQuery.trim().toLowerCase()
        )
        if (exact) {
          setSelectedChildId(exact.id)
          setChildQuery(exact.name)
        }
      }
    }, 200)
  }

  const childrenUnderParent = selectedParentId
    ? allChildren.filter(c => c.parentId === selectedParentId)
    : debouncedParent.length > 0
      ? allChildren.filter(c =>
          c.parentName.toLowerCase().includes(debouncedParent.toLowerCase())
        )
      : allChildren

  const filtered = debouncedChild.length > 0
    ? childrenUnderParent.filter(c =>
        c.name.toLowerCase().includes(debouncedChild.toLowerCase())
      )
    : childrenUnderParent

  const visible = filtered.slice(0, visibleCount)
  const hasMore = visibleCount < filtered.length

  useEffect(() => {
    setVisibleCount(PAGE_SIZE)
  }, [debouncedParent, debouncedChild])

  const openConfig = (child: ChildTopic, mode: 'ADAPTIVE' | 'REVIEW') => {
    setConfigTopic({ id: child.id, name: child.name, parentName: child.parentName })
    setConfigMode(mode)
  }

  const handleStartSession = async (topicId: string, topicName: string, mode: 'ADAPTIVE' | 'REVIEW', totalQuestions: number) => {
    if (starting) return
    setStarting(topicId + mode)
    setConfigTopic(null)

    try {
      const session = await createSession({
        topicId,
        mode,
        totalQuestions,
        targetAudience: 'UNIVERSITY_STUDENT',
      })
      navigate(`/sessions/${session.sessionId}`, {
        state: {
          topicName,
          firstQuestion: session.firstQuestion,
        },
      })
    } catch (err) {
      console.error('Failed to create session:', err)
      setStarting(null)
    }
  }

  // const handleStartFromInput = async (mode: 'ADAPTIVE' | 'REVIEW') => {
  //   if (starting || !childQuery.trim()) return

  //   if (selectedChildId) {
  //     handleStartSession(selectedChildId, mode)
  //     return
  //   }

  //   if (selectedParentId && childSuggestions.length > 0) {
  //     const topMatch = childSuggestions[0]
  //     if (topMatch.name.toLowerCase() === childQuery.trim().toLowerCase()) {
  //       handleStartSession(topMatch.id, mode)
  //       return
  //     }
  //     setConfirmTopic(topMatch)
  //     setPendingMode(mode)
  //     setShowConfirm(true)
  //     return
  //   }

  //   setStarting('input' + mode)
  //   try {
  //     const session = await createSession({
  //       topicName: childQuery.trim(),
  //       parentTopicId: selectedParentId ?? undefined,
  //       parentTopicName: !selectedParentId && parentQuery.trim()
  //         ? parentQuery.trim()
  //         : undefined,
  //       mode,
  //       totalQuestions: 5,
  //       targetAudience: 'UNIVERSITY_STUDENT',
  //     })
  //     navigate(`/sessions/${session.sessionId}`, {
  //       state: {
  //           topicName: childQuery.trim(),
  //           firstQuestion: session.firstQuestion,
  //       },
  //     })
  //   } catch (err) {
  //     console.error('Failed to create session:', err)
  //     setStarting(null)
  //   }
  // }

  const handleConfirmYes = () => {
    setShowConfirm(false)
    if (confirmTopic) {
      handleStartSession(confirmTopic.id, confirmTopic.name, pendingMode, pendingQuestions)
    }
  }
  const handleConfirmNo = async () => {
    setShowConfirm(false)
    setStarting('input' + pendingMode)
    try {
      const session = await createSession({
        topicName: childQuery.trim(),
        parentTopicId: selectedParentId ?? undefined,
        parentTopicName: !selectedParentId && parentQuery.trim()
          ? parentQuery.trim()
          : undefined,
        mode: pendingMode,
        totalQuestions: pendingQuestions,
        targetAudience: 'UNIVERSITY_STUDENT',
      })
      navigate(`/sessions/${session.sessionId}`, {
        state: {
          topicName: childQuery.trim(),
          firstQuestion: session.firstQuestion,
        },
      })
    } catch (err) {
      console.error('Failed to create session:', err)
      setStarting(null)
    }
  }

  const handleOnConfirmNumberQuestionsDialog = (totalQuestions: number, configTopic: ConfigTopic) => {
     if (configTopic.id) {
              handleStartSession(configTopic.id, configTopic.name, configMode, totalQuestions)
            } else if (selectedChildId) {
              handleStartSession(selectedChildId, configTopic.name, configMode, totalQuestions)
            } else if (selectedParentId && childSuggestions.length > 0) {
              const topMatch = childSuggestions[0]
              if (topMatch.name.toLowerCase() === childQuery.trim().toLowerCase()) {
                handleStartSession(topMatch.id, topMatch.name, configMode, totalQuestions)
              } else {
                setConfigTopic(null)
                setConfirmTopic(topMatch)
                setPendingMode(configMode)
                setPendingQuestions(totalQuestions)
                setShowConfirm(true)
              }
            } else {
              setConfigTopic(null)
              setStarting('input' + configMode)
              createSession({
                topicName: configTopic.name,
                parentTopicId: selectedParentId ?? undefined,
                parentTopicName: !selectedParentId && parentQuery.trim()
                  ? parentQuery.trim()
                  : undefined,
                mode: configMode,
                totalQuestions,
                targetAudience: 'UNIVERSITY_STUDENT',
              })
                .then(session => {
                  navigate(`/sessions/${session.sessionId}`, {
                    state: {
                      topicName: configTopic.name,
                      firstQuestion: session.firstQuestion,
                    },
                  })
                })
                .catch(err => {
                  console.error('Failed to create session:', err)
                  setStarting(null)
                })
            }
  }

  if (loading) {
    return <div className="topics-page"><p className="loading-text">Loading topics...</p></div>
  }

  return (
    <div className="topics-page">
      {starting && (
        <div className="session-loading-overlay">
          <div className="session-loading-dialog">
            <div className="loading-spinner"></div>
            <p className="loading-title">Preparing your session</p>
            <p className="loading-subtitle">Generating questions with AI...</p>
          </div>
        </div>
      )}

      {configTopic && (
        <SessionConfigDialog
          topicName={configTopic.name}
          mode={configMode}
          onClose={() => setConfigTopic(null)}
          onConfirm={(totalQuestions) => handleOnConfirmNumberQuestionsDialog(totalQuestions, configTopic)}

        />
      )}
      <h1 className="page-title">Topics</h1>

      <div className="search-fields">
        <div className="search-wrapper">
          <label className="search-label">Parent topic</label>
          <div className="search-input-container">
            <span className="search-icon">⌕</span>
            <input
              type="text"
              className="search-input"
              placeholder="e.g. Data Structures, Algorithms..."
              value={parentQuery}
              onChange={e => {
                setParentQuery(e.target.value)
                setSelectedParentId(null)
                setSelectedParentName(null)
                setShowParentDropdown(e.target.value.length > 0)
                setChildQuery('')
                setSelectedChildId(null)
                setChildSuggestions([])
              }}
              onFocus={() => {
                if (parentQuery.length > 0 && !selectedParentId) {
                  setShowParentDropdown(true)
                }
              }}
              onBlur={handleParentBlur}
            />
            {parentQuery && (
              <button
                className="search-clear"
                onClick={() => {
                  setParentQuery('')
                  setSelectedParentId(null)
                  setSelectedParentName(null)
                  setShowParentDropdown(false)
                  setChildQuery('')
                  setSelectedChildId(null)
                  setChildSuggestions([])
                }}
              >
                ✕
              </button>
            )}
          </div>
          {selectedParentId && (
            <span className="match-badge">✓ Matched</span>
          )}

          {showParentDropdown && parentSuggestions.length > 0 && (
            <div className="search-dropdown">
              {parentSuggestions.map(p => (
                <div
                  key={p.id}
                  className="dropdown-item"
                  onMouseDown={() => handleParentSelect(p)}
                >
                  <span className="dropdown-name">{p.name}</span>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="search-wrapper">
          <label className="search-label">Topic</label>
          <div className="search-input-container">
            <span className="search-icon">⌕</span>
            <input
              type="text"
              className="search-input"
              placeholder="e.g. Binary Trees, Recursion..."
              value={childQuery}
              onChange={e => {
                setChildQuery(e.target.value)
                setSelectedChildId(null)
                setShowChildDropdown(e.target.value.length > 0)
              }}
              onFocus={() => {
                if (childQuery.length > 0 && !selectedChildId) {
                  setShowChildDropdown(true)
                }
              }}
              onBlur={handleChildBlur}
            />
            {childQuery && (
              <button
                className="search-clear"
                onClick={() => {
                  setChildQuery('')
                  setSelectedChildId(null)
                  setShowChildDropdown(false)
                  setChildSuggestions([])
                }}
              >
                ✕
              </button>
            )}
          </div>
          {selectedChildId && (
            <span className="match-badge">✓ Matched</span>
          )}

          {showChildDropdown && childSuggestions.length > 0 && (
            <div className="search-dropdown">
              {childSuggestions.map(t => (
                <div
                  key={t.id}
                  className="dropdown-item"
                  onMouseDown={() => handleChildSelect(t)}
                >
                  <span className="dropdown-name">{t.name}</span>
                  {selectedParentName && (
                    <span className="dropdown-parent">{selectedParentName}</span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>

      {childQuery.trim() && (
        <div className="input-actions">
          <button
            className="topic-btn practice"
            onClick={() => openConfig(
              {id: '', name: childQuery.trim(), parentId: '', parentName: parentQuery.trim(), accuracy: null},
              'ADAPTIVE')}
            disabled={starting !== null}
          >
             Practice "{childQuery.trim()}"
          </button>
          <button
            className="topic-btn review"
            onClick={() => openConfig(
              {id: '', name: childQuery.trim(), parentId: '', parentName: parentQuery.trim(), accuracy: null},
              'REVIEW')}
            disabled={starting !== null}
          >
             Review "{childQuery.trim()}"
          </button>
        </div>
      )}

        {showConfirm && confirmTopic && (
            <div className="confirm-overlay" onClick={() => setShowConfirm(false)}>
            <div className="confirm-dialog" onClick={e => e.stopPropagation()}>
                <button className="confirm-close" onClick={() => setShowConfirm(false)}>✕</button>
                <p className="confirm-title">Did you mean this topic?</p>
                <p className="confirm-text">
                We found <strong>"{confirmTopic.name}"</strong>
                {selectedParentName && (
                    <> under <strong>{selectedParentName}</strong></>
                )} which is similar to what you typed. Would you like to use this existing topic or create a new one?
                </p>
                <div className="confirm-actions">
                <button className="btn-confirm primary" onClick={() => handleConfirmYes(confirmTopic.name)}>
                    Yes, use this
                </button>
                <button className="btn-confirm secondary" onClick={handleConfirmNo}>
                    Create new topic
                </button>
                </div>
            </div>
            </div>
        )}

      <div className="topics-count">
        {filtered.length} topic{filtered.length !== 1 ? 's' : ''} found
      </div>

      <div className="topics-grid">
        {visible.map(t => (
          <div key={t.id} className="topic-card">
            <div className="topic-card-header">
              <div className="topic-card-info">
                <span className="topic-card-name">{t.name}</span>
                <span className="topic-card-parent">{t.parentName}</span>
              </div>
              <span className="topic-card-accuracy">
                {t.accuracy !== null
                  ? `${Math.round(t.accuracy * 100)}% accuracy`
                  : 'No data yet'}
              </span>
            </div>
            {t.accuracy !== null && (
              <div className="topic-card-bar">
                <div
                  className="topic-card-bar-fill"
                  style={{ width: `${Math.round(t.accuracy * 100)}%` }}
                ></div>
              </div>
            )}
            <div className="topic-card-actions">
              <button
                className="topic-btn practice"
                onClick={() => openConfig(t, 'ADAPTIVE')}
                disabled={starting !== null}
              >
                Practice
              </button>
              <button
                className="topic-btn review"
                onClick={() => openConfig(t, 'REVIEW')}
                disabled={starting !== null}
              >
                Review
              </button>
            </div>
          </div>
        ))}
      </div>

      {hasMore && (
        <div className="load-more-wrapper">
          <button
            className="load-more-btn"
            onClick={() => setVisibleCount(prev => prev + PAGE_SIZE)}
          >
            Load more
          </button>
        </div>
      )}
    </div>
  )
}