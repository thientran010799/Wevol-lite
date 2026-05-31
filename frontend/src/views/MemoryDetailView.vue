<template>
  <div v-if="memory">
    <!-- Back -->
    <button class="inline-flex items-center gap-1 text-sm mb-6 hover:opacity-70 transition"
            style="color: var(--color-muted)"
            @click="handleBack">
      ← Back to Memories
    </button>

    <!-- Card -->
    <div class="rounded-2xl p-8" style="background: var(--color-surface); border: 1px solid #e8ddd9">

      <!-- Header row -->
      <div class="flex items-start justify-between mb-6">
        <div class="flex-1 mr-4">
          <!-- View: title + subtitle -->
          <template v-if="!isEditing">
            <h1 class="text-xl font-bold mb-1" style="color: var(--color-text)">{{ memory.title }}</h1>
            <p class="text-sm" style="color: var(--color-muted)">{{ memory.location }} · {{ memory.date }}</p>
          </template>
          <!-- Edit: title + location/date inputs -->
          <template v-else>
            <input v-model="form.title" type="text"
                   class="w-full text-xl font-bold rounded-lg px-3 py-1.5 mb-2 outline-none"
                   :style="fieldStyle(focusedField === 'title')"
                   @focus="focusedField = 'title'" @blur="focusedField = null" />
            <div class="grid grid-cols-2 gap-3">
              <input v-model="form.location" type="text" placeholder="Location"
                     class="w-full rounded-lg px-3 py-1.5 text-sm outline-none"
                     :style="fieldStyle(focusedField === 'location')"
                     @focus="focusedField = 'location'" @blur="focusedField = null" />
              <input v-model="form.date" type="date"
                     class="w-full rounded-xl px-4 py-1.5 text-sm outline-none"
                     :style="fieldStyle(focusedField === 'date')"
                     @focus="focusedField = 'date'" @blur="focusedField = null" />
            </div>
          </template>
        </div>

        <!-- View: emoji + Edit + Delete buttons -->
        <div v-if="!isEditing" class="flex items-center gap-3 flex-shrink-0">
          <span class="text-4xl">{{ moodEmoji[memory.mood] }}</span>
          <button @click="startEdit"
                  class="px-4 py-1.5 rounded-lg text-sm font-medium"
                  style="background: var(--color-bg); border: 1px solid #d6cbc8; color: var(--color-muted)">
            Edit
          </button>
          <button @click="showDeleteMemoryDialog = true"
                  class="px-4 py-1.5 rounded-lg text-sm font-medium"
                  style="background: var(--color-bg); border: 1px solid var(--color-danger); color: var(--color-danger)">
            Delete
          </button>
        </div>
        <!-- Edit: Save + Cancel buttons -->
        <div v-else class="flex items-center gap-2 flex-shrink-0">
          <button @click="cancelEdit"
                  class="px-4 py-1.5 rounded-lg text-sm font-medium"
                  style="background: var(--color-bg); border: 1px solid #d6cbc8; color: var(--color-muted)">
            Cancel
          </button>
          <button @click="saveEdit" :disabled="saving"
                  class="px-4 py-1.5 rounded-lg text-sm font-medium text-white"
                  :style="saving ? 'background: var(--color-rose-dark); opacity:0.7' : 'background: var(--color-rose)'">
            {{ saving ? 'Saving…' : 'Save' }}
          </button>
        </div>
      </div>

      <!-- View: mood tag -->
      <span v-if="!isEditing"
            class="text-xs px-3 py-1 rounded-full capitalize font-medium"
            :style="moodBadgeStyle(memory.mood)">
        {{ memory.mood }}
      </span>

      <!-- Edit: mood selector -->
      <div v-if="isEditing" class="mb-4">
        <label class="block text-xs font-medium mb-2" style="color: var(--color-hint)">Mood</label>
        <div class="grid grid-cols-4 gap-2">
          <button v-for="(emoji, mood) in moodEmoji" :key="mood"
                  class="flex flex-col items-center gap-1 py-2.5 rounded-xl text-xs font-medium transition cursor-pointer"
                  :style="form.mood === mood ? moodActiveStyle : moodDefaultStyle"
                  @click="form.mood = mood">
            <span class="text-xl">{{ emoji }}</span>
            <span class="capitalize">{{ mood }}</span>
          </button>
        </div>
      </div>

      <!-- View: note -->
      <p v-if="!isEditing && memory.note" class="mt-6 text-sm leading-relaxed"
         style="color: var(--color-text)">
        {{ memory.note }}
      </p>

      <!-- Edit: note textarea -->
      <div v-if="isEditing" class="mt-4">
        <label class="block text-xs font-medium mb-1.5" style="color: var(--color-hint)">Note</label>
        <textarea v-model="form.note" rows="3" placeholder="Write something about this memory…"
                  class="w-full rounded-lg px-3 py-2 text-sm outline-none resize-none"
                  :style="fieldStyle(focusedField === 'note')"
                  @focus="focusedField = 'note'" @blur="focusedField = null" />
      </div>

      <!-- Photos section -->
      <div class="mt-6">
        <div class="flex items-center justify-between mb-3">
          <p class="text-xs font-medium" style="color: var(--color-hint)">
            {{ localPhotos.length }} {{ localPhotos.length === 1 ? 'photo' : 'photos' }}
          </p>
          <button @click="triggerUpload"
                  class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-medium"
                  :disabled="uploadProgress !== null"
                  style="background: var(--color-bg); border: 1px solid #d6cbc8; color: var(--color-muted)">
            <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"/>
              <polyline points="17 8 12 3 7 8"/>
              <line x1="12" y1="3" x2="12" y2="15"/>
            </svg>
            Upload photo
          </button>
          <input ref="fileInput" type="file" accept="image/*" multiple class="hidden" @change="handleUpload" />
        </div>

        <!-- Upload progress bar -->
        <div
          v-if="uploadProgress !== null"
          class="mb-3 rounded-xl px-4 py-3"
          style="background: var(--color-bg); border: 1px solid #e8ddd9"
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
          class="mb-3 rounded-xl px-4 py-3 flex items-center justify-between"
          style="background: #fef2f2; border: 1px solid #fca5a5"
        >
          <p class="text-xs font-medium" style="color: var(--color-danger)">{{ uploadError }}</p>
          <button class="text-xs ml-4" style="color: var(--color-danger)" @click="uploadError = null">✕</button>
        </div>

        <div v-if="localPhotos.length > 0" class="grid grid-cols-3 gap-3">
          <div v-for="photo in localPhotos" :key="photo.id"
               class="relative aspect-square rounded-xl overflow-hidden group"
               style="background: #e8ddd9">
            <img v-if="photo.url" :src="photo.url" class="w-full h-full object-cover" alt="" />
            <!-- Trash icon — visible on hover -->
            <button @click="promptDeletePhoto(photo)"
                    class="absolute top-1.5 right-1.5 p-1.5 rounded-lg flex items-center justify-center opacity-0 group-hover:opacity-100 transition"
                    style="background: rgba(0,0,0,0.55)">
              <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24"
                   fill="none" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                <path d="M10 11v6"/>
                <path d="M14 11v6"/>
                <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- Empty state for photos -->
        <div
          v-else
          class="py-10 text-center rounded-xl"
          style="border: 1.5px dashed #c4ddf5"
        >
          <p class="text-2xl mb-2">📷</p>
          <p class="text-xs" style="color: var(--color-hint)">No photos yet</p>
          <button
            class="mt-2 text-xs px-3 py-1.5 rounded-lg"
            style="border: 0.5px dashed var(--color-rose); color: var(--color-rose-dark); background: white"
            @click="triggerUpload"
          >
            + Add photo
          </button>
        </div>
      </div>

    </div>

    <!-- Delete photo confirm dialog -->
    <DeleteConfirmDialog v-model="showDeleteDialog" title="Delete this photo?" @confirm="confirmDeletePhoto" />

    <!-- Delete memory confirm dialog -->
    <DeleteConfirmDialog v-model="showDeleteMemoryDialog" title="Delete this memory?"
                         @confirm="confirmDeleteMemory" />

    <!-- Discard changes confirm dialog -->
    <DiscardConfirmDialog v-model="showDiscardDialog" @confirm="confirmDiscard" />
  </div>

  <!-- Not found -->
  <div v-else class="text-center py-20" style="color: var(--color-hint)">
    Memory not found.
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { useMemoryStore } from '@/stores/memory'
import { useMediaStore } from '@/stores/media'
import api from '@/api/axios'
import DeleteConfirmDialog from '@/components/DeleteConfirmDialog.vue'
import DiscardConfirmDialog from '@/components/DiscardConfirmDialog.vue'

