import { useEffect, useState } from 'react'
import { NavLink, Outlet, useNavigate, useLocation } from 'react-router-dom'
import {
  Bike,
  CalendarDays,
  CircleHelp,
  Clock,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  Settings,
  Shuffle,
  UserRound,
  X,
  Bell,
} from 'lucide-react'
import useAuth from '../hooks/useAuth'
import ConfirmDialog from './ConfirmDialog'

const navigation = [
  { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard },
  { label: 'Find Rides', to: '/rides/find', icon: Search },
  { label: 'My Bookings', to: '/rides/my', icon: CalendarDays },
  { label: 'Profile', to: '/profile', icon: UserRound },
]

function StudentLayout() {
  const [isMenuOpen, setIsMenuOpen] = useState(false)
  const { user } = useAuth()

  return (
    <div className="student-app">
      <StudentSidebar isOpen={isMenuOpen} onNavigate={() => setIsMenuOpen(false)} />
      {isMenuOpen && <button className="sidebar-backdrop" aria-label="Close navigation" onClick={() => setIsMenuOpen(false)} type="button" />}
      <div className="student-workspace">
        <header className="student-topbar">
          <button className="mobile-menu-button" aria-label="Open navigation" onClick={() => setIsMenuOpen(true)} type="button"><Menu size={21} /></button>
          <label className="dashboard-search" aria-label="Ride search">
            <Search size={18} aria-hidden="true" />
            <input disabled placeholder="Search rides (from, to, campus)..." />
          </label>
          <div className="topbar-account">
            <button className="icon-button" aria-label="Notifications" type="button"><Bell size={19} /></button>
            <div className="account-avatar" aria-hidden="true">{getInitials(user)}</div>
            <div className="account-copy"><strong>{user?.firstName} {user?.lastName}</strong><span>Student</span></div>
          </div>
        </header>
        <main className="student-main"><Outlet /></main>
      </div>
    </div>
  )
}

