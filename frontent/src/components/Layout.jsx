import { useEffect, useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { notificationApi } from '../api/client'

const clientLinks = [
  ['/', 'Overview', '⌂'],
  ['/chat', 'AI Chat', '✦'],
  ['/tickets', 'My Tickets', '▣']
]

const staffLinks = [
  ['/staff', 'Ticket Queue', '▣']
]

const adminLinks = [
  ['/analytics', 'Analytics', '◫'],
  ['/knowledge', 'Knowledge Base', '◆'],
  ['/users', 'User Management', '◎']
]

export default function Layout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [notifications, setNotifications] = useState([])
  const [showNotifications, setShowNotifications] = useState(false)

  const loadNotifications = () => notificationApi.mine().then(setNotifications).catch(() => {})
  useEffect(() => {
    loadNotifications()
    const timer = window.setInterval(loadNotifications, 30000)
    return () => window.clearInterval(timer)
  }, [])

  const unreadCount = notifications.filter((item) => !item.read).length
  const markRead = async (item) => {
    if (!item.read) {
      const updated = await notificationApi.markRead(item.id)
      setNotifications((current) => current.map((entry) => entry.id === item.id ? updated : entry))
    }
    if (item.ticketId) navigate(`/tickets/${item.ticketId}`)
    setShowNotifications(false)
  }

  const links = user.role === 'CLIENT'
    ? clientLinks
    : [...staffLinks, ...(user.role === 'ADMIN' ? adminLinks : [['/knowledge', 'Knowledge Base', '◆']])]

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink to={user.role === 'CLIENT' ? '/' : '/staff'} className="brand">
          <span className="brand-mark">S</span>
          <span><strong>SupportFlow</strong><small>AI Automation</small></span>
        </NavLink>

        <nav className="nav-list" aria-label="Primary navigation">
          {links.map(([to, label, icon]) => (
            <NavLink key={to} to={to} end={to === '/'} className={({ isActive }) => isActive ? 'nav-item active' : 'nav-item'}>
              <span aria-hidden="true">{icon}</span>{label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-user">
          <div className="avatar">{user.name.charAt(0).toUpperCase()}</div>
          <div className="sidebar-user-copy">
            <strong>{user.name}</strong>
            <small>{user.role.replace('_', ' ')}</small>
          </div>
          <button className="icon-button" onClick={handleLogout} title="Log out" aria-label="Log out">↗</button>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="eyebrow">AI-Based Customer Support Automation System</p>
          </div>
          <div className="topbar-actions">
            <div className="notification-wrap">
              <button className="notification-button" onClick={() => setShowNotifications((current) => !current)}
                aria-label="Notifications" title="Notifications">
                ◉
                {unreadCount > 0 && <span>{unreadCount}</span>}
              </button>
              {showNotifications && (
                <div className="notification-popover">
                  <div className="notification-popover-head">
                    <strong>Notifications</strong>
                    <button className="text-link" onClick={loadNotifications}>Refresh</button>
                  </div>
                  {notifications.length === 0 ? (
                    <p className="muted">No notifications yet.</p>
                  ) : notifications.slice(0, 8).map((item) => (
                    <button key={item.id} className={item.read ? 'notification-item' : 'notification-item unread'}
                      onClick={() => markRead(item)}>
                      <span>{item.message}</span>
                      <small>{new Date(item.createdAt).toLocaleString()}</small>
                    </button>
                  ))}
                </div>
              )}
            </div>
            <div className="system-status"><span /> System operational</div>
          </div>
        </header>
        <div className="page-container"><Outlet /></div>
      </main>
    </div>
  )
}
