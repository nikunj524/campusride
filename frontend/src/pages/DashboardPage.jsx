import { Link } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

function DashboardPage() {
  const { user } = useAuth()
  return (
    <section className="container dashboard">
      <div className="dashboard-header"><div><h1>Welcome, {user.firstName}!</h1><p className="muted">Your CampusRide dashboard is ready.</p></div><Link className="button button-primary" to="/profile">View profile</Link></div>
      <div className="dashboard-grid">
        <article className="card"><span className="stat-label">Signed in as</span><span className="stat-value">{user.firstName} {user.lastName}</span></article>
        <article className="card"><span className="stat-label">Account role</span><span className="stat-value">{user.role}</span></article>
        <article className="card"><span className="stat-label">Account email</span><span className="stat-value">{user.email}</span></article>
        <article className="card placeholder"><h3>Find a ride</h3><p>This feature will be available in a future CampusRide release.</p></article>
        <article className="card placeholder"><h3>Your bookings</h3><p>Your future bookings will appear here.</p></article>
        <article className="card placeholder"><h3>Campus updates</h3><p>Helpful travel updates will be shown here.</p></article>
      </div>
    </section>
  )
}

export default DashboardPage
