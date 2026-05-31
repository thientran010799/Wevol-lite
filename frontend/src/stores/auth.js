import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const couple = ref({ start_date: null, name: 'Us', anniversary: null })

  const initials   = computed(() => 'We')
  const coupleName = computed(() => couple.value?.name || 'Us')

  function setStartDate(date) {
    couple.value = { ...couple.value, start_date: date }
  }

  function updateCouple(data) {
    couple.value = { ...couple.value, ...data }
  }

  return { couple, initials, coupleName, setStartDate, updateCouple }
})
