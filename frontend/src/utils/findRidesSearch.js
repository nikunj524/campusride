export const EMPTY_RIDE_SEARCH = Object.freeze({
  source: '',
  destination: '',
  date: '',
  minimumAvailableSeats: '',
  vehicleType: '',
  minimumPrice: '',
  maximumPrice: '',
  sortOrder: '',
})

export const VEHICLE_TYPES = Object.freeze(['BIKE', 'CAR'])

export function localToday() {
  const now = new Date()
  const offset = now.getTimezoneOffset() * 60_000
  return new Date(now.getTime() - offset).toISOString().slice(0, 10)
}

export function searchFormFromParams(params) {
  return Object.fromEntries(Object.keys(EMPTY_RIDE_SEARCH).map((key) => [key, params.get(key)?.trim() || '']))
}

export function searchParamsFromForm(form) {
  const params = new URLSearchParams()
  Object.keys(EMPTY_RIDE_SEARCH).forEach((key) => {
    if (form[key]) params.set(key, String(form[key]).trim())
  })
  return params
}

export function buildRideSearchPayload(criteria) {
  const minimumPrice = criteria.minimumPrice === '' ? null : Number(criteria.minimumPrice)
  const maximumPrice = criteria.maximumPrice === '' ? null : Number(criteria.maximumPrice)
  const minimumAvailableSeats = criteria.minimumAvailableSeats === '' ? null : Number(criteria.minimumAvailableSeats)

  if ((minimumPrice !== null && (!Number.isFinite(minimumPrice) || minimumPrice < 0))
      || (maximumPrice !== null && (!Number.isFinite(maximumPrice) || maximumPrice < 0))) {
    throw new Error('Prices must be valid non-negative amounts.')
  }
  if (minimumPrice !== null && maximumPrice !== null && minimumPrice > maximumPrice) {
    throw new Error('Minimum price cannot exceed maximum price.')
  }
  if (minimumAvailableSeats !== null && (!Number.isInteger(minimumAvailableSeats) || minimumAvailableSeats < 1)) {
    throw new Error('Minimum available seats must be a whole number of at least 1.')
  }

  const payload = { type: 'OFFERING' }
  if (criteria.source.trim()) payload.source = criteria.source.trim()
  if (criteria.destination.trim()) payload.destination = criteria.destination.trim()
  if (criteria.date) payload.travelDate = criteria.date
  if (minimumAvailableSeats !== null) payload.minimumAvailableSeats = minimumAvailableSeats
  if (criteria.vehicleType) payload.vehicleType = criteria.vehicleType
  if (minimumPrice !== null) payload.minimumPrice = minimumPrice
  if (maximumPrice !== null) payload.maximumPrice = maximumPrice
  if (criteria.sortOrder) payload.sortOrder = criteria.sortOrder
  return payload
}

// The server is authoritative, but this prevents an accidentally stale or misrouted
// response from rendering a card that contradicts the selected date or vehicle.
export function matchesDisplayedRide(ride, criteria, currentUserId) {
  if (currentUserId && ride.driverId === currentUserId) return false
  if (criteria.date && ride.departureTime?.slice(0, 10) !== criteria.date) return false
  if (criteria.vehicleType && ride.vehicleInfo?.vehicleType?.trim().toUpperCase() !== criteria.vehicleType.trim().toUpperCase()) return false
  return true
}
