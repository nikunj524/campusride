import { BrowserRouter, Route, Routes, useLocation } from 'react-router-dom'
import { AuthProvider } from './context/AuthProvider'
import Footer from './components/Footer'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import StudentLayout from './components/StudentLayout'
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
  const isStudentWorkspace = ['/dashboard', '/profile', '/rides/find', '/rides/create', '/rides/my'].some(path => location.pathname.startsWith(path)) || location.pathname.match(/^\/rides\/\d+$/)

  return (
    <div className="app-shell">
      {!isStudentWorkspace && <Navbar />}
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
              <Route path="/rides/find" element={<FindRidesPage />} />
              <Route path="/rides/create" element={<CreateRidePage />} />
              <Route path="/rides/my" element={<MyRidesPage />} />
              <Route path="/rides/:rideId" element={<RideDetailsPage />} />
            </Route>
          </Route>
          <Route path="*" element={<LandingPage />} />
        </Routes>
      </main>
      {!isStudentWorkspace && <Footer />}
    </div>
  )
}

export default App
