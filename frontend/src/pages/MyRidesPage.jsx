import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowRight,
  CalendarDays,
  CarFront,
  IndianRupee,
  LoaderCircle,
  MapPin,
  UserRound,
} from 'lucide-react'
import { rideApi } from '../api/client'
import useAuth from '../hooks/useAuth'

function MyRidesPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('offered')
  const [offeredRides, setOfferedRides] = useState([])
  const [bookedRides, setBookedRides] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    loadMyRides()
  }, [user?.id])

  const loadMyRides = async () => {
    if (!user?.id) return

    setIsLoading(true)
    setError('')

    try {
      const [offeredResponse, bookedResponse] = await Promise.all([
        rideApi.get(`/api/rides/driver/${user.id}`),
        rideApi.get(`/api/rides/passenger/${user.id}`),
      ])

      setOfferedRides(offeredResponse.data)
      setBookedRides(bookedResponse.data)
    } catch (err) {
      setError(
        err.response?.data?.message || 'Unable to load your rides. Please try again.'
      )
    } finally {
      setIsLoading(false)
    }
  }

  if (isLoading) {
    return (
      <section className="grid min-h-[400px] place-items-center">
        <div className="flex flex-col items-center gap-3">
          <LoaderCircle className="animate-spin text-[#2563eb]" size={32} />
          <p className="text-sm font-medium text-[#53637f]">Loading your rides...</p>
        </div>
      </section>
    )
  }

  const displayRides = activeTab === 'offered' ? offeredRides : bookedRides

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[1120px]">
        <div className="mb-6">
          <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
            My Rides
          </h1>
          <p className="mt-1 text-sm text-[#647590]">
            Manage your offered rides and bookings
          </p>
        </div>

        <div className="mb-6 flex gap-2 overflow-hidden rounded-lg border border-[#e0e9f7] bg-white p-1">
          <button
            className={`flex-1 rounded-md px-4 py-2.5 text-sm font-bold transition ${
              activeTab === 'offered'
                ? 'bg-[#2563eb] text-white shadow-sm'
                : 'text-[#647590] hover:bg-slate-50'
            }`}
            onClick={() => setActiveTab('offered')}
            type="button"
          >
            Offered Rides ({offeredRides.length})
          </button>
          <button
            className={`flex-1 rounded-md px-4 py-2.5 text-sm font-bold transition ${
              activeTab === 'booked'
                ? 'bg-[#2563eb] text-white shadow-sm'
                : 'text-[#647590] hover:bg-slate-50'
            }`}
            onClick={() => setActiveTab('booked')}
            type="button"
          >
            My Bookings ({bookedRides.length})
          </button>
        </div>

        {error && (
          <div className="mb-5 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        )}

        {displayRides.length === 0 ? (
          <div className="flex flex-col items-center rounded-2xl border border-[#e0e9f7] bg-white px-8 py-10 text-center">
            <CarFront className="text-[#647590]" size={40} />
            <p className="mt-3 max-w-md text-sm font-medium text-[#53637f]">
              {activeTab === 'offered'
                ? 'You have not offered any rides yet. Create a ride to get started!'
                : 'You have not booked any rides yet. Find a ride to get started!'}
            </p>
            <button
              className="mt-4 inline-flex items-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-[#164bc7]"
              onClick={() =>
                navigate(activeTab === 'offered' ? '/rides/create' : '/rides/find')
              }
              type="button"
            >
              {activeTab === 'offered' ? 'Create Ride' : 'Find Rides'}
            </button>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2">
            {displayRides.map((ride) => (
              <RideCard
                key={ride.id}
                ride={ride}
                onClick={() => navigate(`/rides/${ride.id}`)}
              />
            ))}
          </div>
        )}
      </div>
    </section>
  )
}

function RideCard({ ride, onClick }) {
  const departureDate = new Date(ride.departureTime).toLocaleDateString('en-IN', {
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  })
  const departureTime = new Date(ride.departureTime).toLocaleTimeString('en-IN', {
    hour: '2-digit',
    minute: '2-digit',
  })

  const statusColors = {
    PENDING: 'bg-yellow-100 text-yellow-700',
    ACCEPTED: 'bg-blue-100 text-blue-700',
    STARTED: 'bg-purple-100 text-purple-700',
    COMPLETED: 'bg-green-100 text-green-700',
    CANCELLED: 'bg-red-100 text-red-700',
  }

  return (
    <article
      className="group cursor-pointer overflow-hidden rounded-xl border border-[#e0e9f7] bg-white p-5 shadow-sm transition hover:border-[#2563eb] hover:shadow-md"
      onClick={onClick}
    >
      <div className="mb-4 flex items-start justify-between">
        <div className="flex items-center gap-3">
          <div className="flex size-10 items-center justify-center rounded-full bg-[#eef5ff] text-sm font-bold text-[#2563eb]">
            <UserRound size={18} />
          </div>
          <div>
            <p className="text-sm font-bold text-[#182950]">
              {ride.type === 'OFFERING' ? 'You (Driver)' : 'Your Booking'}
            </p>
            <p className="text-xs text-[#647590]">Ride ID: {ride.id}</p>
          </div>
        </div>
        <span
          className={`rounded-full px-3 py-1 text-xs font-bold ${
            statusColors[ride.status] || 'bg-gray-100 text-gray-700'
          }`}
        >
          {ride.status}
        </span>
      </div>

      <div className="mb-4 space-y-3">
        <div className="flex items-start gap-2">
          <MapPin className="mt-0.5 flex-shrink-0 text-[#2563eb]" size={16} />
          <div className="min-w-0">
            <p className="text-xs font-medium text-[#647590]">From</p>
            <p className="truncate text-sm font-bold text-[#182950]">{ride.source}</p>
          </div>
        </div>
        <div className="flex items-start gap-2">
          <MapPin className="mt-0.5 flex-shrink-0 text-[#f97316]" size={16} />
          <div className="min-w-0">
            <p className="text-xs font-medium text-[#647590]">To</p>
            <p className="truncate text-sm font-bold text-[#182950]">{ride.destination}</p>
          </div>
        </div>
      </div>

      <div className="mb-4 flex items-center gap-4 text-xs text-[#647590]">
        <div className="flex items-center gap-1">
          <CalendarDays size={14} />
          <span>{departureDate}</span>
        </div>
        <div className="font-medium">{departureTime}</div>
      </div>

      <div className="flex items-center justify-between border-t border-[#edf1f8] pt-4">
        <div className="flex items-center gap-1">
          <IndianRupee size={16} className="text-[#2563eb]" />
          <span className="text-lg font-bold text-[#182950]">{ride.pricePerSeat}</span>
          <span className="text-xs text-[#647590]">per seat</span>
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm font-bold text-[#647590]">{ride.availableSeats} seats</span>
          <ArrowRight
            className="text-[#2563eb] transition group-hover:translate-x-1"
            size={18}
          />
        </div>
      </div>
    </article>
  )
}

export default MyRidesPage
