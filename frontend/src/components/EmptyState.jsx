/**
 * Shows a consistent empty-state message when a page has no records to display.
 *
 * @param {{title: string, message: string, action?: React.ReactNode}} props Empty-state content.
 * @returns {JSX.Element} Empty-state panel content.
 */
export default function EmptyState({ title, message, action }) {
  return (
    <div className="empty-state">
      <div className="empty-mark">◎</div>
      <h3>{title}</h3>
      <p>{message}</p>
      {action}
    </div>
  )
}
