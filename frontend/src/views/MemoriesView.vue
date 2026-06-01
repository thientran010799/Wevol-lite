<template>
  <div>
    <!-- Per-view page header -->
    <header class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold" style="color: var(--foreground)">Memories</h1>
        <p class="text-sm" style="color: var(--muted-foreground)">Your shared moments</p>
      </div>
      <div class="flex items-center gap-3">
        <div class="hidden md:block"><SearchBar /></div>
        <button
          class="flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium text-white cursor-pointer"
          style="background: var(--gradient-primary); border: none"
          @click="showAdd = true"
        >
          <Heart class="w-4 h-4" />
          <span class="hidden sm:inline">New memory</span>
          <span class="sm:hidden">New</span>
        </button>
      </div>
    </header>

    <!-- Skeleton while loading -->
    <div v-if="memoryStore.loading" class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
      <div v-for="n in 6" :key="n" class="flex gap-4 p-4 md:p-6 rounded-2xl" style="background: var(--card)">
        <div class="skeleton w-24 h-24 rounded-2xl flex-shrink-0" />
        <div class="flex-1 flex flex-col justify-center gap-2">
          <div class="skeleton h-5 w-3/4" />
          <div class="skeleton h-4 w-1/3" />
          <div class="skeleton h-4 w-full" />
          <div class="skeleton h-4 w-2/3" />
        </div>
      </div>
    </div>

    <!-- Empty state -->
    <EmptyState
      v-else-if="!memoryStore.memories.length"
      emoji="💝"
      message="No memories yet. Add your first shared moment!"
    />

    <!-- Responsive 2-col memory grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-6">
      <router-link
        v-for="(memory, index) in memoryStore.memories"
        :key="memory.id"
        :to="`/memories/${memory.id}`"
        class="flex gap-4 p-4 md:p-6 rounded-2xl border-2 border-transparent hover:border-[var(--pastel-blue)] hover:shadow-xl transition-all duration-300 cursor-pointer"
        style="background: var(--card); text-decoration: none;"
      >
        <!-- Thumbnail -->
        <div
          class="w-24 h-24 rounded-2xl flex-shrink-0 flex items-center justify-center text-3xl"
          :style="index % 2 === 0
            ? 'background: var(--pastel-blue-light)'
            : 'background: var(--pastel-pink-light)'"
        >
          {{ moodEmoji[memory.mood] || '💝' }}
        </div>

        <!-- Content -->
        <div class="flex-1 min-w-0 flex flex-col justify-center">
          <h3
            class="text-lg font-semibold truncate"
            style="color: var(--foreground)"
          >
            {{ memory.title }}
          </h3>

          <p class="text-sm mt-0.5" style="color: var(--muted-foreground)">
            {{ memory.date }}
          </p>

          <p
            v-if="memory.note"
            class="text-sm line-clamp-2 mt-1"
            style="color: var(--muted-foreground)"
          >
            {{ memory.note }}
          </p>

          <!-- Badges -->
          <div class="flex flex-wrap gap-2 mt-2">
            <!-- Photo count badge -->
            <span
              v-if="memory.photos > 0"
              class="text-xs px-2 py-0.5 rounded-full"
              style="background: var(--pastel-blue-light); color: var(--pastel-blue-dark)"
            >
              {{ memory.photos }} photos
            </span>

            <!-- Mood badge -->
            <span
              v-if="memory.mood"
              class="text-xs px-2 py-0.5 rounded-full capitalize"
              :style="moodBadgeStyle(memory.mood)"
            >
              {{ memory.mood }}
            </span>
          </div>
        </div>
      </router-link>
    </div>

    <AddMemoryDialog v-model="showAdd" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Heart } from '@lucide/vue'
import { useMemoryStore } from '@/stores/memory'
import SearchBar from '@/components/SearchBar.vue'
import AddMemoryDialog from '@/components/AddMemoryDialog.vue'
import EmptyState from '@/components/EmptyState.vue'

const showAdd = ref(false)
const memoryStore = useMemoryStore()
const moodEmoji = { happy: '😊', romantic: '💝', cozy: '🌿', adventure: '🌍' }

function moodBadgeStyle(mood) {
  if (mood === 'happy' || mood === 'romantic') {
    return 'background: rgba(255,193,204,0.4); color: var(--pastel-pink-dark)'
  }
  return 'background: rgba(168,213,226,0.4); color: var(--pastel-blue-dark)'
}

onMounted(() => memoryStore.fetchAll())
</script>
