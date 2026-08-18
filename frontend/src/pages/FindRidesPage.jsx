import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  ArrowRight,
  CalendarDays,
  CarFront,
  IndianRupee,
  LoaderCircle,
  MapPin,
  Search,
  UserRound,
} from 'lucide-react'
import { rideApi } from '../api/client'

function FindRidesPage() {
  const navigate = useNavigate()
  const [rides, setRides] = useState([])
  const [filteredRides, setFilteredRides] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [searchForm, setSearchForm] = useState({
    source: '',
    destination: '',
    date: '',
  })

  useEffect(() => {
    loadAvailableRides()
  }, [])

  const loadAvailableRides = async () => {
    setIsLoading(true)
    setError('')
    try {
      const { data } = await rideApi.post('/api/rides/search', {
        type: 'OFFERING',
        status: 'PENDING',
      })
      setRides(data)
      setFilteredRides(data)
    } catch (err) {
      setError(err.response?.data?.message || 'Unable to load rides. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  const handleSearch = (e) => {
    e.preventDefault()
    
    let filtered = rides

    if (searchForm.source) {
      filtered = filtered.filter(ride => 
        ride.source.toLowerCase().includes(searchForm.source.toLowerCase())
      )
    }

    if (searchForm.destination) {
      filtered = filtered.filter(ride => 
        ride.destination.toLowerCase().includes(searchForm.destination.toLowerCase())
      )
    }

    if (searchForm.date) {
      filtered = filtered.filter(ride => 
        ride.departureTime.startsWith(searchForm.date)
      )
    }

    setFilteredRides(filtered)
  }

  const handleReset = () => {
    setSearchForm({ source: '', destination: '', date: '' })
    setFilteredRides(rides)
  }

  if (isLoading) {
    return (
      <section className="grid min-h-[400px] place-items-center">
        <div className="flex flex-col items-center gap-3">
          <LoaderCircle className="animate-spin text-[#2563eb]" size={32} />
          <p className="text-sm font-medium text-[#53637f]">Loading available rides...</p>
        </div>
      </section>
    )
  }

  return (
    <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
      <div className="mx-auto max-w-[1120px]">
        <div className="mb-6">
          <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">
            Find Rides
          </h1>
          <p className="mt-1 text-sm text-[#647590]">
            Search for rides going to your destination
          </p>
        </div>

        <div className="mb-6 overflow-hidden rounded-2xl border border-[#e0e9f7] bg-white p-5 shadow-sm sm:p-6">
          <form onSubmit={handleSearch}>
            <div className="grid gap-4 sm:grid-cols-3">
              <label className="block">
                <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                  <MapPin size={15} className="text-[#2563eb]" />
                  From
                </span>
                <input
                  className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                  placeholder="Enter source location"
                  type="text"
                  value={searchForm.source}
                  onChange={(e) => setSearchForm({ ...searchForm, source: e.target.value })}
                />
              </label>

              <label className="block">
                <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                  <MapPin size={15} className="text-[#2563eb]" />
                  To
                </span>
                <input
                  className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                  placeholder="Enter destination"
                  type="text"
                  value={searchForm.destination}
                  onChange={(e) => setSearchForm({ ...searchForm, destination: e.target.value })}
                />
              </label>

              <label className="block">
                <span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">
                  <CalendarDays size={15} className="text-[#2563eb]" />
                  Date
                </span>
                <input
                  className="w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]"
                  type="date"
                  value={searchForm.date}
                  onChange={(e) => setSearchForm({ ...searchForm, date: e.target.value })}
                />
              </label>
            </div>

            <div className="mt-4 flex gap-3">
              <button
                className="inline-flex items-center justify-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-[0_8px_16px_rgba(37,99,235,0.16)] transition hover:bg-[#164bc7]"
                type="submit"
              >
                <Search size={16} />
                Search Rides
              </button>
              <button
                className="inline-flex items-center justify-center gap-2 rounded-lg border border-[#cdd9eb] bg-white px-5 py-2.5 text-sm font-bold text-[#273a60] transition hover:bg-slate-50"
                onClick={handleReset}
                type="button"
              >
                Reset
              </button>
            </div>
          </form>
        </div>

        {error && (
          <div className="mb-5 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
            {error}
          </div>
        )}

        {filteredRides.length === 0 ? (
          <div className="flex flex-col items-center rounded-2xl border border-[#e0e9f7] bg-white px-8 py-10 text-center">
            <CarFront className="text-[#647590]" size={40} />
            <p className="mt-3 max-w-md text-sm font-medium text-[#53637f]">
              No rides found matching your search. Try adjusting your filters or check back later.
            </p>
          </div>
        ) : (
          <div className="grid gap-4 sm:grid-cols-2">
            {filteredRides.map((ride) => (
              <RideCard key={ride.id} ride={ride} onClick={() => navigate(`/rides/${ride.id}`)} />
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
            <p className="text-sm font-bold text-[#182950]">Driver</p>
            <p className="text-xs text-[#647590]">ID: {ride.driverId}</p>
          </div>
        </div>
        <span className="rounded-full bg-green-100 px-3 py-1 text-xs font-bold text-green-700">
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

export default FindRidesPage
