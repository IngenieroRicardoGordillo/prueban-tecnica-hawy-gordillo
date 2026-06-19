<template>
  <form class="card product-form" @submit.prevent="handleSubmit" novalidate>
    <h2 class="form-title">Nuevo Producto</h2>

    <div class="form-group">
      <label class="form-label" for="nombre">Nombre *</label>
      <input
        id="nombre"
        v-model.trim="form.nombre"
        class="form-control"
        :class="{ 'is-invalid': errors.nombre }"
        type="text"
        placeholder="Ej: Laptop Dell XPS 15"
        maxlength="255"
      />
      <span v-if="errors.nombre" class="form-error">{{ errors.nombre }}</span>
    </div>

    <div class="form-group">
      <label class="form-label" for="precio">Precio (COP) *</label>
      <input
        id="precio"
        v-model="form.precio"
        class="form-control"
        :class="{ 'is-invalid': errors.precio }"
        type="number"
        min="0.01"
        step="0.01"
        placeholder="Ej: 2499.99"
      />
      <span v-if="errors.precio" class="form-error">{{ errors.precio }}</span>
    </div>

    <div class="form-group">
      <label class="form-label" for="descripcion">Descripción</label>
      <textarea
        id="descripcion"
        v-model.trim="form.descripcion"
        class="form-control"
        rows="3"
        maxlength="1000"
        placeholder="Descripción del producto (opcional)"
      ></textarea>
    </div>

    <div class="form-actions">
      <button type="button" class="btn btn-outline" @click="reset">Limpiar</button>
      <button type="submit" class="btn btn-primary" :disabled="loading">
        <span v-if="loading">Guardando...</span>
        <span v-else>Crear Producto</span>
      </button>
    </div>
  </form>
</template>

<script setup>
import { ref, reactive } from 'vue'

const props = defineProps({
  loading: { type: Boolean, default: false }
})
const emit = defineEmits(['submit'])

const form = reactive({ nombre: '', precio: '', descripcion: '' })
const errors = reactive({ nombre: '', precio: '' })

function validate() {
  errors.nombre = ''
  errors.precio = ''
  let valid = true

  if (!form.nombre || form.nombre.length < 2) {
    errors.nombre = 'El nombre debe tener al menos 2 caracteres'
    valid = false
  }
  if (!form.precio || Number(form.precio) <= 0) {
    errors.precio = 'El precio debe ser mayor a 0'
    valid = false
  }
  return valid
}

function reset() {
  form.nombre = ''
  form.precio = ''
  form.descripcion = ''
  errors.nombre = ''
  errors.precio = ''
}

function handleSubmit() {
  if (!validate()) return
  emit('submit', {
    nombre: form.nombre,
    precio: Number(form.precio),
    descripcion: form.descripcion || undefined
  })
}

defineExpose({ reset })
</script>

<style scoped>
.product-form { display: flex; flex-direction: column; gap: 1.25rem; }
.form-title { font-size: 1.125rem; font-weight: 600; }
.form-actions { display: flex; gap: 0.75rem; justify-content: flex-end; margin-top: 0.5rem; }
textarea.form-control { resize: vertical; }
</style>
