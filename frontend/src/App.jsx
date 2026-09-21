import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import AnalyticsPage from './pages/AnalyticsPage'
import ChatPage from './pages/ChatPage'
import CustomerDashboard from './pages/CustomerDashboard'
import KnowledgeBasePage from './pages/KnowledgeBasePage'
import LoginPage from './pages/LoginPage'
import NewTicketPage from './pages/NewTicketPage'
import NotFoundPage from './pages/NotFoundPage'
import RegisterPage from './pages/RegisterPage'
import StaffDashboard from './pages/StaffDashboard'
import SystemLogsPage from './pages/SystemLogsPage'
import TicketDetailPage from './pages/TicketDetailPage'
import TicketsPage from './pages/TicketsPage'
import UserManagementPage from './pages/UserManagementPage'

/**
 * Sends an authenticated user to the dashboard that matches their role.
 *
 * @returns {JSX.Element} The customer dashboard or a redirect to the staff area.
 */
function HomeRoute() {
  const { user } = useAuth()
  return user?.role === 'CLIENT' ? <CustomerDashboard /> : <Navigate to="/staff" replace />
}

/**
 * Defines the application routes and the role protection applied to each page.
 *
 * @returns {JSX.Element} The complete route tree for the frontend.
 */
export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<HomeRoute />} />
        <Route path="home" element={<HomeRoute />} />
        <Route path="chat" element={<ProtectedRoute roles={['CLIENT']}><ChatPage /></ProtectedRoute>} />
        <Route path="tickets" element={<ProtectedRoute roles={['CLIENT']}><TicketsPage /></ProtectedRoute>} />
        <Route path="tickets/new" element={<ProtectedRoute roles={['CLIENT']}><NewTicketPage /></ProtectedRoute>} />
        <Route path="tickets/:id" element={<TicketDetailPage />} />
        <Route path="staff" element={<ProtectedRoute roles={['AGENT', 'ADMIN']}><StaffDashboard /></ProtectedRoute>} />
        <Route path="analytics" element={<ProtectedRoute roles={['ADMIN']}><AnalyticsPage /></ProtectedRoute>} />
        <Route path="knowledge" element={<ProtectedRoute roles={['AGENT', 'ADMIN']}><KnowledgeBasePage /></ProtectedRoute>} />
        <Route path="users" element={<ProtectedRoute roles={['ADMIN']}><UserManagementPage /></ProtectedRoute>} />
        <Route path="logs" element={<ProtectedRoute roles={['ADMIN']}><SystemLogsPage /></ProtectedRoute>} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
