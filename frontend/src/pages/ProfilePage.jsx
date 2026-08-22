import { useEffect, useState } from 'react'
import {
  CheckCircle2,
  Edit3,
  LoaderCircle,
  Mail,
  Phone,
  Save,
  ShieldCheck,
  UserRound,
  X,
} from 'lucide-react'
import { userApi } from '../api/client'
import useAuth from '../hooks/useAuth'

function ProfilePage() {
  const { user, refreshProfile } = useAuth()
  const [profile, setProfile] = useState(null)
  const [form, setForm] = useState({ firstName: '', lastName: '', phoneNumber: '' })
  const [isEditing, setIsEditing] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [driverRequest, setDriverRequest] = useState(null)
  const [isRequestFormOpen, setIsRequestFormOpen] = useState(false)
  const [isRequestSubmitting, setIsRequestSubmitting] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    let isActive = true

    async function loadProfile() {
      setIsLoading(true)
      setError('')
      try {
        const { data } = await userApi.get(`/api/users/${user.id}`)
        if (!isActive) return
        setProfile(data)
        setForm(toEditableForm(data))
        const { data: request } = await userApi.get('/api/driver-requests/me')
        if (!isActive) return
        setDriverRequest(request)
      } catch (requestError) {
        if (isActive) setError(requestError.response?.data?.message || 'We could not load your profile. Please try again.')
      } finally {
        if (isActive) setIsLoading(false)
      }
    }

    if (user?.id) loadProfile()
    return () => { isActive = false }
  }, [user?.id])

  const beginEditing = () => {
    setForm(toEditableForm(profile))
    setError('')
    setSuccess('')
    setIsEditing(true)
  }

  const cancelEditing = () => {
    setForm(toEditableForm(profile))
    setError('')
    setIsEditing(false)
  }

  const saveProfile = async (event) => {
    event.preventDefault()
    setError('')
    setSuccess('')
    setIsSaving(true)

    const update = {
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      phoneNumber: form.phoneNumber.trim(),
    }

    try {
      const { data } = await userApi.put(`/api/users/${profile.id}`, update)
      setProfile(data)
      setForm(toEditableForm(data))
      setIsEditing(false)
      setSuccess('Your profile has been updated successfully.')
      refreshProfile().catch(() => undefined)
    } catch (requestError) {
      const fieldErrors = requestError.response?.data?.fieldErrors
      setError(Object.values(fieldErrors || {})[0] || requestError.response?.data?.message || 'Your changes could not be saved. Please try again.')
    } finally {
      setIsSaving(false)
    }
  }

  const submitDriverRequest = async () => {
    setError('')
    setSuccess('')
    setIsRequestSubmitting(true)
    try {
      const { data } = await userApi.post('/api/driver-requests')
      setDriverRequest(data)
      setIsRequestFormOpen(false)
      setSuccess('Your driver request has been submitted for admin review.')
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Your driver request could not be submitted. Please try again.')
    } finally {
      setIsRequestSubmitting(false)
    }
  }

  if (isLoading) return <ProfileState icon={LoaderCircle} message="Loading your profile..." spinning />

  if (!profile) return <ProfileState icon={UserRound} message={error || 'Your profile is unavailable right now.'} error />

  const roleLabel = profile.role ? `${profile.role.charAt(0)}${profile.role.slice(1).toLowerCase()}` : 'Student'
  const isStudent = profile.role === 'STUDENT'
  const isDriverApproved = Boolean(profile.driverEligible || driverRequest?.status === 'APPROVED')

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[1120px]">
        <div className="mb-6 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div><h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">Profile</h1><p className="mt-1 text-sm text-[#647590]">Here is your account information.</p></div>
          {!isEditing && <button className="inline-flex w-full items-center justify-center gap-2 rounded-lg border border-[#8db4fb] bg-white px-4 py-2.5 text-sm font-bold text-[#1f59cf] transition hover:border-[#2563eb] hover:bg-[#f5f9ff] sm:w-auto" onClick={beginEditing} type="button"><Edit3 size={16} />Edit profile</button>}
        </div>

        {success && <Feedback type="success">{success}</Feedback>}
        {error && <Feedback type="error">{error}</Feedback>}

        <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white shadow-[0_10px_28px_rgba(31,61,111,0.06)]">
          {isEditing ? (
            <form className="p-5 sm:p-7" onSubmit={saveProfile}>
              <div className="mb-6 border-b border-[#edf1f8] pb-5"><h2 className="m-0 text-lg font-bold tracking-[-0.02em] text-[#14244c]">Edit profile</h2><p className="mt-1 text-sm text-[#647590]">Update your personal information.</p></div>
              <div className="grid gap-5 sm:grid-cols-2">
                <EditableField label="First name" value={form.firstName} onChange={(firstName) => setForm({ ...form, firstName })} />
                <EditableField label="Last name" value={form.lastName} onChange={(lastName) => setForm({ ...form, lastName })} />
                <EditableField className="sm:col-span-2" icon={Phone} label="Phone number" type="tel" value={form.phoneNumber} onChange={(phoneNumber) => setForm({ ...form, phoneNumber })} />
                <ReadOnlyField className="sm:col-span-2" icon={Mail} label="Email" value={profile.email} hint="Email cannot be changed" />
                <ReadOnlyField className="sm:col-span-2" icon={ShieldCheck} label="Role" value={roleLabel} hint="Role is managed by CampusRide" />
              </div>
              <div className="mt-8 flex flex-col-reverse gap-3 border-t border-[#edf1f8] pt-5 sm:flex-row sm:justify-end">
                <button className="inline-flex items-center justify-center gap-2 rounded-lg border border-[#cdd9eb] bg-white px-5 py-2.5 text-sm font-bold text-[#273a60] transition hover:bg-slate-50 disabled:opacity-60" disabled={isSaving} onClick={cancelEditing} type="button"><X size={16} />Cancel</button>
                <button className="inline-flex items-center justify-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-[0_8px_16px_rgba(37,99,235,0.16)] transition hover:bg-[#164bc7] disabled:opacity-60" disabled={isSaving} type="submit">{isSaving ? <LoaderCircle className="animate-spin" size={16} /> : <Save size={16} />}{isSaving ? 'Saving...' : 'Save changes'}</button>
              </div>
            </form>
          ) : <ProfileInformation profile={profile} roleLabel={roleLabel} />}
        </article>
        {isStudent && <DriverApplication
          driverRequest={driverRequest}
          isApproved={isDriverApproved}
          isFormOpen={isRequestFormOpen}
          isSubmitting={isRequestSubmitting}
          onCancel={() => setIsRequestFormOpen(false)}
          onOpen={() => setIsRequestFormOpen(true)}
          onSubmit={submitDriverRequest}
        />}
      </div>
    </section>
  )
}