const props = defineProps({ id: { type: String, required: true } })

const router = useRouter()

const moodEmoji = { happy: '😊', romantic: '💝', cozy: '🌿', adventure: '🌍' }

function moodBadgeStyle(mood) {
  if (mood === 'cozy' || mood === 'adventure') {
    return 'background: var(--color-sage-light); color: var(--color-sage-dark)'
  }
  return 'background: var(--color-blush); color: var(--color-rose-dark)'
}

const moodActiveStyle = 'background: var(--color-blush); border: 1.5px solid var(--color-rose); color: var(--color-rose-dark)'
const moodDefaultStyle = 'background: var(--color-bg); border: 1.5px solid #d6cbc8; color: var(--color-muted)'
const fieldStyle = (focused) =>
  `background: var(--color-bg); border: 1px solid ${focused ? 'var(--color-rose)' : '#d6cbc8'}; color: var(--color-text)`

const memoryStore = useMemoryStore()
const mediaStore = useMediaStore()
const memory = computed(() => memoryStore.getById(props.id))

// ── Photos ────────────────────────────────────────────────────────────────────
const localPhotos = ref([])
const photosInitialized = ref(false)
const fileInput = ref(null)
const showDeleteDialog = ref(false)
const photoToDelete = ref(null)
const showDeleteMemoryDialog = ref(false)
const uploadProgress = ref(null)
const uploadError = ref(null)

