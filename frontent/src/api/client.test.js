import { describe, expect, it } from 'vitest'
import { ApiError } from './client'

describe('ApiError', () => {
  it('preserves status and validation details for form handling', () => {
    const error = new ApiError('Validation failed', 400, { email: 'must be valid' })

    expect(error.name).toBe('ApiError')
    expect(error.message).toBe('Validation failed')
    expect(error.status).toBe(400)
    expect(error.validationErrors.email).toBe('must be valid')
  })
})
