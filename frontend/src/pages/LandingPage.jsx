import { Link } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

function LandingPage() {
  const { isAuthenticated } = useAuth()
  return (
    <>
      <section className="hero">
        <div className="container">
          <h1>Make every campus journey feel connected.</h1>
          <p>CampusRide is a student-focused ride-sharing platform designed to make getting to and from campus simple, secure, and affordable.</p>
          <div className="hero-actions">
            <Link className="button button-primary" to={isAuthenticated ? '/dashboard' : '/register'}>{isAuthenticated ? 'Go to dashboard' : 'Get started'}</Link>
            {!isAuthenticated && <Link className="button button-secondary" to="/login">I already have an account</Link>}
          </div>
        </div>
      </section>
      <section className="container features">
        <article className="card"><h3>Built for students</h3><p>One simple account for students and drivers in your campus community.</p></article>
        <article className="card"><h3>Secure access</h3><p>Your account is protected with encrypted passwords and token-based authentication.</p></article>
        <article className="card"><h3>More coming soon</h3><p>Ride matching and bookings will appear here as CampusRide grows.</p></article>
      </section>
    </>
  )
}

export default LandingPage
