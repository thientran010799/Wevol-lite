<template>
  <div class="space-y-6">

    <!-- Per-view page header -->
    <header class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold" style="color: var(--foreground)">Timeline</h1>
        <p class="text-sm" style="color: var(--muted-foreground)">Your journey together</p>
      </div>
      <div class="hidden md:block">
        <SearchBar />
      </div>
    </header>

    <!-- Empty state: no start date set -->
    <template v-if="!startDate">
      <div class="rounded-2xl px-8 py-12 text-center"
           style="background: var(--gradient-soft)">
        <p class="text-4xl mb-4">💑</p>
        <p class="text-sm font-medium mb-1" style="color: var(--foreground)">When did your story begin?</p>
        <p class="text-xs mb-6" style="color: var(--muted-foreground)">
          Set the date you became a couple to unlock your timeline and milestones.
        </p>
        <div class="flex flex-col items-center gap-3">
          <input
            type="date"
            v-model="dateInput"
            class="text-sm px-4 py-2 rounded-xl"
            style="border: 1px solid var(--border); background: var(--card); color: var(--foreground); outline: none"
          />
          <button
            @click="saveStartDate"
            :disabled="!dateInput"
            class="text-sm font-medium px-6 py-2 rounded-xl transition"
            :style="dateInput
              ? 'background: var(--gradient-primary); color: white; cursor: pointer; border: none'
              : 'background: rgba(168,213,226,0.2); color: var(--muted-foreground); cursor: not-allowed; border: none'"
          >
            Set start date
          </button>
        </div>
      </div>
    </template>

    <!-- Timeline content -->
    <template v-else>

      <!-- Hero counter -->
      <div class="mx-auto max-w-3xl p-8 rounded-2xl text-center w-full"
           style="background: var(--gradient-soft)">

        <!-- Editing start date inline -->
        <template v-if="editingDate">
          <p class="text-sm mb-4" style="color: var(--muted-foreground)">Change start date</p>
          <div class="flex flex-col items-center gap-3">
            <input
              type="date"
              v-model="editDateInput"
              class="text-sm px-4 py-2 rounded-xl"
              style="border: 1px solid var(--border); background: var(--card); color: var(--foreground); outline: none"
            />
            <div class="flex gap-2">
              <button
                @click="editingDate = false"
                class="text-xs px-4 py-2 rounded-xl transition"
                style="border: 1px solid var(--border); color: var(--muted-foreground); background: transparent"
              >
                Cancel
              </button>
              <button
                @click="saveEditedDate"
                :disabled="!editDateInput"
                class="text-xs font-medium px-5 py-2 rounded-xl transition"
                :style="editDateInput
                  ? 'background: var(--gradient-primary); color: white; cursor: pointer; border: none'
                  : 'background: rgba(168,213,226,0.2); color: var(--muted-foreground); cursor: not-allowed; border: none'"
              >
                Save
              </button>
            </div>
          </div>
        </template>

        <!-- Normal view -->
        <template v-else>
          <div class="flex items-center justify-center gap-2 mb-2">
            <p class="text-sm" style="color: var(--muted-foreground)">Together for</p>
            <button
              @click="openEditDate"
              class="text-xs rounded-lg px-2 py-0.5 transition"
              style="border: 1px solid var(--border); color: var(--muted-foreground); background: transparent"
              title="Change date"
            >
              ✏️
            </button>
          </div>
          <p
            class="text-6xl font-bold"
            style="background: var(--gradient-primary); -webkit-background-clip: text; -webkit-text-fill-color: transparent"
          >
            {{ daysTogether }}
          </p>
          <p class="text-sm mt-2" style="color: var(--muted-foreground)">{{ breakdown }}</p>
        </template>

      </div>

      <!-- Milestones -->
      <div>
        <h2 class="text-sm font-semibold mb-4" style="color: var(--foreground)">Milestones</h2>
        <div class="space-y-3">
          <div
            v-for="m in milestones"
            :key="m.label"
            class="flex items-center gap-4 rounded-2xl p-5 border-2 transition-all duration-300"
            :class="m.reached ? 'opacity-75' : ''"
            :style="m.reached
              ? 'background: var(--card); border-color: transparent'
              : 'background: var(--card); border-color: var(--pastel-pink)'"
          >
            <span class="text-2xl">{{ m.emoji }}</span>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-semibold" style="color: var(--foreground)">{{ m.label }}</p>
              <p class="text-sm mt-0.5" style="color: var(--muted-foreground)">{{ m.dateStr }}</p>
            </div>
            <span
              class="text-xs px-3 py-1 rounded-full font-medium shrink-0"
              :style="m.reached
                ? 'background: var(--pastel-blue-light); color: var(--pastel-blue-dark)'
                : 'background: var(--pastel-pink-light); color: var(--pastel-pink-dark)'"
            >
              {{ m.reached ? 'Reached' : `Day ${m.dayNum}` }}
            </span>
          </div>
        </div>
      </div>

    </template>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import { useAuthStore } from '@/stores/auth'
