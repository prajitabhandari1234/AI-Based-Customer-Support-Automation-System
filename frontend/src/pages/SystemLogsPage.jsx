import { useCallback, useEffect, useMemo, useState } from 'react'
import { systemApi } from '../api/client'
import EmptyState from '../components/EmptyState'
import Loader from '../components/Loader'
import { formatDateTime, formatEnum } from '../utils/format'

/**
 * Loads system audit records and provides a client-side search across log details.
 *
 * @returns {JSX.Element} System log administration page.
 */
export default function SystemLogsPage() {
  const [logs, setLogs] = useState([])
  const [search, setSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    setError('')
    return systemApi.logs()
      .then((data) => setLogs(Array.isArray(data) ? data : []))
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  const filtered = useMemo(() => logs.filter((log) => {
    const ticketId = log.ticket?.ticketId ?? log.ticket?.id ?? ''
    const text = `${log.eventType || ''} ${log.description || ''} ${log.user?.name || ''} ${log.user?.email || ''} ${ticketId}`.toLowerCase()
    return text.includes(search.toLowerCase())
  }), [logs, search])

  if (loading) return <Loader label="Loading system logs…" />

  return (
    <>
      <section className="page-heading split">
        <div>
          <h1>System logs</h1>
        </div>
        <button className="button button-secondary" onClick={load}>Refresh logs</button>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      <section className="panel">
        <div className="toolbar">
          <input className="search-input" placeholder="Search event, user, ticket or description…"
            value={search} onChange={(event) => setSearch(event.target.value)} />
          <span className="result-count">{filtered.length} record{filtered.length === 1 ? '' : 's'}</span>
        </div>

        {filtered.length === 0 ? (
          <EmptyState title="No log records found" message="System activity will appear here when the backend records events." />
        ) : (
          <div className="data-table-wrap">
            <table className="data-table">
              <thead><tr><th>Time</th><th>Event</th><th>Description</th><th>Name</th><th>Email</th><th>Ticket</th></tr></thead>
              <tbody>
                {filtered.map((log) => {
                  const ticketId = log.ticket?.ticketId ?? log.ticket?.id
                  return (
                    <tr key={log.logId ?? `${log.eventType}-${log.createdAt}`}>
                      <td>{formatDateTime(log.createdAt)}</td>
                      <td><strong>{formatEnum(log.eventType, log.eventType || 'Event')}</strong></td>
                      <td>{log.description}</td>
                      <td>{log.user ? <><strong>{log.user.name}</strong></> : <span className="muted">System</span>}</td>
                      <td>{log.user ? <><strong>{log.user.email}</strong></> : <span className="muted">System</span>}</td>
                      <td>{ticketId ? `#${ticketId}` : <span className="muted">—</span>}</td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  )
}
