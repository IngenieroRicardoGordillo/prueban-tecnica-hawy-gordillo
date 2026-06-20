<template>
  <div v-if="totalPages > 1" class="pagination">
    <button
      class="btn btn-outline btn-sm"
      :disabled="page === 0"
      @click="$emit('change', page - 1)"
    >
      ← Anterior
    </button>

    <span class="pagination-info">
      Página {{ page + 1 }} de {{ totalPages }}
      <span class="total">({{ totalElements }} registros)</span>
    </span>

    <button
      class="btn btn-outline btn-sm"
      :disabled="last"
      @click="$emit('change', page + 1)"
    >
      Siguiente →
    </button>
  </div>
</template>

<script setup>
defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
  totalElements: { type: Number, required: true },
  last: { type: Boolean, required: true }
})

defineEmits(['change'])
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  margin-top: 1.5rem;
  padding: 1rem 0;
}
.pagination-info {
  font-size: 0.875rem;
  color: var(--color-gray-600);
}
.total {
  color: var(--color-gray-400);
  margin-left: 0.25rem;
}
</style>
