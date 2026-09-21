import { Link } from 'react-router-dom'

/**
 * Shows the two common customer actions used on ticket-related pages.
 * Keeping these links in one component avoids repeating the same markup and routes.
 *
 * @returns {JSX.Element} Links for creating a ticket and starting an AI chat.
 */
export default function CustomerTicketActions() {
  return (
    <div className="button-row heading-actions">
      <Link className="button button-secondary" to="/tickets/new">+ New ticket</Link>
      <Link className="button button-primary" to="/chat">✦ Start AI chat</Link>
    </div>
  )
}
