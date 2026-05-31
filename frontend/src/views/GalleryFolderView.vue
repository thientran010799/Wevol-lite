<template>
  <div v-if="album">
    <!-- Back -->
    <button
      class="inline-flex items-center gap-1 text-sm mb-6 hover:opacity-70 transition"
      style="color: var(--color-muted)"
      @click="handleBack"
    >
      ← Back to Gallery
    </button>

    <!-- Header -->
    <div class="flex items-start justify-between mb-6">
      <div class="flex items-center gap-3">
        <span class="text-3xl">{{ isEditing ? form.emoji : album.emoji }}</span>
        <div>
          <h1
            v-if="!isEditing"
            class="text-lg font-bold"
            style="color: var(--color-text)"
          >
            {{ album.name }}
          </h1>
          <input
            v-else
            v-model="form.name"
            class="text-lg font-bold bg-transparent outline-none border-b"
            style="color: var(--color-text); border-color: var(--color-rose); min-width: 180px"
          />
          <p class="text-xs mt-0.5" style="color: var(--color-hint)">
            {{ album.photos.length }} photos
          </p>
        </div>
      </div>

      <div class="flex items-center gap-2 flex-shrink-0">
        <template v-if="!isEditing">
          <button
            class="px-3 py-1.5 rounded-lg text-xs font-medium"
            style="color: var(--color-muted); background: var(--color-bg); border: 1px solid #d6cbc8"
            @click="startEdit"
          >
            Edit
          </button>
          <button
            class="px-3 py-1.5 rounded-lg text-xs font-medium"
            style="color: var(--color-danger); background: var(--color-bg); border: 1px solid #d6cbc8"
            @click="showDeleteAlbum = true"
          >
            Delete album
          </button>
        </template>
        <template v-else>
          <button
            class="px-3 py-1.5 rounded-lg text-xs font-medium"
            style="color: var(--color-muted); background: var(--color-bg); border: 1px solid #d6cbc8"
            @click="cancelEdit"
          >
            Cancel
          </button>
          <button
            class="px-4 py-1.5 rounded-lg text-xs font-medium text-white"
            style="background: var(--color-rose)"
            :disabled="saving"
            @click="saveEdit"
          >
            {{ saving ? 'Saving…' : 'Save' }}
          </button>
        </template>
      </div>
    </div>

    <!-- Edit mode: emoji picker -->
    <div
      v-if="isEditing"
      class="mb-6 p-4 rounded-xl"
      style="background: var(--color-surface); border: 1px solid #e8ddd9"
    >
      <p class="text-xs font-medium mb-2" style="color: var(--color-hint)">Choose an emoji</p>
      <div class="grid grid-cols-8 gap-1.5">
        <button
          v-for="e in EMOJIS"
          :key="e"
          type="button"
          class="w-8 h-8 rounded-lg flex items-center justify-center text-lg hover:opacity-80 transition"
          :style="form.emoji === e
            ? 'background: var(--color-rose); outline: 2px solid var(--color-rose-dark)'
            : 'background: var(--color-bg); border: 1px solid #d6cbc8'"
          @click="form.emoji = e"
        >
          {{ e }}
        </button>
      </div>
    </div>

    <!-- Upload progress bar -->
    <div
      v-if="uploadProgress !== null"
      class="mb-4 rounded-xl px-4 py-3"
      style="background: var(--color-surface); border: 1px solid #e8ddd9"
    >
      <div class="flex items-center justify-between mb-1.5">
        <p class="text-xs font-medium" style="color: var(--color-text)">Uploading…</p>
        <p class="text-xs" style="color: var(--color-hint)">{{ uploadProgress }}%</p>
      </div>
      <div class="h-1.5 rounded-full overflow-hidden" style="background: #e8ddd9">
        <div
          class="h-full rounded-full transition-all"
          style="background: var(--color-rose)"
          :style="{ width: `${uploadProgress}%` }"
        />
      </div>
    </div>

    <!-- Error toast -->
    <div
      v-if="uploadError"
      class="mb-4 rounded-xl px-4 py-3 flex items-center justify-between"
      style="background: #fef2f2; border: 1px solid #fca5a5"
    >
      <p class="text-xs font-medium" style="color: var(--color-danger)">{{ uploadError }}</p>
      <button
        class="text-xs ml-4"
        style="color: var(--color-danger)"
        @click="uploadError = null"
      >
        ✕
      </button>
    </div>

    <!-- Photos section header -->
    <div class="flex items-center justify-between mb-4">
      <p class="text-sm font-medium" style="color: var(--color-text)">Photos</p>
      <button
        class="px-3 py-1.5 rounded-lg text-xs font-medium text-white"
        style="background: var(--color-rose)"
        :disabled="uploadProgress !== null"
        @click="triggerUpload"
      >
        + Upload photo
      </button>
      <input
        ref="fileInput"
        type="file"
        multiple
        accept="image/*"
        style="display: none"
        @change="handleUpload"
      />
    </div>

    <!-- Photo grid -->
    <div v-if="album.photos.length" class="grid grid-cols-3 gap-3">
      <div
        v-for="(photo, index) in album.photos"
        :key="photo.id"
        class="relative aspect-square rounded-xl overflow-hidden cursor-pointer hover:opacity-90 transition group"
        @click="openLightbox(index)"
      >
        <img v-if="photo.url" :src="photo.url" class="w-full h-full object-cover" />
        <div
          v-else
          class="w-full h-full"
          :style="`background: ${photoColor(index)}`"
        />

        <!-- Delete button on hover -->
        <button
          class="absolute top-2 right-2 w-7 h-7 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition text-white"
          style="background: rgba(0,0,0,0.55); font-size: 16px; line-height: 1"
          @click.stop="promptDeletePhoto(photo)"
        >
          ×
        </button>
      </div>
    </div>

    <!-- Empty state -->
    <div
      v-else
      class="py-16 text-center rounded-2xl"
      style="background: var(--color-surface); border: 1.5px dashed #c4ddf5"
    >
      <p class="text-3xl mb-2">📷</p>
      <p class="text-sm" style="color: var(--color-hint)">No photos yet. Upload your first one!</p>
    </div>

    <!-- Dialogs -->
    <DeleteConfirmDialog
      v-model="showDeletePhoto"
      title="Delete this photo?"
      @confirm="confirmDeletePhoto"
    />
    <DeleteConfirmDialog
      v-model="showDeleteAlbum"
      title="Delete this album?"
      @confirm="confirmDeleteAlbum"
    />
    <DiscardConfirmDialog
      v-model="showDiscard"
      @confirm="confirmDiscard"
    />
  </div>

  <div v-else class="text-center py-20" style="color: var(--color-hint)">
    Album not found.
  </div>

  <!-- Lightbox -->
  <Teleport to="body">
    <div
      v-if="lightboxIndex !== null && album"
      class="fixed inset-0 z-50 flex items-center justify-center"
      style="background: rgba(0,0,0,0.88)"
      @click.self="closeLightbox"
    >
      <!-- Close -->
      <button
        class="absolute top-4 right-5 text-3xl hover:opacity-70 transition"
        style="color: white"
        @click="closeLightbox"
      >
        ×
      </button>

      <!-- Prev -->
      <button
        v-if="lightboxIndex > 0"
        class="absolute left-4 text-4xl hover:opacity-70 transition px-3 py-4"
        style="color: white"
        @click="lightboxIndex--"
      >
        ‹
      </button>

      <!-- Photo -->
      <div class="mx-16 flex items-center justify-center">
        <img
          v-if="lightboxPhoto?.url"
          :src="lightboxPhoto.url"
          class="rounded-xl object-contain"
          style="max-width: 72vw; max-height: 80vh"
        />
        <div
          v-else
          class="rounded-xl"
          style="width: 320px; height: 320px"
          :style="`background: ${photoColor(lightboxIndex)}`"
        />
      </div>

      <!-- Next -->
      <button
        v-if="lightboxIndex < album.photos.length - 1"
        class="absolute right-4 text-4xl hover:opacity-70 transition px-3 py-4"
        style="color: white"
        @click="lightboxIndex++"
      >
        ›
      </button>

      <!-- Counter -->
      <p
        class="absolute bottom-5 text-sm"
        style="color: rgba(255,255,255,0.55)"
      >
        {{ lightboxIndex + 1 }} / {{ album.photos.length }}
      </p>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useGalleryStore } from '@/stores/gallery'
