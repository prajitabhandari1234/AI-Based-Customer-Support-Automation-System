import { spawn, spawnSync } from 'node:child_process'
import { existsSync, mkdtempSync, rmSync } from 'node:fs'
import { createServer } from 'node:http'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

/** Waits until an HTTP URL responds successfully. */
export async function waitForUrl(url, timeoutMs = 30_000) {
  const started = Date.now()
  let lastError
  while (Date.now() - started < timeoutMs) {
    try {
      const response = await fetch(url)
      if (response.ok) return
    } catch (error) {
      lastError = error
    }
    await new Promise((resolve) => setTimeout(resolve, 150))
  }
  throw new Error(`Timed out waiting for ${url}${lastError ? `: ${lastError.message}` : ''}`)
}

/** Builds the production frontend before browser-based tests run. */
export function buildFrontend() {
  const npm = process.platform === 'win32' ? 'npm.cmd' : 'npm'
  const result = spawnSync(npm, ['run', 'build'], { stdio: 'inherit', env: process.env })
  if (result.status !== 0) throw new Error('Frontend build failed before browser testing')
}

/** Starts Vite preview and returns a cleanup function. */
export async function startPreview() {
  const npm = process.platform === 'win32' ? 'npm.cmd' : 'npm'
  const child = spawn(npm, ['run', 'preview', '--', '--host', '127.0.0.1', '--port', '4173'], {
    stdio: ['ignore', 'pipe', 'pipe'],
    env: process.env
  })
  child.stdout.on('data', (chunk) => process.stdout.write(`[preview] ${chunk}`))
  child.stderr.on('data', (chunk) => process.stderr.write(`[preview] ${chunk}`))
  await waitForUrl('http://127.0.0.1:4173/login', 30_000)
  return () => child.kill('SIGTERM')
}

/** Starts a small HTTP mock backend used by browser E2E tests. */
export async function startMockBackend(handler, port = 8080) {
  const server = createServer(async (request, response) => {
    const chunks = []
    for await (const chunk of request) chunks.push(chunk)
    const body = Buffer.concat(chunks).toString('utf8')

    try {
      const result = await handler({
        method: request.method || 'GET',
        url: new URL(request.url || '/', `http://${request.headers.host || 'localhost'}`),
        headers: request.headers,
        body
      })
      const status = result?.status || 200
      const payload = result?.body ?? null
      response.writeHead(status, { 'Content-Type': 'application/json', ...(result?.headers || {}) })
      response.end(payload === null ? '' : JSON.stringify(payload))
    } catch (error) {
      response.writeHead(500, { 'Content-Type': 'application/json' })
      response.end(JSON.stringify({ message: error.message }))
    }
  })

  await new Promise((resolve, reject) => {
    server.once('error', reject)
    server.listen(port, '127.0.0.1', resolve)
  })
  return () => new Promise((resolve) => server.close(resolve))
}

function chromeCandidates() {
  return [
    process.env.CHROME_BIN,
    '/usr/bin/chromium',
    '/usr/bin/chromium-browser',
    '/usr/bin/google-chrome',
    '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome',
    '/Applications/Chromium.app/Contents/MacOS/Chromium',
    process.env.PROGRAMFILES && join(process.env.PROGRAMFILES, 'Google/Chrome/Application/chrome.exe'),
    process.env['PROGRAMFILES(X86)'] && join(process.env['PROGRAMFILES(X86)'], 'Google/Chrome/Application/chrome.exe'),
    process.env.LOCALAPPDATA && join(process.env.LOCALAPPDATA, 'Google/Chrome/Application/chrome.exe')
  ].filter(Boolean)
}

/** Finds a local Chrome/Chromium binary without requiring a browser test dependency. */
export function findChrome() {
  const chrome = chromeCandidates().find((candidate) => existsSync(candidate))
  if (!chrome) {
    throw new Error('Chrome/Chromium was not found. Set CHROME_BIN to the browser executable before running browser tests.')
  }
  return chrome
}

class CdpClient {
  constructor(socket) {
    this.socket = socket
    this.nextId = 1
    this.pending = new Map()
    socket.addEventListener('message', (event) => {
      const message = JSON.parse(event.data)
      if (!message.id) return
      const pending = this.pending.get(message.id)
      if (!pending) return
      this.pending.delete(message.id)
      if (message.error) pending.reject(new Error(message.error.message))
      else pending.resolve(message.result)
    })
  }

