import { TICKET_CATEGORIES } from '../config/constants'
import { formatEnum } from '../utils/format'

/**
 * Builds the shared ticket category options used by support forms.
 * The values come directly from the backend-compatible category constants.
 *
 * @returns {JSX.Element[]} Select option elements for every ticket category.
 */
export default function TicketCategoryOptions() {
  return TICKET_CATEGORIES.map((category) => (
    <option key={category} value={category}>{formatEnum(category)}</option>
  ))
}
