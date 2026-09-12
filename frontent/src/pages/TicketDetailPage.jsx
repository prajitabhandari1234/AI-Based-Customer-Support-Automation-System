import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { ticketApi, usersApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import Badge from '../components/Badge'
import Loader from '../components/Loader'

const statusOptions = ['ESCALATED', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED', 'CLOSED']

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

  const load = () => ticketApi.detail(id)
    .then((data) => {
      setDetail(data)
      setResolutionNotes(data.resolutionNotes || '')
    })
    .catch((err) => setError(err.message))

  useEffect(() => {
    load()
    if (user.role === 'ADMIN') usersApi.agents().then(setAgents).catch(() => {})
  }, [id, user.role])

  const sendMessage = async (event) => {
    event.preventDefault()
    if (!message.trim()) return
    setBusy(true)
    setError('')
    try {
      const updated = await ticketApi.message(id, message)
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
    try {
      const updated = await ticketApi.assign(id, Number(agentId))
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

  return (
    <>
      <section className="page-heading split">
        <div>
          <Link className="back-link" to={staff ? '/staff' : '/tickets'}>← Back to tickets</Link>
          <p className="eyebrow">Ticket #{ticket.id}</p>
          <h1>{ticket.title}</h1>
          <div className="inline-badges">
            <Badge tone={ticket.status}>{ticket.status}</Badge>
            <Badge tone={ticket.priority}>{ticket.priority}</Badge>
            <Badge tone={ticket.sentiment}>{ticket.sentiment}</Badge>
          </div>
        </div>
        <div className="ticket-meta-card">
          <span>Created</span><strong>{new Date(ticket.createdAt).toLocaleString()}</strong>
          <span>Customer</span><strong>{ticket.customerName}</strong>
          <span>Assigned</span><strong>{ticket.assignedAgentName || 'Not assigned'}</strong>
        </div>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      <section className="ticket-detail-grid">
        <article className="panel conversation-panel">
          <div className="panel-header"><div><p className="eyebrow">Conversation</p><h2>Message history</h2></div></div>
          <div className="ticket-messages">
            {detail.messages.map((item) => (
              <div className={`ticket-message ${item.senderType.toLowerCase()}`} key={item.id}>
                <div className="ticket-message-meta">
                  <strong>{item.senderName}</strong>
                  <span>{new Date(item.createdAt).toLocaleString()}</span>
                </div>
                <p>{item.content}</p>
              </div>
            ))}
          </div>
          {!['CLOSED'].includes(ticket.status) && (
            <form className="reply-box" onSubmit={sendMessage}>
              <textarea value={message} onChange={(e) => setMessage(e.target.value)}
                placeholder={staff ? 'Write a professional response to the customer…' : 'Add more information…'} rows="3" />
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
              <div><span>Category</span><strong>{ticket.category.replaceAll('_', ' ')}</strong></div>
              <div><span>Sentiment score</span><strong>{ticket.sentimentScore.toFixed(2)}</strong></div>
              <div><span>AI confidence</span><strong>{Math.round(ticket.aiConfidenceScore * 100)}%</strong></div>
              <div><span>Human escalation</span><strong>{ticket.escalated ? 'Yes' : 'No'}</strong></div>
            </div>
            {detail.escalationReason && <div className="reason-box"><strong>Escalation reason</strong><p>{detail.escalationReason}</p></div>}
          </article>

          {staff && (
            <article className="panel">
              <p className="eyebrow">Agent actions</p>
              <h3>Manage ticket</h3>
              {user.role === 'ADMIN' && (
                <label>Assign agent
                  <select value={ticket.assignedAgentId || ''} onChange={(e) => assign(e.target.value)}>
                    <option value="">Select an agent</option>
                    {agents.map((agent) => <option value={agent.id} key={agent.id}>{agent.name}</option>)}
                  </select>
                </label>
              )}
              {user.role === 'AGENT' && !ticket.assignedAgentId && (
                <button className="button button-secondary button-full" onClick={() => assign(user.id)}>Assign to me</button>
              )}
              <label>Resolution notes
                <textarea rows="4" value={resolutionNotes} onChange={(e) => setResolutionNotes(e.target.value)}
                  placeholder="Document the resolution or next action…" />
              </label>
              <div className="status-actions">
                {statusOptions.map((status) => (
                  <button key={status} className="button button-ghost" disabled={busy || ticket.status === status}
                    onClick={() => updateStatus(status)}>
                    Mark {status.replaceAll('_', ' ').toLowerCase()}
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
