<template>
  <div class="modal-backdrop" @click.self="$emit('close')">
    <div class="modal" role="dialog" aria-modal="true">
      <div class="modal-header">
        <h3>Actualizar Inventario</h3>
        <button class="modal-close" @click="$emit('close')" aria-label="Cerrar">×</button>
      </div>

      <div class="modal-body">
        <p class="modal-subtitle">Producto ID: <code>{{ productoId }}</code></p>

        <div class="form-group">
          <label class="form-label" for="cantidad">Nueva cantidad *</label>
          <input
            id="cantidad"
            v-model.number="cantidad"
            class="form-control"
            :class="{ 'is-invalid': error }"
            type="number"
            min="0"
            placeholder="Ej: 50"
          />
          <span v-if="error" class="form-error">{{ error }}</span>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn btn-outline" @click="$emit('close')">Cancelar</button>
        <button class="btn btn-primary" :disabled="loading" @click="handleSave">
          {{ loading ? 'Guardando...' : 'Guardar' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  productoId: { type: String, required: true },
  currentCantidad: { type: Number, default: 0 },
  loading: { type: Boolean, default: false }
})
const emit = defineEmits(['close', 'save'])

const cantidad = ref(props.currentCantidad)
const error = ref('')

function handleSave() {
  error.value = ''
  if (cantidad.value === null || cantidad.value === undefined || cantidad.value < 0) {
    error.value = 'La cantidad debe ser 0 o mayor'
    return
  }
  emit('save', cantidad.value)
}
</script>

<style scoped>
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 300;
  padding: 1rem;
}
.modal {
  background: var(--color-white);
  border-radius: var(--radius);
  box-shadow: var(--shadow-md);
  width: 100%;
  max-width: 440px;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid var(--color-gray-200);
}
.modal-header h3 { font-size: 1.125rem; font-weight: 600; }
.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: var(--color-gray-500);
  line-height: 1;
}
.modal-body { padding: 1.5rem; display: flex; flex-direction: column; gap: 1rem; }
.modal-subtitle { font-size: 0.875rem; color: var(--color-gray-500); }
.modal-subtitle code { font-family: monospace; color: var(--color-primary); }
.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 1.5rem;
  border-top: 1px solid var(--color-gray-200);
}
</style>