import SearchBar from '@/components/SearchBar.vue'

const authStore = useAuthStore()
const dateInput     = ref('')
const editingDate   = ref(false)
const editDateInput = ref('')

const startDate = computed(() => authStore.couple?.start_date || null)

async function saveStartDate() {
  if (!dateInput.value) return
  await authStore.updateCouple({ ...authStore.couple, start_date: dateInput.value })
}

function openEditDate() {
  editDateInput.value = startDate.value || ''
  editingDate.value = true
}

async function saveEditedDate() {
  if (!editDateInput.value) return
  await authStore.updateCouple({ ...authStore.couple, start_date: editDateInput.value })
  editingDate.value = false
}

const now = dayjs()

const start        = computed(() => startDate.value ? dayjs(startDate.value) : null)
const daysTogether = computed(() => start.value ? now.diff(start.value, 'day') : 0)
const years        = computed(() => start.value ? now.diff(start.value, 'year') : 0)
const months       = computed(() => start.value ? now.diff(start.value, 'month') % 12 : 0)
const remainingDays = computed(() => {
  if (!start.value) return 0
  return now.diff(start.value.add(now.diff(start.value, 'month'), 'month'), 'day')
})

const breakdown = computed(() => {
  const parts = []
  if (years.value > 0) parts.push(`${years.value} year${years.value !== 1 ? 's' : ''}`)
  if (months.value > 0) parts.push(`${months.value} month${months.value !== 1 ? 's' : ''}`)
  parts.push(`${remainingDays.value} day${remainingDays.value !== 1 ? 's' : ''}`)
  return parts.join(', ')
})

const milestones = computed(() => {
  if (!start.value) return []
  const s = start.value

  const defs = [
    { emoji: '💑', label: 'First day together',   date: s },
    { emoji: '💯', label: '100 days',              date: s.add(99, 'day') },
    { emoji: '🌸', label: '6 months',              date: s.add(6, 'month') },
    { emoji: '🎉', label: '1 year anniversary',    date: s.add(1, 'year') },
    { emoji: '✨', label: '500 days',              date: s.add(499, 'day') },
    { emoji: '💑', label: '2 year anniversary',    date: s.add(2, 'year') },
    { emoji: '🏆', label: '1,000 days',            date: s.add(999, 'day') },
    { emoji: '💍', label: '3 year anniversary',    date: s.add(3, 'year') },
    { emoji: '🌟', label: '1,500 days',            date: s.add(1499, 'day') },
    { emoji: '🥂', label: '5 year anniversary',    date: s.add(5, 'year') },
  ]

  return defs.map(m => {
    const dayNum  = m.date.diff(s, 'day') + 1
    const reached = !m.date.isAfter(now, 'day')
    return {
      emoji:   m.emoji,
      label:   m.label,
      dayNum,
      dateStr: m.date.format('MMM D, YYYY'),
      reached,
    }
  })
})
</script>
