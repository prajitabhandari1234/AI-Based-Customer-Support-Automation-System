import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import AuthHero from '../components/AuthHero'
import { ENABLE_DEMO_ACCOUNTS } from '../config/environment'

/**
 * Authenticates an existing user and returns them to the appropriate protected route.
 *
 * @returns {JSX.Element} Login page.
 */
export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  if (user) {
    return <Navigate to={user.role === 'CLIENT' ? '/' : '/staff'} replace />
  }

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setBusy(true)

    try {
      const profile = await login({
        email: form.email.trim(),
        password: form.password
      })

      const fallback = profile.role === 'CLIENT' ? '/' : '/staff'
      navigate(location.state?.from?.pathname || fallback, { replace: true })
    } catch (err) {
      setError(err.message || 'Unable to sign in. Please try again.')
    } finally {
      setBusy(false)
    }
  }

  // These accounts match the optional backend DataSeeder.
  const useDemo = (type) => {
    const credentials = {
      customer: ['customer@support.local', 'Customer123!'],
      agent: ['agent@support.local', 'Agent123!'],
      admin: ['admin@support.local', 'Admin123!']
    }[type]

    if (!credentials) return

    setForm({
      email: credentials[0],
      password: credentials[1]
    })
  }

  return (
    <div className="auth-page">
      <AuthHero />

      <section className="auth-panel">
        <form className="auth-card" onSubmit={submit}>
          <p className="eyebrow">Welcome back</p>
          <h2>Sign in to your account</h2>

          <p className="muted">
            Access your AI-based customer support workspace.
          </p>

          {error && (
            <div className="alert alert-error" role="alert">
              {error}
            </div>
          )}

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
          </label>

          <label>
            Password
            <input
              type="password"
              value={form.password}
              autoComplete="current-password"
              onChange={(event) =>
                setForm({ ...form, password: event.target.value })
              }
              required
            />
          </label>

          <button
            className="button button-primary button-full"
            type="submit"
            disabled={busy}
          >
            {busy ? 'Signing in…' : 'Sign in'}
          </button>

          {ENABLE_DEMO_ACCOUNTS && (
            <div className="demo-roles">
              <span>Demo accounts</span>
              <button type="button" onClick={() => useDemo('customer')}>
                Client
              </button>
              <button type="button" onClick={() => useDemo('agent')}>
                Agent
              </button>
              <button type="button" onClick={() => useDemo('admin')}>
                Admin
              </button>
            </div>
          )}

          <p className="auth-switch">
            New here? <Link to="/register">Create an account</Link>
          </p>
        </form>
      </section>
    </div>
  )
}
