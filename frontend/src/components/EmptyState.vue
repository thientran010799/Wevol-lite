<template>
  <div class="py-8 text-center rounded-xl" style="border: 1px dashed var(--color-border)">
    <p v-if="emoji" class="text-2xl mb-2">{{ emoji }}</p>
    <p class="text-sm" style="color: var(--color-text-muted)">{{ message }}</p>
  </div>
</template>

<script setup>
defineProps({
  emoji:   { type: String, default: '' },
  message: { type: String, default: 'Nothing here yet' },
})
</script>
