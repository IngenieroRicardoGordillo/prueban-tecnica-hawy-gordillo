import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { productService } from '@/services/productService.js'

export const useProductStore = defineStore('products', () => {
  const products = ref([])
  const currentProduct = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const hasProducts = computed(() => products.value.length > 0)

  function clearError() {
    error.value = null
  }

  async function fetchProducts() {
    loading.value = true
    error.value = null
    try {
      products.value = await productService.getAll()
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function fetchProduct(id) {
    loading.value = true
    error.value = null
    try {
      currentProduct.value = await productService.getById(id)
    } catch (err) {
      error.value = err.message
    } finally {
      loading.value = false
    }
  }

  async function createProduct(productData) {
    loading.value = true
    error.value = null
    try {
      const product = await productService.create(productData)
      products.value.unshift(product)
      return product
    } catch (err) {
      error.value = err.message
      throw err
    } finally {
      loading.value = false
    }
  }

  return {
    products,
    currentProduct,
    loading,
    error,
    hasProducts,
    clearError,
    fetchProducts,
    fetchProduct,
    createProduct
  }
})
