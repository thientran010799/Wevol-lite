import { defineStore } from 'pinia'
import api from '@/api/axios'

export const useMediaStore = defineStore('media', () => {
  async function presign(filename, mimeType) {
    const { data } = await api.post('/media/presign', {
      filename,
      mimeType,
    })
    return { uploadUrl: data.uploadUrl, mediaId: data.mediaId }
  }

  async function confirm(mediaId, metadata = {}) {
    const { data } = await api.post('/media/confirm', { mediaId, ...metadata })
    return data
  }

  async function upload(file) {
    const { uploadUrl, mediaId } = await presign(file.name, file.type)
    await fetch(uploadUrl, {
      method: 'PUT',
      headers: { 'Content-Type': file.type },
      body: file,
    })
    return mediaId
  }

  async function deletePhoto(mediaId) {
    await api.delete(`/media/${mediaId}`)
  }

  return { presign, confirm, upload, deletePhoto }
})
