import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'

const MOCK_ALBUMS = [
  {
    id: '1',
    name: 'Cherry blossoms',
    emoji: '🌸',
    photos: [
      { id: 'p1-0', url: null },
      { id: 'p1-1', url: null },
      { id: 'p1-2', url: null },
    ],
  },
  {
    id: '2',
    name: 'Anniversary dinner',
    emoji: '🕯️',
    photos: Array.from({ length: 8 }, (_, i) => ({ id: `p2-${i}`, url: null })),
  },
  {
    id: '3',
    name: 'Phu Quoc',
    emoji: '🌅',
    photos: Array.from({ length: 12 }, (_, i) => ({ id: `p3-${i}`, url: null })),
  },
  {
    id: '4',
    name: 'Bangkok',
    emoji: '✈️',
    photos: Array.from({ length: 5 }, (_, i) => ({ id: `p4-${i}`, url: null })),
  },
  {
    id: '5',
    name: 'Cooking together',
    emoji: '🍝',
    photos: Array.from({ length: 5 }, (_, i) => ({ id: `p5-${i}`, url: null })),
  },
  {
    id: '6',
    name: 'Her birthday',
    emoji: '🎂',
    photos: Array.from({ length: 9 }, (_, i) => ({ id: `p6-${i}`, url: null })),
  },
]

function normalizeAlbum(raw) {
  return {
    id: raw.id,
    name: raw.title ?? raw.name ?? '',
    emoji: raw.emoji ?? '📸',
    coverUrl: raw.coverUrl ?? raw.cover_url ?? null,
    photos: (raw.photos ?? []).map(p => ({
      id: p.id ?? p.mediaId,
      url: p.url ?? p.thumbUrl ?? null,
      thumbUrl: p.thumbUrl ?? null,
      mediaId: p.id ?? p.mediaId,
    })),
  }
}

export const useGalleryStore = defineStore('gallery', () => {
  const albums = ref([])
  const loading = ref(false)

  async function fetchAll() {
    loading.value = true
    try {
      const { data } = await api.get('/albums')
      const list = Array.isArray(data) ? data : (data.content ?? data.albums ?? [])
      albums.value = list.map(normalizeAlbum)
    } catch {
      // keep empty on error
    } finally {
      loading.value = false
    }
  }

  function getById(id) {
    return albums.value.find(a => String(a.id) === String(id))
  }

  async function create(payload) {
    try {
      const { data } = await api.post('/albums', { title: payload.name, emoji: payload.emoji })
      const album = normalizeAlbum(data)
      albums.value.unshift(album)
      return album
    } catch {
      const album = {
        id: crypto.randomUUID(),
        name: payload.name,
        emoji: payload.emoji || '📸',
        coverUrl: null,
        photos: [],
      }
      albums.value.unshift(album)
      return album
    }
  }

  async function update(id, payload) {
    try {
      const { data } = await api.put(`/albums/${id}`, { title: payload.name, emoji: payload.emoji })
      const idx = albums.value.findIndex(a => String(a.id) === String(id))
      if (idx !== -1) albums.value[idx] = { ...albums.value[idx], ...normalizeAlbum(data) }
    } catch {
      const idx = albums.value.findIndex(a => String(a.id) === String(id))
      if (idx !== -1) albums.value[idx] = { ...albums.value[idx], name: payload.name, emoji: payload.emoji }
    }
  }

  async function remove(id) {
    try {
      await api.delete(`/albums/${id}`)
    } catch {
      // proceed with local removal
    }
    albums.value = albums.value.filter(a => String(a.id) !== String(id))
  }

  async function fetchAlbum(id) {
    try {
      const { data } = await api.get(`/albums/${id}`)
      const normalized = normalizeAlbum(data)
      const idx = albums.value.findIndex(a => String(a.id) === String(id))
      if (idx !== -1) {
        albums.value[idx] = normalized
      } else {
        albums.value.push(normalized)
      }
    } catch {
      // keep existing
    }
  }

  function addPhoto(albumId, photo) {
    const album = albums.value.find(a => String(a.id) === String(albumId))
    if (album) album.photos.push(photo)
  }

  function removePhoto(albumId, photoId) {
    const album = albums.value.find(a => String(a.id) === String(albumId))
    if (album) album.photos = album.photos.filter(p => p.id !== photoId)
  }

  return { albums, loading, fetchAll, fetchAlbum, getById, create, update, remove, addPhoto, removePhoto }
})
