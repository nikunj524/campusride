import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  AlertCircle,
  ArrowLeft,
  CalendarDays,
  CarFront,
  CheckCircle2,
  Clock,
  IndianRupee,
  LoaderCircle,
  MapPin,
  UserRound,
  X,
} from 'lucide-react'
import { rideApi } from '../api/client'
import useAuth from '../hooks/useAuth'

function RideDetailsPage() {
  const { rideId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [ride, setRide] = useState(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isActionLoading, setIsActionLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  useEffect(() => {
    loadRideDetails()
  }, [rideId])

  const loadRideDetails = async () => {
    setIsLoading(true)
    setError('')
    try {
      const { data } = await rideApi.get(`/api/rides/${rideId}`)
      setRide(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to load ride details.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleAcceptRide = async () => {
    setError('')
    setSuccess('')
    setIsActionLoading(true)

    try {
      const { data } = await rideApi.patch(`/api/rides/${rideId}/accept`, {
        passengerId: user.id,
      })
      setRide(data)
      setSuccess('Ride accepted successfully! The driver will be notified.')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to accept ride.')
    } finally {
      setIsActionLoading(false)
    }
  }

  const handleStartRide = async () => {
    setError('')
    setSuccess('')
    setIsActionLoading(true)

    try {
      const { data } = await rideApi.patch(`/api/rides/${rideId}/start`)
      setRide(data)
      setSuccess('Ride started successfully!')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to start ride.')
    } finally {
      setIsActionLoading(false)
    }
  }

  const handleCompleteRide = async () => {
    setError('')
    setSuccess('')
    setIsActionLoading(true)

    try {
      const { data } = await rideApi.patch(`/api/rides/${rideId}/complete`)
      setRide(data)
      setSuccess('Ride completed successfully!')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to complete ride.')
    } finally {
      setIsActionLoading(false)
    }
  }

  const handleCancelRide = async () => {
    if (!confirm('Are you sure you want to cancel this ride?')) return

    setError('')
    setSuccess('')
    setIsActionLoading(true)

    try {
      const { data } = await rideApi.patch(`/api/rides/${rideId}/cancel`)
      setRide(data)
      setSuccess('Ride cancelled successfully.')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to cancel ride.')
    } finally {
      setIsActionLoading(false)
    }
  }

  if (isLoading) {
    return (
      <section className="grid min-h-[400px] place-items-center">
        <div className="flex flex-col items-center gap-3">
          <LoaderCircle className="animate-spin text-[#2563eb]" size={32} />
          <p className="text-sm font-medium text-[#53637f]">Loading ride details...</p>
        </div>
      </section>
    )
  }

  if (!ride) {
    return (
      <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
        <div className="mx-auto max-w-[720px]">
          <div className="flex flex-col items-center rounded-2xl border border-[#e0e9f7] bg-white px-8 py-10 text-center">
            <AlertCircle className="text-red-500" size={40} />
            <p className="mt-3 max-w-md text-sm font-medium text-[#53637f]">
              {error || 'Ride not found.'}
            </p>
            <button
              className="mt-4 inline-flex items-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-[#164bc7]"
              onClick={() => navigate('/dashboard')}
              type="button"
            >
              Go to Dashboard
            </button>
          </div>
        </div>
      </section>
    )
  }

  const isDriver = ride.driverId === user.id
  const isPassenger = ride.passengerId === user.id
  const canAccept = !isDriver && ride.status === 'PENDING' && !ride.passengerId
  const canStart = isDriver && ride.status === 'ACCEPTED'
  const canComplete = isDriver && ride.status === 'STARTED'
  const canCancel =
    (isDriver || isPassenger) &&
    ['PENDING', 'ACCEPTED'].includes(ride.status)

  const departureDate = new Date(ride.departureTime).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  })
  const departureTime = new Date(ride.departureTime).toLocaleTimeString('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
  })

  const statusColors = {
    PENDING: 'bg-yellow-100 text-yellow-700 border-yellow-200',
    ACCEPTED: 'bg-blue-100 text-blue-700 border-blue-200',
    STARTED: 'bg-purple-100 text-purple-700 border-purple-200',
    COMPLETED: 'bg-green-100 text-green-700 border-green-200',
    CANCELLED: 'bg-red-100 text-red-700 border-red-200',
  }

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[720px]">
        <button
          className="mb-4 inline-flex items-center gap-2 text-sm font-bold text-[#2563eb] transition hover:underline"
          onClick={() => navigate(-1)}
          type="button"
        >
          <ArrowLeft size={16} />
          Back
        </button>

        <div className="mb-6 flex items-center justify-between">
          <div>
            <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
              Ride Details
            </h1>
            <p className="mt-1 text-sm text-[#647590]">Ride ID: {ride.id}</p>
          </div>
          <span
            className={`rounded-lg border px-4 py-2 text-sm font-bold ${
              statusColors[ride.status] || 'bg-gray-100 text-gray-700 border-gray-200'
            }`}
          >
            {ride.status}
          </span>
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
          <div className="p-5 sm:p-7">
            <div className="mb-6 grid gap-5 sm:grid-cols-2">
              <DetailItem icon={UserRound} label="Driver ID" value={ride.driverId} />
              <DetailItem
                icon={UserRound}
                label="Passenger ID"
                value={ride.passengerId || 'Not assigned yet'}
              />
            </div>

            <div className="mb-6 space-y-4">
              <DetailItem icon={MapPin} label="Pickup Location" value={ride.source} />
              <DetailItem
                icon={MapPin}
                iconColor="text-[#f97316]"
                label="Drop Location"
                value={ride.destination}
              />
            </div>

            <div className="mb-6 grid gap-5 sm:grid-cols-2">
              <DetailItem icon={CalendarDays} label="Date" value={departureDate} />
              <DetailItem icon={Clock} label="Time" value={departureTime} />
            </div>

            <div className="mb-6 grid gap-5 sm:grid-cols-2">
              <DetailItem
                icon={IndianRupee}
                label="Price per Seat"
                value={`₹${ride.pricePerSeat}`}
              />
              <DetailItem
                icon={CarFront}
                label="Available Seats"
                value={ride.availableSeats}
              />
            </div>

            {ride.notes && (
              <div className="mb-6">
                <DetailItem icon={AlertCircle} label="Notes" value={ride.notes} />
              </div>
            )}

            <div className="flex flex-wrap gap-3 border-t border-[#edf1f8] pt-6">
              {canAccept && (
                <button
                  className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-[#164bc7] disabled:opacity-60 sm:flex-none"
                  disabled={isActionLoading}
                  onClick={handleAcceptRide}
                  type="button"
                >
                  <CheckCircle2 size={16} />
                  {isActionLoading ? 'Processing...' : 'Book This Ride'}
                </button>
              )}

              {canStart && (
                <button
                  className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-purple-600 px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-purple-700 disabled:opacity-60 sm:flex-none"
                  disabled={isActionLoading}
                  onClick={handleStartRide}
                  type="button"
                >
                  <CarFront size={16} />
                  {isActionLoading ? 'Processing...' : 'Start Ride'}
                </button>
              )}

              {canComplete && (
                <button
                  className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg bg-green-600 px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-green-700 disabled:opacity-60 sm:flex-none"
                  disabled={isActionLoading}
                  onClick={handleCompleteRide}
                  type="button"
                >
                  <CheckCircle2 size={16} />
                  {isActionLoading ? 'Processing...' : 'Complete Ride'}
                </button>
              )}

              {canCancel && (
                <button
                  className="inline-flex flex-1 items-center justify-center gap-2 rounded-lg border border-red-200 bg-white px-5 py-2.5 text-sm font-bold text-red-600 shadow-sm transition hover:bg-red-50 disabled:opacity-60 sm:flex-none"
                  disabled={isActionLoading}
                  onClick={handleCancelRide}
                  type="button"
                >
                  <X size={16} />
                  {isActionLoading ? 'Processing...' : 'Cancel Ride'}
                </button>
              )}
            </div>
          </div>
        </article>
      </div>
    </section>
  )
}

function DetailItem({ icon: Icon, iconColor = 'text-[#2563eb]', label, value }) {
  return (
    <div>
      <dt className="mb-2 flex items-center gap-1.5 text-xs font-bold uppercase tracking-[0.035em] text-[#72829d]">
        <Icon className={iconColor} size={14} />
        {label}
      </dt>
      <dd className="text-sm font-bold text-[#182950]">{value}</dd>
    </div>
  )
}

export default RideDetailsPage
