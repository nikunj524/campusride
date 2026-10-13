import { Link } from 'react-router-dom'
import { ArrowRight, BadgeCheck, CarFront, Mail, MapPin, Phone, ShieldCheck, Sparkles, UsersRound } from 'lucide-react'
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

      <section className="landing-about" id="about-us">
        <div className="container">
          <div className="about-header">
            <p className="eyebrow"><UsersRound size={16} />About CampusRide</p>
            <h2>Connecting students, one ride at a time</h2>
          </div>
          <div className="about-content">
            <div className="about-text">
              <h3>Our Mission</h3>
              <p>
                CampusRide was created to solve a common problem faced by students every day: finding reliable, 
                affordable transportation to and from campus. We believe that getting to class, events, or home 
                shouldn't be a hassle or drain your budget.
              </p>
              <p>
                By connecting students who need rides with those who have extra seats, we're building a community 
                where everyone benefits. Drivers can offset their fuel costs, and passengers get convenient, 
                cost-effective rides with fellow students they can trust.
              </p>
              
              <h3>Why Choose CampusRide?</h3>
              <ul className="about-list">
                <li><strong>Campus-Focused:</strong> Built specifically for student needs and campus routes</li>
                <li><strong>Safe & Verified:</strong> All drivers are verified students from your campus</li>
                <li><strong>Affordable:</strong> Share costs with fellow students and save money</li>
                <li><strong>Eco-Friendly:</strong> Reduce carbon footprint by sharing rides</li>
                <li><strong>Community-Driven:</strong> Connect with classmates and make new friends</li>
              </ul>

              <h3>Our Vision</h3>
              <p>
                We envision a future where every student has access to safe, reliable, and affordable transportation. 
                CampusRide is more than just a ride-sharing app—it's a movement to make campus life easier, more 
                sustainable, and more connected.
              </p>
            </div>
          </div>
        </div>
      </section>

      <section className="landing-contact" id="contact">
        <div className="container">
          <div className="contact-header">
            <p className="eyebrow"><Mail size={16} />Get in Touch</p>
            <h2>We'd love to hear from you</h2>
            <p>Have questions, feedback, or need support? Our team is here to help.</p>
          </div>
          <div className="contact-grid">
            <div className="contact-info">
              <div className="contact-card">
                <div className="contact-icon">
                  <Mail size={22} />
                </div>
                <div>
                  <h3>Email Us</h3>
                  <p>For general inquiries and support</p>
                  <a href="mailto:support@campusride.com">support@campusride.com</a>
                </div>
              </div>
              
              <div className="contact-card">
                <div className="contact-icon">
                  <Phone size={22} />
                </div>
                <div>
                  <h3>Call Us</h3>
                  <p>Monday to Friday, 9AM - 6PM</p>
                  <a href="tel:+919876543210">+91 98765 43210</a>
                </div>
              </div>
              
              <div className="contact-card">
                <div className="contact-icon">
                  <MapPin size={22} />
                </div>
                <div>
                  <h3>Visit Us</h3>
                  <p>CampusRide Headquarters</p>
                  <address>
                    Gujarat Vidyapith Campus<br />
                    Ashram Road, Ahmedabad<br />
                    Gujarat 380014, India
                  </address>
                </div>
              </div>
            </div>

            <div className="contact-form-wrapper">
              <h3>Send us a message</h3>
              <form className="contact-form">
                <div className="form-group">
                  <label htmlFor="name">Your Name</label>
                  <input type="text" id="name" placeholder="Enter your full name" required />
                </div>
                <div className="form-group">
                  <label htmlFor="email">Your Email</label>
                  <input type="email" id="email" placeholder="your.email@example.com" required />
                </div>
                <div className="form-group">
                  <label htmlFor="subject">Subject</label>
                  <input type="text" id="subject" placeholder="How can we help?" required />
                </div>
                <div className="form-group">
                  <label htmlFor="message">Message</label>
                  <textarea id="message" rows="5" placeholder="Tell us more about your question or feedback..." required />
                </div>
                <button type="submit" className="button button-primary">
                  Send Message <ArrowRight size={16} />
                </button>
              </form>
            </div>
          </div>
        </div>
      </section>
    </>
  )
}

export default LandingPage
