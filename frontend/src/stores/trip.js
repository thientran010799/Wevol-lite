import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'
import dayjs from 'dayjs'

const MOCK_TRIPS = [
  {
    id: '1',
    name: 'Da Nang beach',
    start_date: '2025-05-10',
    end_date: '2025-05-14',
    dates: 'May 10–14, 2025',
    status: 'planning',
    raw_budget: 8400000,
    currency: 'VND',
    budget: '8,400,000 VND',
    duration: '5 days',
    notes: null,
    days: [
      {
        number: 1, label: 'May 10',
        stops: [
          { id: 's1', type: 'flight',   place: 'Flight HAN → DAD',          time: '07:30', address: null,                   cost: '1,200,000 VND',    raw_cost: 1200000 },
          { id: 's2', type: 'hotel',    place: 'Mường Thanh Luxury',         time: '14:00', address: 'Bắc Mỹ Phú, Da Nang', cost: '850,000 VND/night', raw_cost: 850000 },
          { id: 's3', type: 'food',     place: 'Dinner at Bà Mị',            time: '19:00', address: 'Ngũ Hành Sơn',        cost: null,               raw_cost: null },
        ],
      },
      {
        number: 2, label: 'May 11',
        stops: [
          { id: 's4', type: 'activity', place: 'Marble Mountains',           time: '08:00', address: 'Ngũ Hành Sơn',        cost: '40,000 VND',       raw_cost: 40000 },
          { id: 's5', type: 'food',     place: 'Bún chả cá Bà Tới',          time: '12:00', address: 'Quận Hải Châu',       cost: null,               raw_cost: null },
          { id: 's6', type: 'activity', place: 'My Khe Beach',                time: '15:00', address: 'Sơn Trà',             cost: null,               raw_cost: null },
        ],
      },
    ],
  },
  {
    id: '2',
    name: 'Phu Quoc island',
    start_date: '2024-12-26',
    end_date: '2024-12-31',
    dates: 'Dec 26–31, 2024',
    status: 'done',
    raw_budget: 12000000,
    currency: 'VND',
    budget: '12,000,000 VND',
    duration: '6 days',
    notes: null,
    days: [
      {
        number: 1, label: 'Dec 26',
        stops: [
          { id: 's1', type: 'flight', place: 'Flight SGN → PQC',              time: '10:00', address: null,      cost: '1,500,000 VND',    raw_cost: 1500000 },
          { id: 's2', type: 'hotel', place: 'Premier Residences Phu Quoc',    time: '13:00', address: 'Bãi Dài', cost: '2,200,000 VND/night', raw_cost: 2200000 },
        ],
      },
    ],
  },
  {
    id: '3',
    name: 'Hanoi culture',
    start_date: '2024-04-03',
    end_date: '2024-04-06',
    dates: 'Apr 3–6, 2024',
    status: 'done',
    raw_budget: 5200000,
    currency: 'VND',
    budget: '5,200,000 VND',
    duration: '4 days',
    notes: null,
    days: [],
  },
  {
    id: '4',
    name: 'Bangkok',
    start_date: '2024-10-08',
    end_date: '2024-10-12',
    dates: 'Oct 8–12, 2024',
    status: 'done',
    raw_budget: 18500000,
    currency: 'VND',
    budget: '18,500,000 VND',
    duration: '5 days',
    notes: null,
    days: [],
  },
]

function buildDisplayStrings({ start_date, end_date, raw_budget, currency }) {
  const result = {}
  if (start_date && end_date) {
    const s = dayjs(start_date)
    const e = dayjs(end_date)
    result.dates = s.year() === e.year() && s.month() === e.month()
      ? `${s.format('MMM D')}–${e.format('D, YYYY')}`
      : `${s.format('MMM D')} – ${e.format('MMM D, YYYY')}`
    const n = e.diff(s, 'day') + 1
    result.duration = `${n} day${n !== 1 ? 's' : ''}`
  } else if (start_date) {
    result.dates = dayjs(start_date).format('MMM D, YYYY')
    result.duration = ''
  } else {
    result.dates = ''
    result.duration = ''
  }
  result.budget = raw_budget != null && raw_budget !== ''
    ? `${Number(raw_budget).toLocaleString()} ${currency || 'VND'}`
    : ''
  return result
}

