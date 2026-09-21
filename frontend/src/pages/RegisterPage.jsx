import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import AuthHero from '../components/AuthHero'

/**
 * Registers a new customer account and starts a session after successful creation.
 *
 * @returns {JSX.Element} Customer registration page.
 */
export default function RegisterPage() {
  const { user, register } = useAuth()
  const navigate = useNavigate()

  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: ''
  })
  const [error, setError] = useState('')
  const [validationErrors, setValidationErrors] = useState({})
  const [busy, setBusy] = useState(false)

  if (user) {
    return <Navigate to={user.role === 'CLIENT' ? '/' : '/staff'} replace />
  }

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setValidationErrors({})

    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match')
      return
    }

    setBusy(true)

    try {
      await register({
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password
      })

      navigate('/')
    } catch (err) {
      setError(err.message || 'Unable to create the account. Please try again.')
      setValidationErrors(err.validationErrors || {})
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <AuthHero />

      <section className="auth-panel">
        <form className="auth-card" onSubmit={submit}>
          <p className="eyebrow">Create account</p>
          <h2>Create your customer account</h2>

          <p className="muted">
            Register to access AI-assisted support and manage your support
            tickets.
          </p>

          {error && (
            <div className="alert alert-error" role="alert">
              {error}
            </div>
          )}

          <label>
            Name
            <input
              type="text"
              value={form.name}
              autoComplete="name"
              minLength="2"
              maxLength="120"
              onChange={(event) =>
                setForm({ ...form, name: event.target.value })
              }
              required
            />
            {validationErrors.name && (
              <small className="field-error">{validationErrors.name}</small>
            )}
          </label>

          <label>
            Email
            <input
              type="email"
              value={form.email}
              autoComplete="email"
              onChange={(event) =>
                setForm({ ...form, email: event.target.value })
              }
              required
            />
            {validationErrors.email && (
              <small className="field-error">{validationErrors.email}</small>
            )}
          </label>

          <label>
            Password
            <input
              type="password"
              value={form.password}
              autoComplete="new-password"
              minLength="8"
              maxLength="100"
              onChange={(event) =>
                setForm({ ...form, password: event.target.value })
              }
              required
            />
            {validationErrors.password && (
              <small className="field-error">
                {validationErrors.password}
              </small>
            )}
          </label>

          <label>
            Confirm password
            <input
              type="password"
              value={form.confirmPassword}
              autoComplete="new-password"
              minLength="8"
              maxLength="100"
              onChange={(event) =>
                setForm({ ...form, confirmPassword: event.target.value })
              }
              required
            />
          </label>

          <button
            className="button button-primary button-full"
            type="submit"
            disabled={busy}
          >
            {busy ? 'Creating account…' : 'Create account'}
          </button>

          <p className="auth-switch">
            Already registered? <Link to="/login">Sign in</Link>
          </p>
        </form>
      </section>
    </div>
  )
}
