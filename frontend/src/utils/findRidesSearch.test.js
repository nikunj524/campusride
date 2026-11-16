import assert from 'node:assert/strict'
import test from 'node:test'
import { buildRideSearchPayload, matchesDisplayedRide, searchFormFromParams, searchParamsFromForm } from './findRidesSearch.js'

test('builds the exact typed API payload for combined ride criteria', () => {
  assert.deepEqual(buildRideSearchPayload({
    source: ' GVP ', destination: 'Mahuva ', date: '2026-12-02', minimumAvailableSeats: '4',
    vehicleType: 'BIKE', minimumPrice: '100', maximumPrice: '300', sortOrder: 'LOWEST_PRICE',
  }), {
    type: 'OFFERING', source: 'GVP', destination: 'Mahuva', travelDate: '2026-12-02',
    minimumAvailableSeats: 4, vehicleType: 'BIKE', minimumPrice: 100, maximumPrice: 300,
    sortOrder: 'LOWEST_PRICE',
  })
})

test('omits empty filters and rejects invalid price ranges', () => {
  assert.deepEqual(buildRideSearchPayload({ source: '', destination: '', date: '', minimumAvailableSeats: '', vehicleType: '', minimumPrice: '', maximumPrice: '', sortOrder: '' }), { type: 'OFFERING' })
  assert.throws(() => buildRideSearchPayload({ source: '', destination: '', date: '', minimumAvailableSeats: '', vehicleType: '', minimumPrice: '301', maximumPrice: '300', sortOrder: '' }), /cannot exceed/)
  assert.throws(() => buildRideSearchPayload({ source: '', destination: '', date: '', minimumAvailableSeats: '', vehicleType: '', minimumPrice: '-1', maximumPrice: '', sortOrder: '' }), /non-negative/)
})

test('persists every active search filter in the URL', () => {
  const form = searchFormFromParams(new URLSearchParams('source=GVP&destination=Mahuva&minimumAvailableSeats=4&sortOrder=LOWEST_PRICE'))
  assert.equal(form.source, 'GVP')
  assert.equal(form.minimumAvailableSeats, '4')
  assert.equal(searchParamsFromForm(form).toString(), 'source=GVP&destination=Mahuva&minimumAvailableSeats=4&sortOrder=LOWEST_PRICE')
})

test('never displays previous or next dates, or a mismatched vehicle', () => {
  const criteria = { source: '', destination: '', date: '2026-11-20', minimumAvailableSeats: '', vehicleType: 'CAR', minimumPrice: '', maximumPrice: '', sortOrder: '' }
  const carOnDate = { id: 1, driverId: 2, departureTime: '2026-11-20T09:00:00', vehicleInfo: { vehicleType: 'CAR' } }
  const bikeOnDate = { id: 2, driverId: 3, departureTime: '2026-11-20T09:00:00', vehicleInfo: { vehicleType: 'BIKE' } }
  const carPreviousDay = { id: 3, driverId: 4, departureTime: '2026-11-19T09:00:00', vehicleInfo: { vehicleType: 'CAR' } }
  const carNextDay = { id: 4, driverId: 5, departureTime: '2026-11-21T09:00:00', vehicleInfo: { vehicleType: 'CAR' } }
  assert.equal(matchesDisplayedRide(carOnDate, criteria), true)
  assert.equal(matchesDisplayedRide(bikeOnDate, criteria), false)
  assert.equal(matchesDisplayedRide(carPreviousDay, criteria), false)
  assert.equal(matchesDisplayedRide(carNextDay, criteria), false)
})
