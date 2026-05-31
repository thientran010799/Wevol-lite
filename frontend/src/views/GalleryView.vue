<template>
  <div>
    <header class="mb-8 flex items-center justify-between">
      <div>
        <h1 class="text-3xl font-bold" style="color: var(--foreground)">Gallery</h1>
        <p class="text-sm" style="color: var(--muted-foreground)">Your photo albums</p>
      </div>
      <div class="flex items-center gap-3">
        <div class="hidden md:block"><SearchBar /></div>
        <button
          class="flex items-center gap-1.5 px-4 py-2 rounded-lg text-sm font-medium text-white cursor-pointer"
          style="background: var(--gradient-primary); border: none"
          @click="showAdd = true"
        >
          <Camera class="w-4 h-4" />
          <span class="hidden sm:inline">New album</span>
          <span class="sm:hidden">New</span>
        </button>
      </div>
    </header>

    <div v-if="galleryStore.albums.length" class="grid grid-cols-2 md:grid-cols-3 gap-4 md:gap-6">
      <router-link
        v-for="(album, index) in galleryStore.albums"
        :key="album.id"
        :to="`/gallery/${album.id}`"
        class="rounded-2xl p-6 md:p-8 text-center block border-2 border-transparent hover:border-[var(--pastel-blue)] hover:shadow-xl transition-all duration-300"
        :style="index % 2 === 0
          ? 'background: var(--pastel-blue-light); text-decoration: none'
          : 'background: var(--pastel-pink-light); text-decoration: none'"
      >
        <div class="text-5xl mb-3">{{ album.emoji }}</div>
        <p class="text-lg font-semibold truncate" style="color: var(--foreground)">{{ album.name }}</p>
        <p class="text-sm mt-1" style="color: var(--muted-foreground)">{{ album.photos.length }} photos</p>
      </router-link>
    </div>

    <EmptyState
      v-else
      emoji="🖼️"
      message="No albums yet — create your first album to start collecting photos."
    />

    <AddAlbumDialog v-model="showAdd" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Camera } from '@lucide/vue'
import { useGalleryStore } from '@/stores/gallery'
import SearchBar from '@/components/SearchBar.vue'
import AddAlbumDialog from '@/components/AddAlbumDialog.vue'
import EmptyState from '@/components/EmptyState.vue'

const galleryStore = useGalleryStore()
const showAdd = ref(false)

onMounted(() => galleryStore.fetchAll())
</script>
