import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import api from '../api/client'
import useAuth from '../hooks/useAuth'

function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { saveSession } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const { data } = await api.post('/auth/login', form)
      saveSession(data)
      navigate(location.state?.from?.pathname || '/dashboard', { replace: true })
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Unable to sign in. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-page"><div className="auth-card">
      <h1>Welcome back</h1><p className="muted">Sign in to access your CampusRide account.</p>
      <form className="form" onSubmit={submit}>
        {error && <p className="form-error">{error}</p>}
        <label className="field">Email<input type="email" required value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
        <label className="field">Password<input type="password" required value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} /></label>
        <Link className="form-link" to="/forgot-password">Forgot Password?</Link>
        <button className="button button-primary" disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'}</button>
      </form>
      <p className="muted">New to CampusRide? <Link className="nav-link" to="/register">Create an account</Link></p>
    </div></section>
  )
}

export default LoginPage
