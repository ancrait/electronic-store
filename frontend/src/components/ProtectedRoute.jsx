import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function ProtectedRoute({ children, adminOnly = false }) {
  const { user, isAdmin } = useAuth()
  const location = useLocation()

  if (!user) {
    return <Navigate to="/login" state={{ from: location.pathname }} replace />
  }

  if (adminOnly && !isAdmin) {
    return (
      <div className="empty">
        <h2>Сторінка лише для адміністраторів</h2>
        <p>Увійдіть під обліковим записом з роллю ADMIN.</p>
      </div>
    )
  }

  return children
}
