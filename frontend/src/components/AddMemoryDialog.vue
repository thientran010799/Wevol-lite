<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-center justify-center p-4"
      style="background: rgba(0,0,0,0.4)"
      @click.self="cancel"
    >
      <div
        class="w-full max-w-lg rounded-2xl p-8 shadow-xl"
        style="background: var(--color-surface); border: 1px solid var(--color-border)"
      >
        <h2 class="text-lg font-bold mb-6" style="color: var(--color-text)">New memory</h2>

        <!-- Title -->
        <div class="mb-4">
          <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Title *</label>
          <input
            v-model="form.title"
            type="text"
            placeholder="What happened?"
            class="w-full rounded-lg px-3 py-2 text-sm outline-none"
            :style="fieldStyle(focusedField === 'title')"
            @focus="focusedField = 'title'"
            @blur="focusedField = null"
          />
          <p v-if="errors.title" class="text-xs mt-1" style="color: #e07070">{{ errors.title }}</p>
        </div>

        <!-- Location + Date -->
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Location</label>
            <input
              v-model="form.location"
              type="text"
              placeholder="Where?"
              class="w-full rounded-lg px-3 py-2 text-sm outline-none"
              :style="fieldStyle(focusedField === 'location')"
              @focus="focusedField = 'location'"
              @blur="focusedField = null"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Date</label>
            <input
              v-model="form.date"
              type="date"
              class="w-full rounded-xl px-4 py-2 text-sm outline-none"
              :style="fieldStyle(focusedField === 'date')"
              @focus="focusedField = 'date'"
              @blur="focusedField = null"
            />
          </div>
        </div>

        <!-- Mood -->
        <div class="mb-4">
          <label class="block text-xs font-medium mb-2" style="color: var(--color-text-muted)">Mood</label>
          <div class="grid grid-cols-4 gap-2">
            <button
              v-for="(emoji, mood) in moodEmoji"
              :key="mood"
              class="flex flex-col items-center gap-1 py-2.5 rounded-xl text-xs font-medium transition cursor-pointer"
              :style="form.mood === mood ? moodActiveStyle : moodDefaultStyle"
              @click="form.mood = mood"
            >
              <span class="text-xl">{{ emoji }}</span>
              <span class="capitalize">{{ mood }}</span>
            </button>
          </div>
        </div>

        <!-- Note -->
        <div class="mb-6">
          <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Note</label>
          <textarea
            v-model="form.note"
            rows="3"
            placeholder="Write something about this memory…"
            class="w-full rounded-lg px-3 py-2 text-sm outline-none resize-none"
            :style="fieldStyle(focusedField === 'note')"
            @focus="focusedField = 'note'"
            @blur="focusedField = null"
          />
        </div>

        <!-- Actions -->
        <div class="flex justify-end gap-3">
          <button
            class="px-5 py-2 rounded-lg text-sm font-medium transition"
            style="color: var(--color-text-muted); background: var(--color-bg); border: 1px solid var(--color-border)"
            @click="cancel"
          >
            Cancel
          </button>
          <button
            class="px-5 py-2 rounded-lg text-sm font-medium text-white transition"
            :style="saving ? 'background: var(--color-primary-dark); opacity: 0.7' : 'background: var(--color-primary)'"
            :disabled="saving"
            @click="save"
          >
            {{ saving ? 'Saving…' : 'Save memory' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useMemoryStore } from '@/stores/memory'
import dayjs from 'dayjs'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const memoryStore = useMemoryStore()

const moodEmoji = { happy: '😊', romantic: '💕', cozy: '☕', adventure: '🌄' }

const moodActiveStyle = 'background: rgba(201,169,110,0.2); border: 1.5px solid var(--color-primary); color: var(--color-primary)'
const moodDefaultStyle = 'background: var(--color-bg); border: 1.5px solid var(--color-border); color: var(--color-text-muted)'

const defaultForm = () => ({
  title: '',
  location: '',
  date: dayjs().format('YYYY-MM-DD'),
  mood: '',
  note: '',
})

const form = reactive(defaultForm())
const errors = reactive({ title: '' })
const saving = ref(false)
const focusedField = ref(null)

const fieldStyle = (focused) =>
  `background: var(--color-bg); border: 1px solid ${focused ? 'var(--color-primary)' : 'var(--color-border)'}; color: var(--color-text)`

watch(() => props.modelValue, (val) => {
  if (val) {
    Object.assign(form, defaultForm())
    errors.title = ''
    focusedField.value = null
  }
})

function cancel() {
  emit('update:modelValue', false)
}

async function save() {
  errors.title = form.title.trim() ? '' : 'Title is required'
  if (errors.title) return

  saving.value = true
  try {
    const memory = await memoryStore.create({
      title: form.title.trim(),
      location: form.location.trim(),
      date: form.date,
      mood: form.mood || 'happy',
      note: form.note.trim() || null,
    })
    emit('update:modelValue', false)
    router.push(`/memories/${memory.id}`)
  } finally {
    saving.value = false
  }
}
</script>
