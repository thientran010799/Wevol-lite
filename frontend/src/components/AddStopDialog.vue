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
        <h2 class="text-lg font-bold mb-6" style="color: var(--color-text)">
          {{ isEditMode ? 'Edit stop' : 'Add stop' }}
        </h2>

        <!-- Type selector -->
        <div class="mb-4">
          <label class="block text-xs font-medium mb-2" style="color: var(--color-text-muted)">Type</label>
          <div class="grid grid-cols-4 gap-2">
            <button
              v-for="(emoji, type) in typeEmoji"
              :key="type"
              class="flex flex-col items-center gap-1 py-2.5 rounded-xl text-xs font-medium transition cursor-pointer"
              :style="form.type === type ? typeActiveStyle : typeDefaultStyle"
              @click="form.type = type"
            >
              <span class="text-xl">{{ emoji }}</span>
              <span class="capitalize">{{ type }}</span>
            </button>
          </div>
        </div>

        <!-- Place name -->
        <div class="mb-4">
          <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Place / Activity *</label>
          <input
            v-model="form.place_name"
            type="text"
            placeholder="e.g. Hotel, Restaurant, Attraction…"
            class="w-full rounded-lg px-3 py-2 text-sm outline-none"
            :style="fieldStyle(focusedField === 'place_name')"
            @focus="focusedField = 'place_name'"
            @blur="focusedField = null"
          />
          <p v-if="errors.place_name" class="text-xs mt-1" style="color: #e07070">{{ errors.place_name }}</p>
        </div>

        <!-- Time + Cost -->
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Time</label>
            <input
              v-model="form.time"
              type="time"
              class="w-full rounded-lg px-3 py-2 text-sm outline-none"
              :style="fieldStyle(focusedField === 'time')"
              @focus="focusedField = 'time'"
              @blur="focusedField = null"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Cost</label>
            <input
              :value="form.cost"
              type="text"
              inputmode="numeric"
              placeholder="0"
              class="w-full rounded-lg px-3 py-2 text-sm outline-none"
              :style="fieldStyle(focusedField === 'cost')"
              @input="onCostInput"
              @focus="focusedField = 'cost'"
              @blur="focusedField = null"
            />
          </div>
        </div>

        <!-- Address -->
        <div class="mb-6">
          <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Address</label>
          <input
            v-model="form.address"
            type="text"
            placeholder="Optional"
            class="w-full rounded-lg px-3 py-2 text-sm outline-none"
            :style="fieldStyle(focusedField === 'address')"
            @focus="focusedField = 'address'"
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
            {{ saving ? 'Saving…' : isEditMode ? 'Save changes' : 'Add stop' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useTripStore } from '@/stores/trip'

const props = defineProps({
  modelValue: Boolean,
  tripId: { type: String, required: true },
  dayNumber: { type: Number, required: true },
  stopId: { type: String, default: null },
  initialStop: { type: Object, default: null },
})
const emit = defineEmits(['update:modelValue'])

const tripStore = useTripStore()

const isEditMode = computed(() => !!props.stopId)

const typeEmoji = { hotel: '🏨', flight: '✈️', food: '🍜', activity: '🎯' }
const typeActiveStyle = 'background: rgba(201,169,110,0.2); border: 1.5px solid var(--color-primary); color: var(--color-primary)'
const typeDefaultStyle = 'background: var(--color-bg); border: 1.5px solid var(--color-border); color: var(--color-text-muted)'
const fieldStyle = (focused) =>
  `background: var(--color-bg); border: 1px solid ${focused ? 'var(--color-primary)' : 'var(--color-border)'}; color: var(--color-text)`

function onCostInput(e) {
  const digits = e.target.value.replace(/[^0-9]/g, '')
  const formatted = digits ? digits.replace(/\B(?=(\d{3})+(?!\d))/g, "'") : ''
  form.cost = formatted
  e.target.value = formatted
}

function formatCost(rawCost) {
  if (rawCost == null || rawCost === '') return ''
  return String(rawCost).replace(/\B(?=(\d{3})+(?!\d))/g, "'")
}

const defaultForm = () => ({
  type: 'activity',
  place_name: '',
  time: '',
  cost: '',
  currency: 'VND',
  address: '',
})

const form = reactive(defaultForm())
const errors = reactive({ place_name: '' })
const saving = ref(false)
const focusedField = ref(null)

watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.initialStop) {
      form.type       = props.initialStop.type     || 'activity'
      form.place_name = props.initialStop.place    || ''
      form.time       = props.initialStop.time     || ''
      form.cost       = formatCost(props.initialStop.raw_cost)
      form.currency   = 'VND'
      form.address    = props.initialStop.address  || ''
    } else {
      Object.assign(form, defaultForm())
    }
    errors.place_name = ''
    focusedField.value = null
  }
})

function cancel() {
  emit('update:modelValue', false)
}

async function save() {
  errors.place_name = form.place_name.trim() ? '' : 'Place name is required'
  if (errors.place_name) return

  const payload = {
    type:       form.type,
    place_name: form.place_name.trim(),
    time:       form.time || null,
    cost:       form.cost ? Number(form.cost.replace(/'/g, '')) : null,
    currency:   form.currency,
    address:    form.address.trim() || null,
  }

  saving.value = true
  try {
    if (isEditMode.value) {
      tripStore.updateStop(props.tripId, props.dayNumber, props.stopId, payload)
    } else {
      await tripStore.addStop(props.tripId, props.dayNumber, payload)
    }
    emit('update:modelValue', false)
  } finally {
    saving.value = false
  }
}
</script>
