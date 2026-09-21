import { useCallback, useEffect, useRef, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { notificationApi, systemApi } from '../api/client'
import { useAuth } from '../auth/AuthContext'
import { formatDateTime } from '../utils/format'

const clientLinks = [
  ['/', 'Overview', '⌂'],
  ['/chat', 'AI Chat', '✦'],
  ['/tickets', 'My Tickets', '▣'],
  ['/tickets/new', 'New Ticket', '+']
]

const staffLinks = [
  ['/staff', 'Ticket Queue', '▣'],
  ['/knowledge', 'Knowledge Base', '◆']
]

const adminLinks = [
  ['/analytics', 'Analytics', '◫'],
  ['/users', 'User Management', '◎'],
  ['/logs', 'System Logs', '≡']
]

/**
 * Renders the authenticated application shell, navigation, notifications and health status.
 * Navigation links are selected from the current user's role without changing page logic.
 *
 * @returns {JSX.Element} Shared authenticated layout.
 */
export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const notificationRef = useRef(null)
  const [notifications, setNotifications] = useState([])
  const [showNotifications, setShowNotifications] = useState(false)
  const [backendOnline, setBackendOnline] = useState(null)

  const loadNotifications = useCallback(() => {
    return notificationApi.mine(user.role).then(setNotifications).catch(() => {})
  }, [user.role])

  const checkHealth = useCallback(() => {
    systemApi.health()
      .then((result) => setBackendOnline(result?.status === 'UP'))
      .catch(() => setBackendOnline(false))
  }, [])

  useEffect(() => {
    loadNotifications()
    checkHealth()
    const notificationTimer = window.setInterval(loadNotifications, 30000)
    const healthTimer = window.setInterval(checkHealth, 60000)
    return () => {
      window.clearInterval(notificationTimer)
      window.clearInterval(healthTimer)
    }
  }, [loadNotifications, checkHealth])

  const closeNotifications = useCallback(async () => {
    setShowNotifications(false)
    try {
      const updated = await notificationApi.markAllRead(user.role, notifications)
      setNotifications(updated)
    } catch {
      // Notification read failures should not block navigation or close the panel.
    }
  }, [notifications, user.role])

  useEffect(() => {
    if (!showNotifications) return undefined

    const handlePointer = (event) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target)) closeNotifications()
    }
    const handleKey = (event) => {
      if (event.key === 'Escape') closeNotifications()
    }

    document.addEventListener('mousedown', handlePointer)
    document.addEventListener('keydown', handleKey)
    return () => {
      document.removeEventListener('mousedown', handlePointer)
      document.removeEventListener('keydown', handleKey)
    }
  }, [showNotifications, closeNotifications])

  const unreadCount = notifications.filter((item) => !item.read).length

  const toggleNotifications = async () => {
    if (showNotifications) {
      await closeNotifications()
      return
    }
    await loadNotifications()
    setShowNotifications(true)
  }

  const openNotification = async (item) => {
    let selected = item
    if (!item.read && item.id) {
      try {
        selected = await notificationApi.markRead(user.role, item.id)
        setNotifications((current) => current.map((entry) => entry.id === item.id ? selected : entry))
      } catch {
        selected = item
      }
    }

    await closeNotifications()
    if (selected.ticketId) navigate(`/tickets/${selected.ticketId}`)
  }

  const links = user.role === 'CLIENT'
    ? clientLinks
    : [...staffLinks, ...(user.role === 'ADMIN' ? adminLinks : [])]

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink to={user.role === 'CLIENT' ? '/' : '/staff'} className="brand">
          <span className="sidebar-brand-icon" aria-hidden="true">✦</span>

          <span>
            <strong>AI Customer Support</strong>
            <small>Automation System</small>
          </span>
        </NavLink>

        <nav className="nav-list" aria-label="Primary navigation">
          {links.map(([to, label, icon]) => (
            <NavLink key={to} to={to} end={to === '/'} className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
              <span aria-hidden="true">{icon}</span>{label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-user">
          <div className="avatar">{user.name?.charAt(0).toUpperCase() || '?'}</div>
          <div className="sidebar-user-copy">
            <strong>{user.name}</strong>
            <small>{String(user.role).replaceAll('_', ' ')}</small>
          </div>
          <button className="icon-button" onClick={handleLogout} title="Log out" aria-label="Log out">↗</button>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div><p className="eyebrow">AI-Based Customer Support Automation System</p></div>
          <div className="topbar-actions">
            <div className="notification-wrap" ref={notificationRef}>
              <button className="notification-button" onClick={toggleNotifications}
                aria-label="Notifications" title="Notifications" aria-expanded={showNotifications}>
                ◉
                {unreadCount > 0 && <span>{unreadCount}</span>}
              </button>
              {showNotifications && (
                <div className="notification-popover">
                  <div className="notification-popover-head">
                    <strong>Notifications</strong>
                    <div className="notification-head-actions">
                      {unreadCount > 0 && <button className="text-link" onClick={closeNotifications}>Mark all read</button>}
                      <button className="text-link" onClick={loadNotifications}>Refresh</button>
                    </div>
                  </div>
                  {notifications.length === 0 ? (
                    <p className="muted notification-empty">No notifications yet.</p>
                  ) : notifications.slice(0, 12).map((item) => (
                    <button key={item.id} className={item.read ? 'notification-item' : 'notification-item unread'}
                      onClick={() => openNotification(item)}>
                      <span>{item.message}</span>
                      <small>{formatDateTime(item.createdAt)}</small>
                    </button>
                  ))}
                </div>
              )}
            </div>
            <div className={`system-status ${backendOnline === false ? 'offline' : ''}`}>
              <span /> {backendOnline === false ? 'System Offline' : backendOnline === true ? 'System Online' : 'Checking system'}
            </div>
            <button className="topbar-logout" onClick={handleLogout}>Sign out</button>
          </div>
        </header>
        <div className="page-container"><Outlet /></div>
      </main>
    </div>
  )
}
