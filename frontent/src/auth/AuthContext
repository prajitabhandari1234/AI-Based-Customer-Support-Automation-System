import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const raw = localStorage.getItem('support_user')
    return raw ? JSON.parse(raw) : null
  })
  const [loading, setLoading] = useState(Boolean(localStorage.getItem('support_token')))

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
      .catch(() => logout())
      .finally(() => setLoading(false))
  }, [])

  const saveSession = (response) => {
    localStorage.setItem('support_token', response.token)
    localStorage.setItem('support_user', JSON.stringify(response.user))
    setUser(response.user)
  }

  const login = async (credentials) => {
    const response = await authApi.login(credentials)
    saveSession(response)
    return response.user
  }

  const register = async (details) => {
    const response = await authApi.register(details)
    saveSession(response)
    return response.user
  }

  const logout = () => {
    localStorage.removeItem('support_token')
    localStorage.removeItem('support_user')
    setUser(null)
  }

  const value = useMemo(() => ({ user, loading, login, register, logout, setUser }), [user, loading])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error('useAuth must be used inside AuthProvider')
  return context
}
