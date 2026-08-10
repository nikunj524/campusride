import { useState } from 'react'
import { Link } from 'react-router-dom'
import api from '../api/client'

const getApiError = (requestError, fallbackMessage) => {
  const fieldErrors = requestError.response?.data?.fieldErrors
  const firstFieldError = Object.values(fieldErrors || {})[0]
  return firstFieldError || requestError.response?.data?.message || fallbackMessage
}

function ForgotPasswordPage() {
  const [step, setStep] = useState('request')
  const [email, setEmail] = useState('')
  const [otp, setOtp] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const requestOtp = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')
    setSubmitting(true)

    try {
      const { data } = await api.post('/auth/forgot-password', { email })
      setMessage(data.message || 'If an account exists for this email, an OTP has been sent.')
      setStep('verify')
    } catch (requestError) {
      setError(getApiError(requestError, 'Unable to request a password reset. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  const verifyOtp = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')
    setSubmitting(true)

    try {
      const { data } = await api.post('/auth/verify-reset-otp', { email, otp })
      setMessage(data.message || 'OTP verified successfully.')
      setStep('reset')
    } catch (requestError) {
      setError(getApiError(requestError, 'The OTP is invalid or has expired.'))
    } finally {
      setSubmitting(false)
    }
  }

  const resetPassword = async (event) => {
    event.preventDefault()
    setError('')
    setMessage('')

    if (newPassword !== confirmPassword) {
      setError('New password and confirm password must match.')
      return
    }

    setSubmitting(true)
    try {
      const { data } = await api.post('/auth/reset-password', { email, newPassword })
      setMessage(data.message || 'Password reset successfully. You can now sign in.')
      setOtp('')
      setNewPassword('')
      setConfirmPassword('')
      setStep('complete')
    } catch (requestError) {
      setError(getApiError(requestError, 'Unable to reset your password. Please try again.'))
    } finally {
      setSubmitting(false)
    }
  }

  const useDifferentEmail = () => {
    setStep('request')
    setOtp('')
    setNewPassword('')
    setConfirmPassword('')
    setMessage('')
    setError('')
  }

  return (
    <section className="auth-page"><div className="auth-card">
      {step === 'request' && <>
        <p className="step-label">Step 1 of 3</p>
        <h1>Forgot your password?</h1>
        <p className="muted">Enter your email and we&apos;ll send a password reset OTP.</p>
        <form className="form" onSubmit={requestOtp}>
          {error && <p className="form-error">{error}</p>}
          <label className="field">Email<input type="email" required maxLength="120" value={email} onChange={(event) => setEmail(event.target.value)} /></label>
          <button className="button button-primary" disabled={submitting}>{submitting ? 'Sending OTP...' : 'Send OTP'}</button>
        </form>
        <p className="muted">Remembered your password? <Link className="nav-link" to="/login">Sign in</Link></p>
      </>}

      {step === 'verify' && <>
        <p className="step-label">Step 2 of 3</p>
        <h1>Verify your OTP</h1>
        <p className="muted">Enter the six-digit OTP sent to {email}.</p>
        <form className="form" onSubmit={verifyOtp}>
          {message && <p className="form-success">{message}</p>}
          {error && <p className="form-error">{error}</p>}
          <label className="field">OTP<input inputMode="numeric" autoComplete="one-time-code" pattern="[0-9]{6}" required maxLength="6" value={otp} onChange={(event) => setOtp(event.target.value.replace(/\D/g, ''))} /></label>
          <button className="button button-primary" disabled={submitting}>{submitting ? 'Verifying...' : 'Verify OTP'}</button>
        </form>
        <button className="form-link" type="button" onClick={useDifferentEmail}>Use a different email</button>
      </>}

      {step === 'reset' && <>
        <p className="step-label">Step 3 of 3</p>
        <h1>Set a new password</h1>
        <p className="muted">Choose a new password for {email}.</p>
        <form className="form" onSubmit={resetPassword}>
          {message && <p className="form-success">{message}</p>}
          {error && <p className="form-error">{error}</p>}
          <label className="field">New password<input type="password" required minLength="8" maxLength="72" value={newPassword} onChange={(event) => setNewPassword(event.target.value)} /></label>
          <label className="field">Confirm password<input type="password" required minLength="8" maxLength="72" value={confirmPassword} onChange={(event) => setConfirmPassword(event.target.value)} /></label>
          <button className="button button-primary" disabled={submitting}>{submitting ? 'Resetting password...' : 'Reset password'}</button>
        </form>
      </>}

      {step === 'complete' && <>
        <p className="step-label">Complete</p>
        <h1>Password reset complete</h1>
        <p className="form-success">{message}</p>
        <div className="form-actions"><Link className="button button-primary" to="/login">Return to Login</Link></div>
      </>}
    </div></section>
  )
}

export default ForgotPasswordPage
