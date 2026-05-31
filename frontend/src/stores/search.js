import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/api/axios'

export const useSearchStore = defineStore('search', () => {
  const results = ref([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref(null)

  async function search(q, type = 'all') {
    if (!q || q.trim().length < 2) { results.value = []; total.value = 0; return }
    loading.value = true
    error.value = null
    try {
      const { data } = await api.get('/search', { params: { q: q.trim(), type } })
      results.value = data.results
      total.value = data.total
    } catch {
      error.value = 'Search failed'
      results.value = []
    } finally {
      loading.value = false
    }
  }

  function clear() { results.value = []; total.value = 0; error.value = null }

  return { results, total, loading, error, search, clear }
})