// Converts API response (camelCase, flat stops[]) → store shape (snake_case, days[])
function normalizeTrip(t) {
  const start_date = t.startDate || null
  const end_date   = t.endDate   || null
  const raw_budget = t.budget != null ? Number(t.budget) : null
  const currency   = t.currency || 'VND'
  const display    = buildDisplayStrings({ start_date, end_date, raw_budget, currency })

  const stopsByDay = {}
  for (const s of (t.stops || [])) {
    const dn = s.dayNumber
    if (!stopsByDay[dn]) stopsByDay[dn] = []
    stopsByDay[dn].push({
      id:       s.id,
      type:     s.placeType || 'activity',
      place:    s.placeName || '',
      time:     s.time     || null,
      address:  s.address  || null,
      cost:     s.cost != null ? `${Number(s.cost).toLocaleString()} ${s.currency || currency}` : null,
      raw_cost: s.cost != null ? Number(s.cost) : null,
    })
  }

  const days = Object.keys(stopsByDay)
    .map(Number)
    .sort((a, b) => a - b)
    .map(dn => ({
      number: dn,
      label:  start_date ? dayjs(start_date).add(dn - 1, 'day').format('MMM D') : '',
      stops:  stopsByDay[dn],
    }))

  return {
    id: t.id,
    name: t.name,
    start_date,
    end_date,
    ...display,
    status:     t.status     || 'planning',
    raw_budget,
    currency,
    notes:      t.notes      || null,
    days,
  }
}