function DriverApplication({ driverRequest, isApproved, isFormOpen, isSubmitting, onCancel, onOpen, onSubmit }) {
  if (isApproved) {
    return <DriverApplicationCard title="Driver Status" status="Approved"><p className="mt-2 text-sm text-[#53637f]">You can now switch to Driver from the sidebar.</p></DriverApplicationCard>
  }

  if (driverRequest?.status === 'PENDING') {
    return <DriverApplicationCard title="Driver Application" status="Pending"><p className="mt-2 text-sm text-[#53637f]">Your request is waiting for admin approval.</p></DriverApplicationCard>
  }

  if (driverRequest?.status === 'REJECTED') {
    return <DriverApplicationCard title="Driver Application" status="Rejected"><p className="mt-2 text-sm text-[#53637f]">You can submit a new request for review.</p><button className="mt-4 inline-flex items-center justify-center rounded-lg bg-[#2563eb] px-4 py-2.5 text-sm font-bold text-white transition hover:bg-[#164bc7]" onClick={onOpen} type="button">Become a Driver</button>{isFormOpen && <DriverRequestConfirmation isSubmitting={isSubmitting} onCancel={onCancel} onSubmit={onSubmit} />}</DriverApplicationCard>
  }

  return <DriverApplicationCard title="Become a Driver" status={null}><p className="mt-2 text-sm text-[#53637f]">Request approval to use Driver mode and manage a vehicle on CampusRide.</p>{isFormOpen ? <DriverRequestConfirmation isSubmitting={isSubmitting} onCancel={onCancel} onSubmit={onSubmit} /> : <button className="mt-4 inline-flex items-center justify-center rounded-lg bg-[#2563eb] px-4 py-2.5 text-sm font-bold text-white transition hover:bg-[#164bc7]" onClick={onOpen} type="button">Become a Driver</button>}</DriverApplicationCard>
}

function DriverApplicationCard({ children, title, status }) {
  return <section className="mt-6 rounded-2xl border border-[#e0e9f7] bg-white p-5 shadow-[0_10px_28px_rgba(31,61,111,0.06)] sm:p-7"><div className="flex flex-wrap items-center justify-between gap-3"><h2 className="m-0 text-lg font-bold tracking-[-0.02em] text-[#14244c]">{title}</h2>{status && <span className="rounded-full bg-[#eef5ff] px-3 py-1 text-xs font-bold uppercase tracking-[0.04em] text-[#2563eb]">Status: {status}</span>}</div>{children}</section>
}

