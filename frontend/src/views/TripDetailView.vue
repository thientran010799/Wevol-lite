<template>
  <div v-if="trip">

    <!-- Back -->
    <button
      class="inline-flex items-center gap-1 text-sm mb-6 hover:opacity-70 transition"
      style="color: var(--color-text-muted)"
      @click="handleBack"
    >
      ← Back to Trips
    </button>

    <!-- Header card -->
    <div
      class="rounded-2xl px-8 py-6 mb-6"
      :style="isEditing
        ? 'background: var(--color-surface); border: 2px dashed var(--color-primary)'
        : 'background: var(--color-sidebar)'"
    >

      <!-- View mode -->
      <template v-if="!isEditing">
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-xl font-bold mb-1" style="color: #fff">{{ trip.name }}</h1>
            <p class="text-sm" style="color: var(--color-sidebar-text)">{{ trip.dates }}</p>
          </div>
          <div class="flex items-center gap-3">
            <span class="text-xs px-3 py-1 rounded-full capitalize font-medium"
                  :style="statusBadgeStyle[trip.status]">
              {{ trip.status }}
            </span>
            <button
              class="px-4 py-1.5 rounded-lg text-sm font-medium transition"
              style="background: rgba(255,255,255,0.1); border: 1px solid rgba(255,255,255,0.2); color: rgba(255,255,255,0.85)"
              @click="startEdit"
            >
              Edit
            </button>
          </div>
        </div>
        <div class="flex gap-6 mt-5">
          <div>
            <p class="text-xs" style="color: var(--color-sidebar-text)">Budget</p>
            <p class="text-sm font-semibold mt-0.5" style="color: var(--color-primary)">{{ trip.budget || '—' }}</p>
          </div>
          <div>
            <p class="text-xs" style="color: var(--color-sidebar-text)">Duration</p>
            <p class="text-sm font-semibold mt-0.5" style="color: #fff">{{ trip.duration || '—' }}</p>
          </div>
        </div>
        <p v-if="trip.notes" class="mt-4 text-sm leading-relaxed" style="color: var(--color-sidebar-text)">
          {{ trip.notes }}
        </p>
      </template>

      <!-- Edit mode -->
      <template v-else>
        <div class="flex items-center justify-between mb-6">
          <p class="text-sm font-semibold" style="color: var(--color-text-muted)">Editing trip</p>
          <div class="flex gap-2">
            <button
              class="px-4 py-1.5 rounded-lg text-sm font-medium transition"
              style="background: var(--color-bg); border: 1px solid var(--color-border); color: var(--color-text-muted)"
              @click="cancelEdit"
            >
              Cancel
            </button>
            <button
              class="px-4 py-1.5 rounded-lg text-sm font-medium text-white transition"
              :style="saving ? 'background: var(--color-primary-dark); opacity:0.7' : 'background: var(--color-primary)'"
              :disabled="saving"
              @click="saveEdit"
            >
              {{ saving ? 'Saving…' : 'Save' }}
            </button>
          </div>
        </div>

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
        </div>

        <!-- Dates -->
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
                :style="fieldStyle(focusedField === 'raw_budget')"
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
        <div>
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
      </template>
    </div>

    <!-- Budget tracker -->
    <div
      v-if="trip.raw_budget || totalSpent > 0"
      class="rounded-xl px-6 py-5 mb-6"
      style="background: var(--color-surface); border: 1px solid var(--color-border)"
    >
      <div class="flex items-center justify-between mb-3">
        <p class="text-xs font-semibold uppercase tracking-wider" style="color: var(--color-text-muted)">Budget</p>
        <p v-if="trip.raw_budget" class="text-xs font-medium" :style="overBudget ? 'color: #e05050' : 'color: var(--color-text-muted)'">
          {{ overBudget ? 'Over budget' : budgetPct + '% used' }}
        </p>
      </div>

      <!-- Amounts row -->
      <div class="flex items-end justify-between mb-3">
        <div>
          <p class="text-xs mb-0.5" style="color: var(--color-text-muted)">Spent so far</p>
          <p class="text-base font-bold" style="color: var(--color-text)">
            {{ totalSpent > 0 ? totalSpent.toLocaleString() + ' ' + (trip.currency || 'VND') : '—' }}
          </p>
        </div>
        <div v-if="trip.raw_budget" class="text-right">
          <p class="text-xs mb-0.5" style="color: var(--color-text-muted)">Budget</p>
          <p class="text-base font-bold" style="color: var(--color-text)">{{ trip.budget }}</p>
        </div>
      </div>

      <!-- Progress bar (only when budget is set) -->
      <div v-if="trip.raw_budget" class="rounded-full overflow-hidden" style="height: 6px; background: var(--color-border)">
        <div
          class="h-full rounded-full transition-all"
          :style="{
            width: Math.min(budgetPct, 100) + '%',
            background: overBudget ? '#e05050' : budgetPct >= 80 ? '#d97706' : 'var(--color-primary)',
          }"
        />
      </div>

      <!-- Remaining -->
      <p v-if="trip.raw_budget && !overBudget" class="text-xs mt-2" style="color: var(--color-text-muted)">
        {{ (trip.raw_budget - totalSpent).toLocaleString() }} {{ trip.currency || 'VND' }} remaining
      </p>
      <p v-else-if="trip.raw_budget && overBudget" class="text-xs mt-2" style="color: #e05050">
        {{ (totalSpent - trip.raw_budget).toLocaleString() }} {{ trip.currency || 'VND' }} over budget
      </p>
    </div>

    <!-- Itinerary section -->
    <div>
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xs font-semibold uppercase tracking-wider" style="color: var(--color-text-muted)">
          Itinerary by day
        </h2>
        <button
          class="text-xs font-medium px-3 py-1.5 rounded-lg transition"
          style="border: 0.5px dashed var(--color-primary); color: var(--color-primary-dark); background: var(--color-surface)"
          @click="tripStore.addDay(props.id)"
        >
          + Add day
        </button>
      </div>

      <!-- Empty: no days -->
      <div
        v-if="trip.days.length === 0"
        class="rounded-xl py-12 flex flex-col items-center gap-3"
        style="border: 1.5px dashed var(--color-border)"
      >
        <span class="text-3xl">🗺️</span>
        <p class="text-sm" style="color: var(--color-text-muted)">No itinerary yet</p>
        <button
          class="text-xs font-medium px-3 py-1.5 rounded-lg transition"
          style="border: 0.5px dashed var(--color-primary); color: var(--color-primary-dark); background: var(--color-surface)"
          @click="tripStore.addDay(props.id)"
        >
          + Add first day
        </button>
      </div>

      <!-- Day cards -->
      <div v-for="day in trip.days" :key="day.number" class="mb-6 group/day">
        <div class="flex items-center justify-between mb-3">
          <h3
            class="text-xs font-semibold uppercase tracking-wider"
            style="color: var(--color-text-muted)"
          >
            Day {{ day.number }}{{ day.label ? ' — ' + day.label : '' }}
          </h3>
          <button
            class="p-1 rounded-lg transition opacity-0 group-hover/day:opacity-100"
            style="color: #e07070"
            title="Delete day"
            @click="promptDeleteDay(day.number)"
          >
            <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24"
                 fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
              <path d="M10 11v6"/><path d="M14 11v6"/>
              <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
            </svg>
          </button>
        </div>

        <div
          class="rounded-xl px-5 py-4"
          style="background: var(--color-surface); border: 1px solid var(--color-border)"
        >
          <!-- Stops (draggable) -->
          <draggable
            v-if="day.stops.length > 0"
            :list="day.stops"
            item-key="id"
            handle=".drag-handle"
            class="space-y-3 mb-4"
            @end="() => tripStore.persistStopOrder(props.id, day.number, day.stops)"
          >
            <template #item="{ element: stop }">
              <div
                class="flex items-start gap-3 group rounded-lg px-2 py-1 -mx-2 transition"
              >
                <!-- Drag handle -->
                <div
                  class="drag-handle self-center cursor-grab opacity-0 group-hover:opacity-100 transition-opacity shrink-0"
                  style="color: var(--color-border); touch-action: none"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
                    <circle cx="9" cy="6" r="2"/><circle cx="15" cy="6" r="2"/>
                    <circle cx="9" cy="12" r="2"/><circle cx="15" cy="12" r="2"/>
                    <circle cx="9" cy="18" r="2"/><circle cx="15" cy="18" r="2"/>
                  </svg>
                </div>
                <div
                  class="w-8 h-8 rounded-lg flex items-center justify-center text-base shrink-0"
                  :style="stopBgStyle[stop.type]"
                >
                  {{ typeEmoji[stop.type] }}
                </div>
                <div class="flex-1 min-w-0">
                  <div class="flex items-center justify-between">
                    <p class="text-sm font-medium" style="color: var(--color-text)">{{ stop.place }}</p>
                    <p v-if="stop.time" class="text-xs ml-3 shrink-0" style="color: var(--color-text-muted)">{{ stop.time }}</p>
                  </div>
                  <p v-if="stop.address" class="text-xs mt-0.5" style="color: var(--color-text-muted)">{{ stop.address }}</p>
                  <p v-if="stop.cost" class="text-xs mt-1 font-medium" style="color: var(--color-primary)">{{ stop.cost }}</p>
                </div>
                <!-- Edit / Delete buttons -->
                <div class="flex gap-1 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity">
                  <button
                    class="p-1.5 rounded-lg transition"
                    style="color: var(--color-text-muted); background: var(--color-bg)"
                    title="Edit stop"
                    @click="openEditStop(day.number, stop)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/>
                      <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/>
                    </svg>
                  </button>
                  <button
                    class="p-1.5 rounded-lg transition"
                    style="color: #e07070; background: var(--color-bg)"
                    title="Delete stop"
                    @click="promptDeleteStop(day.number, stop.id)"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" width="13" height="13" viewBox="0 0 24 24"
                         fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="3 6 5 6 21 6"/>
                      <path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6"/>
                      <path d="M10 11v6"/><path d="M14 11v6"/>
                      <path d="M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"/>
                    </svg>
                  </button>
                </div>
              </div>
            </template>
          </draggable>

          <!-- No stops yet -->
          <p v-else class="text-xs mb-4" style="color: var(--color-text-muted)">No stops yet</p>

          <!-- Add stop button -->
          <button
            class="text-xs font-medium px-3 py-1.5 rounded-lg transition"
            style="border: 0.5px dashed var(--color-primary); color: var(--color-primary-dark); background: var(--color-bg)"
            @click="openAddStop(day.number)"
          >
            + Add stop
          </button>
        </div>
      </div>
    </div>

    <!-- Add stop dialog -->
    <AddStopDialog
      v-model="showAddStop"
      :trip-id="props.id"
      :day-number="addStopForDay"
    />

    <!-- Edit stop dialog -->
    <AddStopDialog
      v-if="editStopTarget"
      v-model="showEditStop"
      :trip-id="props.id"
      :day-number="editStopTarget.dayNumber"
      :stop-id="editStopTarget.stop.id"
      :initial-stop="editStopTarget.stop"
    />

    <!-- Delete stop confirm -->
    <DeleteConfirmDialog
      v-model="showDeleteStop"
      title="Delete this stop?"
      @confirm="confirmDeleteStop"
    />

    <!-- Delete day confirm -->
    <DeleteConfirmDialog
      v-model="showDeleteDay"
      title="Delete this day and all its stops?"
      @confirm="confirmDeleteDay"
    />

    <!-- Discard confirm -->
    <DiscardConfirmDialog v-model="showDiscardDialog" @confirm="confirmDiscard" />

  </div>

  <div v-else class="text-center py-20" style="color: var(--color-text-muted)">
    Trip not found.
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import draggable from 'vuedraggable'
import { useTripStore } from '@/stores/trip'
import AddStopDialog from '@/components/AddStopDialog.vue'
import DeleteConfirmDialog from '@/components/DeleteConfirmDialog.vue'
import DiscardConfirmDialog from '@/components/DiscardConfirmDialog.vue'

