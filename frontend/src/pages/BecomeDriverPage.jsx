import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowLeft,
  Car,
  CheckCircle2,
  FileText,
  Send,
  Shield,
  UserCheck,
} from 'lucide-react'
import useAuth from '../hooks/useAuth'

function BecomeDriverPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setIsSubmitting(true)

    try {
      const response = await fetch('http://localhost:8082/api/driver-requests', {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${localStorage.getItem('campusride_token')}`,
          'Content-Type': 'application/json',
        },
      })

      if (response.ok) {
        setSuccess(true)
      } else {
        const errorData = await response.json()
        setError(errorData.message || 'Failed to submit driver request.')
      }
    } catch (err) {
      setError('Unable to submit your request. Please try again.')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (success) {
    return (
      <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
        <div className="mx-auto max-w-[720px]">
          <div className="flex flex-col items-center rounded-2xl border border-green-200 bg-white px-8 py-12 text-center">
            <div className="flex size-16 items-center justify-center rounded-full bg-green-100">
              <CheckCircle2 className="text-green-600" size={32} />
            </div>
            <h2 className="mt-4 text-xl font-bold text-[#12214a]">
              Application Submitted!
            </h2>
            <p className="mt-2 max-w-md text-sm text-[#647590]">
              Your driver application has been submitted successfully. Our admin team will
              review your request and notify you via email once approved.
            </p>
            <div className="mt-6 flex gap-3">
              <button
                className="inline-flex items-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-[#164bc7]"
                onClick={() => navigate('/dashboard', { state: { refreshDriverStatus: true } })}
                type="button"
              >
                Go to Dashboard
              </button>
            </div>
          </div>
        </div>
      </section>
    )
  }

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[720px]">
        <button
          className="mb-4 inline-flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-bold text-[#2563eb] transition hover:bg-blue-50"
          onClick={() => navigate('/dashboard')}
          type="button"
        >
          <ArrowLeft size={16} />
          Back to Dashboard
        </button>

        <div className="mb-6">
          <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
            Become a Driver
          </h1>
          <p className="mt-1 text-sm text-[#647590]">
            Apply to offer rides and earn while helping fellow students
          </p>
        </div>

        {/* Benefits Section */}
        <div className="mb-6 overflow-hidden rounded-2xl border border-[#e0e9f7] bg-gradient-to-br from-blue-50 to-indigo-50 p-6">
          <h2 className="mb-4 text-lg font-bold text-[#12214a]">Why Become a Driver?</h2>
          <div className="grid gap-4 sm:grid-cols-3">
            <div className="flex flex-col items-center text-center">
              <div className="flex size-12 items-center justify-center rounded-full bg-blue-100">
                <Car className="text-blue-600" size={24} />
              </div>
              <h3 className="mt-2 text-sm font-bold text-[#12214a]">Earn Money</h3>
              <p className="mt-1 text-xs text-[#647590]">
                Make extra income by offering rides to fellow students
              </p>
            </div>
            <div className="flex flex-col items-center text-center">
              <div className="flex size-12 items-center justify-center rounded-full bg-green-100">
                <UserCheck className="text-green-600" size={24} />
              </div>
              <h3 className="mt-2 text-sm font-bold text-[#12214a]">Flexible Schedule</h3>
              <p className="mt-1 text-xs text-[#647590]">
                Offer rides according to your availability and route
              </p>
            </div>
            <div className="flex flex-col items-center text-center">
              <div className="flex size-12 items-center justify-center rounded-full bg-purple-100">
                <Shield className="text-purple-600" size={24} />
              </div>
              <h3 className="mt-2 text-sm font-bold text-[#12214a]">Safe Platform</h3>
              <p className="mt-1 text-xs text-[#647590]">
                All users are verified members of your campus community
              </p>
            </div>
          </div>
        </div>

        {/* Application Form */}
        <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white shadow-sm">
          <form className="p-5 sm:p-7" onSubmit={handleSubmit}>
            <div className="mb-6 border-b border-[#edf1f8] pb-5">
              <h2 className="m-0 text-lg font-bold tracking-[-0.02em] text-[#14244c]">
                Driver Application
              </h2>
              <p className="mt-1 text-sm text-[#647590]">
                Submit your application to become a verified driver on CampusRide
              </p>
            </div>

            {error && (
              <div className="mb-5 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                {error}
              </div>
            )}

            {/* Current User Info */}
            <div className="mb-6 space-y-4">
              <div className="rounded-lg bg-slate-50 p-4">
                <h3 className="mb-3 text-sm font-bold text-[#304260]">Your Information</h3>
                <div className="grid gap-3 sm:grid-cols-2">
                  <div>
                    <p className="text-xs font-medium text-[#647590]">Full Name</p>
                    <p className="mt-1 text-sm font-bold text-[#182950]">
                      {user?.firstName} {user?.lastName}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs font-medium text-[#647590]">Email</p>
                    <p className="mt-1 text-sm font-bold text-[#182950]">{user?.email}</p>
                  </div>
                  <div>
                    <p className="text-xs font-medium text-[#647590]">Phone</p>
                    <p className="mt-1 text-sm font-bold text-[#182950]">
                      {user?.phoneNumber}
                    </p>
                  </div>
                  <div>
                    <p className="text-xs font-medium text-[#647590]">Account Type</p>
                    <p className="mt-1 text-sm font-bold text-[#182950]">Student</p>
                  </div>
                </div>
              </div>

              {/* Requirements */}
              <div className="rounded-lg border-2 border-blue-100 bg-blue-50 p-4">
                <div className="flex items-start gap-3">
                  <FileText className="mt-0.5 text-blue-600" size={20} />
                  <div>
                    <h3 className="text-sm font-bold text-blue-900">Requirements</h3>
                    <ul className="mt-2 space-y-1 text-xs text-blue-700">
                      <li>• Valid driver's license</li>
                      <li>• Registered vehicle with valid documents</li>
                      <li>• Clean driving record</li>
                      <li>• Active student or staff member</li>
                    </ul>
                    <p className="mt-3 text-xs text-blue-700">
                      After approval, you'll need to add your vehicle details before offering
                      rides.
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* Submit Button */}
            <div className="flex gap-3 border-t border-[#edf1f8] pt-5">
              <button
                className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-[0_8px_16px_rgba(37,99,235,0.16)] transition hover:bg-[#164bc7] disabled:opacity-60"
                disabled={isSubmitting}
                type="submit"
              >
                <Send size={16} />
                {isSubmitting ? 'Submitting Application...' : 'Submit Application'}
              </button>
              <button
                className="inline-flex items-center justify-center gap-2 rounded-lg border border-[#cdd9eb] bg-white px-5 py-2.5 text-sm font-bold text-[#273a60] transition hover:bg-slate-50"
                disabled={isSubmitting}
                onClick={() => navigate('/dashboard')}
                type="button"
              >
                Cancel
              </button>
            </div>
          </form>
        </article>

        {/* Footer Note */}
        <div className="mt-4 text-center text-xs text-[#7b8ca7]">
          By submitting this application, you agree to CampusRide's driver terms and conditions.
        </div>
      </div>
    </section>
  )
}

export default BecomeDriverPage
