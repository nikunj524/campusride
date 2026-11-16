import { Navigate, Outlet, useLocation } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

const dashboardByRole = {
  STUDENT: '/dashboard',
  DRIVER: '/driver/dashboard',
  ADMIN: '/admin/dashboard',
}

function ProtectedRoute({ allowedRoles }) {
  const { isAuthenticated, user } = useAuth()
  const location = useLocation()

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (allowedRoles && !allowedRoles.includes(user?.role)) {
    return <Navigate to={dashboardByRole[user?.role] || '/login'} replace />
  }

  return <Outlet />
}

export default ProtectedRoute
