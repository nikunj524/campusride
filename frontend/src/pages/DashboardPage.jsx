import { Link } from 'react-router-dom'
import { ArrowRight, CalendarDays, CarFront, MapPin, UserRound } from 'lucide-react'
import useAuth from '../hooks/useAuth'

const studentQuickActions = [
  { title: 'Find a ride', copy: 'Search for rides going to your campus.', icon: MapPin, tone: 'blue', action: 'Find rides' },
  { title: 'My bookings', copy: 'Your upcoming and past bookings will appear here.', icon: CalendarDays, tone: 'gold', action: 'View bookings' },
  { title: 'Profile', copy: 'Keep your CampusRide account details up to date.', icon: UserRound, tone: 'violet', action: 'View profile', to: '/profile' },
]

const driverQuickActions = [
  { title: 'My vehicle', copy: 'Register and manage the vehicle you use for campus rides.', icon: CarFront, tone: 'blue', action: 'Manage vehicle', to: '/vehicles' },
  { title: 'Profile', copy: 'Update your account details and contact information.', icon: UserRound, tone: 'violet', action: 'View profile', to: '/profile' },
  { title: 'Find riders', copy: 'Ride listings will appear here once the ride flow is enabled.', icon: MapPin, tone: 'gold', action: 'Coming soon' },
]

function DashboardPage() {
  const { user, isDriverWorkspace } = useAuth()
  const isAdmin = user?.role === 'ADMIN'
  const isDriver = !isAdmin && isDriverWorkspace
  const roleLabel = isAdmin ? 'Admin' : isDriver ? 'Driver' : 'Student'
  const firstName = user?.firstName || roleLabel
  const quickActions = isDriver ? driverQuickActions : studentQuickActions

  return (
    <section className="dashboard-page">
      <div className="dashboard-welcome">
        <div><p className="section-kicker">{roleLabel} dashboard</p><h1>Welcome back, {firstName}!</h1><p>{isAdmin ? 'Manage CampusRide operations from your admin workspace.' : isDriver ? 'Manage your vehicle and keep your driver profile ready for campus trips.' : 'Here is what is happening with your campus travel.'}</p></div>
        <Link className="button button-primary" to="/profile">View profile <ArrowRight size={16} /></Link>
      </div>
      <div className="account-overview">
        <OverviewCard label="Signed in as" value={`${user?.firstName || ''} ${user?.lastName || ''}`.trim() || 'CampusRide member'} />
        <OverviewCard label="Account role" value={roleLabel} />
        <OverviewCard label="Account email" value={user?.email || 'Not available'} />
      </div>
      <div className="dashboard-section-heading"><h2>Quick actions</h2><p>Shortcuts for the tools you use most.</p></div>
      <div className="quick-actions-grid">
        {quickActions.map(({ title, copy, icon: Icon, tone, action, to }) => <article className={`quick-action-card quick-${tone}`} key={title}>
          <span className="quick-action-icon"><Icon size={23} /></span><h3>{title}</h3><p>{copy}</p>
          {to ? <Link className="quick-action-link" to={to}>{action} <ArrowRight size={14} /></Link> : <span className="quick-action-link is-coming">{action} <ArrowRight size={14} /></span>}
        </article>)}
      </div>
      <div className="dashboard-empty-grid">
        <article className="empty-state-card"><div className="empty-icon"><CarFront size={25} /></div><div><h2>No rides available yet.</h2><p>Ride listings will appear here once the Ride Service is available.</p></div><span className="empty-action">Find a Ride <ArrowRight size={15} /></span></article>
        <article className="empty-state-card"><div className="empty-icon"><CalendarDays size={24} /></div><div><h2>Your bookings will appear here.</h2><p>When booking becomes available, you will be able to manage your trips in one place.</p></div><span className="empty-action">My Bookings <ArrowRight size={15} /></span></article>
      </div>
    </section>
  )
}

function OverviewCard({ label, value }) { return <article className="overview-card"><span>{label}</span><strong>{value}</strong></article> }

export default DashboardPage
