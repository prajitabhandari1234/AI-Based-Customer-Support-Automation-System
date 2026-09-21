/** Server-rendered tests for reusable presentational components. */
import { renderToStaticMarkup } from 'react-dom/server'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import Badge from './Badge'
import CustomerTicketActions from './CustomerTicketActions'
import EmptyState from './EmptyState'
import TicketCategoryOptions from './TicketCategoryOptions'

describe('shared components', () => {
  it('formats badge labels and tone classes', () => {
    const html = renderToStaticMarkup(<Badge tone="RESOLVED_BY_AI">RESOLVED_BY_AI</Badge>)
    expect(html).toContain('badge-resolved-by-ai')
    expect(html).toContain('Resolved By Ai')
  })

  it('renders an empty state with an optional action', () => {
    const html = renderToStaticMarkup(
      <EmptyState title="Nothing here" message="Create the first item." action={<button>Add</button>} />
    )
    expect(html).toContain('Nothing here')
    expect(html).toContain('Create the first item.')
    expect(html).toContain('<button>Add</button>')
  })

  it('keeps common customer ticket actions on the expected routes', () => {
    const html = renderToStaticMarkup(<MemoryRouter><CustomerTicketActions /></MemoryRouter>)
    expect(html).toContain('href="/tickets/new"')
    expect(html).toContain('href="/chat"')
  })

  it('provides every backend ticket category as a select option', () => {
    const html = renderToStaticMarkup(<select><TicketCategoryOptions /></select>)
    expect(html).toContain('value="GENERAL_INQUIRY"')
    expect(html).toContain('General Inquiry')
    expect((html.match(/<option/g) || []).length).toBe(7)
  })
})
