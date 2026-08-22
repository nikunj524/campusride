import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Bike, Menu, X } from 'lucide-react'
import useAuth from '../hooks/useAuth'

function Navbar() {
  const [isOpen, setIsOpen] = useState(false)
  const { isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const closeMenu = () => setIsOpen(false)
  const handleLogout = () => { logout(); closeMenu(); navigate('/') }

  return (
    <header className="navbar"><nav className="container nav-inner" aria-label="Main navigation">
      <Link className="brand" onClick={closeMenu} to="/"><Bike size={25} strokeWidth={2.7} />CampusRide</Link>
      <button className="nav-toggle" aria-expanded={isOpen} aria-label="Toggle navigation" onClick={() => setIsOpen(!isOpen)} type="button">{isOpen ? <X size={22} /> : <Menu size={22} />}</button>
      <div className={`nav-links ${isOpen ? 'is-open' : ''}`}>
        <Link className="nav-link" onClick={closeMenu} to="/">Home</Link><a className="nav-link" href="/#features" onClick={closeMenu}>Features</a><a className="nav-link" href="/#about" onClick={closeMenu}>About</a><a className="nav-link" href="/#contact" onClick={closeMenu}>Contact</a>
        {isAuthenticated ? <><Link className="nav-link" onClick={closeMenu} to="/dashboard">Dashboard</Link><button className="button button-outline nav-logout" onClick={handleLogout} type="button">Logout</button></> : <><Link className="button button-outline" onClick={closeMenu} to="/login">Login</Link><Link className="button button-primary" onClick={closeMenu} to="/register">Create account</Link></>}
      </div>
    </nav></header>
  )
}

export default Navbar