  /** Sends one Chrome DevTools Protocol command. */
  send(method, params = {}) {
    const id = this.nextId++
    this.socket.send(JSON.stringify({ id, method, params }))
    return new Promise((resolve, reject) => this.pending.set(id, { resolve, reject }))
  }

  /** Evaluates JavaScript inside the active browser page. */
  async evaluate(expression) {
    const result = await this.send('Runtime.evaluate', {
      expression,
      awaitPromise: true,
      returnByValue: true,
      userGesture: true
    })
    if (result.exceptionDetails) throw new Error(result.exceptionDetails.text || 'Browser evaluation failed')
    return result.result?.value
  }

  /** Polls a browser expression until it becomes truthy. */
  async waitFor(expression, timeoutMs = 10_000) {
    const started = Date.now()
    while (Date.now() - started < timeoutMs) {
      if (await this.evaluate(expression)) return
      await new Promise((resolve) => setTimeout(resolve, 100))
    }
    throw new Error(`Timed out waiting for browser condition: ${expression}`)
  }

  /** Navigates and waits until the document is ready. */
  async navigate(url) {
    await this.send('Page.navigate', { url })
    await this.waitFor(`document.readyState === 'complete'`, 15_000)
  }

  /** Updates a React-controlled input or textarea and dispatches the normal input events. */
  async setValue(selector, value) {
    const encodedSelector = JSON.stringify(selector)
    const encodedValue = JSON.stringify(value)
    await this.evaluate(`(() => {
      const element = document.querySelector(${encodedSelector});
      if (!element) throw new Error('Missing element: ' + ${encodedSelector});
      const prototype = Object.getPrototypeOf(element);
      const setter = Object.getOwnPropertyDescriptor(prototype, 'value')?.set;
      if (setter) setter.call(element, ${encodedValue}); else element.value = ${encodedValue};
      element.dispatchEvent(new Event('input', { bubbles: true }));
      element.dispatchEvent(new Event('change', { bubbles: true }));
      return true;
    })()`)
  }

  /** Clicks the first element that matches a CSS selector. */
  async click(selector) {
    const encodedSelector = JSON.stringify(selector)
    await this.evaluate(`(() => {
      const element = document.querySelector(${encodedSelector});
      if (!element) throw new Error('Missing element: ' + ${encodedSelector});
      element.click();
      return true;
    })()`)
  }

  /** Closes the CDP socket. */
  close() {
    this.socket.close()
  }
}

/** Launches Chrome in headless mode and returns a small CDP client plus cleanup function. */
export async function launchBrowser(startUrl) {
  const chrome = findChrome()
  const userDataDir = mkdtempSync(join(tmpdir(), 'frontend-browser-test-'))
  const port = 9222 + Math.floor(Math.random() * 500)
  const child = spawn(chrome, [
    '--headless=new',
    '--no-sandbox',
    '--disable-gpu',
    '--disable-dev-shm-usage',
    '--remote-debugging-address=127.0.0.1',
    `--remote-debugging-port=${port}`,
    `--user-data-dir=${userDataDir}`,
    'about:blank'
  ], { stdio: ['ignore', 'ignore', 'ignore'] })

  const versionUrl = `http://127.0.0.1:${port}/json/version`
  await waitForUrl(versionUrl, 20_000)
  const targetResponse = await fetch(`http://127.0.0.1:${port}/json/new?${encodeURIComponent(startUrl)}`, { method: 'PUT' })
  if (!targetResponse.ok) throw new Error(`Unable to create browser tab: HTTP ${targetResponse.status}`)
  const target = await targetResponse.json()
  const socket = new WebSocket(target.webSocketDebuggerUrl)
  await new Promise((resolve, reject) => {
    socket.addEventListener('open', resolve, { once: true })
    socket.addEventListener('error', reject, { once: true })
  })

  const client = new CdpClient(socket)
  await client.send('Runtime.enable')
  await client.send('Page.enable')
  await client.waitFor(`document.readyState === 'complete'`, 15_000)

  return {
    client,
    cleanup: () => {
      client.close()
      child.kill('SIGTERM')
      rmSync(userDataDir, { recursive: true, force: true })
    }
  }
}
