/** Unit tests for shared formatting helpers. */
import { describe, expect, it, vi } from 'vitest'
import { formatDate, formatDateTime, formatEnum, formatPercent, formatScore } from './format'

describe('format helpers', () => {
  it('formats backend enum values as readable labels', () => {
    expect(formatEnum('RESOLVED_BY_AI')).toBe('Resolved By Ai')
    expect(formatEnum('', 'Fallback')).toBe('Fallback')
  })

  it('formats numeric scores and percentages safely', () => {
    expect(formatScore(0.87654)).toBe('0.88')
    expect(formatScore('not-a-number')).toBe('Not available')
    expect(formatPercent(0.876)).toBe('88%')
    expect(formatPercent(undefined)).toBe('Not available')
  })

  it('returns fallbacks for invalid dates', () => {
    expect(formatDate('bad-date')).toBe('Not available')
    expect(formatDateTime(null)).toBe('Not available')
  })

  it('uses browser date formatting for valid values', () => {
    const toLocaleDateString = vi.spyOn(Date.prototype, 'toLocaleDateString').mockReturnValue('DATE')
    const toLocaleString = vi.spyOn(Date.prototype, 'toLocaleString').mockReturnValue('DATE_TIME')

    expect(formatDate('2026-09-21T00:00:00Z')).toBe('DATE')
    expect(formatDateTime('2026-09-21T00:00:00Z')).toBe('DATE_TIME')

    toLocaleDateString.mockRestore()
    toLocaleString.mockRestore()
  })
})
