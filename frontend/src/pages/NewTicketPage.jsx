import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ticketApi } from '../api/client'
import { TICKET_PRIORITIES } from '../config/constants'
import TicketCategoryOptions from '../components/TicketCategoryOptions'
import { formatEnum } from '../utils/format'

const initialForm = {
  title: '',
  message: '',
  category: 'GENERAL_INQUIRY',
  priority: 'MEDIUM'
}

/**
 * Creates a customer support ticket using the selected category, priority and message.
 *
 * @returns {JSX.Element} New-ticket form page.
 */
export default function NewTicketPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(initialForm)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')

  const submit = async (event) => {
    event.preventDefault()
    setBusy(true)
    setError('')

    try {
      const detail = await ticketApi.create({
        title: form.title.trim() || null,
        message: form.message.trim(),
        category: form.category,
        priority: form.priority
      })
      navigate(`/tickets/${detail.ticket.id}`, { replace: true })
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <>
      <section className="page-heading split">
        <div>
          <Link className="back-link" to="/tickets">← Back to tickets</Link>
          <h1>Create a human support request ticket</h1>
        </div>
        <Link className="button button-secondary" to="/chat">Use AI chat instead</Link>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      <section className="content-grid new-ticket-grid">
        <form className="panel" onSubmit={submit}>
          <p className="eyebrow">Ticket details</p>
          <h2>Describe the issue</h2>

          <label>Title
            <input value={form.title} maxLength="180"
              onChange={(event) => setForm({ ...form, title: event.target.value })}
              placeholder="Short summary of the problem" />
          </label>

          <label>Category
            <select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}>
              <TicketCategoryOptions />
            </select>
          </label>

          <label>Priority
            <select value={form.priority} onChange={(event) => setForm({ ...form, priority: event.target.value })}>
              {TICKET_PRIORITIES.map((priority) => <option key={priority} value={priority}>{formatEnum(priority)}</option>)}
            </select>
          </label>

          <label>Message
            <textarea rows="8" maxLength="10000" required value={form.message}
              onChange={(event) => setForm({ ...form, message: event.target.value })}
              placeholder="Include the problem, what you expected to happen, and any useful context." />
          </label>

          <button className="button button-primary" disabled={busy || !form.message.trim()}>
            {busy ? 'Creating ticket…' : 'Create support ticket'}
          </button>
        </form>

        <aside className="panel ticket-guidance">
          <p className="eyebrow">What happens next</p>
          <h2>AI-assisted routing, human resolution</h2>
          <ol>
            <li>Your message is analysed for category, sentiment, priority, and confidence.</li>
            <li>The backend creates an escalated ticket and assigns an available agent when possible.</li>
            <li>You can continue the conversation from the ticket detail page and receive notifications for updates.</li>
          </ol>
          <p className="muted">Avoid including passwords, security answers, or full payment-card information.</p>
        </aside>
      </section>
    </>
  )
}
