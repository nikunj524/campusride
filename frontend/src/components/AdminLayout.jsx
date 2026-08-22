import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  Bell,
  Bike,
  CarFront,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  UserRound,
  UsersRound,
  X,
} from 'lucide-react'
import useAuth from '../hooks/useAuth'

const navigation = [
  { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
  { label: 'Driver Requests', to: '/admin/driver-requests', icon: ClipboardList },
]

const inactiveNavigation = [
  { label: 'Users', icon: UsersRound },
  { label: 'Vehicles', icon: CarFront },
  { label: 'Profile', icon: UserRound },
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
            <input disabled placeholder="Search CampusRide..." />
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
      <div className="sidebar-brand"><Bike size={25} strokeWidth={2.7} /><span>CampusRide Admin</span><button className="sidebar-close" aria-label="Close navigation" onClick={onNavigate} type="button"><X size={20} /></button></div>
      <nav className="sidebar-nav" aria-label="Admin navigation">
        {navigation.map(({ label, to, icon: Icon }) => (
          <NavLink className={({ isActive }) => `sidebar-link ${isActive ? 'is-active' : ''}`} key={label} onClick={onNavigate} to={to}><Icon size={18} />{label}</NavLink>
        ))}
        {inactiveNavigation.map(({ label, icon: Icon }) => (
          <button className="sidebar-link sidebar-placeholder" key={label} title={`${label} will be available soon`} type="button"><Icon size={18} />{label}</button>
        ))}
      </nav>
      <div className="sidebar-divider" />
      <div className="sidebar-bottom">
        <button className="sidebar-link" onClick={handleLogout} type="button"><LogOut size={18} />Logout</button>
      </div>
    </aside>
  )
}

function getInitials(user) {
  return `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase() || 'CR'
}

export default AdminLayout
