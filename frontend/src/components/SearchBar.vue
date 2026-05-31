<template>
  <div class="relative" ref="rootRef">

    <!-- Input wrapper -->
    <div
      class="search-input-wrap flex items-center gap-1.5 px-2.5"
      :class="{ 'search-input-wrap--focused': focused }"
    >
      <!-- Search icon / spinner -->
      <span class="shrink-0 flex items-center justify-center" style="color: var(--color-hint)">
        <svg v-if="!store.loading" width="14" height="14" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/>
          <line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <svg v-else class="spinner" width="14" height="14" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <path d="M12 2a10 10 0 0 1 10 10"/>
        </svg>
      </span>

      <input
        ref="inputRef"
        v-model="query"
        type="text"
        placeholder="Search memories & trips…"
        class="search-input flex-1 bg-transparent outline-none"
        style="font-size: 13px; color: var(--color-text)"
        @focus="focused = true"
        @blur="handleBlur"
        @keydown.escape="handleEscape"
        @input="handleInput"
      />

      <!-- Clear button -->
      <button
        v-if="query"
        class="clear-btn shrink-0 flex items-center justify-center w-4 h-4 rounded-full transition-colors"
        style="color: var(--color-hint)"
        @mousedown.prevent="clearSearch"
        aria-label="Clear search"
      >
        <svg width="10" height="10" viewBox="0 0 24 24" fill="none"
             stroke="currentColor" stroke-width="2.5" stroke-linecap="round">
          <line x1="18" y1="6" x2="6" y2="18"/>
          <line x1="6" y1="6" x2="18" y2="18"/>
        </svg>
      </button>
    </div>

    <!-- Dropdown -->
    <Transition name="search-drop">
      <div
        v-if="showDropdown"
        class="search-dropdown absolute left-0 mt-1.5 rounded-xl border overflow-hidden"
        style="min-width: 320px; background: var(--color-surface); border-color: var(--color-cream-dark); box-shadow: 0 4px 20px rgba(0,0,0,0.12); z-index: 300"
      >
        <!-- Results list -->
        <div class="overflow-y-auto" style="max-height: 400px">
          <template v-if="store.results.length">
            <button
              v-for="item in store.results"
              :key="item.id"
              class="result-row w-full flex items-start gap-3 px-4 py-3 text-left transition-colors"
              @mousedown.prevent="navigateTo(item)"
            >
              <!-- Type icon chip -->
              <div
                class="flex items-center justify-center w-6 h-6 rounded-md shrink-0 mt-0.5 text-sm"
                :style="typeChipStyle(item.type)"
              >
                {{ typeIcon(item.type) }}
              </div>

              <!-- Content -->
              <div class="flex-1 min-w-0">
                <div class="flex items-baseline justify-between gap-2">
                  <span class="result-title truncate" style="font-size: 13px; font-weight: 500; color: var(--color-text)">
                    {{ item.title }}
                  </span>
                  <span class="shrink-0" style="font-size: 11px; color: var(--color-hint)">
                    {{ formatDate(item.date) }}
                  </span>
                </div>
                <p class="result-snippet mt-0.5 truncate" style="font-size: 11px; color: var(--color-muted)">
                  {{ item.snippet }}
                </p>
              </div>
            </button>
          </template>

          <!-- No results -->
          <div v-else-if="!store.loading" class="flex flex-col items-center justify-center py-10 gap-1.5">
            <span style="font-size: 24px">🔍</span>
            <p class="text-sm" style="color: var(--color-muted)">No results found</p>
            <p class="text-xs" style="color: var(--color-hint)">"{{ query }}"</p>
          </div>
        </div>
      </div>
    </Transition>

  </div>
</template>

<script setup>
import { ref, computed, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { useSearchStore } from '@/stores/search'

const router = useRouter()
const store = useSearchStore()

const rootRef = ref(null)
const inputRef = ref(null)
const query = ref('')
const focused = ref(false)

let debounceTimer = null

const showDropdown = computed(() => {
  return focused.value && query.value.trim().length >= 2 && (store.results.length > 0 || (!store.loading && store.error === null))
})

function handleInput() {
  clearTimeout(debounceTimer)
  if (query.value.trim().length < 2) {
    store.clear()
    return
  }
  debounceTimer = setTimeout(() => {
    store.search(query.value)
  }, 300)
}

function handleBlur() {
  focused.value = false
}

function handleEscape() {
  focused.value = false
  inputRef.value?.blur()
}

function clearSearch() {
  query.value = ''
  store.clear()
  focused.value = false
}

function navigateTo(item) {
  focused.value = false
  store.clear()
  query.value = ''
  if (item.type === 'memory') {
    router.push(`/memories/${item.id}`)
  } else {
    router.push(`/trips/${item.id}`)
  }
}

function typeIcon(type) {
  return type === 'memory' ? '🧠' : '✈️'
}

function typeChipStyle(type) {
  if (type === 'memory') return 'background: #F5D0CE'
  return 'background: #F3EBDB'
}

function formatDate(date) {
  if (!date) return ''
  return dayjs(date).format('MMM D, YYYY')
}

watch(query, val => {
  if (!val.trim()) store.clear()
})

onUnmounted(() => {
  clearTimeout(debounceTimer)
  store.clear()
})
</script>

<style scoped>
.search-input-wrap {
  height: 34px;
  width: 240px;
  background: var(--color-surface);
  border: 1px solid var(--color-cream-dark);
  border-radius: 8px;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}
.search-input-wrap--focused {
  border-color: var(--color-rose);
  box-shadow: 0 0 0 3px rgba(242, 151, 160, 0.15);
}

.search-input::placeholder {
  color: var(--color-hint);
}

.clear-btn:hover {
  background: var(--color-blush);
  color: var(--color-rose-dark);
}

.result-row:hover {
  background: var(--color-blush-light);
}

.spinner {
  animation: spin 0.75s linear infinite;
  transform-origin: center;
}
@keyframes spin {
  from { transform: rotate(0deg) }
  to   { transform: rotate(360deg) }
}

.search-drop-enter-active,
.search-drop-leave-active {
  transition: opacity 0.15s ease, transform 0.15s ease;
}
.search-drop-enter-from,
.search-drop-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>
