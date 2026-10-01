import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../api/client'
import AuthHero from '../components/AuthHero'

/**
 * Lets a user request a simulated password reset after account lookup.
 *
 * @returns {JSX.Element} Forgot password page.
 */
export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)

  const submit = async (event) => {
    event.preventDefault()

    setError('')
    setMessage('')
    setBusy(true)

    try {
      const response = await authApi.forgotPassword({
        email: email.trim()
      })

      setMessage(
        response?.message ||
          'Password reset link is sent via email'
      )
    } catch (err) {
      setError(
        err.message ||
          'Unable to request a password reset. Please try again.'
      )
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <AuthHero />

      <section className="auth-panel">
        <form className="auth-card" onSubmit={submit}>
          <p className="eyebrow">Password reset</p>

          <h2>Reset your password</h2>

          <p className="muted">
            Enter the email address linked to your account.
          </p>

          {error && (
            <div
              className="alert alert-error"
              role="alert"
            >
              {error}
            </div>
          )}

          {message && (
            <div
              className="alert alert-success"
              role="status"
            >
              {message}
            </div>
          )}

          <label>
            Email

            <input
              type="email"
              value={email}
              autoComplete="email"
              onChange={(event) =>
                setEmail(event.target.value)
              }
              required
            />
          </label>

          <button
            className="button button-primary button-full"
            type="submit"
            disabled={busy}
          >
            {busy ? 'Checking…' : 'Send reset link'}
          </button>

          <p className="auth-switch">
            Remembered your password?{' '}
            <Link to="/login">
              Back to sign in
            </Link>
          </p>
        </form>
      </section>
    </div>
  )
}
