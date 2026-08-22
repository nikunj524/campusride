import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Bell,
  Bike,
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

const navigation = [
  { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
  { label: 'Vehicle', to: '/vehicles', icon: CarFront },
  { label: 'Profile', to: '/profile', icon: UserRound },
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
  const { logout, switchToStudentWorkspace } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const handleSwitchToStudent = async () => {
    try {
      await switchToStudentWorkspace()
      navigate('/dashboard', { replace: true })
      onNavigate()
    } catch {
      return
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
      <button className="sidebar-link" onClick={handleSwitchToStudent} title="Switch to student workspace" type="button"><Shuffle size={18} />Switch to Student</button>
      <NavLink className={({ isActive }) => `sidebar-link ${isActive ? 'is-active' : ''}`} onClick={onNavigate} to="/vehicles"><CarFront size={18} />Manage Vehicle</NavLink>
      <div className="sidebar-bottom">
        <button className="sidebar-link sidebar-placeholder" title="Settings will be available soon" type="button"><Settings size={18} />Settings</button>
        <button className="sidebar-link" onClick={handleLogout} type="button"><LogOut size={18} />Logout</button>
        <div className="sidebar-help"><CircleHelp size={19} /><div><strong>Need help?</strong><span>Contact support</span></div></div>
      </div>
    </aside>
  )
}

function getInitials(user) {
  return `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase() || 'CR'
}

export default DriverLayout
