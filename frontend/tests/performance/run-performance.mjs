import assert from 'node:assert/strict'
import { buildFrontend, launchBrowser, startPreview } from '../browser/browserHarness.mjs'

let stopPreview
let browser

try {
  buildFrontend()
  stopPreview = await startPreview()
  browser = await launchBrowser('http://127.0.0.1:4173/login')
  const { client } = browser
  await client.waitFor(`document.body.innerText.includes('Sign in to your account')`)

  const metrics = await client.evaluate(`(() => {
    const navigation = performance.getEntriesByType('navigation')[0];
    const resources = performance.getEntriesByType('resource');
    return {
      domContentLoaded: navigation?.domContentLoadedEventEnd || 0,
      loadComplete: navigation?.loadEventEnd || 0,
      resourceCount: resources.length,
      transferredBytes: resources.reduce((sum, item) => sum + (item.transferSize || 0), 0)
    };
  })()`)

  assert.ok(metrics.domContentLoaded < 3000, `DOMContentLoaded budget exceeded: ${metrics.domContentLoaded}ms`)
  assert.ok(metrics.loadComplete < 5000, `Load budget exceeded: ${metrics.loadComplete}ms`)
  assert.ok(metrics.resourceCount < 80, `Resource-count budget exceeded: ${metrics.resourceCount}`)
  assert.ok(metrics.transferredBytes < 8 * 1024 * 1024, `Transfer-size budget exceeded: ${metrics.transferredBytes} bytes`)

  console.log('Performance smoke test passed:', metrics)
} finally {
  browser?.cleanup()
  stopPreview?.()
}
