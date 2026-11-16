import { useCallback, useEffect, useRef, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { ArrowRight, CalendarDays, CarFront, ChevronDown, IndianRupee, LoaderCircle, MapPin, Search, SlidersHorizontal, UserRound } from 'lucide-react'
import { rideApi } from '../api/client'
import useAuth from '../hooks/useAuth'
import { EMPTY_RIDE_SEARCH, buildRideSearchPayload, localToday, matchesDisplayedRide, searchFormFromParams, searchParamsFromForm, VEHICLE_TYPES } from '../utils/findRidesSearch'

const fieldClass = 'w-full rounded-lg border border-[#cfdbec] bg-white px-3.5 py-2.5 text-sm font-medium text-[#162750] outline-none transition focus:border-[#2563eb] focus:ring-4 focus:ring-[#e5efff]'

export default function FindRidesPage() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const currentUserId = user?.id
  const [params, setParams] = useSearchParams()
  const [form, setForm] = useState(() => searchFormFromParams(params))
  const [filtersOpen, setFiltersOpen] = useState(false)
  const [rides, setRides] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const activeRequest = useRef(null)
  const requestVersion = useRef(0)

  const load = useCallback(async (criteria) => {
    let payload
    try {
      payload = buildRideSearchPayload(criteria)
    } catch (validationError) {
      setError(validationError.message)
      setRides([])
      setLoading(false)
      return
    }
    activeRequest.current?.abort()
    const controller = new AbortController()
    activeRequest.current = controller
    const version = ++requestVersion.current
    setLoading(true)
    setError('')
    try {
      const { data } = await rideApi.post('/api/rides/search', payload, { signal: controller.signal })
      if (version !== requestVersion.current) return
      const available = data.filter((ride) => matchesDisplayedRide(ride, criteria, currentUserId))
      setRides(available)
    } catch (err) {
      if (err.code === 'ERR_CANCELED' || version !== requestVersion.current) return
      setError(err.response?.data?.message || 'Unable to load rides. Please try again.')
      setRides([])
    } finally {
      if (version === requestVersion.current) setLoading(false)
    }
  }, [currentUserId])

  useEffect(() => {
    const timer = setTimeout(() => load(form), 300)
    return () => {
      clearTimeout(timer)
      activeRequest.current?.abort()
    }
  }, [form, load])

  const set = (name, value) => setForm((old) => {
    const next = { ...old, [name]: value }
    setParams(searchParamsFromForm(next), { replace: true })
    return next
  })
  const submit = (event) => { event.preventDefault(); load(form) }
  const reset = () => { setForm(EMPTY_RIDE_SEARCH); setParams({}) }
  const activeFilterCount = ['minimumAvailableSeats', 'vehicleType', 'minimumPrice', 'maximumPrice', 'sortOrder'].filter((key) => form[key]).length

  return <section className="bg-[#f6f9ff] px-1 py-1 sm:px-2 sm:py-2">
    <div className="mx-auto max-w-[1120px]">
      <div className="mb-6">
        <h1 className="m-0 text-2xl font-bold tracking-[-0.035em] text-[#12214a] sm:text-[1.8rem]">Find Rides</h1>
        <p className="mt-1 text-sm text-[#647590]">Search available campus rides by route, date, seats and price.</p>
      </div>

      <div className="mb-6 rounded-2xl border border-[#e0e9f7] bg-white p-4 shadow-sm sm:p-6">
        <form onSubmit={submit}>
          <div className="grid gap-4 md:grid-cols-3">
            <Field label="From" icon={MapPin}><input className={fieldClass} placeholder="Enter source location" value={form.source} onChange={(event) => set('source', event.target.value)} /></Field>
            <Field label="To" icon={MapPin}><input className={fieldClass} placeholder="Enter destination" value={form.destination} onChange={(event) => set('destination', event.target.value)} /></Field>
            <Field label="Travel date" icon={CalendarDays}><input className={fieldClass} type="date" min={localToday()} value={form.date} onChange={(event) => set('date', event.target.value)} /></Field>
          </div>

          <div className="mt-5 border-t border-[#edf1f8] pt-3">
            <button className="flex w-full items-center justify-between rounded-lg px-1 py-2 text-left text-sm font-bold text-[#304260] transition hover:bg-[#f6f9ff] focus:outline-none focus:ring-4 focus:ring-[#e5efff]" type="button" aria-expanded={filtersOpen} aria-controls="ride-filters" onClick={() => setFiltersOpen((open) => !open)}>
              <span className="flex items-center gap-2"><SlidersHorizontal size={17} className="text-[#2563eb]" />Filters &amp; Sorting{activeFilterCount > 0 && <span className="rounded-full bg-[#e5efff] px-2 py-0.5 text-xs text-[#2563eb]">{activeFilterCount}</span>}</span>
              <ChevronDown size={19} className={`text-[#2563eb] transition-transform duration-200 ${filtersOpen ? 'rotate-180' : ''}`} aria-hidden="true" />
            </button>

            {filtersOpen && <div id="ride-filters" className="mt-4 border-t border-[#edf1f8] pt-4">
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
                <Field label="Minimum available seats"><select className={fieldClass} value={form.minimumAvailableSeats} onChange={(event) => set('minimumAvailableSeats', event.target.value)}><option value="">Any seats</option>{[1, 2, 3, 4, 5, 6].map((number) => <option key={number} value={number}>{number}+ seats</option>)}</select></Field>
                <Field label="Vehicle type"><select className={fieldClass} value={form.vehicleType} onChange={(event) => set('vehicleType', event.target.value)}><option value="">Any vehicle</option>{VEHICLE_TYPES.map((type) => <option key={type} value={type}>{type}</option>)}</select></Field>
                <Field label="Minimum price (₹)"><input className={fieldClass} type="number" min="0" step="0.01" placeholder="No minimum" value={form.minimumPrice} onChange={(event) => set('minimumPrice', event.target.value)} /></Field>
                <Field label="Maximum price (₹)"><input className={fieldClass} type="number" min="0" step="0.01" placeholder="No maximum" value={form.maximumPrice} onChange={(event) => set('maximumPrice', event.target.value)} /></Field>
              </div>
              <div className="mt-4 md:max-w-xs"><Field label="Sort by"><select className={fieldClass} value={form.sortOrder} onChange={(event) => set('sortOrder', event.target.value)}><option value="">Latest rides</option><option value="LOWEST_PRICE">Lowest price</option><option value="HIGHEST_PRICE">Highest price</option></select></Field></div>
            </div>}
          </div>

          <div className="mt-5 flex flex-col gap-3 md:flex-row">
            <button className="inline-flex min-h-11 items-center justify-center gap-2 rounded-lg bg-[#2563eb] px-5 py-2.5 text-sm font-bold text-white shadow-[0_8px_16px_rgba(37,99,235,0.16)] transition hover:bg-[#164bc7]" type="submit"><Search size={16} />Search Rides</button>
            <button className="min-h-11 rounded-lg border border-[#cdd9eb] bg-white px-5 py-2.5 text-sm font-bold text-[#273a60] transition hover:bg-slate-50" onClick={reset} type="button">Reset All</button>
          </div>
        </form>
      </div>

      {error && <div className="mb-5 rounded-lg border border-red-100 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">{error}</div>}
      <p className="mb-4 text-sm text-[#647590]">{loading ? 'Updating results…' : <><span className="font-bold text-[#2563eb]">{rides.length} {rides.length === 1 ? 'ride' : 'rides'} found</span> matching your search</>}</p>
      {loading ? <Loading /> : rides.length === 0 ? <Empty /> : <div className="grid gap-4 sm:grid-cols-2">{rides.map((ride) => <RideCard key={ride.id} ride={ride} onClick={() => navigate(user?.role === 'DRIVER' ? `/driver/rides/find/${ride.id}` : `/rides/${ride.id}`)} />)}</div>}
    </div>
  </section>
}

