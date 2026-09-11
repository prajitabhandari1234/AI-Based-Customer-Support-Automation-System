export default function Loader({ label = 'Loading…' }) {
  return (
    <div className="center-state">
      <div className="spinner" aria-hidden="true" />
      <p>{label}</p>
    </div>
  )
}
