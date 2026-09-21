import { useCallback, useEffect, useState } from 'react'
import { usersApi } from '../api/client'
import { USER_ROLES, USER_STATUSES } from '../config/constants'
import Badge from '../components/Badge'
import Loader from '../components/Loader'
import { formatDate, formatEnum } from '../utils/format'

const newUser = {
  name: '',
  email: '',
  password: '',
  role: 'AGENT',
  status: 'ACTIVE'
}

/**
 * Allows administrators to create user accounts and update account roles or statuses.
 *
 * @returns {JSX.Element} User administration page.
 */
export default function UserManagementPage() {
  const [users, setUsers] = useState([])
  const [form, setForm] = useState(newUser)
  const [savingId, setSavingId] = useState(null)
  const [creating, setCreating] = useState(false)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    setError('')
    return usersApi.list()
      .then(setUsers)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  const update = async (user, patch) => {
    setSavingId(user.id)
    setError('')
    try {
      const updated = await usersApi.update(user.id, patch)
      setUsers((current) => current.map((item) => item.id === user.id ? updated : item))
    } catch (err) {
      setError(err.message)
    } finally {
      setSavingId(null)
    }
  }

  const create = async (event) => {
    event.preventDefault()
    setCreating(true)
    setError('')
    try {
      const created = await usersApi.create(form)
      setUsers((current) => [...current, created].sort((a, b) => a.name.localeCompare(b.name)))
      setForm(newUser)
    } catch (err) {
      setError(err.message)
    } finally {
      setCreating(false)
    }
  }

  if (loading) return <Loader label="Loading user accounts…" />

  return (
    <>
      <section className="page-heading split">
        <div>
          <h1>User management</h1>
        </div>
        <button className="button button-secondary" onClick={load}>Refresh users</button>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      <section className="content-grid user-management-grid">
        <form className="panel sticky-form" onSubmit={create}>
          <h2>Register account</h2>
          <label>Full name
            <input required maxLength="120" value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
          </label>
          <label>Email address
            <input required type="email" value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
          </label>
          <label>Temporary password
            <input required type="password" minLength="8" maxLength="100" value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })} />
            <small>Minimum 8 characters as required by the backend.</small>
          </label>
          <label>Role
            <select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value })}>
              {USER_ROLES.map((role) => <option key={role} value={role}>{formatEnum(role)}</option>)}
            </select>
          </label>
          <label>Status
            <select value={form.status} onChange={(event) => setForm({ ...form, status: event.target.value })}>
              {USER_STATUSES.map((status) => <option key={status} value={status}>{formatEnum(status)}</option>)}
            </select>
          </label>
          <button className="button button-primary button-full" disabled={creating}>
            {creating ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <article className="panel user-table-panel">
          <div className="panel-header"><div><h2>Current user accounts</h2></div></div>
          <div className="data-table-wrap">
            <table className="data-table">
              <thead><tr><th>User</th><th>Email</th><th>Role</th><th>Status</th><th>Created</th><th>Actions</th></tr></thead>
              <tbody>
                {users.map((item) => (
                  <tr key={item.id}>
                    <td><div className="user-cell"><div className="avatar small">{item.name?.charAt(0).toUpperCase() || '?'}</div><strong>{item.name}</strong></div></td>
                    <td>{item.email}</td>
                    <td><Badge>{item.role}</Badge></td>
                    <td><Badge tone={item.status}>{item.status}</Badge></td>
                    <td>{formatDate(item.createdAt)}</td>
                    <td>
                      <div className="inline-controls">
                        <select value={item.role} disabled={savingId === item.id}
                          onChange={(event) => update(item, { role: event.target.value })}>
                          {USER_ROLES.map((role) => <option key={role} value={role}>{formatEnum(role)}</option>)}
                        </select>
                        <button className="button button-small button-ghost" disabled={savingId === item.id}
                          onClick={() => update(item, { status: item.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE' })}>
                          {item.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </article>
      </section>
    </>
  )
}