import { useMediaStore } from '@/stores/media'
import api from '@/api/axios'
import DeleteConfirmDialog from '@/components/DeleteConfirmDialog.vue'
import DiscardConfirmDialog from '@/components/DiscardConfirmDialog.vue'

const EMOJIS = [
  '🌸', '🕯️', '🌅', '✈️', '🍝', '🎂', '🏖️', '🏔️',
  '🌃', '🎉', '💕', '🎆', '🌹', '🎊', '🎵', '🌺',
  '🍃', '☕', '🎨', '📸', '🌍', '🎭', '🏠', '🌙',
]

const PLACEHOLDER_COLORS = ['#e8d5b7', '#d4c5a9', '#c9b99a', '#bfad8f', '#b4a183', '#a89577']

const props = defineProps({ folderId: { type: String, required: true } })

const router = useRouter()
const galleryStore = useGalleryStore()
const mediaStore = useMediaStore()

const album = computed(() => galleryStore.getById(props.folderId))

onMounted(() => galleryStore.fetchAlbum(props.folderId))

// ── Back ────────────────────────────────────────────────
function handleBack() {
  if (isEditing.value && hasChanges()) {
    showDiscard.value = true
    pendingBackNav.value = true
  } else {
    router.push('/gallery')
  }
}

const pendingBackNav = ref(false)

// ── Edit mode ───────────────────────────────────────────
const isEditing = ref(false)
const saving = ref(false)
const form = reactive({ name: '', emoji: '' })
const showDiscard = ref(false)

