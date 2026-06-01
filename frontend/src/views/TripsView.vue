<template>
  <div>
    <header class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold" style="color: var(--foreground)">Trips</h1>
        <p class="text-sm" style="color: var(--muted-foreground)">Adventures together</p>
      </div>
      <div class="flex items-center gap-3">
        <div class="hidden md:block"><SearchBar /></div>
        <button
          class="flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium text-white cursor-pointer"
          style="background: var(--gradient-primary); border: none"
          @click="showAdd = true"
        >
          <MapPin class="w-4 h-4" />
          <span class="hidden sm:inline">Plan trip</span>
          <span class="sm:hidden">New</span>
        </button>
      </div>
    </header>

    <!-- Skeleton while loading -->
    <div v-if="tripStore.loading">
      <div class="skeleton rounded-2xl h-24 mb-6" />
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
        <div v-for="n in 4" :key="n" class="rounded-2xl p-5" style="background: var(--card)">
          <div class="flex items-start justify-between gap-2 mb-3">
            <div class="flex-1 flex flex-col gap-2">
              <div class="skeleton h-5 w-3/4" />
              <div class="skeleton h-4 w-1/3" />
            </div>
            <div class="skeleton h-6 w-20 rounded-full" />
          </div>
          <div class="skeleton h-32 rounded-xl" />
        </div>
      </div>
    </div>

    <!-- Loaded content -->
    <template v-else>
      <!-- Upcoming banner -->
      <div v-if="upcoming" class="rounded-2xl p-5 mb-6 flex items-center justify-between"
           style="background: var(--gradient-soft)">
        <div>
          <p class="text-xs uppercase tracking-widest mb-1" style="color: var(--muted-foreground)">Next trip</p>
          <p class="text-lg font-bold" style="color: var(--foreground)">{{ upcoming.name }}</p>
          <p class="text-xs mt-1" style="color: var(--muted-foreground)">{{ upcoming.dates }}</p>
        </div>
        <router-link
          :to="`/trips/${upcoming.id}`"
          class="text-sm px-4 py-2 rounded-lg font-medium shrink-0"
          style="background: var(--gradient-primary); color: white; text-decoration: none"
        >
          View
        </router-link>
      </div>

      <!-- Empty state -->
      <EmptyState
        v-if="!tripStore.trips.length"
        emoji="✈️"
        message="No trips yet — plan your next adventure!"
      />

      <!-- Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
        <router-link
          v-for="trip in tripStore.trips"
          :key="trip.id"
          :to="`/trips/${trip.id}`"
          class="rounded-2xl p-5 block border-2 border-transparent hover:border-[var(--pastel-blue)] hover:shadow-xl transition-all duration-300"
          style="background: var(--card); text-decoration: none"
        >
          <div class="flex items-start justify-between gap-2">
            <div class="min-w-0">
              <h3 class="text-base font-semibold truncate" style="color: var(--foreground)">{{ trip.name }}</h3>
              <p class="text-xs mt-0.5" style="color: var(--muted-foreground)">{{ trip.dates }}</p>
            </div>
            <span
              class="text-xs px-2.5 py-0.5 rounded-full capitalize font-medium shrink-0"
              :style="`background: ${statusColor[trip.status]}40; color: ${statusColor[trip.status]}`"
            >
              {{ trip.status }}
            </span>
          </div>
          <!-- Colored preview block -->
          <div
            class="h-32 rounded-xl mt-3 flex items-end p-3"
            :style="`background: ${statusColor[trip.status]}33`"
          >
            <p v-if="trip.budget" class="text-xs font-medium" style="color: var(--pastel-pink-dark)">
              {{ trip.budget }}
            </p>
          </div>
        </router-link>
      </div>
    </template>

    <AddTripDialog v-model="showAdd" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import dayjs from 'dayjs'
import { MapPin } from '@lucide/vue'
import { useTripStore } from '@/stores/trip'
import SearchBar from '@/components/SearchBar.vue'
import AddTripDialog from '@/components/AddTripDialog.vue'
import EmptyState from '@/components/EmptyState.vue'

const tripStore = useTripStore()
const showAdd = ref(false)

onMounted(() => tripStore.fetchAll())

// Hex values used for the color+opacity badge/preview pattern (color + '40' = 25% bg, color + '33' = 20% bg).
// planning/done use palette tokens; confirmed/ongoing are status-only colors with no theme variable equivalent.
const statusColor = {
  planning:  '#a8d5e2', // --pastel-blue
  confirmed: '#a8e2b4', // soft green — no theme token
  ongoing:   '#f9c784', // soft amber — no theme token
  done:      '#c4c4c4', // neutral gray — no theme token
}

const today = dayjs().format('YYYY-MM-DD')

const upcoming = computed(() =>
  tripStore.trips.find(t =>
    (t.status === 'planning' || t.status === 'confirmed') &&
    (!t.start_date || t.start_date >= today)
  )
)
</script>
