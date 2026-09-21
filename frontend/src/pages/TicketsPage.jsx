import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ticketApi } from '../api/client'
import { TICKET_STATUSES } from '../config/constants'
import Badge from '../components/Badge'
import CustomerTicketActions from '../components/CustomerTicketActions'
import EmptyState from '../components/EmptyState'
import Loader from '../components/Loader'
import { formatDateTime, formatEnum } from '../utils/format'

/**
 * Lists the signed-in customer's tickets and filters them by ticket status.
 *
 * @returns {JSX.Element} Customer ticket list page.
 */
export default function TicketsPage() {
  const [tickets, setTickets] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    ticketApi.mine()
      .then(setTickets)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  const filtered = useMemo(
    () => filter === 'ALL' ? tickets : tickets.filter((ticket) => ticket.status === filter),
    [tickets, filter]
  )

  if (loading) return <Loader label="Loading your tickets…" />
  if (error) return <div className="alert alert-error">{error}</div>

  return (
    <>
      <section className="page-heading split">
        <div>
          <h1>My tickets</h1>
        </div>
        <CustomerTicketActions />
      </section>

      <section className="panel">
        <div className="toolbar">
          <div className="segmented">
            {['ALL', ...TICKET_STATUSES].map((status) => (
              <button key={status} className={filter === status ? 'active' : ''} onClick={() => setFilter(status)}>
                {formatEnum(status, status)}
              </button>
            ))}
          </div>
          <span className="result-count">{filtered.length} result{filtered.length === 1 ? '' : 's'}</span>
        </div>

        {filtered.length === 0 ? (
          <EmptyState title="No matching tickets" message="Try a different filter or start a new support request." />
        ) : (
          <div className="data-table-wrap">
            <table className="data-table">
              <thead><tr><th>Ticket</th><th>Category</th><th>Priority</th><th>Sentiment</th><th>Status</th><th>Updated</th><th /></tr></thead>
              <tbody>
                {filtered.map((ticket) => (
                  <tr key={ticket.id}>
                    <td><strong>#{ticket.id} · {ticket.title || 'Support ticket'}</strong><small>{ticket.assignedAgentName ? `Assigned to ${ticket.assignedAgentName}` : 'Automated or unassigned support'}</small></td>
                    <td>{formatEnum(ticket.category)}</td>
                    <td><Badge tone={ticket.priority}>{ticket.priority}</Badge></td>
                    <td><Badge tone={ticket.sentiment}>{ticket.sentiment || 'NEUTRAL'}</Badge></td>
                    <td><Badge tone={ticket.status}>{ticket.status}</Badge></td>
                    <td>{formatDateTime(ticket.updatedAt)}</td>
                    <td><Link className="icon-button table-arrow" to={`/tickets/${ticket.id}`} aria-label={`Open ticket ${ticket.id}`}>›</Link></td>
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
