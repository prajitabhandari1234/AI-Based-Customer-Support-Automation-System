export default function StatCard({ label, value, helper, icon }) {
  return (
    <article className="stat-card">
      <div className="stat-icon" aria-hidden="true">{icon}</div>
      <div>
        <p className="stat-label">{label}</p>
        <strong className="stat-value">{value}</strong>
        {helper && <p className="stat-helper">{helper}</p>}
      </div>
    </article>
  )
}
