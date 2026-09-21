import assert from 'node:assert/strict'
import { buildFrontend, launchBrowser, startMockBackend, startPreview } from '../browser/browserHarness.mjs'

const customer = { id: 1, name: 'Test Customer', email: 'customer@example.com', role: 'CLIENT', status: 'ACTIVE' }
const baseTicket = {
  id: 11,
  ticketId: 11,
  title: 'Mouse stopped working',
  category: 'TECHNICAL',
  priority: 'MEDIUM',
  sentiment: 'NEUTRAL',
  status: 'OPEN',
  escalated: false,
  customerName: customer.name,
  updatedAt: '2026-09-21T08:00:00Z'
}

/** Returns deterministic backend responses so the browser flow does not need the real API. */
async function backendRoute({ method, url }) {
  const path = url.pathname
  if (path === '/actuator/health') return { body: { status: 'UP' } }
  if (path === '/api/customer/notifications') return { body: [] }
  if (path === '/api/auth/login' && method === 'POST') return { body: { token: 'test-token', user: customer } }
  if (path === '/api/auth/me') return { body: customer }
  if (path === '/api/tickets/my/summary') return { body: { total: 1, open: 1 } }
  if (path === '/api/tickets/my') return { body: [baseTicket] }
  if (path === '/api/chat/messages' && method === 'POST') {
    return {
      body: {
        reply: 'Try reconnecting the mouse and testing another USB port.',
        ticket: baseTicket,
        analysis: {
          category: 'TECHNICAL',
          priority: 'MEDIUM',
          sentiment: 'NEUTRAL',
          sentimentScore: 0,
          confidence: 0.91,
          knowledgeBaseMatch: true,
          escalated: false,
          escalationReasons: []
        }
      }
    }
  }
  if (path === '/api/tickets' && method === 'POST') {
    return {
      body: {
        ticket: { ...baseTicket, id: 12, ticketId: 12, title: 'Created from e2e' },
        messages: [{ id: 1, senderType: 'CLIENT', senderName: customer.name, content: 'Printer is offline' }]
      }
    }
  }
  if (path === '/api/tickets/12') {
    return {
      body: {
        ticket: { ...baseTicket, id: 12, ticketId: 12, title: 'Created from e2e' },
        messages: [{ id: 1, senderType: 'CLIENT', senderName: customer.name, content: 'Printer is offline' }]
      }
    }
  }
  return { status: 404, body: { message: `No mock route for ${method} ${path}` } }
}

let stopBackend
let stopPreview
let browser

try {
  buildFrontend()
  stopBackend = await startMockBackend(backendRoute)
  stopPreview = await startPreview()
  browser = await launchBrowser('http://127.0.0.1:4173/login')
  const { client } = browser

  await client.waitFor(`document.body.innerText.includes('Sign in to your account')`)
  await client.setValue('input[type="email"]', 'customer@example.com')
  await client.setValue('input[type="password"]', 'Customer123!')
  await client.click('button[type="submit"]')
  await client.waitFor(`document.body.innerText.includes('Hello, Test')`)

  assert.equal(await client.evaluate(`document.body.innerText.includes('Mouse stopped working')`), true)
  assert.equal(await client.evaluate(`document.body.innerText.includes('System Online')`), true)
  console.log('PASS: customer login and dashboard')

  await client.click('a[href="/chat"]')
  await client.waitFor(`document.body.innerText.includes('Chat with AI')`)
  await client.setValue('textarea[placeholder="Describe your question or issue…"]', 'My mouse stopped working')
  await client.click('button[aria-label="Send message"]')
  await client.waitFor(`document.body.innerText.includes('Try reconnecting the mouse')`)

  assert.equal(await client.evaluate(`document.body.innerText.includes('91%')`), true)
  assert.equal(await client.evaluate(`document.body.innerText.includes('Ticket #11')`), true)
  console.log('PASS: AI chat response and analysis')

  await client.navigate('http://127.0.0.1:4173/tickets/new')
  await client.waitFor(`document.body.innerText.includes('Create a human support request ticket')`)
  await client.setValue('input[placeholder="Short summary of the problem"]', 'Created from e2e')
  await client.setValue('textarea[placeholder*="Include the problem"]', 'Printer is offline')
  await client.click('button.button-primary')
  await client.waitFor(`location.pathname === '/tickets/12'`)
  await client.waitFor(`document.body.innerText.includes('Created from e2e') && document.body.innerText.includes('Printer is offline')`)
  console.log('PASS: support ticket creation and detail navigation')

  console.log('E2E browser tests passed.')
} finally {
  browser?.cleanup()
  stopPreview?.()
  if (stopBackend) await stopBackend()
}
