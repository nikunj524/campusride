import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { Bike, Menu, X } from 'lucide-react'
import useAuth from '../hooks/useAuth'

function Navbar() {
  const [isOpen, setIsOpen] = useState(false)
  const { isAuthenticated, logout } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const closeMenu = () => setIsOpen(false)
  const handleLogout = () => { logout(); closeMenu(); navigate('/') }

  const scrollToSection = (sectionId) => {
    closeMenu()
    
    // If not on home page, navigate first
    if (location.pathname !== '/') {
      navigate('/')
      // Wait for navigation then scroll
      setTimeout(() => {
        const element = document.getElementById(sectionId)
        if (element) {
          element.scrollIntoView({ behavior: 'smooth', block: 'start' })
        }
      }, 100)
    } else {
      // Already on home page, just scroll
      const element = document.getElementById(sectionId)
      if (element) {
        element.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }
    }
  }

  const handleHomeClick = () => {
    closeMenu()
    navigate('/')
    // Scroll to top when home is clicked
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  return (
    <header className="navbar"><nav className="container nav-inner" aria-label="Main navigation">
      <button className="brand" onClick={handleHomeClick} type="button"><Bike size={25} strokeWidth={2.7} />CampusRide</button>
      <button className="nav-toggle" aria-expanded={isOpen} aria-label="Toggle navigation" onClick={() => setIsOpen(!isOpen)} type="button">{isOpen ? <X size={22} /> : <Menu size={22} />}</button>
      <div className={`nav-links ${isOpen ? 'is-open' : ''}`}>
        <button className="nav-link" onClick={handleHomeClick} type="button">Home</button>
        <button className="nav-link" onClick={() => scrollToSection('features')} type="button">Features</button>
        <button className="nav-link" onClick={() => scrollToSection('about-us')} type="button">About</button>
        <button className="nav-link" onClick={() => scrollToSection('contact')} type="button">Contact</button>
        {isAuthenticated ? <><Link className="nav-link" onClick={closeMenu} to="/dashboard">Dashboard</Link><button className="button button-outline nav-logout" onClick={handleLogout} type="button">Logout</button></> : <><Link className="button button-outline" onClick={closeMenu} to="/login">Login</Link><Link className="button button-primary" onClick={closeMenu} to="/register">Create account</Link></>}
      </div>
    </nav></header>
  )
}

export default Navbar
