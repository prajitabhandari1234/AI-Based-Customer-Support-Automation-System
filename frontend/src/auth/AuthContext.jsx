import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { AUTH_EXPIRED_EVENT, authApi } from '../api/client'

const AuthContext = createContext(null)

/** Reads the cached user safely and removes corrupted session data. */
function storedUser() {
  try {
    const raw = localStorage.getItem('support_user')
    return raw ? JSON.parse(raw) : null
  } catch {
    localStorage.removeItem('support_user')
    return null
  }
}

/**
 * Stores the authenticated user and exposes login, registration and logout actions.
 * It also refreshes an existing token session when the application is reloaded.
 *
 * @param {{children: React.ReactNode}} props Provider properties.
 * @returns {JSX.Element} Authentication context provider.
 */
export function AuthProvider({ children }) {
  const [user, setUser] = useState(storedUser)
  const [loading, setLoading] = useState(Boolean(localStorage.getItem('support_token')))

  const logout = useCallback(() => {
    localStorage.removeItem('support_token')
    localStorage.removeItem('support_user')
    setUser(null)
  }, [])

  // Refresh the profile on page reload so role and account changes are reflected immediately.
  useEffect(() => {
    const token = localStorage.getItem('support_token')
    if (!token) {
      setLoading(false)
      return
    }

    authApi.me()
      .then((profile) => {
        setUser(profile)
        localStorage.setItem('support_user', JSON.stringify(profile))
      })
      .catch(logout)
      .finally(() => setLoading(false))
  }, [logout])

  // Any authenticated request returning 401 clears the stale local session.
  useEffect(() => {
    window.addEventListener(AUTH_EXPIRED_EVENT, logout)
    return () => window.removeEventListener(AUTH_EXPIRED_EVENT, logout)
  }, [logout])

  const saveSession = useCallback((response) => {
    if (!response?.token || !response?.user) throw new Error('Invalid login response from server')
    localStorage.setItem('support_token', response.token)
    localStorage.setItem('support_user', JSON.stringify(response.user))
    setUser(response.user)
  }, [])

  const login = useCallback(async (credentials) => {
    const response = await authApi.login(credentials)
    saveSession(response)
    return response.user
  }, [saveSession])

  const register = useCallback(async (details) => {
    const response = await authApi.register(details)
    saveSession(response)
    return response.user
  }, [saveSession])

  const value = useMemo(
    () => ({ user, loading, login, register, logout, setUser }),
    [user, loading, login, register, logout]
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

/**
 * Reads the current authentication context for pages and protected components.
 *
 * @returns {Object} Authentication state and session actions.
 */
export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
