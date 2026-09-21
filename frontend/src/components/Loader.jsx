/**
 * Displays the shared loading state used while pages wait for API data.
 *
 * @param {{label?: string}} props Loading message properties.
 * @returns {JSX.Element} Loading indicator.
 */
export default function Loader({ label = 'Loading…' }) {
  return (
    <div className="center-state">
      <div className="spinner" aria-hidden="true" />
      <p>{label}</p>
    </div>
  )
}
