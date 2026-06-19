<template>
  <div class="error-box" role="alert">
    <span class="error-icon">⚠️</span>
    <div class="error-content">
      <strong>Error</strong>
      <p>{{ message }}</p>
    </div>
    <button v-if="dismissible" class="error-close" @click="$emit('dismiss')" aria-label="Cerrar">×</button>
  </div>
</template>

<script setup>
defineProps({
  message: { type: String, required: true },
  dismissible: { type: Boolean, default: true }
})
defineEmits(['dismiss'])
</script>

<style scoped>
.error-box {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  background: var(--color-danger-light);
  border: 1px solid var(--color-danger);
  border-radius: var(--radius);
  color: var(--color-danger);
}
.error-icon { font-size: 1.25rem; flex-shrink: 0; }
.error-content { flex: 1; }
.error-content p { font-size: 0.875rem; margin-top: 0.125rem; }
.error-close {
  background: none;
  border: none;
  font-size: 1.25rem;
  cursor: pointer;
  color: var(--color-danger);
  line-height: 1;
  padding: 0 0.25rem;
}
</style>
