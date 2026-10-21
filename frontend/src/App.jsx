import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import { AuthProvider } from './context/AuthProvider'
import AdminLayout from './components/AdminLayout'
import Footer from './components/Footer'
import DriverLayout from './components/DriverLayout'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import StudentLayout from './components/StudentLayout'
import AdminDashboard from './pages/AdminDashboard'
import AdminDriverRequests from './pages/AdminDriverRequests'
import AdminUsers from './pages/AdminUsers'
import AdminSettings from './pages/AdminSettings'
import AdminHelp from './pages/AdminHelp'
import BecomeDriverPage from './pages/BecomeDriverPage'
import CreateRidePage from './pages/CreateRidePage'
import DashboardPage from './pages/DashboardPage'
import FindRidesPage from './pages/FindRidesPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import LandingPage from './pages/LandingPage'
import LoginPage from './pages/LoginPage'
import MyRidesPage from './pages/MyRidesPage'
import ProfilePage from './pages/ProfilePage'
import RegisterPage from './pages/RegisterPage'
import RideDetailsPage from './pages/RideDetailsPage'
import VehiclePage from './pages/VehiclePage'
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
  const isStudentWorkspace = ['/dashboard', '/profile', '/become-driver', '/rides/find', '/rides/create', '/rides/my'].some(path => location.pathname.startsWith(path)) || location.pathname.match(/^\/rides\/\d+$/)
  const isDriverWorkspace = ['/driver/'].some(path => location.pathname.startsWith(path))
  const isAdminWorkspace = ['/admin/'].some(path => location.pathname.startsWith(path))

  return (
    <div className="app-shell">
      {!isStudentWorkspace && !isDriverWorkspace && !isAdminWorkspace && <Navbar />}
      <main className="page-content">
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route element={<ProtectedRoute />}>
            <Route element={<StudentLayout />}>
              <Route path="/dashboard" element={<DashboardPage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route path="/become-driver" element={<BecomeDriverPage />} />
              <Route path="/rides/find" element={<FindRidesPage />} />
              <Route path="/rides/create" element={<CreateRidePage />} />
              <Route path="/rides/my" element={<MyRidesPage />} />
              <Route path="/rides/:rideId" element={<RideDetailsPage />} />
            </Route>
            <Route element={<DriverLayout />}>
              <Route path="/driver/dashboard" element={<DashboardPage />} />
              <Route path="/driver/profile" element={<ProfilePage />} />
              <Route path="/driver/vehicles" element={<VehiclePage />} />
              <Route path="/driver/rides/create" element={<CreateRidePage />} />
              <Route path="/driver/rides/my" element={<MyRidesPage />} />
              <Route path="/driver/rides/:rideId" element={<RideDetailsPage />} />
            </Route>
            <Route element={<AdminLayout />}>
              <Route path="/admin/dashboard" element={<AdminDashboard />} />
              <Route path="/admin/driver-requests" element={<AdminDriverRequests />} />
              <Route path="/admin/users" element={<AdminUsers />} />
              <Route path="/admin/settings" element={<AdminSettings />} />
              <Route path="/admin/help" element={<AdminHelp />} />
              <Route path="/admin/profile" element={<ProfilePage />} />
            </Route>
          </Route>
          <Route path="*" element={<LandingPage />} />
        </Routes>
      </main>
      {!isStudentWorkspace && !isDriverWorkspace && !isAdminWorkspace && <Footer />}
    </div>
  )
}

export default App
