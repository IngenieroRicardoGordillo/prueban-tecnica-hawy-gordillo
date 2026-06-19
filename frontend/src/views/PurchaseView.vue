<template>
  <div class="purchase-page">
    <div class="page-header">
      <h1 class="page-title">Comprar Productos</h1>
    </div>

    <LoadingSpinner v-if="productStore.loading" text="Cargando productos..." />

    <ErrorMessage
      v-else-if="productStore.error"
      :message="productStore.error"
      @dismiss="productStore.clearError()"
    />

    <div v-else class="purchase-layout">
      <PurchaseForm
        v-if="!inventoryStore.purchaseResult"
        :products="productStore.products"
        :loading="inventoryStore.loading"
        @submit="handlePurchase"
      />

      <PurchaseResult
        v-if="inventoryStore.purchaseResult"
        :result="inventoryStore.purchaseResult"
        @new-purchase="inventoryStore.clearPurchaseResult()"
      />

      <ErrorMessage
        v-if="inventoryStore.error"
        :message="inventoryStore.error"
        class="mt-4"
        @dismiss="inventoryStore.clearError()"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useProductStore } from '@/stores/productStore.js'
import { useInventoryStore } from '@/stores/inventoryStore.js'
import PurchaseForm from '@/components/purchase/PurchaseForm.vue'
import PurchaseResult from '@/components/purchase/PurchaseResult.vue'
import LoadingSpinner from '@/components/shared/LoadingSpinner.vue'
import ErrorMessage from '@/components/shared/ErrorMessage.vue'

const productStore = useProductStore()
const inventoryStore = useInventoryStore()

onMounted(() => {
  if (!productStore.hasProducts) {
    productStore.fetchProducts()
  }
  inventoryStore.clearPurchaseResult()
})

async function handlePurchase({ productoId, cantidad }) {
  try {
    await inventoryStore.purchase(productoId, cantidad)
  } catch {
    // error shown via inventoryStore.error
  }
}
</script>

<style scoped>
.purchase-page { max-width: 600px; margin: 0 auto; }
.purchase-layout { display: flex; flex-direction: column; gap: 1rem; }
.mt-4 { margin-top: 1rem; }
</style>
