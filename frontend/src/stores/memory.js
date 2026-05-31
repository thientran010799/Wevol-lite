import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'
import dayjs from 'dayjs'

const MOCK_MEMORIES = [
  { id: '1', title: 'First cherry blossoms', location: 'Hanoi',    memoryDate: '2025-04-05', date: 'Apr 5, 2025',  mood: 'happy',     photos: 3,  note: 'We walked around Hoan Kiem lake and found the most beautiful blossoms.' },
  { id: '2', title: 'Anniversary dinner',    location: 'HCMC',     memoryDate: '2025-02-14', date: 'Feb 14, 2025', mood: 'romantic',  photos: 8,  note: 'Candlelight dinner at the rooftop restaurant. Perfect evening.' },
  { id: '3', title: 'Phu Quoc sunrise',      location: 'Phu Quoc', memoryDate: '2024-12-28', date: 'Dec 28, 2024', mood: 'adventure', photos: 12, note: 'Woke up at 5am to catch the sunrise on the beach. Worth it.' },
  { id: '4', title: 'First flight together', location: 'Bangkok',  memoryDate: '2024-10-10', date: 'Oct 10, 2024', mood: 'happy',     photos: 5,  note: null },
  { id: '5', title: 'Cooking together',      location: 'Home',     memoryDate: '2025-01-20', date: 'Jan 20, 2025', mood: 'cozy',      photos: 5,  note: 'Made pasta from scratch. Flour everywhere but it was delicious.' },
  { id: '6', title: 'Her birthday',          location: 'HCMC',     memoryDate: '2024-08-03', date: 'Aug 3, 2024',  mood: 'happy',     photos: 9,  note: null },
]

// Map API response → frontend shape
function fromApi(m) {
  return {
    id: m.id,
    coupleId: m.coupleId,
    createdBy: m.createdBy,
    title: m.title,
    location: m.location || '',
    memoryDate: m.memoryDate ?? null,                                    // ISO "2025-04-05"
    date: m.memoryDate ? dayjs(m.memoryDate).format('MMM D, YYYY') : '', // display "Apr 5, 2025"
    mood: m.mood || 'happy',
    note: m.note || null,
    photos: m.mediaCount ?? 0,
    mediaIds: m.mediaIds ?? [],
    mediaUrls: m.mediaUrls ?? [],
    createdAt: m.createdAt,
    updatedAt: m.updatedAt,
  }
}

// Map frontend form payload → API request body
function toApi(payload) {
  return {
    title: payload.title,
    location: payload.location || null,
    memoryDate: payload.date || null,   // form uses ISO YYYY-MM-DD
    mood: payload.mood || 'happy',
    note: payload.note || null,
  }
}

export const useMemoryStore = defineStore('memory', () => {
  const memories = ref([...MOCK_MEMORIES])

  async function fetchAll() {
    try {
      const { data } = await api.get('/memories')
      memories.value = data.map(fromApi)
    } catch {
      // keep mock data when backend is unavailable
    }
  }

  function getById(id) {
    return memories.value.find(m => String(m.id) === String(id))
  }

  async function create(payload) {
    try {
      const { data } = await api.post('/memories', toApi(payload))
      const memory = fromApi(data)
      memories.value.unshift(memory)
      return memory
    } catch {
      const memory = {
        id: crypto.randomUUID(),
        photos: 0,
        memoryDate: payload.date || null,
        date: payload.date ? dayjs(payload.date).format('MMM D, YYYY') : '',
        ...payload,
      }
      memories.value.unshift(memory)
      return memory
    }
  }

  async function update(id, payload) {
    try {
      const { data } = await api.put(`/memories/${id}`, toApi(payload))
      const memory = fromApi(data)
      const idx = memories.value.findIndex(m => String(m.id) === String(id))
      if (idx !== -1) memories.value[idx] = memory
      return memory
    } catch {
      const idx = memories.value.findIndex(m => String(m.id) === String(id))
      if (idx !== -1) {
        memories.value[idx] = {
          ...memories.value[idx],
          ...payload,
          memoryDate: payload.date || memories.value[idx].memoryDate,
          date: payload.date ? dayjs(payload.date).format('MMM D, YYYY') : memories.value[idx].date,
        }
      }
      return memories.value.find(m => String(m.id) === String(id))
    }
  }

  async function remove(id) {
    try {
      await api.delete(`/memories/${id}`)
    } catch {
      // fall through — remove from local state regardless
    }
    memories.value = memories.value.filter(m => String(m.id) !== String(id))
  }

  return { memories, fetchAll, getById, create, update, remove }
})