function startEdit() {
  form.name = album.value.name
  form.emoji = album.value.emoji
  isEditing.value = true
}

function hasChanges() {
  return form.name !== album.value.name || form.emoji !== album.value.emoji
}

function cancelEdit() {
  if (hasChanges()) {
    pendingBackNav.value = false
    showDiscard.value = true
  } else {
    isEditing.value = false
  }
}

function confirmDiscard() {
  isEditing.value = false
  if (pendingBackNav.value) {
    pendingBackNav.value = false
    router.push('/gallery')
  }
}

async function saveEdit() {
  if (!form.name.trim()) return
  saving.value = true
  try {
    await galleryStore.update(props.folderId, { name: form.name.trim(), emoji: form.emoji })
    isEditing.value = false
  } finally {
    saving.value = false
  }
}

// ── Delete album ────────────────────────────────────────
const showDeleteAlbum = ref(false)

async function confirmDeleteAlbum() {
  await galleryStore.remove(props.folderId)
  router.push('/gallery')
}

// ── Photos — presign upload flow ────────────────────────
const fileInput = ref(null)
const showDeletePhoto = ref(false)
const photoToDelete = ref(null)
const uploadProgress = ref(null)
const uploadError = ref(null)

function photoColor(index) {
  return PLACEHOLDER_COLORS[index % PLACEHOLDER_COLORS.length]
}

function triggerUpload() {
  fileInput.value?.click()
}

async function handleUpload(e) {
  const files = Array.from(e.target.files)
  e.target.value = ''
  if (!files.length) return

  uploadError.value = null
  const total = files.length
  let done = 0

  for (const file of files) {
    uploadProgress.value = Math.round((done / total) * 100)
    try {
      // Step 1: presign
      const { uploadUrl, mediaId } = await mediaStore.presign(file.name, file.type)

      // Step 2: PUT directly to R2 — no auth header
      const putResp = await fetch(uploadUrl, {
        method: 'PUT',
        headers: { 'Content-Type': file.type },
        body: file,
      })
      if (!putResp.ok) {
        const body = await putResp.text().catch(() => '(unreadable)')
        console.error('[R2 upload] failed', { status: putResp.status, url: uploadUrl, body })
        throw new Error(`R2 upload failed: ${putResp.status} — ${body}`)
      }

      // Step 3: confirm metadata with Media Service
      const confirmed = await mediaStore.confirm(mediaId, {
        fileName: file.name,
        mimeType: file.type,
        sizeBytes: file.size,
      })

      // Step 4: link to album
      await api.post(`/albums/${props.folderId}/photos`, { mediaId })

      // Update local store immediately with the confirmed URL
      galleryStore.addPhoto(props.folderId, {
        id: confirmed.id ?? mediaId,
        url: confirmed.url ?? URL.createObjectURL(file),
        thumbUrl: confirmed.thumbUrl ?? null,
        mediaId: confirmed.id ?? mediaId,
      })

      done++
      uploadProgress.value = Math.round((done / total) * 100)
    } catch (err) {
      uploadError.value = done > 0
        ? `Uploaded ${done} of ${total} photos. Failed on "${file.name}". Please try again for the rest.`
        : `Upload failed: ${err?.message ?? 'unknown error'}. Please try again.`
      uploadProgress.value = null
      return
    }
  }

  // Refresh album from API to get server-side URLs
  await galleryStore.fetchAlbum(props.folderId)
  uploadProgress.value = null
}

function promptDeletePhoto(photo) {
  photoToDelete.value = photo
  showDeletePhoto.value = true
}

async function confirmDeletePhoto() {
  if (!photoToDelete.value) return
  if (lightboxIndex.value !== null) closeLightbox()
  try {
    const mediaId = photoToDelete.value.mediaId ?? null
    if (mediaId) {
      await api.delete(`/albums/${props.folderId}/photos/${mediaId}`)
    }
  } catch {
    // proceed with local removal regardless
  }
  galleryStore.removePhoto(props.folderId, photoToDelete.value.id)
  photoToDelete.value = null
}

// ── Lightbox ────────────────────────────────────────────
const lightboxIndex = ref(null)

const lightboxPhoto = computed(() => {
  if (lightboxIndex.value === null || !album.value) return null
  return album.value.photos[lightboxIndex.value] ?? null
})

function openLightbox(index) {
  lightboxIndex.value = index
}

function closeLightbox() {
  lightboxIndex.value = null
}

function handleKeydown(e) {
  if (lightboxIndex.value === null || !album.value) return
  if (e.key === 'Escape') closeLightbox()
  if (e.key === 'ArrowLeft' && lightboxIndex.value > 0) lightboxIndex.value--
  if (e.key === 'ArrowRight' && lightboxIndex.value < album.value.photos.length - 1) lightboxIndex.value++
}

onMounted(() => window.addEventListener('keydown', handleKeydown))
onUnmounted(() => window.removeEventListener('keydown', handleKeydown))
</script>
