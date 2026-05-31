import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUIStore = defineStore('ui', () => {
  const isEditing = ref(false)
  const activeModal = ref(null)
  const pendingDiscard = ref(false)

  return { isEditing, activeModal, pendingDiscard }
})
