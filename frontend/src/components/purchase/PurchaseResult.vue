<template>
  <div class="purchase-result card">
    <div class="result-icon">✅</div>
    <h3>¡Compra Exitosa!</h3>

    <div class="result-grid">
      <div class="result-item">
        <span class="result-label">Producto</span>
        <span class="result-value">{{ result.productoNombre }}</span>
      </div>
      <div class="result-item">
        <span class="result-label">Cantidad</span>
        <span class="result-value">{{ result.cantidad }} unidades</span>
      </div>
      <div class="result-item">
        <span class="result-label">Precio unitario</span>
        <span class="result-value">{{ formatPrice(result.precioUnitario) }}</span>
      </div>
      <div class="result-item result-item--total">
        <span class="result-label">Total</span>
        <span class="result-value">{{ formatPrice(result.total) }}</span>
      </div>
      <div class="result-item">
        <span class="result-label">Estado</span>
        <span class="badge badge-success">{{ result.status }}</span>
      </div>
      <div class="result-item">
        <span class="result-label">Orden #</span>
        <span class="result-value" style="font-family: monospace; font-size: 0.75rem;">{{ result.purchaseId }}</span>
      </div>
    </div>

    <button class="btn btn-primary" @click="$emit('new-purchase')">Nueva Compra</button>
  </div>
</template>

<script setup>
defineProps({
  result: { type: Object, required: true }
})
defineEmits(['new-purchase'])

function formatPrice(price) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(price)
}
</script>

<style scoped>
.purchase-result {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.25rem;
  text-align: center;
}
.result-icon { font-size: 3rem; }
.purchase-result h3 { font-size: 1.25rem; font-weight: 700; color: var(--color-success); }
.result-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
  width: 100%;
  text-align: left;
}
.result-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  padding: 0.75rem;
  background: var(--color-gray-50);
  border-radius: var(--radius);
}
.result-item--total { grid-column: 1 / -1; }
.result-label { font-size: 0.75rem; color: var(--color-gray-500); text-transform: uppercase; letter-spacing: 0.05em; }
.result-value { font-weight: 600; }
.result-item--total .result-value { color: var(--color-success); font-size: 1.25rem; }
</style>
