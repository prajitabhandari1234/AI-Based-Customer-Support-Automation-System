import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

/**
 * Vite development and preview configuration.
 * Both servers use the same backend target so local and browser tests behave like deployment.
 */
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const proxyTarget = env.VITE_API_PROXY_TARGET || 'http://localhost:8080'
  const proxy = {
    '/api': proxyTarget,
    '/actuator': proxyTarget
  }

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy
    },
    preview: {
      port: 4173,
      proxy
    }
  }
})
