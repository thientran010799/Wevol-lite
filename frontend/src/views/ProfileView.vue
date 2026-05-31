<template>
  <div class="max-w-xl space-y-6">

    <div>
      <h2 class="text-lg font-semibold" style="color: var(--color-text)">Profile &amp; Settings</h2>
      <p class="text-sm mt-1" style="color: var(--color-text-muted)">Update your couple details.</p>
    </div>

    <!-- Couple card -->
    <div class="rounded-2xl p-6 space-y-4" style="background: var(--color-surface); border: 1px solid var(--color-border)">
      <h3 class="text-sm font-semibold" style="color: var(--color-text)">Couple details</h3>

      <div>
        <label class="block text-sm font-medium mb-1" style="color: var(--color-text)">Couple name</label>
        <input
          v-model="form.coupleName"
          type="text"
          placeholder="e.g. AN & TK"
          class="w-full rounded-lg px-4 py-2.5 text-sm outline-none transition focus:ring-2"
          style="background: var(--color-bg); border: 1px solid var(--color-border); color: var(--color-text); --tw-ring-color: var(--color-primary)"
        />
      </div>

      <div>
        <label class="block text-sm font-medium mb-1" style="color: var(--color-text)">
          Together since
          <span class="font-normal" style="color: var(--color-text-muted)">(used for the days counter)</span>
        </label>
        <input
          v-model="form.startDate"
          type="date"
          class="w-full rounded-lg px-4 py-2.5 text-sm outline-none transition focus:ring-2"
          style="background: var(--color-bg); border: 1px solid var(--color-border); color: var(--color-text); --tw-ring-color: var(--color-primary)"
        />
      </div>

      <div>
        <label class="block text-sm font-medium mb-1" style="color: var(--color-text)">
          Anniversary date
          <span class="font-normal" style="color: var(--color-text-muted)">(optional)</span>
        </label>
        <input
          v-model="form.anniversary"
          type="date"
          class="w-full rounded-lg px-4 py-2.5 text-sm outline-none transition focus:ring-2"
          style="background: var(--color-bg); border: 1px solid var(--color-border); color: var(--color-text); --tw-ring-color: var(--color-primary)"
        />
      </div>
    </div>

    <div v-if="successMsg" class="px-4 py-3 rounded-lg text-sm text-green-700 bg-green-50 border border-green-200">
      {{ successMsg }}
    </div>

    <button
      class="w-full py-3 rounded-lg font-semibold text-sm text-white transition"
      style="background: var(--color-primary)"
      @click="save"
      @mouseover="e => e.currentTarget.style.background = 'var(--color-primary-dark)'"
      @mouseleave="e => e.currentTarget.style.background = 'var(--color-primary)'"
    >
      Save changes
    </button>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()

const form = ref({
  coupleName:  auth.couple?.name || '',
  startDate:   auth.couple?.start_date || '',
  anniversary: auth.couple?.anniversary || '',
})

const successMsg = ref('')

function save() {
  auth.updateCouple({
    name:        form.value.coupleName.trim() || 'Us',
    start_date:  form.value.startDate || null,
    anniversary: form.value.anniversary || null,
  })
  successMsg.value = 'Changes saved.'
  setTimeout(() => { successMsg.value = '' }, 3000)
}
</script>
