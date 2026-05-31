<template>
  <div class="min-h-screen relative" style="background: var(--background)">

    <!-- Gradient background orbs -->
    <div class="fixed inset-0 pointer-events-none overflow-hidden" style="z-index: 0">
      <div
        class="absolute top-0 right-0 w-[300px] md:w-[600px] h-[300px] md:h-[600px] rounded-full opacity-20 blur-[100px] md:blur-[150px]"
        style="background: var(--pastel-pink);"
      />
      <div
        class="absolute bottom-0 left-0 w-[300px] md:w-[600px] h-[300px] md:h-[600px] rounded-full opacity-20 blur-[100px] md:blur-[150px]"
        style="background: var(--pastel-blue);"
      />
    </div>

    <!-- Floating glassmorphism sidebar — desktop only -->
    <aside
      class="fixed left-6 top-6 bottom-6 w-20 hidden md:flex flex-col items-center rounded-3xl"
      style="
        background: var(--sidebar);
        backdrop-filter: blur(20px);
        -webkit-backdrop-filter: blur(20px);
        border: 1px solid var(--sidebar-border);
        box-shadow: 0 8px 32px rgba(168, 213, 226, 0.15);
        z-index: 10;
      "
    >
      <!-- Logo -->
      <div
        class="mt-4 mb-6 w-12 h-12 rounded-[14px] flex items-center justify-center text-base font-bold text-white shrink-0"
        style="background: var(--gradient-primary)"
      >
        We
      </div>

      <!-- Nav items -->
      <nav class="flex flex-col gap-1 flex-1 items-center w-full px-2">
        <button
          v-for="item in navItems"
          :key="item.to"
          class="w-10 h-10 rounded-xl flex items-center justify-center relative transition-all hover:bg-[rgba(168,213,226,0.15)]"
          :style="isActive(item) ? activeNavStyle : inactiveNavStyle"
          :title="item.label"
          @click="router.push(item.to)"
        >
          <component :is="item.icon" class="w-5 h-5" />
          <span
            v-if="isActive(item)"
            class="absolute left-0 top-2 bottom-2 w-1 rounded-full"
            style="background: var(--gradient-primary)"
          />
        </button>
      </nav>

      <!-- Bottom: avatar -->
      <div class="mb-4 flex flex-col items-center gap-2">
        <button
          class="w-10 h-10 rounded-full flex items-center justify-center text-xs font-bold text-white transition-opacity hover:opacity-80"
          style="background: var(--gradient-primary)"
          :title="auth.coupleName"
          @click="router.push('/profile')"
        >
          {{ auth.initials }}
        </button>
      </div>
    </aside>

    <!-- Mobile top bar -->
    <header
      class="md:hidden sticky top-0 left-0 right-0 flex items-center justify-between px-4 h-14"
      style="
        background: var(--sidebar);
        backdrop-filter: blur(20px);
        -webkit-backdrop-filter: blur(20px);
        border-bottom: 1px solid var(--sidebar-border);
        z-index: 50;
      "
    >
      <div class="flex items-center gap-3 min-w-0">
        <div
          class="w-10 h-10 rounded-[10px] flex items-center justify-center text-sm font-bold text-white shrink-0"
          style="background: var(--gradient-primary)"
        >
          We
        </div>
        <span class="text-sm font-semibold truncate" style="color: var(--foreground)">
          {{ pageTitle }}
        </span>
      </div>

      <div class="flex items-center gap-2 shrink-0">
        <button
          class="p-2 rounded-full flex items-center justify-center transition-opacity hover:opacity-80"
          style="background: var(--gradient-primary)"
          :title="auth.coupleName"
          @click="router.push('/profile')"
        >
          <span class="w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold text-white">
            {{ auth.initials }}
          </span>
        </button>
      </div>
    </header>

    <!-- Main content -->
    <main
      class="flex-1 ml-0 md:ml-32 mr-0 md:mr-6 px-4 md:px-0 py-4 md:py-6 pb-20 md:pb-6 relative"
      style="z-index: 1"
    >
      <div class="pb-6">
        <router-view />
      </div>
    </main>

    <!-- Mobile bottom navigation -->
    <nav
      class="md:hidden fixed bottom-0 left-0 right-0 pb-safe"
      style="z-index: 50"
    >
      <div
        class="mx-4 mb-4 rounded-3xl px-2 py-3 flex items-center justify-around"
        style="
          background: var(--sidebar);
          backdrop-filter: blur(20px);
          -webkit-backdrop-filter: blur(20px);
          border: 1px solid var(--sidebar-border);
          box-shadow: 0 8px 32px rgba(168, 213, 226, 0.15);
        "
      >
        <button
          v-for="item in navItems"
          :key="item.to"
          class="min-w-12 min-h-12 flex flex-col items-center justify-center gap-0.5 rounded-xl px-2 transition-all"
          :aria-label="item.label"
          @click="router.push(item.to)"
        >
          <component
            :is="item.mobileIcon"
            class="w-5 h-5"
            :style="isActive(item) ? 'color: var(--pastel-blue-dark)' : 'color: var(--muted-foreground)'"
          />
          <span
            class="text-xs"
            :style="isActive(item)
              ? 'color: var(--pastel-blue-dark); font-weight: 700'
              : 'color: var(--muted-foreground); font-weight: 400'"
          >
            {{ item.label }}
          </span>
        </button>
      </div>
    </nav>

  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Home, LayoutDashboard, Heart, Camera, MapPin, Calendar } from '@lucide/vue'
import { useAuthStore } from '@/stores/auth'

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

const navItems = [
  { label: 'Dashboard', to: '/dashboard',          icon: Home,     mobileIcon: LayoutDashboard, prefix: '/dashboard' },
  { label: 'Memories',  to: '/memories',            icon: Heart,    mobileIcon: Heart,           prefix: '/memories' },
  { label: 'Gallery',   to: '/gallery',             icon: Camera,   mobileIcon: Camera,          prefix: '/gallery' },
  { label: 'Trips',     to: '/trips',               icon: MapPin,   mobileIcon: MapPin,          prefix: '/trips' },
  { label: 'Timeline',  to: '/dashboard/timeline',  icon: Calendar, mobileIcon: Calendar,        prefix: '/dashboard/timeline' },
]

const pageTitleMap = {
  '/dashboard':           'Dashboard',
  '/dashboard/timeline':  'Timeline',
  '/memories':            'Memories',
  '/gallery':             'Gallery',
  '/trips':               'Trips',
  '/profile':             'Profile',
}

const pageTitle = computed(() => {
  const exact = pageTitleMap[route.path]
  if (exact) return exact
  if (route.path.startsWith('/memories/'))  return 'Memory'
  if (route.path.startsWith('/gallery/'))   return 'Album'
  if (route.path.startsWith('/trips/'))     return 'Trip'
  return ''
})

function isActive(item) {
  if (item.prefix === '/dashboard' && item.to === '/dashboard') {
    return route.path === '/dashboard'
  }
  return route.path === item.prefix || route.path.startsWith(item.prefix + '/')
}

const activeNavStyle   = `background: var(--sidebar-accent); color: var(--pastel-blue-dark);`
const inactiveNavStyle = `color: var(--muted-foreground);`
</script>
