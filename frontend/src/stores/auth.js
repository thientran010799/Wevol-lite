import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '@/api/axios'

export const useAuthStore = defineStore('auth', () => {
  const couple = ref({ start_date: null, name: 'Us', anniversary: null })

  const initials   = computed(() => 'We')
  const coupleName = computed(() => couple.value?.name || 'Us')

  async function fetchCouple() {
    const { data } = await api.get('/couple')
    couple.value = {
      name:        data.name,
      start_date:  data.startDate,
      anniversary: data.anniversary,
    }
  }

  function setStartDate(date) {
    couple.value = { ...couple.value, start_date: date }
  }

  async function updateCouple(data) {
    const { data: saved } = await api.put('/couple', {
      name:        data.name,
      startDate:   data.start_date,
      anniversary: data.anniversary,
    })
    couple.value = {
      name:        saved.name,
      start_date:  saved.startDate,
      anniversary: saved.anniversary,
    }
  }

  return { couple, initials, coupleName, fetchCouple, setStartDate, updateCouple }
})
