import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom'
import { useContext } from 'react'
import { AuthProvider, AuthContext } from './context/AuthContext'
import ProtectedRoute from './components/ProtectedRoute'
import Navbar from './components/Navbar'

import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ComplainantDashboard from './pages/ComplainantDashboard'
import MyComplaintsPage from './pages/MyComplaintsPage'
import NewComplaintPage from './pages/NewComplaintPage'
import ComplaintDetailPage from './pages/ComplaintDetailPage'
import NotificationsPage from './pages/NotificationsPage'
import AdminDashboardPage from './pages/AdminDashboardPage'
import NotFoundPage from './pages/NotFoundPage'
import ForbiddenPage from './pages/ForbiddenPage'

const AUTH_ROUTES = ['/login', '/register']

function AppLayout({ children }) {
  const location = useLocation()
  const hideNav = AUTH_ROUTES.includes(location.pathname)
  return (
    <>
      {!hideNav && <Navbar />}
      {children}
    </>
  )
}

/**
 * Smart home redirect based on role:
 *   COMPLAINANT -> /dashboard
 *   STAFF       -> /dashboard  (sees assigned complaints there)
 *   ADMIN       -> /admin
 */
function HomeRedirect() {
  const { user } = useContext(AuthContext)
  if (!user) return <Navigate to="/login" replace />
  if (user.role === 'ADMIN') return <Navigate to="/admin" replace />
  return <Navigate to="/dashboard" replace />
}

function AppRoutes() {
  const { user, loading } = useContext(AuthContext)

  if (loading) {
    return (
      <div style={{
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        height: '100vh', fontFamily: "'DM Sans', sans-serif", color: '#888',
        fontSize: '1rem'
      }}>
        Loading...
      </div>
    )
  }

  return (
    <AppLayout>
      <Routes>
        {/* Public auth routes */}
        <Route path="/login"    element={user ? <HomeRedirect /> : <LoginPage />} />
        <Route path="/register" element={user ? <HomeRedirect /> : <RegisterPage />} />

        {/* Dashboard — COMPLAINANT and STAFF see the complaint dashboard */}
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute requiredRoles={['COMPLAINANT', 'STAFF']}>
              <ComplainantDashboard />
            </ProtectedRoute>
          }
        />

        {/* My Complaints list — COMPLAINANT only (STAFF have assigned complaints in admin) */}
        <Route
          path="/my-complaints"
          element={
            <ProtectedRoute requiredRoles={['COMPLAINANT', 'STAFF']}>
              <MyComplaintsPage />
            </ProtectedRoute>
          }
        />

        {/* File new complaint — COMPLAINANT only */}
        <Route
          path="/new-complaint"
          element={
            <ProtectedRoute requiredRoles={['COMPLAINANT']}>
              <NewComplaintPage />
            </ProtectedRoute>
          }
        />

        {/* Complaint detail — all roles */}
        <Route
          path="/complaint/:id"
          element={
            <ProtectedRoute>
              <ComplaintDetailPage />
            </ProtectedRoute>
          }
        />

        {/* Notifications — all roles */}
        <Route
          path="/notifications"
          element={
            <ProtectedRoute>
              <NotificationsPage />
            </ProtectedRoute>
          }
        />

        {/* Admin panel — ADMIN and STAFF */}
        <Route
          path="/admin"
          element={
            <ProtectedRoute requiredRoles={['ADMIN', 'STAFF']}>
              <AdminDashboardPage />
            </ProtectedRoute>
          }
        />

        {/* Root: smart redirect based on role */}
        <Route path="/" element={<HomeRedirect />} />

        {/* Error pages */}
        <Route path="/403" element={<ForbiddenPage />} />
        <Route path="/404" element={<NotFoundPage />} />
        <Route path="*"    element={<Navigate to="/404" replace />} />
      </Routes>
    </AppLayout>
  )
}

function App() {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </Router>
  )
}

export default App
