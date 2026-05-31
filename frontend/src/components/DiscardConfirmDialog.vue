<template>
  <Teleport to="body">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4"
         style="background: rgba(0,0,0,0.4)">
      <div class="w-full max-w-sm rounded-2xl p-7 shadow-xl"
           style="background: var(--color-surface); border: 1px solid var(--color-border)">
        <h3 class="text-base font-bold mb-2" style="color: var(--color-text)">Discard changes?</h3>
        <p class="text-sm mb-6" style="color: var(--color-text-muted)">Your unsaved changes will be lost.</p>
        <div class="flex justify-end gap-3">
          <button class="px-4 py-2 rounded-lg text-sm font-medium"
                  style="color: var(--color-text-muted); background: var(--color-bg); border: 1px solid var(--color-border)"
                  @click="emit('update:modelValue', false)">
            Keep editing
          </button>
          <button class="px-4 py-2 rounded-lg text-sm font-medium text-white"
                  style="background: #e07070"
                  @click="confirm">
            Discard
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
defineProps({ modelValue: Boolean })
const emit = defineEmits(['update:modelValue', 'confirm'])

function confirm() {
  emit('confirm')
  emit('update:modelValue', false)
}
</script>