const props = defineProps({ id: { type: String, required: true } })

const router = useRouter()
const tripStore = useTripStore()
const trip = computed(() => tripStore.getById(props.id))

// ── Constants ─────────────────────────────────────────────────────────────────
const typeEmoji = { hotel: '🏨', flight: '✈️', food: '🍜', activity: '🎯' }
const stopBgStyle = {
  hotel:    'background: rgba(201,169,110,0.12)',
  flight:   'background: rgba(59,130,246,0.1)',
  food:     'background: rgba(120,160,80,0.12)',
  activity: 'background: rgba(201,169,110,0.08)',
}
const statusBadgeStyle = {
  planning:  'background: rgba(201,169,110,0.2);  color: var(--color-primary)',
  confirmed: 'background: rgba(34,197,94,0.15);   color: #16a34a',
  ongoing:   'background: rgba(59,130,246,0.15);  color: #2563eb',
  done:      'background: rgba(156,163,175,0.25); color: #9ca3af',
}
const statusEmoji = { planning: '📋', confirmed: '✅', ongoing: '🚀', done: '🏁' }
const statusActiveStyle  = 'background: rgba(201,169,110,0.2); border: 1.5px solid var(--color-primary); color: var(--color-primary)'
const statusDefaultStyle = 'background: var(--color-bg); border: 1.5px solid var(--color-border); color: var(--color-text-muted)'
const fieldStyle = (focused) =>
  `background: var(--color-bg); border: 1px solid ${focused ? 'var(--color-primary)' : 'var(--color-border)'}; color: var(--color-text)`

