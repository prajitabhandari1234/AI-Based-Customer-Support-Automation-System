import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ticketApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import Badge from '../components/Badge'
import EmptyState from '../components/EmptyState'
import Loader from '../components/Loader'
import StatCard from '../components/StatCard'

export default function StaffDashboard() {
  const { user } = useAuth()
  const [tickets, setTickets] = useState([])
  const [search, setSearch] = useState('')
  const [priority, setPriority] = useState('ALL')
  const [status, setStatus] = useState('ALL')
  const [loading, setLoading] = useState(true)

  const load = () => ticketApi.staff().then(setTickets).finally(() => setLoading(false))
  useEffect(() => { load() }, [])

  const filtered = useMemo(() => tickets.filter((ticket) => {
    const text = `${ticket.id} ${ticket.title} ${ticket.customerName}`.toLowerCase()
    return text.includes(search.toLowerCase())
      && (priority === 'ALL' || ticket.priority === priority)
      && (status === 'ALL' || ticket.status === status)
  }), [tickets, search, priority, status])

  if (loading) return <Loader label="Loading the support queue…" />

  const high = tickets.filter((ticket) => ['HIGH', 'CRITICAL'].includes(ticket.priority)).length
  const unassigned = tickets.filter((ticket) => !ticket.assignedAgentId).length
  const negative = tickets.filter((ticket) => ticket.sentiment === 'NEGATIVE').length

  return (
    <>
      <section className="page-heading split">
        <div>
          <p className="eyebrow">{user.role === 'ADMIN' ? 'Administration workspace' : 'Support agent workspace'}</p>
          <h1>Support ticket queue</h1>
          <p>Review AI escalations, customer sentiment, priority signals, and complete conversation history.</p>
        </div>
        {user.role === 'ADMIN' && <Link className="button button-secondary" to="/analytics">View analytics</Link>}
      </section>

      <section className="stats-grid">
        <StatCard label="Visible tickets" value={tickets.length} helper="Current queue" icon="▣" />
        <StatCard label="High priority" value={high} helper="Requires attention" icon="!" />
        <StatCard label="Unassigned" value={unassigned} helper="Available to claim" icon="◎" />
        <StatCard label="Negative sentiment" value={negative} helper="Sensitive customers" icon="◔" />
      </section>

      <section className="panel">
        <div className="toolbar staff-toolbar">
          <input className="search-input" placeholder="Search ticket, title, or customer…" value={search}
            onChange={(e) => setSearch(e.target.value)} />
          <select value={priority} onChange={(e) => setPriority(e.target.value)}>
            <option value="ALL">All priorities</option>
            {['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'].map((item) => <option key={item}>{item}</option>)}
          </select>
          <select value={status} onChange={(e) => setStatus(e.target.value)}>
            <option value="ALL">All statuses</option>
            {['NEW', 'ESCALATED', 'IN_PROGRESS', 'ON_HOLD', 'RESOLVED_BY_AI', 'RESOLVED', 'CLOSED'].map((item) => <option key={item}>{item}</option>)}
          </select>
          <button className="button button-ghost" onClick={load}>Refresh</button>
        </div>

        {filtered.length === 0 ? (
          <EmptyState title="No tickets match" message="Adjust the filters or wait for a new escalation." />
        ) : (
          <div className="data-table-wrap">
            <table className="data-table">
              <thead><tr><th>Ticket</th><th>Customer</th><th>AI signals</th><th>Priority</th><th>Status</th><th>Assigned</th><th /></tr></thead>
              <tbody>
                {filtered.map((ticket) => (
                  <tr key={ticket.id}>
                    <td><strong>#{ticket.id} · {ticket.title}</strong><small>{ticket.category.replaceAll('_', ' ')}</small></td>
                    <td>{ticket.customerName}</td>
                    <td><Badge tone={ticket.sentiment}>{ticket.sentiment}</Badge> <small>{Math.round(ticket.aiConfidenceScore * 100)}% confidence</small></td>
                    <td><Badge tone={ticket.priority}>{ticket.priority}</Badge></td>
                    <td><Badge tone={ticket.status}>{ticket.status}</Badge></td>
                    <td>{ticket.assignedAgentName || <span className="muted">Unassigned</span>}</td>
                    <td><Link className="button button-small button-secondary" to={`/tickets/${ticket.id}`}>Open</Link></td>
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
