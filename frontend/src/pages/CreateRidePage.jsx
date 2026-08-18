import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  CalendarDays,
  CarFront,
  CheckCircle2,
  Clock,
  IndianRupee,
  MapPin,
  Save,
  Users,
} from 'lucide-react'
import { rideApi } from '../api/client'
import useAuth from '../hooks/useAuth'

function CreateRidePage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const [form, setForm] = useState({
    source: '',
    destination: '',
    departureTime: '',
    availableSeats: '1',
    pricePerSeat: '',
    notes: '',
  })
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setIsSubmitting(true)

    const rideData = {
      driverId: user.id,
      type: 'OFFERING',
      source: form.source.trim(),
      destination: form.destination.trim(),
      departureTime: form.departureTime,
      availableSeats: parseInt(form.availableSeats, 10),
      pricePerSeat: parseFloat(form.pricePerSeat),
      notes: form.notes.trim() || null,
    }

    try {
      const { data } = await rideApi.post('/api/rides', rideData)
      setSuccess('Ride created successfully!')
      setTimeout(() => {
        navigate(`/rides/${data.id}`)
      }, 1500)
    } catch (err) {
      setError(
        err.response?.data?.message || 'Failed to create ride. Please try again.'
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[720px]">
        <div className="mb-6">
          <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
            Create Ride
          </h1>
          <p className="mt-1 text-sm text-[#647590]">
            Offer a ride to fellow students
          </p>
        </div>

        {success && (
          <div className="mb-5 flex items-center gap-2 rounded-lg border border-green-100 bg-green-50 px-4 py-3 text-sm font-medium text-green-800">
            <CheckCircle2 size={18} />
            {success}
          </div>
        )}

        {error && (
          <div className="mb-5 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        )}

        <article className="overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white shadow-sm">
          <form className="p-5 sm:p-7" onSubmit={handleSubmit}>
            <div className="mb-6 border-b border-[#edf1f8] pb-5">
              <h2 className="m-0 text-lg font-bold tracking-[-0.02em] text-[#14244c]">
                Ride Details
              </h2>
              <p className="mt-1 text-sm text-[#647590]">
                Fill in the details about your ride offer
              </p>
            </div>

            <div className="space-y-5">
              <label className="block">
                <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                  <MapPin size={15} className="text-[#2563eb]" />
                  Pickup Location (From)
                </span>
                <input
                  className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                  placeholder="e.g., Satellite, Ahmedabad"
                  required
                  type="text"
                  value={form.source}
                  onChange={(e) => setForm({ ...form, source: e.target.value })}
                />
              </label>

              <label className="block">
                <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                  <MapPin size={15} className="text-[#f97316]" />
                  Drop Location (To)
                </span>
                <input
                  className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                  placeholder="e.g., Gujarat University, Ahmedabad"
                  required
                  type="text"
                  value={form.destination}
                  onChange={(e) =>
                    setForm({ ...form, destination: e.target.value })
                  }
                />
              </label>

              <div className="grid gap-5 sm:grid-cols-2">
                <label className="block">
                  <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                    <CalendarDays size={15} className="text-[#2563eb]" />
                    Departure Date & Time
                  </span>
                  <input
                    className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                    required
                    type="datetime-local"
                    value={form.departureTime}
                    onChange={(e) =>
                      setForm({ ...form, departureTime: e.target.value })
                    }
                  />
                </label>

                <label className="block">
                  <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                    <Users size={15} className="text-[#2563eb]" />
                    Available Seats
                  </span>
                  <input
                    className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                    min="1"
                    max="10"
                    required
                    type="number"
                    value={form.availableSeats}
                    onChange={(e) =>
                      setForm({ ...form, availableSeats: e.target.value })
                    }
                  />
                </label>
              </div>

              <label className="block">
                <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                  <IndianRupee size={15} className="text-[#2563eb]" />
                  Price Per Seat (₹)
                </span>
                <input
                  className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                  min="0"
                  placeholder="e.g., 50"
                  required
                  step="0.01"
                  type="number"
                  value={form.pricePerSeat}
                  onChange={(e) =>
                    setForm({ ...form, pricePerSeat: e.target.value })
                  }
                />
              </label>

              <label className="block">
                <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                  <CarFront size={15} className="text-[#2563eb]" />
                  Additional Notes (Optional)
                </span>
                <textarea
                  className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                  placeholder="Any special instructions or preferences..."
                  rows="3"
                  value={form.notes}
                  onChange={(e) => setForm({ ...form, notes: e.target.value })}
                />
              </label>
            </div>

            <div className="mt-8 flex gap-3 border-t border-[#edf1f8] pt-5">
              <button
                className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-[0_8px_16px_rgba(37,99,235,0.16)] transition hover:bg-[#164bc7] disabled:opacity-60"
                disabled={isSubmitting}
                type="submit"
              >
                <Save size={16} />
                {isSubmitting ? 'Creating Ride...' : 'Create Ride'}
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
      </div>
    </section>
  )
}

export default CreateRidePage
