<template>
  <div>
    <div class="page-header">
      <h1 class="page-title">Productos</h1>
      <button class="btn btn-primary" @click="showForm = !showForm">
        {{ showForm ? '✕ Cancelar' : '+ Nuevo Producto' }}
      </button>
    </div>

    <Transition name="slide">
      <ProductForm
        v-if="showForm"
        ref="productFormRef"
        :loading="store.loading"
        class="mb-6"
        @submit="handleCreate"
      />
    </Transition>

    <ErrorMessage
      v-if="store.error"
      :message="store.error"
      class="mb-6"
      @dismiss="store.clearError()"
    />

    <div v-if="successMsg" class="success-banner mb-6">✅ {{ successMsg }}</div>

    <LoadingSpinner v-if="store.loading && !store.hasProducts" text="Cargando productos..." />

    <div v-else-if="store.hasProducts">
      <div class="products-grid">
        <ProductCard v-for="product in store.products" :key="product.id" :product="product" />
      </div>
      <PaginationControls
        :page="store.pagination.page"
        :total-pages="store.pagination.totalPages"
        :total-elements="store.pagination.totalElements"
        :last="store.pagination.last"
        @change="store.fetchProducts($event)"
      />
    </div>

    <div v-else-if="!store.loading" class="empty-state card">
      <p>No hay productos registrados.</p>
      <button class="btn btn-primary" @click="showForm = true">Crear el primero</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useProductStore } from '@/stores/productStore.js'
import ProductForm from '@/components/products/ProductForm.vue'
import ProductCard from '@/components/products/ProductCard.vue'
import LoadingSpinner from '@/components/shared/LoadingSpinner.vue'
import ErrorMessage from '@/components/shared/ErrorMessage.vue'
import PaginationControls from '@/components/shared/PaginationControls.vue'

const store = useProductStore()
const showForm = ref(false)
const successMsg = ref('')
const productFormRef = ref(null)

onMounted(() => store.fetchProducts())

async function handleCreate(data) {
  try {
    await store.createProduct(data)
    showForm.value = false
    productFormRef.value?.reset()
    successMsg.value = 'Producto creado exitosamente'
    setTimeout(() => { successMsg.value = '' }, 3000)
  } catch {
    // error is set in store
  }
}
</script>

<style scoped>
.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 1rem;
}
.mb-6 { margin-bottom: 1.5rem; }
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;
  padding: 3rem;
  color: var(--color-gray-500);
}
.success-banner {
  padding: 0.875rem 1rem;
  background: var(--color-success-light);
  border: 1px solid var(--color-success);
  border-radius: var(--radius);
  color: var(--color-success);
  font-weight: 500;
}
.slide-enter-active, .slide-leave-active { transition: all 0.2s ease; }
.slide-enter-from, .slide-leave-to { opacity: 0; transform: translateY(-10px); }
</style>
