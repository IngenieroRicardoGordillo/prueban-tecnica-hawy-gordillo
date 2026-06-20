import { defineStore } from 'pinia'
import { ref } from 'vue'
import { inventoryService } from '@/services/inventoryService.js'

export const useInventoryStore = defineStore('inventory', () => {
  const inventoryList = ref([])
  const purchaseResult = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const pagination = ref({ page: 0, size: 10, totalElements: 0, totalPages: 0, last: true })

  function clearError() {
    error.value = null
  }

  function clearPurchaseResult() {
    purchaseResult.value = null
  }

  async function fetchInventory(page = 0) {
    loading.value = true
    error.value = null
    try {
      const pageResponse = await inventoryService.getAll(page, pagination.value.size)
      inventoryList.value = pageResponse.content
      pagination.value = {
        page: pageResponse.page,
        size: pageResponse.size,
        totalElements: pageResponse.totalElements,
        totalPages: pageResponse.totalPages,
        last: pageResponse.last
      }
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function updateInventory(productoId, cantidad) {
    loading.value = true
    error.value = null
    try {
      const updated = await inventoryService.update(productoId, { cantidad })
      const index = inventoryList.value.findIndex(i => i.productoId === productoId)
      if (index !== -1) {
        inventoryList.value[index] = updated
      } else {
        inventoryList.value.push(updated)
      }
      return updated
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  async function purchase(productoId, cantidad) {
    loading.value = true
    error.value = null
    purchaseResult.value = null
    try {
      purchaseResult.value = await inventoryService.purchase({ productoId, cantidad })
      return purchaseResult.value
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    inventoryList,
    purchaseResult,
    loading,
    error,
    pagination,
    clearError,
    clearPurchaseResult,
    fetchInventory,
    updateInventory,
    purchase
  }
})