// ── Edit mode ─────────────────────────────────────────────────────────────────
const isEditing = ref(false)
const saving = ref(false)
const focusedField = ref(null)
const showDiscardDialog = ref(false)
const budgetDisplay = ref('')

const form = reactive({
  name: '', start_date: '', end_date: '', status: 'planning',
  raw_budget: null, currency: 'VND', notes: '',
})

function formatBudget(val, currency) {
  if (val == null || val === '') return ''
  const decimals = (currency === 'USD' || currency === 'EUR') ? 2 : 0
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  }).format(val)
}

function onBudgetFocus() {
  focusedField.value = 'raw_budget'
  budgetDisplay.value = form.raw_budget != null ? String(form.raw_budget) : ''
}

function onBudgetInput(e) {
  e.target.value = e.target.value.replace(/[^0-9.]/g, '')
  budgetDisplay.value = e.target.value
}

function onBudgetBlur() {
  focusedField.value = null
  const num = parseFloat(budgetDisplay.value)
  if (!isNaN(num) && num >= 0) {
    form.raw_budget = num
    budgetDisplay.value = formatBudget(num, form.currency)
  } else {
    form.raw_budget = null
    budgetDisplay.value = ''
  }
}

function reformatBudget() {
  if (form.raw_budget != null) {
    budgetDisplay.value = formatBudget(form.raw_budget, form.currency)
  }
}

