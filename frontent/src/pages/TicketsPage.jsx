import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ticketApi } from '../api/client'
import Badge from '../components/Badge'
import EmptyState from '../components/EmptyState'
import Loader from '../components/Loader'

export default function TicketsPage() {
  const [tickets, setTickets] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    ticketApi.mine().then(setTickets).finally(() => setLoading(false))
  }, [])

  const filtered = useMemo(() => filter === 'ALL' ? tickets : tickets.filter((ticket) => ticket.status === filter),
    [tickets, filter])

  if (loading) return <Loader label="Loading your tickets…" />

  return (
    <>
      <section className="page-heading split">
        <div><p className="eyebrow">Ticket tracking</p><h1>My support tickets</h1><p>Review AI and human responses in one complete conversation history.</p></div>
        <Link className="button button-primary" to="/chat">✦ Start AI chat</Link>
      </section>
      <section className="panel">
        <div className="toolbar">
          <div className="segmented">
            {['ALL', 'ESCALATED', 'IN_PROGRESS', 'RESOLVED_BY_AI', 'RESOLVED', 'CLOSED'].map((status) => (
              <button key={status} className={filter === status ? 'active' : ''} onClick={() => setFilter(status)}>
                {status.replaceAll('_', ' ')}
              </button>
            ))}
          </div>
          <span className="result-count">{filtered.length} result{filtered.length === 1 ? '' : 's'}</span>
        </div>
        {filtered.length === 0 ? (
          <EmptyState title="No matching tickets" message="Try a different filter or start a new chat." />
        ) : (
          <div className="data-table-wrap">
            <table className="data-table">
              <thead><tr><th>Ticket</th><th>Category</th><th>Priority</th><th>Sentiment</th><th>Status</th><th>Updated</th><th /></tr></thead>
              <tbody>
                {filtered.map((ticket) => (
                  <tr key={ticket.id}>
                    <td><strong>#{ticket.id} · {ticket.title}</strong><small>{ticket.assignedAgentName ? `Assigned to ${ticket.assignedAgentName}` : 'Automated support'}</small></td>
                    <td>{ticket.category.replaceAll('_', ' ')}</td>
                    <td><Badge tone={ticket.priority}>{ticket.priority}</Badge></td>
                    <td><Badge tone={ticket.sentiment}>{ticket.sentiment}</Badge></td>
                    <td><Badge tone={ticket.status}>{ticket.status}</Badge></td>
                    <td>{new Date(ticket.updatedAt).toLocaleString()}</td>
                    <td><Link className="icon-button" to={`/tickets/${ticket.id}`}>›</Link></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  )
}
