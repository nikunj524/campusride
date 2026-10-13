import { Link } from 'react-router-dom'
import { ArrowRight, CalendarDays, CarFront, MapPin, UserRound, Car } from 'lucide-react'
import useAuth from '../hooks/useAuth'

const studentQuickActions = [
  { title: 'Find a ride', copy: 'Search for rides going to your campus.', icon: MapPin, tone: 'blue', action: 'Find rides', to: '/rides/find' },
  { title: 'My bookings', copy: 'View and manage your upcoming and past bookings.', icon: CalendarDays, tone: 'gold', action: 'View bookings', to: '/rides/my' },
  { title: 'Profile', copy: 'Keep your CampusRide account details up to date.', icon: UserRound, tone: 'violet', action: 'View profile', to: '/profile' },
]

const driverQuickActions = [
  { title: 'Create Ride', copy: 'Offer a ride to fellow students.', icon: Car, tone: 'blue', action: 'Create ride', to: '/driver/rides/create' },
  { title: 'My Rides', copy: 'View and manage your offered rides.', icon: CalendarDays, tone: 'gold', action: 'View rides', to: '/driver/rides/my' },
  { title: 'Profile', copy: 'Keep your driver account details up to date.', icon: UserRound, tone: 'violet', action: 'View profile', to: '/driver/profile' },
]

function DashboardPage() {
  const { user } = useAuth()
  const isDriver = user?.role === 'DRIVER'
  const firstName = user?.firstName || (isDriver ? 'Driver' : 'Student')
  const roleLabel = isDriver ? 'Driver' : 'Student'
  const quickActions = isDriver ? driverQuickActions : studentQuickActions

  return (
    <section className="dashboard-page">
      <div className="dashboard-welcome">
        <div><p className="section-kicker">{roleLabel} dashboard</p><h1>Welcome back, {firstName}!</h1><p>Here is what is happening with your campus travel.</p></div>
        <Link className="button button-primary" to={isDriver ? '/driver/profile' : '/profile'}>View profile <ArrowRight size={16} /></Link>
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
          <Link className="quick-action-link" to={to}>{action} <ArrowRight size={14} /></Link>
        </article>)}
      </div>
      {!isDriver && (
        <div className="dashboard-empty-grid">
          <Link to="/rides/find" className="empty-state-card"><div className="empty-icon"><CarFront size={25} /></div><div><h2>Find available rides.</h2><p>Browse and book rides offered by fellow students going to your destination.</p></div><span className="empty-action">Find a Ride <ArrowRight size={15} /></span></Link>
          <Link to="/rides/my" className="empty-state-card"><div className="empty-icon"><CalendarDays size={24} /></div><div><h2>Manage your bookings.</h2><p>View your upcoming trips, track ride status, and manage your travel plans.</p></div><span className="empty-action">My Bookings <ArrowRight size={15} /></span></Link>
        </div>
      )}
      {isDriver && (
        <div className="dashboard-empty-grid">
          <Link to="/driver/rides/create" className="empty-state-card"><div className="empty-icon"><Car size={25} /></div><div><h2>Create a ride.</h2><p>Offer rides to fellow students and earn while helping others.</p></div><span className="empty-action">Create Ride <ArrowRight size={15} /></span></Link>
          <Link to="/driver/rides/my" className="empty-state-card"><div className="empty-icon"><CalendarDays size={24} /></div><div><h2>Manage your rides.</h2><p>View your offered rides, track bookings, and manage passengers.</p></div><span className="empty-action">My Rides <ArrowRight size={15} /></span></Link>
        </div>
      )}
    </section>
  )
}

function OverviewCard({ label, value }) { return <article className="overview-card"><span>{label}</span><strong>{value}</strong></article> }

export default DashboardPage
