import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import ProtectedRoute from './components/ProtectedRoute'
import { useAuth } from './auth/AuthContext'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import CustomerDashboard from './pages/CustomerDashboard'
import ChatPage from './pages/ChatPage'
import TicketsPage from './pages/TicketsPage'
import TicketDetailPage from './pages/TicketDetailPage'
import StaffDashboard from './pages/StaffDashboard'
import AnalyticsPage from './pages/AnalyticsPage'
import KnowledgeBasePage from './pages/KnowledgeBasePage'
import UserManagementPage from './pages/UserManagementPage'
import NotFoundPage from './pages/NotFoundPage'

function HomeRoute() {
  const { user } = useAuth()
  return user?.role === 'CLIENT' ? <CustomerDashboard /> : <Navigate to="/staff" replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
        <Route index element={<HomeRoute />} />
        <Route path="chat" element={<ProtectedRoute roles={['CLIENT']}><ChatPage /></ProtectedRoute>} />
        <Route path="tickets" element={<ProtectedRoute roles={['CLIENT']}><TicketsPage /></ProtectedRoute>} />
        <Route path="tickets/:id" element={<TicketDetailPage />} />
        <Route path="staff" element={<ProtectedRoute roles={['AGENT', 'ADMIN']}><StaffDashboard /></ProtectedRoute>} />
        <Route path="analytics" element={<ProtectedRoute roles={['ADMIN']}><AnalyticsPage /></ProtectedRoute>} />
        <Route path="knowledge" element={<ProtectedRoute roles={['AGENT', 'ADMIN']}><KnowledgeBasePage /></ProtectedRoute>} />
        <Route path="users" element={<ProtectedRoute roles={['ADMIN']}><UserManagementPage /></ProtectedRoute>} />
        <Route path="home" element={<HomeRoute />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
