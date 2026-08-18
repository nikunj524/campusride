import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../api/client'
import useAuth from '../hooks/useAuth'

function RegisterPage() {
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', phoneNumber: '', role: 'STUDENT' })
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const { saveSession } = useAuth()
  const navigate = useNavigate()

  const submit = async (event) => {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const { data } = await api.post('/auth/register', form)
      saveSession(data)
      navigate('/dashboard', { replace: true })
    } catch (requestError) {
      const fieldErrors = requestError.response?.data?.fieldErrors
      setError(fieldErrors ? Object.values(fieldErrors)[0] : requestError.response?.data?.message || 'Unable to create the account.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="auth-page"><div className="auth-card">
      <h1>Create your account</h1><p className="muted">Join CampusRide with your campus details.</p>
      <form className="form" onSubmit={submit}>
        {error && <p className="form-error">{error}</p>}
        <div className="form-row">
          <label className="field">First name<input required maxLength="50" value={form.firstName} onChange={(event) => setForm({ ...form, firstName: event.target.value })} /></label>
          <label className="field">Last name<input required maxLength="50" value={form.lastName} onChange={(event) => setForm({ ...form, lastName: event.target.value })} /></label>
        </div>
        <label className="field">Email<input type="email" required value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} /></label>
        <label className="field">Phone number<input type="tel" required value={form.phoneNumber} onChange={(event) => setForm({ ...form, phoneNumber: event.target.value })} /></label>
        <label className="field">Password<input type="password" minLength="8" required value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} /></label>
        <label className="field">Account type<select value={form.role} onChange={(event) => setForm({ ...form, role: event.target.value })}><option value="STUDENT">Student</option><option value="DRIVER">Driver</option></select></label>
        <button className="button button-primary" disabled={submitting}>{submitting ? 'Creating account…' : 'Create account'}</button>
      </form>
      <p className="muted">Already have an account? <Link className="nav-link" to="/login">Sign in</Link></p>
    </div></section>
  )
}

export default RegisterPage
