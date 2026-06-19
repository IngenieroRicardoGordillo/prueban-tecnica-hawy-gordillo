<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Inventario</h1>
      <button class="btn btn-outline" @click="store.fetchInventory()">↺ Actualizar</button>
    </div>

    <ErrorMessage
      v-if="store.error"
      :message="store.error"
      class="mb-4"
      @dismiss="store.clearError()"
    />

    <div v-if="successMsg" class="success-banner mb-4">✅ {{ successMsg }}</div>

    <LoadingSpinner v-if="store.loading" text="Cargando inventario..." />

    <div v-else class="card">
      <div v-if="store.inventoryList.length === 0" class="empty-state">
        <p>No hay registros de inventario.</p>
        <p class="hint">Crea productos y luego actualiza su inventario aquí.</p>
      </div>

      <div v-else class="table-wrapper">
        <table>
          <thead>
            <tr>
              <th>Producto ID</th>
              <th>Cantidad</th>
              <th>Estado</th>
              <th>Última actualización</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in store.inventoryList" :key="item.id">
              <td><code class="mono">{{ item.productoId }}</code></td>
              <td>
                <span :class="stockBadgeClass(item.cantidad)">{{ item.cantidad }}</span>
              </td>
              <td>
                <span :class="['badge', item.cantidad > 0 ? 'badge-success' : 'badge-danger']">
                  {{ item.cantidad > 0 ? 'En stock' : 'Sin stock' }}
                </span>
              </td>
              <td>{{ formatDate(item.updatedAt) }}</td>
              <td>
                <button
                  class="btn btn-outline btn-sm"
                  @click="openModal(item)"
                >
                  Editar
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <UpdateInventoryModal
      v-if="selectedItem"
      :producto-id="selectedItem.productoId"
      :current-cantidad="selectedItem.cantidad"
      :loading="store.loading"
      @close="selectedItem = null"
      @save="handleUpdate"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useInventoryStore } from '@/stores/inventoryStore.js'
import LoadingSpinner from '@/components/shared/LoadingSpinner.vue'
import ErrorMessage from '@/components/shared/ErrorMessage.vue'
import UpdateInventoryModal from '@/components/inventory/UpdateInventoryModal.vue'

const store = useInventoryStore()
const selectedItem = ref(null)
const successMsg = ref('')

onMounted(() => store.fetchInventory())

function openModal(item) {
  selectedItem.value = item
}

async function handleUpdate(cantidad) {
  try {
    await store.updateInventory(selectedItem.value.productoId, cantidad)
    selectedItem.value = null
    successMsg.value = 'Inventario actualizado exitosamente'
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch {
    selectedItem.value = null
  }
}

function stockBadgeClass(cantidad) {
  if (cantidad <= 0) return 'stock-zero'
  if (cantidad < 10) return 'stock-low'
  return 'stock-ok'
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleString('es-CO')
}
</script>

<style scoped>
.mb-4 { margin-bottom: 1rem; }
.mono { font-family: monospace; font-size: 0.75rem; color: var(--color-primary); }
.empty-state {
  text-align: center;
  padding: 2rem;
  color: var(--color-gray-500);
}
.hint { font-size: 0.875rem; margin-top: 0.25rem; }
.stock-zero { color: var(--color-danger); font-weight: 700; }
.stock-low { color: var(--color-warning); font-weight: 700; }
.stock-ok { color: var(--color-success); font-weight: 700; }
.success-banner {
  padding: 0.875rem 1rem;
  background: var(--color-success-light);
  border: 1px solid var(--color-success);
  border-radius: var(--radius);
  color: var(--color-success);
  font-weight: 500;
}
</style>
