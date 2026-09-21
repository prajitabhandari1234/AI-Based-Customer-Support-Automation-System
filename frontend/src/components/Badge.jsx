import { formatEnum } from '../utils/format'

/**
 * Displays a small status badge and converts backend enum text into readable labels.
 *
 * @param {{children: React.ReactNode, tone?: string}} props Badge properties.
 * @returns {JSX.Element} A styled badge.
 */
export default function Badge({ children, tone }) {
  const raw = String(tone || children || 'default')
  const normalized = raw.toLowerCase().replaceAll('_', '-')
  return <span className={`badge badge-${normalized}`}>{formatEnum(children, 'Unknown')}</span>
}
