<template>
  <article class="product-card card">
    <div class="product-card__header">
      <h3 class="product-card__name">{{ product.nombre }}</h3>
      <span class="product-card__price">{{ formatPrice(product.precio) }}</span>
    </div>
    <p v-if="product.descripcion" class="product-card__desc">{{ product.descripcion }}</p>
    <p v-else class="product-card__desc product-card__desc--empty">Sin descripción</p>
    <div class="product-card__footer">
      <span class="product-card__id">ID: {{ shortId(product.id) }}</span>
    </div>
  </article>
</template>

<script setup>
defineProps({
  product: { type: Object, required: true }
})

function formatPrice(price) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(price)
}

function shortId(id) {
  return id ? id.split('-')[0] : '-'
}
</script>

<style scoped>
.product-card {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  transition: box-shadow var(--transition);
}
.product-card:hover { box-shadow: var(--shadow-md); }
.product-card__header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 0.5rem;
}
.product-card__name { font-weight: 600; font-size: 1rem; color: var(--color-gray-900); }
.product-card__price {
  font-weight: 700;
  color: var(--color-primary);
  white-space: nowrap;
}
.product-card__desc { font-size: 0.875rem; color: var(--color-gray-500); }
.product-card__desc--empty { font-style: italic; }
.product-card__footer {
  border-top: 1px solid var(--color-gray-100);
  padding-top: 0.5rem;
}
.product-card__id { font-size: 0.75rem; color: var(--color-gray-300); font-family: monospace; }
</style>
