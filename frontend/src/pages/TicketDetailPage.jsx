import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ticketApi, usersApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { STAFF_STATUS_OPTIONS } from '../config/constants'
import Badge from '../components/Badge'
import EmptyState from '../components/EmptyState'
import Loader from '../components/Loader'
import { formatDateTime, formatEnum, formatPercent, formatScore } from '../utils/format'

/**
 * Shows one ticket conversation and exposes role-appropriate ticket management actions.
 * Customers can reply while staff can also assign tickets and update their status.
 *
 * @returns {JSX.Element} Ticket detail page.
 */
export default function TicketDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [detail, setDetail] = useState(null)
  const [agents, setAgents] = useState([])
  const [message, setMessage] = useState('')
  const [resolutionNotes, setResolutionNotes] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const staff = user.role !== 'CLIENT'

  const load = useCallback(() => {
    setError('')
    return ticketApi.detail(id)
      .then((data) => {
        setDetail(data)
        setResolutionNotes(data.resolutionNotes || data.ticket?.resolutionNotes || '')
      })
      .catch((err) => setError(err.message))
  }, [id])

  useEffect(() => {
    load()
    if (user.role === 'ADMIN') usersApi.agents().then(setAgents).catch(() => {})
  }, [load, user.role])

  const sendMessage = async (event) => {
    event.preventDefault()
    if (!message.trim()) return
    setBusy(true)
    setError('')
    try {
      const updated = await ticketApi.message(id, message.trim())
      setDetail(updated)
      setMessage('')
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const updateStatus = async (status) => {
    setBusy(true)
    setError('')
    try {
      const updated = await ticketApi.status(id, { status, resolutionNotes })
      setDetail(updated)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  const assign = async (agentId) => {
    if (!agentId) return
    setBusy(true)
    setError('')
    try {
      const updated = user.role === 'AGENT'
        ? await ticketApi.assignToMe(id)
        : await ticketApi.assign(id, Number(agentId))
      setDetail(updated)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (!detail && !error) return <Loader label="Loading ticket details…" />
  if (!detail) return <div className="alert alert-error">{error}<button className="text-link" onClick={() => navigate(-1)}>Go back</button></div>

  const ticket = detail.ticket
  const isClosed = ticket.status === 'CLOSED'

  return (
    <>
      <section className="page-heading split">
        <div>
          <Link className="back-link" to={staff ? '/staff' : '/tickets'}>← Back to tickets</Link>
          <p className="eyebrow">Ticket #{ticket.id}</p>
          <h1>{ticket.title || 'Support ticket'}</h1>
          <div className="inline-badges">
            <Badge tone={ticket.status}>{ticket.status}</Badge>
            <Badge tone={ticket.priority}>{ticket.priority}</Badge>
            <Badge tone={ticket.sentiment}>{ticket.sentiment || 'NEUTRAL'}</Badge>
          </div>
        </div>
        <div className="ticket-meta-card">
          <span>Created</span><strong>{formatDateTime(ticket.createdAt)}</strong>
          <span>Customer</span><strong>{ticket.customerName}</strong>
          <span>Assigned</span><strong>{ticket.assignedAgentName || 'Not assigned'}</strong>
        </div>
      </section>

      {error && <div className="alert alert-error">{error}<button className="text-link" onClick={load}>Reload</button></div>}

      <section className="ticket-detail-grid">
        <article className="panel conversation-panel">
          <div className="panel-header"><div><p className="eyebrow">Conversation</p><h2>Message history</h2></div></div>
          {detail.messages.length === 0 ? (
            <EmptyState title="No messages yet" message="This ticket does not currently contain conversation messages." />
          ) : (
            <div className="ticket-messages">
              {detail.messages.map((item) => (
                <div className={`ticket-message ${String(item.senderType || 'SYSTEM').toLowerCase()}`} key={item.id || `${item.senderType}-${item.createdAt}`}>
                  <div className="ticket-message-meta">
                    <strong>{item.senderName}</strong>
                    <span>{formatDateTime(item.createdAt)}</span>
                  </div>
                  <p>{item.content}</p>
                </div>
              ))}
            </div>
          )}

          {!isClosed && (
            <form className="reply-box" onSubmit={sendMessage}>
              <textarea value={message} onChange={(event) => setMessage(event.target.value)} maxLength="10000"
                placeholder={staff ? 'Write a professional response to the customer…' : 'Add more information or ask a follow-up question…'} rows="3" />
              <button className="button button-primary" disabled={busy || !message.trim()}>
                {busy ? 'Sending…' : 'Send response'}
              </button>
            </form>
          )}
        </article>

        <aside className="ticket-sidebar">
          <article className="panel">
            <p className="eyebrow">AI assessment</p>
            <div className="detail-list">
              <div><span>Category</span><strong>{formatEnum(ticket.category)}</strong></div>
              <div><span>Sentiment</span><strong>{formatEnum(ticket.sentiment || 'NEUTRAL')}</strong></div>
              <div><span>Sentiment score</span><strong>{formatScore(ticket.sentimentScore)}</strong></div>
              <div><span>AI confidence</span><strong>{formatPercent(ticket.aiConfidenceScore)}</strong></div>
              <div><span>Human escalation</span><strong>{ticket.escalated ? 'Yes' : 'No'}</strong></div>
            </div>
            {(detail.escalationReason || ticket.escalationReason) && (
              <div className="reason-box"><strong>Escalation reason</strong><p>{detail.escalationReason || ticket.escalationReason}</p></div>
            )}
          </article>

          <article className="panel timeline-panel">
            <p className="eyebrow">Timing</p>
            <div className="detail-list">
              <div><span>First response</span><strong>{formatDateTime(detail.firstResponseAt || ticket.firstResponseAt)}</strong></div>
              <div><span>Resolved</span><strong>{formatDateTime(detail.resolvedAt || ticket.resolvedAt)}</strong></div>
              <div><span>Closed</span><strong>{formatDateTime(detail.closedAt || ticket.closedAt)}</strong></div>
            </div>
          </article>

          {staff && (
            <article className="panel">
              <p className="eyebrow">Agent actions</p>
              <h3>Manage ticket</h3>
              {user.role === 'ADMIN' && (
                <label>Assign agent
                  <select value={ticket.assignedAgentId || ''} disabled={busy} onChange={(event) => assign(event.target.value)}>
                    <option value="">Select an agent</option>
                    {agents.map((agent) => <option value={agent.id} key={agent.id}>{agent.name}</option>)}
                  </select>
                </label>
              )}
              {user.role === 'AGENT' && !ticket.assignedAgentId && (
                <button className="button button-secondary button-full" disabled={busy} onClick={() => assign(user.id)}>Assign to me</button>
              )}
              <label>Resolution notes
                <textarea rows="4" value={resolutionNotes} onChange={(event) => setResolutionNotes(event.target.value)}
                  placeholder="Document the resolution or next action…" />
              </label>
              <div className="status-actions">
                {STAFF_STATUS_OPTIONS.map((status) => (
                  <button key={status} className="button button-ghost" disabled={busy || ticket.status === status}
                    onClick={() => updateStatus(status)}>
                    Mark {formatEnum(status).toLowerCase()}
                  </button>
                ))}
              </div>
            </article>
          )}
        </aside>
      </section>
    </>
  )
}