function startEdit() {
  const t = trip.value
  form.name       = t.name       || ''
  form.start_date = t.start_date || ''
  form.end_date   = t.end_date   || ''
  form.status     = t.status     || 'planning'
  form.raw_budget = t.raw_budget != null ? t.raw_budget : null
  form.currency   = t.currency   || 'VND'
  form.notes      = t.notes      || ''
  budgetDisplay.value = formatBudget(form.raw_budget, form.currency)
  isEditing.value = true
}

function hasChanges() {
  const t = trip.value
  return (
    form.name       !== (t.name       || '') ||
    form.start_date !== (t.start_date || '') ||
    form.end_date   !== (t.end_date   || '') ||
    form.status     !== (t.status     || 'planning') ||
    String(form.raw_budget) !== String(t.raw_budget ?? '') ||
    form.currency   !== (t.currency   || 'VND') ||
    form.notes      !== (t.notes      || '')
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
  if (!form.name.trim()) return
  saving.value = true
  try {
    await tripStore.update(props.id, {
      name:       form.name.trim(),
      start_date: form.start_date || null,
      end_date:   form.end_date   || null,
      status:     form.status,
      raw_budget: form.raw_budget !== '' ? Number(form.raw_budget) : null,
      currency:   form.currency,
      notes:      form.notes.trim() || null,
    })
    isEditing.value = false
    focusedField.value = null
  } finally {
    saving.value = false
  }
}

function handleBack() {
  if (isEditing.value && hasChanges()) {
    showDiscardDialog.value = true
  } else {
    router.push('/trips')
  }
}

// ── Budget tracking ───────────────────────────────────────────────────────────
const totalSpent = computed(() => {
  if (!trip.value) return 0
  return trip.value.days.reduce((sum, day) =>
    sum + day.stops.reduce((s, stop) => s + (stop.raw_cost || 0), 0), 0)
})

const budgetPct = computed(() => {
  if (!trip.value?.raw_budget || trip.value.raw_budget === 0) return 0
  return Math.round((totalSpent.value / trip.value.raw_budget) * 100)
})

const overBudget = computed(() => budgetPct.value > 100)

// ── Add stop ──────────────────────────────────────────────────────────────────
const showAddStop = ref(false)
const addStopForDay = ref(1)

function openAddStop(dayNumber) {
  addStopForDay.value = dayNumber
  showAddStop.value = true
}

// ── Edit / delete stop ────────────────────────────────────────────────────────
const showEditStop = ref(false)
const editStopTarget = ref(null)

const showDeleteStop = ref(false)
const deleteStopTarget = ref(null)

function openEditStop(dayNumber, stop) {
  editStopTarget.value = { dayNumber, stop }
  showEditStop.value = true
}

function promptDeleteStop(dayNumber, stopId) {
  deleteStopTarget.value = { dayNumber, stopId }
  showDeleteStop.value = true
}

function confirmDeleteStop() {
  if (!deleteStopTarget.value) return
  tripStore.removeStop(props.id, deleteStopTarget.value.dayNumber, deleteStopTarget.value.stopId)
  deleteStopTarget.value = null
}

// ── Delete day ────────────────────────────────────────────────────────────────
const showDeleteDay = ref(false)
const deleteDayTarget = ref(null)

function promptDeleteDay(dayNumber) {
  deleteDayTarget.value = dayNumber
  showDeleteDay.value = true
}

function confirmDeleteDay() {
  if (deleteDayTarget.value == null) return
  tripStore.removeDay(props.id, deleteDayTarget.value)
  deleteDayTarget.value = null
}
</script>
