import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Bell,
  Bike,
  CalendarDays,
  CarFront,
  CircleHelp,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  Settings,
  Shuffle,
  UserRound,
  X,
} from 'lucide-react'
import useAuth from '../hooks/useAuth'
import ConfirmDialog from './ConfirmDialog'

const navigation = [
  { label: 'Dashboard', to: '/driver/dashboard', icon: LayoutDashboard },
  { label: 'Create Ride', to: '/driver/rides/create', icon: Bike },
  { label: 'My Rides', to: '/driver/rides/my', icon: CalendarDays },
  { label: 'Vehicle', to: '/driver/vehicles', icon: CarFront },
  { label: 'Profile', to: '/driver/profile', icon: UserRound },
]

function DriverLayout() {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const { user } = useAuth()

  return (
    <div className="student-app">
      <DriverSidebar isOpen={isMenuOpen} onNavigate={() => setIsMenuOpen(false)} />
      {isMenuOpen && <button className="sidebar-backdrop" aria-label="Close navigation" onClick={() => setIsMenuOpen(false)} type="button" />}
      <div className="student-workspace">
        <header className="student-topbar">
          <button className="mobile-menu-button" aria-label="Open navigation" onClick={() => setIsMenuOpen(true)} type="button"><Menu size={21} /></button>
          <label className="dashboard-search" aria-label="Ride search">
            <Search size={18} aria-hidden="true" />
            <input disabled placeholder="Search rides, passengers, and routes..." />
          </label>
          <div className="topbar-account">
            <button className="icon-button" aria-label="Notifications" type="button"><Bell size={19} /></button>
            <div className="account-avatar" aria-hidden="true">{getInitials(user)}</div>
            <div className="account-copy"><strong>{user?.firstName} {user?.lastName}</strong><span>Driver</span></div>
          </div>
        </header>
        <main className="student-main"><Outlet /></main>
      </div>
    </div>
  )
}

function DriverSidebar({ isOpen, onNavigate }) {
  const { logout, switchWorkspace } = useAuth()
  const navigate = useNavigate()
  const [isSwitching, setIsSwitching] = useState(false)
  const [showConfirmDialog, setShowConfirmDialog] = useState(false)

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const handleSwitchToStudent = () => {
    setShowConfirmDialog(true)
  }

  const confirmSwitchToStudent = async () => {
    try {
      setIsSwitching(true)
      await switchWorkspace('STUDENT')
      navigate('/dashboard')
    } catch (error) {
      console.error('Failed to switch workspace:', error)
      alert('Failed to switch to student mode. Please try again.')
    } finally {
      setIsSwitching(false)
    }
  }

  return (
    <aside className={`student-sidebar ${isOpen ? 'is-open' : ''}`}>
      <div className="sidebar-brand">
        <Bike size={25} strokeWidth={2.7} />
        <span>CampusRide</span>
        <button className="sidebar-close" aria-label="Close navigation" onClick={onNavigate} type="button"><X size={20} /></button>
      </div>
      <nav className="sidebar-nav" aria-label="Driver navigation">
        {navigation.map(({ label, to, icon: Icon }) => (
          <NavLink
            className={({ isActive }) => `sidebar-link ${isActive ? 'is-active' : ''}`}
            key={label}
            onClick={onNavigate}
            to={to}
          >
            <Icon size={18} />
            {label}
          </NavLink>
        ))}
      </nav>
      <div className="sidebar-divider" />
      <button 
        className="sidebar-link" 
        onClick={handleSwitchToStudent}
        disabled={isSwitching}
        type="button"
      >
        <Shuffle size={18} />
        {isSwitching ? 'Switching...' : 'Switch to Student'}
      </button>
      <NavLink className={({ isActive }) => `sidebar-link ${isActive ? 'is-active' : ''}`} onClick={onNavigate} to="/driver/vehicles"><CarFront size={18} />Manage Vehicle</NavLink>
      <div className="sidebar-bottom">
        <button className="sidebar-link sidebar-placeholder" title="Settings will be available soon" type="button"><Settings size={18} />Settings</button>
        <button className="sidebar-link" onClick={handleLogout} type="button"><LogOut size={18} />Logout</button>
        <div className="sidebar-help"><CircleHelp size={19} /><div><strong>Need help?</strong><span>Contact support</span></div></div>
      </div>

      {/* Custom Confirmation Dialog */}
      <ConfirmDialog
        isOpen={showConfirmDialog}
        onClose={() => setShowConfirmDialog(false)}
        onConfirm={confirmSwitchToStudent}
        title="Switch to Student Mode?"
        message="You will be redirected to the student dashboard. Any unsaved changes on this page will be lost. Are you sure you want to continue?"
        confirmText="Switch to Student"
        cancelText="Cancel"
        variant="warning"
      />
    </aside>
  )
}

function getInitials(user) {
  return `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase() || 'CR'
}

export default DriverLayout
