// Small formatting helpers keep null API values from breaking page rendering.
/**
 * Converts a backend enum value such as IN_PROGRESS into readable title case.
 *
 * @param {*} value Value to format.
 * @param {string} [fallback='Not available'] Text used when the value is empty.
 * @returns {string} Human-readable value.
 */
export function formatEnum(value, fallback = 'Not available') {
  if (!value) return fallback
  return String(value).replaceAll('_', ' ').toLowerCase().replace(/\b\w/g, (letter) => letter.toUpperCase())
}

/**
 * Formats a date/time value using the browser locale and handles invalid values safely.
 *
 * @param {*} value Date-like value.
 * @param {string} [fallback='Not available'] Text used for missing or invalid dates.
 * @returns {string} Localised date and time.
 */
export function formatDateTime(value, fallback = 'Not available') {
  if (!value) return fallback
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? fallback : date.toLocaleString()
}

/**
 * Formats a date value using the browser locale while protecting the UI from invalid input.
 *
 * @param {*} value Date-like value.
 * @param {string} [fallback='Not available'] Text used for missing or invalid dates.
 * @returns {string} Localised date.
 */
export function formatDate(value, fallback = 'Not available') {
  if (!value) return fallback
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? fallback : date.toLocaleDateString()
}

/**
 * Formats a numeric AI score to a fixed number of decimal places.
 *
 * @param {*} value Score value.
 * @param {number} [digits=2] Decimal places to display.
 * @param {string} [fallback='Not available'] Text used for non-numeric input.
 * @returns {string} Formatted score.
 */
export function formatScore(value, digits = 2, fallback = 'Not available') {
  const number = Number(value)
  return Number.isFinite(number) ? number.toFixed(digits) : fallback
}

/**
 * Converts a decimal score between zero and one into a rounded percentage label.
 *
 * @param {*} value Decimal value.
 * @param {string} [fallback='Not available'] Text used for non-numeric input.
 * @returns {string} Percentage label.
 */
export function formatPercent(value, fallback = 'Not available') {
  const number = Number(value)
  return Number.isFinite(number) ? `${Math.round(number * 100)}%` : fallback
}