function StudentSidebar({ isOpen, onNavigate }) {
  const { logout, switchWorkspace, user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [isSwitching, setIsSwitching] = useState(false)
  const [driverRequestStatus, setDriverRequestStatus] = useState(null)
  const [showConfirmDialog, setShowConfirmDialog] = useState(false)

  useEffect(() => {
    // Check driver request status
    const checkDriverRequest = async () => {
      try {
        const response = await fetch('http://localhost:8082/api/driver-requests/my-status', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('campusride_token')}`
          }
        })
        const data = await response.json()
        setDriverRequestStatus(data?.status || null)
      } catch (error) {
        setDriverRequestStatus(null)
      }
    }
    
    if (user?.role === 'STUDENT') {
      checkDriverRequest()
      
      // Re-check when page becomes visible (user switches tabs/windows)
      const handleVisibilityChange = () => {
        if (!document.hidden) {
          checkDriverRequest()
        }
      }
      
      document.addEventListener('visibilitychange', handleVisibilityChange)
      
      // Also check periodically (every 30 seconds)
      const interval = setInterval(checkDriverRequest, 30000)
      
      return () => {
        clearInterval(interval)
        document.removeEventListener('visibilitychange', handleVisibilityChange)
      }
    }
  }, [user])

  // Force refresh when navigating from BecomeDriverPage
  useEffect(() => {
    if (location.state?.refreshDriverStatus) {
      const checkStatus = async () => {
        try {
          const response = await fetch('http://localhost:8082/api/driver-requests/my-status', {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('campusride_token')}`
            }
          })
          const data = await response.json()
          setDriverRequestStatus(data?.status || null)
        } catch (error) {
          setDriverRequestStatus(null)
        }
      }
      checkStatus()
      // Clear the state to avoid re-triggering
      window.history.replaceState({}, document.title)
    }
  }, [location])

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const handleSwitchToDriver = async () => {
    if (driverRequestStatus === 'APPROVED') {
      // Show custom confirmation dialog
      setShowConfirmDialog(true)
    } else if (driverRequestStatus === 'PENDING') {
      alert('Your driver request is pending admin approval. Please wait for confirmation.')
    } else if (driverRequestStatus === 'REJECTED') {
      alert('Your previous driver request was rejected. Please contact support for more information.')
    } else {
      // No request exists, show become driver modal/page
      navigate('/become-driver')
    }
  }

  const confirmSwitchToDriver = async () => {
    try {
      setIsSwitching(true)
      await switchWorkspace('DRIVER')
      navigate('/driver/dashboard')
    } catch (error) {
      console.error('Failed to switch workspace:', error)
      alert('Failed to switch to driver mode. Please try again.')
    } finally {
      setIsSwitching(false)
    }
  }

  const getButtonText = () => {
    if (isSwitching) return 'Switching...'
    if (driverRequestStatus === 'PENDING') return 'Request Pending'
    if (driverRequestStatus === 'APPROVED') return 'Switch to Driver'
    if (driverRequestStatus === 'REJECTED') return 'Request Rejected'
    return 'Become a Driver'
  }

  const getButtonIcon = () => {
    if (driverRequestStatus === 'PENDING') return Clock
    if (driverRequestStatus === 'APPROVED') return Shuffle
    if (driverRequestStatus === 'REJECTED') return X
    return UserRound
  }

  const ButtonIcon = getButtonIcon()

  return (
    <aside className={`student-sidebar ${isOpen ? 'is-open' : ''}`}>
      <div className="sidebar-brand"><Bike size={25} strokeWidth={2.7} /><span>CampusRide</span><button className="sidebar-close" aria-label="Close navigation" onClick={onNavigate} type="button"><X size={20} /></button></div>
      <nav className="sidebar-nav" aria-label="Student navigation">
        {navigation.map(({ label, to, icon: Icon, disabled }) => disabled ? (
          <span className="sidebar-link is-disabled" key={label} title={`${label} will be available soon`}><Icon size={18} />{label}</span>
        ) : (
          <NavLink className={({ isActive }) => `sidebar-link ${isActive ? 'is-active' : ''}`} key={label} onClick={onNavigate} to={to}><Icon size={18} />{label}</NavLink>
        ))}
      </nav>
      <div className="sidebar-divider" />
      <button 
        className="sidebar-link" 
        onClick={handleSwitchToDriver}
        disabled={isSwitching || driverRequestStatus === 'REJECTED'}
        type="button"
        title={
          driverRequestStatus === 'PENDING' 
            ? 'Your request is pending approval' 
            : driverRequestStatus === 'REJECTED' 
            ? 'Your request was rejected. Contact support.' 
            : driverRequestStatus === 'APPROVED'
            ? 'Switch to driver mode'
            : 'Apply to become a driver'
        }
      >
        <ButtonIcon size={18} />
        {getButtonText()}
      </button>
      <div className="sidebar-bottom">
        <button className="sidebar-link sidebar-placeholder" title="Settings will be available soon" type="button"><Settings size={18} />Settings</button>
        <button className="sidebar-link" onClick={handleLogout} type="button"><LogOut size={18} />Logout</button>
        <div className="sidebar-help"><CircleHelp size={19} /><div><strong>Need help?</strong><span>Contact support</span></div></div>
      </div>

      {/* Custom Confirmation Dialog */}
      <ConfirmDialog
        isOpen={showConfirmDialog}
        onClose={() => setShowConfirmDialog(false)}
        onConfirm={confirmSwitchToDriver}
        title="Switch to Driver Mode?"
        message="You will be redirected to the driver dashboard where you can manage your rides and passengers. Are you sure you want to continue?"
        confirmText="Switch to Driver"
        cancelText="Cancel"
        variant="primary"
      />
    </aside>
  )
}

function getInitials(user) {
  return `${user?.firstName?.[0] || ''}${user?.lastName?.[0] || ''}`.toUpperCase() || 'CR'
}

export default StudentLayout
