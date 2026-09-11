export default function Badge({ children, tone }) {
  const normalized = String(tone || children || '').toLowerCase().replaceAll('_', '-')
  return <span className={`badge badge-${normalized}`}>{String(children).replaceAll('_', ' ')}</span>
}
