import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import { AuthProvider } from './context/AuthProvider'
import AdminLayout from './components/AdminLayout'
import Footer from './components/Footer'
import DriverLayout from './components/DriverLayout'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import StudentLayout from './components/StudentLayout'
import DashboardPage from './pages/DashboardPage'
import AdminDriverRequestsPage from './pages/AdminDriverRequestsPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import ProfilePage from './pages/ProfilePage'
import RegisterPage from './pages/RegisterPage'
import VehiclePage from './pages/VehiclePage'
import useAuth from './hooks/useAuth'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </BrowserRouter>
  )
}

function AppContent() {
  const location = useLocation()
  const { isDriverWorkspace, user } = useAuth()
  const WorkspaceLayout = user?.role === 'ADMIN'
    ? AdminLayout
    : isDriverWorkspace ? DriverLayout : StudentLayout
  const isWorkspace = ['/dashboard', '/profile', '/vehicles', '/admin/driver-requests'].includes(location.pathname)

  return (
    <div className="app-shell">
      {!isWorkspace && <Navbar />}
      <main className="page-content">
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<WorkspaceLayout />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/admin/driver-requests" element={<AdminDriverRequestsPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/vehicles" element={<VehiclePage />} />
            </Route>
          </Route>
          <Route path="*" element={<LandingPage />} />
        </Routes>
      </main>
      {!isWorkspace && <Footer />}
    </div>
  )
}

export default App
