import { Link, useNavigate } from 'react-router-dom'
import useAuth from '../hooks/useAuth'

function Navbar() {
  const { isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  return (
    <header className="navbar">
      <nav className="container nav-inner" aria-label="Main navigation">
        <Link className="brand" to="/">CampusRide</Link>
        <div className="nav-links">
          {isAuthenticated ? (
            <>
              <Link className="nav-link" to="/dashboard">Dashboard</Link>
              <Link className="nav-link" to="/profile">Profile</Link>
              <button className="button button-danger" onClick={handleLogout}>Logout</button>
            </>
          ) : (
            <>
              <Link className="nav-link" to="/login">Login</Link>
              <Link className="button button-primary" to="/register">Create account</Link>
            </>
          )}
        </div>
      </nav>
    </header>
  )
}

export default Navbar
