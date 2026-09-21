import { Link } from 'react-router-dom'

/**
 * Displays a simple fallback for routes that do not exist in the application.
 *
 * @returns {JSX.Element} Not-found page.
 */
export default function NotFoundPage() {
  return (
    <div className="center-state">
      <div className="empty-mark">404</div>
      <h1>Page not found</h1>
      <p>The requested support page does not exist.</p>
      <Link className="button button-primary" to="/">Return home</Link>
    </div>
  )
}