function Field({ label, icon: Icon, children }) { return <label className="block"><span className="mb-2 flex items-center gap-1.5 text-sm font-bold text-[#304260]">{Icon && <Icon size={15} className="text-[#2563eb]" />}{label}</span>{children}</label> }
function Loading() { return <div className="grid min-h-[260px] place-items-center"><div className="flex flex-col items-center gap-3"><LoaderCircle className="animate-spin text-[#2563eb]" size={32} /><p className="text-sm font-medium text-[#53637f]">Loading available rides...</p></div></div> }
function Empty() { return <div className="flex flex-col items-center rounded-2xl border border-[#e0e9f7] bg-white px-8 py-10 text-center"><CarFront className="text-[#647590]" size={40} /><p className="mt-3 max-w-md text-sm font-medium text-[#53637f]">No available rides match your search. Try adjusting the filters or check back later.</p></div> }
function RideCard({ ride, onClick }) { const departure = new Date(ride.departureTime); return <article className="group cursor-pointer overflow-hidden rounded-xl border border-[#e0e9f7] bg-white p-5 shadow-sm transition hover:border-[#2563eb] hover:shadow-md" onClick={onClick}><div className="mb-4 flex items-start justify-between"><div className="flex items-center gap-3"><div className="flex size-10 items-center justify-center rounded-full bg-[#eef5ff] text-[#2563eb]"><UserRound size={18} /></div><div><p className="text-sm font-bold text-[#182950]">Driver</p><p className="text-xs text-[#647590]">ID: {ride.driverId}</p>{ride.vehicleInfo?.vehicleNumber && <p className="mt-1 text-sm font-bold text-[#12214a]">🚗 {ride.vehicleInfo.vehicleNumber} · {ride.vehicleInfo.vehicleType}</p>}</div></div><span className="rounded-full bg-green-100 px-3 py-1 text-xs font-bold text-green-700">{ride.status}</span></div><Point label="From" value={ride.source} /><Point label="To" value={ride.destination} destination /><div className="my-4 flex items-center gap-4 text-xs text-[#647590]"><CalendarDays size={14} /><span>{departure.toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}</span><span>{departure.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}</span></div><div className="flex items-center justify-between border-t border-[#edf1f8] pt-4"><div className="flex items-center gap-1"><IndianRupee size={16} className="text-[#2563eb]" /><span className="text-lg font-bold text-[#182950]">{ride.pricePerSeat}</span><span className="text-xs text-[#647590]">per seat</span></div><div className="flex items-center gap-2"><span className="text-sm font-bold text-[#647590]">{ride.availableSeats} seats</span><ArrowRight className="text-[#2563eb]" size={18} /></div></div></article> }
function Point({ label, value, destination }) { return <div className="mb-3 flex items-start gap-2"><MapPin className={destination ? 'text-[#f97316]' : 'text-[#2563eb]'} size={16} /><div><p className="text-xs text-[#647590]">{label}</p><p className="text-sm font-bold text-[#182950]">{value}</p></div></div> }
