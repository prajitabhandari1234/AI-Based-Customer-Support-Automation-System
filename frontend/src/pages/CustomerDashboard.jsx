import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { ticketApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import Badge from '../components/Badge'
import CustomerTicketActions from '../components/CustomerTicketActions'
import EmptyState from '../components/EmptyState'
import Loader from '../components/Loader'
import StatCard from '../components/StatCard'
import { formatEnum } from '../utils/format'

/**
 * Shows a customer summary, recent tickets and shortcuts into the support workflow.
 *
 * @returns {JSX.Element} Customer dashboard page.
 */
export default function CustomerDashboard() {
  const { user } = useAuth()
  const [tickets, setTickets] = useState([])
  const [summary, setSummary] = useState({ total: 0, open: 0 })
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    Promise.all([ticketApi.mine(), ticketApi.mySummary()])
      .then(([ticketData, summaryData]) => {
        setTickets(ticketData)
        setSummary(summaryData)
      })
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <Loader label="Loading your support overview…" />
  if (error) return <div className="alert alert-error">{error}</div>

  const resolved = tickets.filter((ticket) => ['RESOLVED', 'RESOLVED_BY_AI', 'CLOSED'].includes(ticket.status)).length
  const escalated = tickets.filter((ticket) => ticket.escalated).length

  return (
    <>
      <section className="page-heading split">
        <div>
          <p className="eyebrow">Client Dashboard</p>
          <h1>Hello, {user.name?.split(' ')[0] || 'there'}</h1>
         </div>
        <CustomerTicketActions />
      </section>

      <section className="stats-grid">
        <StatCard label="Total tickets" value={summary.total} helper="AI and human support" icon="◌" />
        <StatCard label="Open tickets" value={summary.open} helper="Waiting or in progress" icon="▣" />
        <StatCard label="Resolved" value={resolved} helper="Completed support requests" icon="✓" />
        <StatCard label="Human escalations" value={escalated} helper="Complex issues reviewed" icon="↗" />
      </section>

      <section className="content-grid customer-dashboard-stack">
        <article className="panel">
          <div className="panel-header">
            <div><p className="eyebrow">Recent activity</p><h2>Your latest tickets</h2></div>
            <Link to="/tickets" className="text-link">View all →</Link>
          </div>
          {tickets.length === 0 ? (
            <EmptyState title="No support history yet" message="Start an AI chat or create a support ticket and it will appear here."
              action={<Link className="button button-secondary" to="/chat">Ask a question</Link>} />
          ) : (
            <div className="ticket-list">
              {tickets.slice(0, 5).map((ticket) => (
                <Link to={`/tickets/${ticket.id}`} className="ticket-row" key={ticket.id}>
                  <div className="ticket-id">#{ticket.id}</div>
                  <div className="ticket-main"><strong>{ticket.title || 'Support ticket'}</strong><small>{formatEnum(ticket.category)}</small></div>
                  <Badge tone={ticket.priority}>{ticket.priority}</Badge>
                  <Badge tone={ticket.status}>{ticket.status}</Badge>
                  <span className="row-arrow">›</span>
                </Link>
              ))}
            </div>
          )}
        </article>

        <aside className="panel quick-help">
          <p className="eyebrow">Quick help</p>
          <h2>Popular questions</h2>
          {[
            'How do I reset my password?',
            'Where can I track my order?',
            'How do I request a refund?',
            'What are the support hours?'
          ].map((question) => (
            <Link key={question} to="/chat" state={{ suggested: question }} className="question-link">
              <span>?</span>{question}
            </Link>
          ))}
          <div className="human-note">
            <strong>Need a person?</strong>
            <p>Create a support ticket directly, or ask for a human agent during chat. Negative, urgent, or low-confidence cases can also be escalated automatically.</p>
            <Link to="/tickets/new" className="text-link">Create human support ticket →</Link>
          </div>
        </aside>
      </section>
    </>
  )
}
