import { Link } from 'react-router-dom'
import { ArrowRight, BadgeCheck, CarFront, MapPin, ShieldCheck, Sparkles, UsersRound } from 'lucide-react'
import useAuth from '../hooks/useAuth'

const features = [
  { icon: UsersRound, title: 'Built for students', copy: 'One simple account for students and drivers in your campus community.' },
  { icon: ShieldCheck, title: 'Secure and private', copy: 'Your account is protected with secure, token-based access.' },
  { icon: MapPin, title: 'Campus focused', copy: 'A smoother way to plan everyday trips to and from campus.' },
  { icon: Sparkles, title: 'More coming soon', copy: 'Ride matching and booking tools will arrive as CampusRide grows.' },
]

function LandingPage() {
  const { isAuthenticated } = useAuth()
  const primaryDestination = isAuthenticated ? '/dashboard' : '/register'

  return (
    <>
      <section className="landing-hero">
        <div className="container landing-hero-grid">
          <div className="hero-copy">
            <p className="eyebrow"><BadgeCheck size={16} />Your campus travel companion</p>
            <h1>Make every campus journey feel <span>connected.</span></h1>
            <p className="hero-description">CampusRide is a student-focused ride-sharing platform designed to make getting to and from campus simple, secure, and affordable.</p>
            <div className="hero-actions">
              <Link className="button button-primary button-large" to={primaryDestination}>{isAuthenticated ? 'Go to dashboard' : 'Get started'} <ArrowRight size={17} /></Link>
              {!isAuthenticated && <Link className="button button-outline button-large" to="/login">I already have an account</Link>}
            </div>
          </div>
          <div className="hero-visual" aria-hidden="true">
            <div className="visual-cloud cloud-one" /><div className="visual-cloud cloud-two" />
            <div className="visual-campus"><span /><span /><span /></div>
            <div className="visual-road" />
            <div className="visual-car"><CarFront size={126} strokeWidth={1.55} /><div className="car-passengers"><i /><i /><i /></div></div>
            <div className="visual-tree tree-one" /><div className="visual-tree tree-two" />
          </div>
        </div>
      </section>

      <section className="container landing-features" id="features">
        {features.map(({ icon: Icon, title, copy }) => <article className="feature-card" key={title}><span className="feature-icon"><Icon size={23} /></span><h2>{title}</h2><p>{copy}</p></article>)}
      </section>

      <section className="container landing-trust" id="about">
        <div><p className="eyebrow"><BadgeCheck size={16} />Designed for campus life</p><h2>A better way to move through your day.</h2></div>
        <p>CampusRide brings student travel into one thoughtful, easy-to-use place. Explore what is available today and stay ready for the services coming next.</p>
      </section>
    </>
  )
}

export default LandingPage