export const useTripStore = defineStore('trip', () => {
  const trips = ref([])
  const loading = ref(false)

  async function fetchAll() {
    loading.value = true
    try {
      const { data } = await api.get('/trips')
      trips.value = data.map(normalizeTrip)
    } catch {
      // keep empty on error
    } finally {
      loading.value = false
    }
  }

  function getById(id) {
    return trips.value.find(t => String(t.id) === String(id))
  }

  async function create(payload) {
    const display = buildDisplayStrings({
      start_date: payload.start_date,
      end_date:   payload.end_date,
      raw_budget: payload.budget,
      currency:   payload.currency,
    })
    try {
      const { data } = await api.post('/trips', {
        name:      payload.name,
        startDate: payload.start_date || null,
        endDate:   payload.end_date   || null,
        status:    payload.status     || 'planning',
        budget:    payload.budget != null ? Number(payload.budget) : null,
        currency:  payload.currency   || 'VND',
        notes:     payload.notes      || null,
      })
      const normalized = normalizeTrip(data)
      trips.value.unshift(normalized)
      return normalized
    } catch {
      const trip = {
        id:         crypto.randomUUID(),
        name:       payload.name,
        start_date: payload.start_date || null,
        end_date:   payload.end_date   || null,
        ...display,
        status:     payload.status  || 'planning',
        raw_budget: payload.budget != null ? Number(payload.budget) : null,
        currency:   payload.currency || 'VND',
        notes:      payload.notes   || null,
        days:       [],
      }
      trips.value.unshift(trip)
      return trip
    }
  }

  async function update(id, payload) {
    const existing = trips.value.find(t => String(t.id) === String(id))
    const display = buildDisplayStrings({
      start_date: payload.start_date !== undefined ? payload.start_date : existing?.start_date,
      end_date:   payload.end_date   !== undefined ? payload.end_date   : existing?.end_date,
      raw_budget: payload.raw_budget !== undefined ? payload.raw_budget : existing?.raw_budget,
      currency:   payload.currency   !== undefined ? payload.currency   : existing?.currency,
    })
    try {
      const { data } = await api.put(`/trips/${id}`, {
        name:      payload.name,
        startDate: payload.start_date || null,
        endDate:   payload.end_date   || null,
        status:    payload.status,
        budget:    payload.raw_budget != null ? Number(payload.raw_budget) : null,
        currency:  payload.currency,
        notes:     payload.notes || null,
      })
      // merge normalized response but preserve local days (stops unchanged by this call)
      const normalized = normalizeTrip(data)
      const idx = trips.value.findIndex(t => String(t.id) === String(id))
      if (idx !== -1) {
        trips.value[idx] = { ...normalized, days: trips.value[idx].days }
      }
      return trips.value[idx]
    } catch {
      const fullUpdate = {
        name:       payload.name,
        start_date: payload.start_date,
        end_date:   payload.end_date,
        status:     payload.status,
        raw_budget: payload.raw_budget,
        currency:   payload.currency,
        notes:      payload.notes,
        ...display,
      }
      const idx = trips.value.findIndex(t => String(t.id) === String(id))
      if (idx !== -1) trips.value[idx] = { ...trips.value[idx], ...fullUpdate }
      return trips.value[idx]
    }
  }

  function addDay(tripId) {
    const trip = trips.value.find(t => String(t.id) === String(tripId))
    if (!trip) return
    const nextNumber = trip.days.length > 0
      ? Math.max(...trip.days.map(d => d.number)) + 1
      : 1
    const label = trip.start_date
      ? dayjs(trip.start_date).add(nextNumber - 1, 'day').format('MMM D')
      : ''
    trip.days.push({ number: nextNumber, label, stops: [] })
  }

  async function addStop(tripId, dayNumber, stop) {
    const costDisplay = stop.cost
      ? `${Number(stop.cost).toLocaleString()} ${stop.currency || 'VND'}`
      : null
    const tempId = crypto.randomUUID()
    const newStop = {
      id:       tempId,
      type:     stop.type     || 'activity',
      place:    stop.place_name,
      time:     stop.time     || null,
      address:  stop.address  || null,
      cost:     costDisplay,
      raw_cost: stop.cost ? Number(stop.cost) : null,
    }
    const trip = trips.value.find(t => String(t.id) === String(tripId))
    if (trip) {
      const day = trip.days.find(d => d.number === dayNumber)
      if (day) day.stops.push(newStop)
    }
    try {
      const { data } = await api.post(`/trips/${tripId}/stops`, {
        dayNumber,
        time:      stop.time      || null,
        placeName: stop.place_name,
        placeType: stop.type      || 'activity',
        address:   stop.address   || null,
        notes:     stop.notes     || null,
        cost:      stop.cost ? Number(stop.cost) : null,
        currency:  stop.currency  || 'VND',
      })
      // replace temp id with the real server-assigned id
      if (trip) {
        const day = trip.days.find(d => d.number === dayNumber)
        if (day) {
          const s = day.stops.find(s => s.id === tempId)
          if (s) s.id = data.id
        }
      }
    } catch {
      // local state already updated
    }
    return newStop
  }

  function updateStop(tripId, dayNumber, stopId, updated) {
    const costDisplay = updated.cost
      ? `${Number(updated.cost).toLocaleString()} ${updated.currency || 'VND'}`
      : null
    const trip = trips.value.find(t => String(t.id) === String(tripId))
    if (!trip) return
    const day = trip.days.find(d => d.number === dayNumber)
    if (!day) return
    const idx = day.stops.findIndex(s => String(s.id) === String(stopId))
    if (idx === -1) return
    day.stops[idx] = {
      ...day.stops[idx],
      type:     updated.type,
      place:    updated.place_name,
      time:     updated.time    || null,
      address:  updated.address || null,
      cost:     costDisplay,
      raw_cost: updated.cost ? Number(updated.cost) : null,
    }
    api.put(`/trips/${tripId}/stops/${stopId}`, {
      time:      updated.time      || null,
      placeName: updated.place_name,
      placeType: updated.type      || 'activity',
      address:   updated.address   || null,
      notes:     updated.notes     || null,
      cost:      updated.cost ? Number(updated.cost) : null,
      currency:  updated.currency  || 'VND',
    }).catch(() => {})
  }

  function removeStop(tripId, dayNumber, stopId) {
    const trip = trips.value.find(t => String(t.id) === String(tripId))
    if (!trip) return
    const day = trip.days.find(d => d.number === dayNumber)
    if (!day) return
    day.stops = day.stops.filter(s => String(s.id) !== String(stopId))
    api.delete(`/trips/${tripId}/stops/${stopId}`).catch(() => {})
  }

  function removeDay(tripId, dayNumber) {
    const trip = trips.value.find(t => String(t.id) === String(tripId))
    if (!trip) return
    trip.days = trip.days.filter(d => d.number !== dayNumber)
    trip.days.forEach((d, i) => {
      d.number = i + 1
      d.label = trip.start_date
        ? dayjs(trip.start_date).add(i, 'day').format('MMM D')
        : ''
    })
    api.delete(`/trips/${tripId}/days/${dayNumber}`).catch(() => {})
  }

  function persistStopOrder(tripId, dayNumber, stops) {
    api.put(`/trips/${tripId}/stops/reorder`, {
      stopIds: stops.map(s => s.id),
    }).catch(() => {})
  }

  async function remove(id) {
    try {
      await api.delete(`/trips/${id}`)
    } catch {
      // proceed with local removal even if backend fails
    }
    trips.value = trips.value.filter(t => String(t.id) !== String(id))
  }

  return { trips, loading, fetchAll, getById, create, update, addDay, addStop, updateStop, removeStop, removeDay, persistStopOrder, remove }
})
