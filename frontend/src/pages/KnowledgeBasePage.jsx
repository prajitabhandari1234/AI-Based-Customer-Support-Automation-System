import { useCallback, useEffect, useState } from 'react'
import { knowledgeApi } from '../api/client'
import Badge from '../components/Badge'
import EmptyState from '../components/EmptyState'
import Loader from '../components/Loader'
import TicketCategoryOptions from '../components/TicketCategoryOptions'
import { formatDateTime } from '../utils/format'

const blank = { questionPattern: '', answerTemplate: '', category: 'GENERAL_INQUIRY', active: true }

/**
 * Allows staff to create, edit, list and remove approved chatbot knowledge entries.
 *
 * @returns {JSX.Element} Knowledge-base administration page.
 */
export default function KnowledgeBasePage() {
  const [entries, setEntries] = useState([])
  const [form, setForm] = useState(blank)
  const [editingId, setEditingId] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const load = useCallback(() => {
    setError('')
    return knowledgeApi.list()
      .then(setEntries)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => { load() }, [load])

  const save = async (event) => {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      if (editingId) await knowledgeApi.update(editingId, form)
      else await knowledgeApi.create(form)
      setForm(blank)
      setEditingId(null)
      await load()
    } catch (err) {
      setError(err.message)
    } finally {
      setSaving(false)
    }
  }

  const edit = (entry) => {
    setEditingId(entry.id)
    setForm({
      questionPattern: entry.questionPattern,
      answerTemplate: entry.answerTemplate,
      category: entry.category,
      active: entry.active
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const remove = async (id) => {
    if (!window.confirm('Delete this knowledge base entry?')) return
    setError('')
    try {
      await knowledgeApi.remove(id)
      if (editingId === id) {
        setEditingId(null)
        setForm(blank)
      }
      await load()
    } catch (err) {
      setError(err.message)
    }
  }

  if (loading) return <Loader label="Loading the knowledge base…" />

  return (
    <>
      <section className="page-heading">
        <p className="eyebrow">Smart knowledge base</p>
        <h1>Frequently Asked Questions</h1>
      </section>

      {error && <div className="alert alert-error">{error}</div>}

      <section className="content-grid knowledge-grid">
        <form className="panel sticky-form" onSubmit={save}>
          <p className="eyebrow">{editingId ? 'Edit entry' : 'New entry'}</p>
          <h2>{editingId ? `Update FAQ #${editingId}` : 'Add chatbot knowledge'}</h2>
          <label>Question patterns
            <input value={form.questionPattern} onChange={(event) => setForm({ ...form, questionPattern: event.target.value })}
              placeholder="refund policy, request refund, money back" required />
            <small>Separate alternative phrases with commas.</small>
          </label>
          <label>Category
            <select value={form.category} onChange={(event) => setForm({ ...form, category: event.target.value })}>
              <TicketCategoryOptions />
            </select>
          </label>
          <label>Approved answer
            <textarea rows="8" value={form.answerTemplate}
              onChange={(event) => setForm({ ...form, answerTemplate: event.target.value })}
              placeholder="Write a concise, accurate customer-facing response…" required />
          </label>
          <label className="checkbox-row">
            <input type="checkbox" checked={form.active}
              onChange={(event) => setForm({ ...form, active: event.target.checked })} />
            Active for chatbot matching
          </label>
          <div className="button-row">
            <button className="button button-primary" disabled={saving}>{saving ? 'Saving…' : editingId ? 'Save changes' : 'Create entry'}</button>
            {editingId && <button type="button" className="button button-ghost" onClick={() => { setEditingId(null); setForm(blank) }}>Cancel</button>}
          </div>
        </form>

        <article className="panel">
          <div className="panel-header">
            <div><p className="eyebrow">Current library</p><h2>{entries.length} entries</h2></div>
            <button className="text-link" onClick={load}>Refresh</button>
          </div>
          {entries.length === 0 ? (
            <EmptyState title="Knowledge base is empty" message="Create the first approved chatbot response." />
          ) : (
            <div className="kb-list">
              {entries.map((entry) => (
                <article className="kb-entry" key={entry.id}>
                  <div className="kb-entry-head">
                    <div><Badge>{entry.category}</Badge> {!entry.active && <Badge tone="inactive">Inactive</Badge>}</div>
                    <div><button className="text-link" onClick={() => edit(entry)}>Edit</button><button className="text-link danger" onClick={() => remove(entry.id)}>Delete</button></div>
                  </div>
                  <h3>{entry.questionPattern}</h3>
                  <p>{entry.answerTemplate}</p>
                  <small>Updated {formatDateTime(entry.lastUpdatedAt)}{entry.lastUpdatedByName ? ` by ${entry.lastUpdatedByName}` : ''}</small>
                </article>
              ))}
            </div>
          )}
        </article>
      </section>
    </>
  )
}
