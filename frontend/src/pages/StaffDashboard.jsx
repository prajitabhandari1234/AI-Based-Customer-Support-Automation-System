import { useCallback, useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ticketApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { TICKET_PRIORITIES, TICKET_STATUSES } from '../config/constants'
import Badge from '../components/Badge'
import EmptyState from '../components/EmptyState'
import Loader from '../components/Loader'
import StatCard from '../components/StatCard'
import { formatEnum, formatPercent } from '../utils/format'

/**
 * Displays the agent/admin ticket queue with search, priority and status filters.
 * The queue refreshes periodically so staff can see recent escalations.
 *
 * @returns {JSX.Element} Staff ticket dashboard.
 */
export default function StaffDashboard() {
  const { user } = useAuth()
  const [tickets, setTickets] = useState([])
  const [search, setSearch] = useState('')
  const [priority, setPriority] = useState('ALL')
  const [status, setStatus] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    setError('')
    return ticketApi.staff()
      .then(setTickets)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    load()
    const timer = window.setInterval(load, 30000)
    return () => window.clearInterval(timer)
  }, [load])

  const filtered = useMemo(() => tickets.filter((ticket) => {
    const text = `${ticket.id || ''} ${ticket.title || ''} ${ticket.customerName || ''}`.toLowerCase()
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
          <p className="eyebrow">{user.role === 'ADMIN' ? 'ADMIN DASHBOARD' : 'AGENT DASHBOARD'}</p>
          <h1>Ticket Queue</h1>
         </div>
        {user.role === 'ADMIN' && <Link className="button button-secondary" to="/analytics">View analytics</Link>}
      </section>

      {error && <div className="alert alert-error">{error}<button className="text-link" onClick={load}>Try again</button></div>}

      <section className="stats-grid">
        <StatCard label="Visible tickets" value={tickets.length} helper="Current queue" icon="▣" />
        <StatCard label="High priority" value={high} helper="Requires attention" icon="!" />
        <StatCard label="Unassigned" value={unassigned} helper="Available to claim" icon="◎" />
        <StatCard label="Negative sentiment" value={negative} helper="Sensitive customers" icon="◔" />
      </section>

      <section className="panel">
        <div className="toolbar staff-toolbar">
          <input className="search-input" placeholder="Search ticket, title, or customer…" value={search}
            onChange={(event) => setSearch(event.target.value)} />
          <select value={priority} onChange={(event) => setPriority(event.target.value)}>
            <option value="ALL">All priorities</option>
            {TICKET_PRIORITIES.map((item) => <option key={item} value={item}>{formatEnum(item)}</option>)}
          </select>
          <select value={status} onChange={(event) => setStatus(event.target.value)}>
            <option value="ALL">All statuses</option>
            {TICKET_STATUSES.map((item) => <option key={item} value={item}>{formatEnum(item)}</option>)}
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
                    <td><strong>#{ticket.id} · {ticket.title || 'Support ticket'}</strong><small>{formatEnum(ticket.category)}</small></td>
                    <td>{ticket.customerName}</td>
                    <td><Badge tone={ticket.sentiment}>{ticket.sentiment || 'NEUTRAL'}</Badge> <small>{formatPercent(ticket.aiConfidenceScore)} confidence</small></td>
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
