import { useContext } from 'react'
import { Navigate } from 'react-router-dom'
import { AuthContext } from '../context/AuthContext'

export default function ProtectedRoute({ children, requiredRoles }) {
  const { user, loading } = useContext(AuthContext)

  if (loading) {
    return <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100vh' }}>Loading...</div>
  }

  if (!user) {
    return <Navigate to="/login" replace />
  }

  if (requiredRoles && !requiredRoles.includes(user.role)) {
    return <Navigate to="/403" replace />
  }

  return children
}
