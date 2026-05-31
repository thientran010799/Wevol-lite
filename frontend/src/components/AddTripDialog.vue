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
        <h2 class="text-lg font-bold mb-6" style="color: var(--color-text)">Plan a trip</h2>

        <!-- Name -->
        <div class="mb-4">
          <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Trip name *</label>
          <input
            v-model="form.name"
            type="text"
            placeholder="Where are you going?"
            class="w-full rounded-lg px-3 py-2 text-sm outline-none"
            :style="fieldStyle(focusedField === 'name')"
            @focus="focusedField = 'name'"
            @blur="focusedField = null"
          />
          <p v-if="errors.name" class="text-xs mt-1" style="color: #e07070">{{ errors.name }}</p>
        </div>

        <!-- Start date + End date -->
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Start date</label>
            <input
              v-model="form.start_date"
              type="date"
              class="w-full rounded-xl px-4 py-2 text-sm outline-none"
              :style="fieldStyle(focusedField === 'start_date')"
              @focus="focusedField = 'start_date'"
              @blur="focusedField = null"
            />
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">End date</label>
            <input
              v-model="form.end_date"
              type="date"
              class="w-full rounded-xl px-4 py-2 text-sm outline-none"
              :style="fieldStyle(focusedField === 'end_date')"
              @focus="focusedField = 'end_date'"
              @blur="focusedField = null"
            />
          </div>
        </div>

        <!-- Status -->
        <div class="mb-4">
          <label class="block text-xs font-medium mb-2" style="color: var(--color-text-muted)">Status</label>
          <div class="grid grid-cols-4 gap-2">
            <button
              v-for="(emoji, status) in statusEmoji"
              :key="status"
              class="flex flex-col items-center gap-1 py-2.5 rounded-xl text-xs font-medium transition cursor-pointer"
              :style="form.status === status ? statusActiveStyle : statusDefaultStyle"
              @click="form.status = status"
            >
              <span class="text-xl">{{ emoji }}</span>
              <span class="capitalize">{{ status }}</span>
            </button>
          </div>
        </div>

        <!-- Budget + Currency -->
        <div class="grid grid-cols-2 gap-4 mb-4">
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Budget</label>
            <div class="relative">
              <input
                type="text"
                inputmode="numeric"
                :value="budgetDisplay"
                placeholder="0"
                class="w-full rounded-xl px-4 py-2 text-sm outline-none"
                style="padding-right: 3.2rem"
                :style="fieldStyle(focusedField === 'budget')"
                @focus="onBudgetFocus"
                @input="onBudgetInput"
                @blur="onBudgetBlur"
              />
              <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs font-medium pointer-events-none"
                    style="color: var(--color-text-muted)">
                {{ form.currency }}
              </span>
            </div>
          </div>
          <div>
            <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Currency</label>
            <select
              v-model="form.currency"
              class="w-full rounded-xl px-4 py-2 text-sm outline-none"
              :style="fieldStyle(focusedField === 'currency')"
              @focus="focusedField = 'currency'"
              @blur="focusedField = null"
              @change="reformatBudget"
            >
              <option value="VND">VND — Vietnamese Dong</option>
              <option value="USD">USD — US Dollar</option>
              <option value="EUR">EUR — Euro</option>
              <option value="THB">THB — Thai Baht</option>
            </select>
          </div>
        </div>

        <!-- Notes -->
        <div class="mb-6">
          <label class="block text-xs font-medium mb-1.5" style="color: var(--color-text-muted)">Notes</label>
          <textarea
            v-model="form.notes"
            rows="3"
            placeholder="Any plans or ideas for this trip…"
            class="w-full rounded-lg px-3 py-2 text-sm outline-none resize-none"
            :style="fieldStyle(focusedField === 'notes')"
            @focus="focusedField = 'notes'"
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
            {{ saving ? 'Saving…' : 'Save trip' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useTripStore } from '@/stores/trip'
import dayjs from 'dayjs'

const props = defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue'])

const router = useRouter()
const tripStore = useTripStore()

const statusEmoji = { planning: '📋', confirmed: '✅', ongoing: '🚀', done: '🏁' }

const statusActiveStyle = 'background: rgba(201,169,110,0.2); border: 1.5px solid var(--color-primary); color: var(--color-primary)'
const statusDefaultStyle = 'background: var(--color-bg); border: 1.5px solid var(--color-border); color: var(--color-text-muted)'

const defaultForm = () => ({
  name: '',
  start_date: dayjs().format('YYYY-MM-DD'),
  end_date: '',
  status: 'planning',
  budget: null,
  currency: 'VND',
  notes: '',
})

const form = reactive(defaultForm())
const errors = reactive({ name: '' })
const saving = ref(false)
const focusedField = ref(null)
const budgetDisplay = ref('')

const fieldStyle = (focused) =>
  `background: var(--color-bg); border: 1px solid ${focused ? 'var(--color-primary)' : 'var(--color-border)'}; color: var(--color-text)`

function formatBudget(val, currency) {
  if (val == null || val === '') return ''
  const decimals = (currency === 'USD' || currency === 'EUR') ? 2 : 0
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(val)
}

function onBudgetFocus() {
  focusedField.value = 'budget'
  budgetDisplay.value = form.budget != null ? String(form.budget) : ''
}

function onBudgetInput(e) {
  e.target.value = e.target.value.replace(/[^0-9.]/g, '')
  budgetDisplay.value = e.target.value
}

function onBudgetBlur() {
  focusedField.value = null
  const num = parseFloat(budgetDisplay.value)
  if (!isNaN(num) && num >= 0) {
    form.budget = num
    budgetDisplay.value = formatBudget(num, form.currency)
  } else {
    form.budget = null
    budgetDisplay.value = ''
  }
}

function reformatBudget() {
  if (form.budget != null) {
    budgetDisplay.value = formatBudget(form.budget, form.currency)
  }
}

watch(() => props.modelValue, (val) => {
  if (val) {
    Object.assign(form, defaultForm())
    budgetDisplay.value = ''
    errors.name = ''
    focusedField.value = null
  }
})

function cancel() {
  emit('update:modelValue', false)
}

async function save() {
  errors.name = form.name.trim() ? '' : 'Trip name is required'
  if (errors.name) return

  saving.value = true
  try {
    const trip = await tripStore.create({
      name: form.name.trim(),
      start_date: form.start_date,
      end_date: form.end_date || null,
      status: form.status,
      budget: form.budget ?? null,
      currency: form.currency,
      notes: form.notes.trim() || null,
    })
    emit('update:modelValue', false)
    router.push(`/trips/${trip.id}`)
  } finally {
    saving.value = false
  }
}
</script>