function DriverRequestConfirmation({ isSubmitting, onCancel, onSubmit }) {
  return <div className="mt-5 rounded-xl border border-[#e2e9f4] bg-[#f8fbff] p-4"><p className="m-0 text-sm font-medium text-[#304260]">Submit a request to become a Driver?</p><div className="mt-4 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end"><button className="rounded-lg border border-[#cdd9eb] bg-white px-4 py-2.5 text-sm font-bold text-[#273a60]" disabled={isSubmitting} onClick={onCancel} type="button">Cancel</button><button className="rounded-lg bg-[#2563eb] px-4 py-2.5 text-sm font-bold text-white disabled:opacity-60" disabled={isSubmitting} onClick={onSubmit} type="button">{isSubmitting ? 'Submitting...' : 'Submit request'}</button></div></div>
}

function ProfileInformation({ profile, roleLabel }) {
  const memberSince = profile.createdAt ? new Date(profile.createdAt).toLocaleDateString() : 'Not available'

  return (
    <div className="grid gap-7 p-5 sm:p-7 md:grid-cols-[170px_minmax(0,1fr)] md:items-center">
      <div className="flex flex-col items-center text-center md:items-start md:text-left">
        <div className="flex size-[92px] items-center justify-center rounded-full border-4 border-[#edf4ff] bg-gradient-to-br from-[#dceaff] to-[#aac9fb] text-2xl font-extrabold tracking-[-0.05em] text-[#174ba6] shadow-sm">{initials(profile)}</div>
        <strong className="mt-3 text-base text-[#182950]">{profile.firstName} {profile.lastName}</strong>
        <span className="mt-1 rounded-full bg-[#eef5ff] px-2.5 py-1 text-xs font-bold text-[#2563eb]">{roleLabel}</span>
      </div>
      <dl className="grid gap-x-8 gap-y-5 sm:grid-cols-2">
        <ProfileItem label="First name" value={profile.firstName} />
        <ProfileItem label="Last name" value={profile.lastName} />
        <ProfileItem icon={Mail} label="Email" value={profile.email} readOnly />
        <ProfileItem icon={Phone} label="Phone number" value={profile.phoneNumber} />
        <ProfileItem icon={ShieldCheck} label="Role" value={roleLabel} readOnly />
        <ProfileItem label="Member since" value={memberSince} />
      </dl>
    </div>
  )
}

function ProfileItem({ icon: Icon, label, value, readOnly = false }) {
  return <div className="min-w-0"><dt className="flex items-center gap-1.5 text-xs font-bold uppercase tracking-[0.035em] text-[#72829d]">{Icon && <Icon size={14} className="text-[#2563eb]" />}{label}</dt><dd className="mt-2 break-words text-sm font-bold text-[#182950]">{value || 'Not provided'}</dd>{readOnly && <span className="mt-1 block text-[11px] font-medium text-[#8797b2]">Read-only</span>}</div>
}

function EditableField({ className = '', icon: Icon, label, type = 'text', value, onChange }) {
  return <label className={`block ${className}`}><span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">{Icon && <Icon size={15} className="text-[#2563eb]" />}{label}</span><input className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]" maxLength={type === 'tel' ? 20 : 50} required type={type} value={value} onChange={(event) => onChange(event.target.value)} /></label>
}

function ReadOnlyField({ className = '', icon: Icon, label, value, hint }) {
  return <div className={className}><span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]"><Icon size={15} className="text-[#6c82aa]" />{label}</span><div className="rounded-lg border border-[#e2e9f4] bg-[#f5f7fb] px-3.5 py-2.5 text-sm font-medium text-[#647590]">{value}</div><p className="mt-1.5 text-xs text-[#7b8ca7]">{hint}</p></div>
}

function Feedback({ children, type }) {
  const isSuccess = type === 'success'
  return <div className={`mb-5 flex items-center gap-2 rounded-lg border px-4 py-3 text-sm font-medium ${isSuccess ? 'border-green-100 bg-green-50 text-green-800' : 'border-red-100 bg-red-50 text-red-700'}`}>{isSuccess && <CheckCircle2 size={18} />}{children}</div>
}

function ProfileState({ icon: Icon, message, spinning = false, error = false }) {
  return <section className="grid min-h-[300px] place-items-center bg-[#f6f9ff] px-1 py-1"><div className="flex flex-col items-center rounded-2xl border border-[#e0e9f7] bg-white px-8 py-10 text-center shadow-sm"><Icon className={`${spinning ? 'animate-spin' : ''} ${error ? 'text-red-500' : 'text-[#2563eb]'}`} size={32} /><p className="mt-3 max-w-xs text-sm font-medium text-[#53637f]">{message}</p></div></section>
}

function toEditableForm(profile) {
  return { firstName: profile?.firstName || '', lastName: profile?.lastName || '', phoneNumber: profile?.phoneNumber || '' }
}

function initials(profile) {
  return `${profile?.firstName?.[0] || ''}${profile?.lastName?.[0] || ''}`.toUpperCase() || 'CR'
}

export default ProfilePage
