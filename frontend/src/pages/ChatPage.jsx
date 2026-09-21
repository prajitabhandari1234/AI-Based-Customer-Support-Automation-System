import { useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { chatApi } from '../api/client'
import Badge from '../components/Badge'
import { formatPercent } from '../utils/format'

const openingMessage = 'Hello. I am the AI Based customer support assistant. How can I help you today?'

/**
 * Provides the customer AI chat interface and shows analysis returned by the backend.
 * The current ticket identifier is reused so follow-up messages stay in the same conversation.
 *
 * @returns {JSX.Element} AI chat page.
 */
export default function ChatPage() {
  const location = useLocation()
  const [messages, setMessages] = useState([{ sender: 'AI', content: openingMessage }])
  const [input, setInput] = useState(location.state?.suggested || '')
  const [ticketId, setTicketId] = useState(null)
  const [analysis, setAnalysis] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth', block: 'end' })
  }, [messages])

  const submit = async (event) => {
    event.preventDefault()
    const text = input.trim()
    if (!text || busy) return

    setMessages((current) => [...current, { sender: 'CLIENT', content: text }])
    setInput('')
    setBusy(true)
    setError('')

    try {
      const response = await chatApi.send(text, ticketId)

      if (response.ticket?.id) setTicketId(response.ticket.id)
      setAnalysis(response.analysis || null)
      setMessages((current) => [...current, { sender: 'AI', content: response.reply }])
    } catch (err) {
      setError(err.message)
      setMessages((current) => [...current, {
        sender: 'SYSTEM',
        content: 'The message could not be processed. Please try again.'
      }])
    } finally {
      setBusy(false)
    }
  }

  const reset = () => {
    setMessages([{ sender: 'AI', content: 'A new conversation has started. What can I help you with?' }])
    setTicketId(null)
    setAnalysis(null)
    setError('')
    setInput('')
  }

  return (
    <>
      <section className="page-heading split">
        <div>
          <h1>Chat with AI</h1>
        </div>
        <button className="button button-secondary" onClick={reset}>New conversation</button>
      </section>

      <section className="chat-layout">
        <article className="chat-panel">
          <div className="chat-header">
            <div className="bot-avatar">✦</div>
            <div><strong>AI Based Customer Support Assistant</strong><small><span className="online-dot" /> Online</small></div>
            {ticketId && <Link to={`/tickets/${ticketId}`} className="ticket-reference">Ticket #{ticketId}</Link>}
          </div>

          <div className="messages" aria-live="polite">
            {messages.map((message, index) => (
              <div className={`message-row ${message.sender.toLowerCase()}`} key={`${message.sender}-${index}`}>
                <div className="message-bubble">
                  <span className="message-label">
                    {message.sender === 'CLIENT' ? 'You' : message.sender === 'AI' ? 'AI Assistant' : 'System'}
                  </span>
                  <p>{message.content}</p>
                </div>
              </div>
            ))}
            {busy && <div className="typing" aria-label="AI is responding"><span /><span /><span /></div>}
            <div ref={bottomRef} />
          </div>

          {error && <div className="alert alert-error chat-error">{error}</div>}

          <form className="chat-composer" onSubmit={submit}>
            <textarea value={input} onChange={(event) => setInput(event.target.value)}
              placeholder="Describe your question or issue…" rows="2" maxLength="10000"
              onKeyDown={(event) => {
                if (event.key === 'Enter' && !event.shiftKey) {
                  event.preventDefault()
                  event.currentTarget.form.requestSubmit()
                }
              }} />
            <button className="send-button" disabled={busy || !input.trim()} aria-label="Send message">➤</button>
          </form>
          <p className="composer-note">Do not share passwords or full payment-card details.</p>
        </article>

        <aside className="analysis-panel panel">
          <p className="eyebrow">Live AI analysis</p>
          <h2>Decision signals</h2>
          {!analysis ? (
            <div className="analysis-placeholder">
              <div>✦</div>
              <p>{ticketId
                ? 'No new support analysis was returned for the last message.'
                : 'Send a support-related message to see classification, sentiment, priority, confidence and escalation decisions.'}</p>
            </div>
          ) : (
            <div className="analysis-list">
              <div><span>Category</span><Badge>{analysis.category}</Badge></div>
              <div><span>Priority</span><Badge tone={analysis.priority}>{analysis.priority}</Badge></div>
              <div><span>Sentiment</span><Badge tone={analysis.sentiment}>{analysis.sentiment}</Badge></div>
              <div><span>Sentiment score</span><strong>{Number(analysis.sentimentScore ?? 0).toFixed(2)}</strong></div>
              <div><span>AI confidence</span><strong>{formatPercent(analysis.confidence)}</strong></div>
              <div className="confidence-track"><span style={{ width: `${Math.max(0, Math.min(1, analysis.confidence || 0)) * 100}%` }} /></div>
              <div><span>Knowledge match</span><strong>{analysis.knowledgeBaseMatch ? 'Yes' : 'No'}</strong></div>
              <div><span>Escalated</span><Badge tone={analysis.escalated ? 'high' : 'resolved'}>{analysis.escalated ? 'Human review' : 'AI resolved'}</Badge></div>
              {analysis.escalationReasons?.length > 0 && (
                <div className="analysis-reasons">
                  <span>Why it escalated</span>
                  <ul>{analysis.escalationReasons.map((reason) => <li key={reason}>{reason}</li>)}</ul>
                </div>
              )}
            </div>
          )}
        </aside>
      </section>
    </>
  )
}
