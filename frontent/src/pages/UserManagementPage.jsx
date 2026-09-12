import { useEffect, useState } from 'react'
import { usersApi } from '../api/client'
import Badge from '../components/Badge'
import Loader from '../components/Loader'

export default function UserManagementPage() {
  const [users, setUsers] = useState([])
  const [savingId, setSavingId] = useState(null)
  const [error, setError] = useState('')

  const load = () => usersApi.list().then(setUsers)
  useEffect(() => { load() }, [])

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

  if (!users.length && !error) return <Loader label="Loading user accounts…" />

  return (
    <>
      <section className="page-heading">
        <p className="eyebrow">Role-based access control</p>
        <h1>User management</h1>
        <p>Review accounts, assign system roles, and deactivate access when required.</p>
      </section>
      {error && <div className="alert alert-error">{error}</div>}
      <section className="panel">
        <div className="data-table-wrap">
          <table className="data-table">
            <thead><tr><th>User</th><th>Email</th><th>Role</th><th>Status</th><th>Created</th><th>Actions</th></tr></thead>
            <tbody>
              {users.map((item) => (
                <tr key={item.id}>
                  <td><div className="user-cell"><div className="avatar small">{item.name.charAt(0)}</div><strong>{item.name}</strong></div></td>
                  <td>{item.email}</td>
                  <td><Badge>{item.role}</Badge></td>
                  <td><Badge tone={item.status}>{item.status}</Badge></td>
                  <td>{new Date(item.createdAt).toLocaleDateString()}</td>
                  <td>
                    <div className="inline-controls">
                      <select value={item.role} disabled={savingId === item.id}
                        onChange={(e) => update(item, { role: e.target.value })}>
                        {['CLIENT', 'AGENT', 'ADMIN'].map((role) => <option key={role}>{role}</option>)}
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
      </section>
    </>
  )
}
