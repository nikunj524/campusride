import { useEffect, useState } from 'react'
import useAuth from '../hooks/useAuth'

function ProfilePage() {
  const { user, refreshProfile } = useAuth()
  const [error, setError] = useState('')

  useEffect(() => {
    refreshProfile().catch(() => setError('Your latest profile details could not be loaded.'))
  }, [refreshProfile])

  return (
    <section className="container dashboard"><div className="card profile-card">
      <h1>Your profile</h1><p className="muted">The details associated with your CampusRide account.</p>
      {error && <p className="form-error">{error}</p>}
      <dl className="profile-list">
        <div className="profile-item"><dt>First name</dt><dd>{user.firstName}</dd></div>
        <div className="profile-item"><dt>Last name</dt><dd>{user.lastName}</dd></div>
        <div className="profile-item"><dt>Email</dt><dd>{user.email}</dd></div>
        <div className="profile-item"><dt>Phone number</dt><dd>{user.phoneNumber}</dd></div>
        <div className="profile-item"><dt>Role</dt><dd>{user.role}</dd></div>
        <div className="profile-item"><dt>Member since</dt><dd>{user.createdAt ? new Date(user.createdAt).toLocaleDateString() : 'Just now'}</dd></div>
      </dl>
    </div></section>
  )
}

export default ProfilePage