watch(memory, (m) => {
  if (!m || photosInitialized.value) return
  // Render from mediaUrls returned by API if available, else fall back to count placeholders
  if (Array.isArray(m.mediaUrls) && m.mediaUrls.length > 0) {
    localPhotos.value = m.mediaUrls.map((url, i) => ({
      id: m.mediaIds?.[i] ?? `media-${i}`,
      url,
      mediaId: m.mediaIds?.[i] ?? null,
    }))
  } else {
    const count = typeof m.photos === 'number' ? m.photos : (m.mediaUrls?.length ?? 0)
    localPhotos.value = Array.from({ length: count }, (_, i) => ({
      id: `placeholder-${i}`,
      url: null,
      mediaId: null,
    }))
  }
  photosInitialized.value = true
}, { immediate: true })

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

      // Step 4: link media to memory via Memory Service
      await api.post(`/memories/${props.id}/media`, { mediaId, url: confirmed.url ?? null })

      localPhotos.value.push({
        id: confirmed.id ?? mediaId,
        url: confirmed.url ?? URL.createObjectURL(file),
        mediaId: confirmed.id ?? mediaId,
      })

      done++
      uploadProgress.value = Math.round((done / total) * 100)
    } catch (err) {
      uploadError.value = `Upload failed: ${err?.message ?? 'unknown error'}. Please try again.`
      uploadProgress.value = null
      return
    }
  }

  uploadProgress.value = null
}

function promptDeletePhoto(photo) {
  photoToDelete.value = photo
  showDeleteDialog.value = true
}

async function confirmDeletePhoto() {
  if (!photoToDelete.value) { photoToDelete.value = null; return }
  try {
    const mediaId = photoToDelete.value.mediaId ?? photoToDelete.value.id
    if (mediaId && !mediaId.startsWith('placeholder-')) {
      await mediaStore.deletePhoto(mediaId)
    }
  } catch {
    // proceed with local removal
  }
  localPhotos.value = localPhotos.value.filter(p => p.id !== photoToDelete.value?.id)
  photoToDelete.value = null
}

// ── Delete memory ─────────────────────────────────────────────────────────────
async function confirmDeleteMemory() {
  await memoryStore.remove(props.id)
  router.push('/memories')
}

function handleBack() {
  if (isEditing.value && hasChanges()) {
    showDiscardDialog.value = true
  } else {
    router.push('/memories')
  }
}

// ── Edit mode ─────────────────────────────────────────────────────────────────
const isEditing = ref(false)
const saving = ref(false)
const focusedField = ref(null)
const showDiscardDialog = ref(false)

const form = reactive({ title: '', location: '', date: '', mood: '', note: '' })

function startEdit() {
  const m = memory.value
  form.title = m.title || ''
  form.location = m.location || ''
  form.date = m.memoryDate || ''
  form.mood = m.mood || ''
  form.note = m.note || ''
  isEditing.value = true
}

function hasChanges() {
  const m = memory.value
  const formattedDate = form.date ? dayjs(form.date).format('MMM D, YYYY') : ''
  return (
    form.title.trim() !== (m.title || '') ||
    form.location.trim() !== (m.location || '') ||
    formattedDate !== (m.date || '') ||
    form.mood !== (m.mood || '') ||
    form.note.trim() !== (m.note || '')
  )
}

function cancelEdit() {
  if (hasChanges()) {
    showDiscardDialog.value = true
  } else {
    isEditing.value = false
    focusedField.value = null
  }
}

function confirmDiscard() {
  isEditing.value = false
  focusedField.value = null
}

async function saveEdit() {
  if (!form.title.trim()) return
  saving.value = true
  try {
    await memoryStore.update(props.id, {
      title: form.title.trim(),
      location: form.location.trim(),
      date: form.date || null,
      mood: form.mood,
      note: form.note.trim() || null,
    })
    isEditing.value = false
    focusedField.value = null
  } finally {
    saving.value = false
  }
}
</script>
