/** Central frontend environment settings loaded by Vite at build/development time. */
const rawApiBaseUrl = import.meta.env.VITE_API_BASE_URL || ''

/** Base URL used by browser API requests. Blank means same-origin proxying. */
export const API_BASE_URL = rawApiBaseUrl.trim().replace(/\/$/, '')
/** Maximum browser request time in milliseconds. */
export const API_TIMEOUT_MS = Number(import.meta.env.VITE_API_TIMEOUT_MS || 30000)
/** Controls whether optional demo-account shortcuts are displayed. */
export const ENABLE_DEMO_ACCOUNTS = String(import.meta.env.VITE_ENABLE_DEMO_ACCOUNTS || 'false').toLowerCase() === 'true'
