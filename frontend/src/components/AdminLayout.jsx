import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Bell,
  Bike,
  CircleHelp,
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  Settings,
  UserRound,
  Users,
  X,
} from 'lucide-react'
import useAuth from '../hooks/useAuth'

const navigation = [
  { label: 'Dashboard', to: '/admin/dashboard', icon: LayoutDashboard },
  { label: 'Driver Requests', to: '/admin/driver-requests', icon: FileText },
  { label: 'Users', to: '/admin/users', icon: Users },
]

function AdminLayout() {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const { user } = useAuth()

  return (
    <div className="student-app">
      <AdminSidebar isOpen={isMenuOpen} onNavigate={() => setIsMenuOpen(false)} />
      {isMenuOpen && <button className="sidebar-backdrop" aria-label="Close navigation" onClick={() => setIsMenuOpen(false)} type="button" />}
      <div className="student-workspace">
        <header className="student-topbar">
          <button className="mobile-menu-button" aria-label="Open navigation" onClick={() => setIsMenuOpen(true)} type="button"><Menu size={21} /></button>
          <label className="dashboard-search" aria-label="Admin search">
            <Search size={18} aria-hidden="true" />
            <input disabled placeholder="Search users, requests, and settings..." />
          </label>
          <div className="topbar-account">
            <button className="icon-button" aria-label="Notifications" type="button"><Bell size={19} /></button>
            <div className="account-avatar" aria-hidden="true">{getInitials(user)}</div>
            <div className="account-copy"><strong>{user?.firstName} {user?.lastName}</strong><span>Admin</span></div>
          </div>
        </header>
        <main className="student-main"><Outlet /></main>
      </div>
    </div>
  )
}

function AdminSidebar({ isOpen, onNavigate }) {
  const { logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <aside className={`student-sidebar ${isOpen ? 'is-open' : ''}`}>
      <div className="sidebar-brand">
        <Bike size={25} strokeWidth={2.7} />
        <span>CampusRide</span>
        <button className="sidebar-close" aria-label="Close navigation" onClick={onNavigate} type="button"><X size={20} /></button>
      </div>
      <nav className="sidebar-nav" aria-label="Admin navigation">
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
      <NavLink 
        className={({ isActive }) => `sidebar-link ${isActive ? 'is-active' : ''}`} 
        onClick={onNavigate} 
        to="/admin/profile"
      >
        <UserRound size={18} />
        Profile
      </NavLink>
      <div className="sidebar-bottom">
        {/* <NavLink 
          className={({ isActive }) => `sidebar-link ${isActive ? 'is-active' : ''}`}
          onClick={onNavigate}
          to="/admin/settings"
        >
          <Settings size={18} />
          Settings
        </NavLink> */}
        <button className="sidebar-link" onClick={handleLogout} type="button"><LogOut size={18} />Logout</button>
        <NavLink 
          className="sidebar-help"
          onClick={onNavigate}
          to="/admin/help"
        >
          <CircleHelp size={19} />
          <div><strong>Need help?</strong><span>Admin Support</span></div>
        </NavLink>
      </div>
    </aside>
  )
}

function getInitials(user) {
  return `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase() || 'AD'
}

export default AdminLayout
