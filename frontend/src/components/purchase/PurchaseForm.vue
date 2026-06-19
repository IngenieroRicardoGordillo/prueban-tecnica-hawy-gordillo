<template>
  <form class="card purchase-form" @submit.prevent="handleSubmit" novalidate>
    <h2 class="form-title">Realizar Compra</h2>

    <div class="form-group">
      <label class="form-label" for="product-select">Producto *</label>
      <select
        id="product-select"
        v-model="form.productoId"
        class="form-control"
        :class="{ 'is-invalid': errors.productoId }"
      >
        <option value="">Selecciona un producto...</option>
        <option v-for="p in products" :key="p.id" :value="p.id">
          {{ p.nombre }} — {{ formatPrice(p.precio) }}
        </option>
      </select>
      <span v-if="errors.productoId" class="form-error">{{ errors.productoId }}</span>
    </div>

    <div class="form-group">
      <label class="form-label" for="cantidad">Cantidad *</label>
      <input
        id="cantidad"
        v-model.number="form.cantidad"
        class="form-control"
        :class="{ 'is-invalid': errors.cantidad }"
        type="number"
        min="1"
        placeholder="Ej: 2"
      />
      <span v-if="errors.cantidad" class="form-error">{{ errors.cantidad }}</span>
    </div>

    <div v-if="selectedProduct" class="price-preview">
      <span>Precio unitario:</span>
      <strong>{{ formatPrice(selectedProduct.precio) }}</strong>
      <span>Total estimado:</span>
      <strong>{{ formatPrice(selectedProduct.precio * (form.cantidad || 0)) }}</strong>
    </div>

    <button type="submit" class="btn btn-success btn-lg w-full" :disabled="loading">
      {{ loading ? 'Procesando...' : '✓ Confirmar Compra' }}
    </button>
  </form>
</template>

<script setup>
import { reactive, computed } from 'vue'

const props = defineProps({
  products: { type: Array, default: () => [] },
  loading: { type: Boolean, default: false }
})
const emit = defineEmits(['submit'])

const form = reactive({ productoId: '', cantidad: 1 })
const errors = reactive({ productoId: '', cantidad: '' })

const selectedProduct = computed(() =>
  props.products.find(p => p.id === form.productoId) || null
)

function formatPrice(price) {
  return new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP' }).format(price)
}

function validate() {
  errors.productoId = ''
  errors.cantidad = ''
  let valid = true
  if (!form.productoId) {
    errors.productoId = 'Selecciona un producto'
    valid = false
  }
  if (!form.cantidad || form.cantidad < 1) {
    errors.cantidad = 'La cantidad mínima es 1'
    valid = false
  }
  return valid
}

function handleSubmit() {
  if (!validate()) return
  emit('submit', { productoId: form.productoId, cantidad: form.cantidad })
}
</script>

<style scoped>
.purchase-form { display: flex; flex-direction: column; gap: 1.25rem; }
.form-title { font-size: 1.125rem; font-weight: 600; }
.price-preview {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 0.25rem 1rem;
  padding: 0.875rem 1rem;
  background: var(--color-gray-50);
  border-radius: var(--radius);
  font-size: 0.875rem;
  color: var(--color-gray-700);
  align-items: center;
}
.price-preview strong { color: var(--color-primary); font-size: 1rem; }
.w-full { width: 100%; justify-content: center; }
</style>
