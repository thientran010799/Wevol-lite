<template>
  <div class="space-y-6">

    <!-- Header -->
    <header class="flex items-start justify-between">
      <div>
        <h1 class="text-3xl font-bold" style="color: var(--foreground)">Dashboard</h1>
        <p class="text-sm mt-1" style="color: var(--muted-foreground)">Welcome back to your shared moments</p>
      </div>
      <span
        class="flex items-center gap-1.5 px-4 py-2 rounded-full text-sm font-semibold shrink-0"
        style="background: var(--pastel-pink-light); color: var(--pastel-pink-dark)"
      >
        💛 Day {{ daysTogether }}
      </span>
    </header>

    <!-- Stat cards — horizontal: icon left + number/label right -->
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6">
      <div
        v-for="stat in statCards"
        :key="stat.label"
        class="flex items-center gap-4 p-5 rounded-2xl cursor-pointer border-2 border-transparent hover:border-[var(--pastel-blue)] hover:shadow-xl transition-all duration-300"
        :style="stat.bg"
        @click="router.push(stat.route)"
      >
        <div
          class="w-12 h-12 rounded-2xl flex items-center justify-center shrink-0"
          :style="stat.iconBg"
        >
          <component :is="stat.icon" class="w-6 h-6 text-white" />
        </div>
        <div>
          <p class="text-4xl font-bold leading-none" style="color: var(--foreground)">
            {{ stat.value.toLocaleString() }}
          </p>
          <p class="text-sm mt-1" style="color: var(--muted-foreground)">{{ stat.label }}</p>
        </div>
      </div>
    </div>

    <!-- Recent Memories -->
    <div>
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-xl font-semibold" style="color: var(--foreground)">Recent Memories</h2>
        <router-link
          to="/memories"
          class="text-sm px-4 py-1.5 rounded-full border font-medium"
          style="border-color: var(--pastel-blue); color: var(--pastel-blue-dark); text-decoration: none"
        >
          View All
        </router-link>
      </div>

      <EmptyState v-if="recentMemories.length === 0" message="No memories yet — start capturing moments together." />

      <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
        <div
          v-for="(memory, i) in recentMemories"
          :key="memory.id"
          class="flex items-center gap-4 p-4 rounded-2xl cursor-pointer hover:shadow-md transition-all duration-300"
          style="background: var(--card)"
          @click="router.push(`/memories/${memory.id}`)"
        >
          <!-- Solid colored thumbnail — no emoji -->
          <div
            class="w-20 h-20 rounded-2xl shrink-0"
            :style="i % 2 === 0 ? 'background: var(--pastel-pink-light)' : 'background: var(--pastel-blue-light)'"
          />

          <!-- Content -->
          <div class="flex-1 min-w-0">
            <p class="font-semibold text-base leading-tight truncate" style="color: var(--foreground)">
              {{ memory.title }}
            </p>
            <p class="text-sm mt-1" style="color: var(--muted-foreground)">{{ memory.date }}</p>
            <p
              class="text-sm mt-1 capitalize font-medium"
              :style="moodTextStyle[memory.mood] ?? moodTextStyle.happy"
            >
              {{ memory.mood }}
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- Upcoming trip banner -->
    <div
      v-if="upcomingTrips[0]"
      class="flex items-center justify-between p-5 rounded-2xl"
      style="background: var(--gradient-primary)"
    >
      <div class="flex items-center gap-4">
        <div
          class="w-14 h-14 rounded-2xl flex items-center justify-center text-2xl shrink-0"
          style="background: rgba(255,255,255,0.85)"
        >
          ✈️
        </div>
        <div>
          <p class="font-semibold text-base" style="color: var(--foreground)">
            {{ upcomingTrips[0].name }}
          </p>
          <p class="text-sm mt-0.5" style="color: var(--foreground); opacity: 0.75">
            {{ upcomingTrips[0].dates }}
            <span v-if="daysUntil(upcomingTrips[0]) !== null">
              · {{ daysUntil(upcomingTrips[0]) }} days away
            </span>
          </p>
        </div>
      </div>
      <router-link
        :to="`/trips/${upcomingTrips[0].id}`"
        class="shrink-0 px-5 py-2 rounded-full text-sm font-medium"
        style="background: rgba(255,255,255,0.7); color: var(--foreground); text-decoration: none"
      >
        View Details
      </router-link>
    </div>
    <EmptyState v-else message="No upcoming trips — plan your next adventure." />

  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'
import { useRouter } from 'vue-router'
import { Heart, Camera, MapPin } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'
import { useMemoryStore } from '@/stores/memory'
import { useTripStore } from '@/stores/trip'
import { useGalleryStore } from '@/stores/gallery'
import EmptyState from '@/components/EmptyState.vue'

const router = useRouter()
const authStore = useAuthStore()
const memoryStore = useMemoryStore()
const tripStore = useTripStore()
const galleryStore = useGalleryStore()

const startDate = computed(() => authStore.couple?.start_date || '2022-02-14')
const daysTogether = computed(() => dayjs().diff(dayjs(startDate.value), 'day'))

const photoCount = computed(() =>
  galleryStore.albums.reduce((sum, a) => sum + a.photos.length, 0)
)

const statCards = computed(() => [
  {
    label: 'Memories', value: memoryStore.memories.length, route: '/memories',
    icon: Heart,
    bg: 'background: var(--gradient-soft)',
    iconBg: 'background: var(--pastel-blue)',
  },
  {
    label: 'Photos', value: photoCount.value, route: '/gallery',
    icon: Camera,
    bg: 'background: var(--gradient-soft-reversed)',
    iconBg: 'background: var(--pastel-pink)',
  },
  {
    label: 'Adventures', value: tripStore.trips.length, route: '/trips',
    icon: MapPin,
    bg: 'background: var(--gradient-soft)',
    iconBg: 'background: var(--pastel-blue)',
  },
])

const moodTextStyle = {
  happy:     'color: var(--pastel-blue-dark)',
  romantic:  'color: var(--pastel-pink-dark)',
  cozy:      'color: var(--pastel-blue-dark)',
  adventure: 'color: var(--pastel-pink-dark)',
}

const recentMemories = computed(() =>
  [...memoryStore.memories]
    .sort((a, b) => dayjs(b.memoryDate).valueOf() - dayjs(a.memoryDate).valueOf())
    .slice(0, 4)
)

const today = computed(() => dayjs().format('YYYY-MM-DD'))
const upcomingTrips = computed(() =>
  tripStore.trips
    .filter(t =>
      ['planning', 'confirmed'].includes(t.status) &&
      (!t.start_date || t.start_date >= today.value)
    )
    .sort((a, b) => {
      if (!a.start_date) return 1
      if (!b.start_date) return -1
      return a.start_date.localeCompare(b.start_date)
    })
    .slice(0, 1)
)

function daysUntil(trip) {
  if (!trip.start_date) return null
  const d = dayjs(trip.start_date).diff(dayjs(), 'day')
  return d > 0 ? d : null
}
</script>
