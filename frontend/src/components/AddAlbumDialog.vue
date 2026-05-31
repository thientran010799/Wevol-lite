<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="fixed inset-0 z-50 flex items-center justify-center p-4"
      style="background: rgba(0,0,0,0.4)"
      @click.self="cancel"
    >
      <div
        class="w-full max-w-md rounded-2xl p-8 shadow-xl"
        style="background: var(--color-surface); border: 1px solid var(--color-border)"
      >
        <h2 class="text-lg font-bold mb-6" style="color: var(--color-text)">New Album</h2>

        <!-- Name -->
        <div class="mb-5">
          <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">
            Album name *
          </label>
          <input
            v-model="form.name"
            type="text"
            placeholder="e.g. Summer trip"
            class="w-full rounded-lg px-3 py-2 text-sm outline-none"
            :style="fieldStyle(focusedField === 'name')"
            @focus="focusedField = 'name'"
            @blur="focusedField = null"
          />
          <p v-if="errors.name" class="text-xs mt-1" style="color: #e07070">{{ errors.name }}</p>
        </div>

        <!-- Emoji picker -->
        <div class="mb-6">
          <label class="block text-xs font-medium mb-2" style="color: var(--color-text-muted)">
            Choose an emoji
          </label>
          <div class="grid grid-cols-8 gap-1.5">
            <button
              v-for="e in EMOJIS"
              :key="e"
              type="button"
              class="w-8 h-8 rounded-lg flex items-center justify-center text-lg hover:opacity-80 transition"
              :style="form.emoji === e
                ? 'background: var(--color-primary); outline: 2px solid var(--color-primary-dark)'
                : 'background: var(--color-bg); border: 1px solid var(--color-border)'"
              @click="form.emoji = e"
            >
              {{ e }}
            </button>
          </div>
        </div>

        <!-- Actions -->
        <div class="flex justify-end gap-3">
          <button
            class="px-4 py-2 rounded-lg text-sm font-medium"
            style="color: var(--color-text-muted); background: var(--color-bg); border: 1px solid var(--color-border)"
            @click="cancel"
          >
            Cancel
          </button>
          <button
            class="px-4 py-2 rounded-lg text-sm font-medium text-white"
            style="background: var(--color-primary)"
            :style="saving ? 'opacity: 0.7' : ''"
            :disabled="saving"
            @click="save"
          >
            {{ saving ? 'Creating…' : 'Create album' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useGalleryStore } from '@/stores/gallery'

const EMOJIS = [
  '🌸', '🕯️', '🌅', '✈️', '🍝', '🎂', '🏖️', '🏔️',
  '🌃', '🎉', '💕', '🎆', '🌹', '🎊', '🎵', '🌺',
  '🍃', '☕', '🎨', '📸', '🌍', '🎭', '🏠', '🌙',
]

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const galleryStore = useGalleryStore()

const defaultForm = () => ({ name: '', emoji: '📸' })
const form = reactive(defaultForm())
const errors = reactive({ name: '' })
const saving = ref(false)
const focusedField = ref(null)

const fieldStyle = (focused) =>
  `background: var(--color-bg); border: 1px solid ${focused ? 'var(--color-primary)' : 'var(--color-border)'}; color: var(--color-text)`

watch(() => props.modelValue, (val) => {
  if (val) {
    Object.assign(form, defaultForm())
    errors.name = ''
    focusedField.value = null
  }
})

function cancel() {
  emit('update:modelValue', false)
}

async function save() {
  errors.name = form.name.trim() ? '' : 'Album name is required'
  if (errors.name) return

  saving.value = true
  try {
    const album = await galleryStore.create({ name: form.name.trim(), emoji: form.emoji })
    emit('update:modelValue', false)
    router.push(`/gallery/${album.id}`)
  } finally {
    saving.value = false
  }
}
</script>
