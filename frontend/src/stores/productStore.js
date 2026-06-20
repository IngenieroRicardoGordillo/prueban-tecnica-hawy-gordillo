import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { productService } from '@/services/productService.js'

export const useProductStore = defineStore('products', () => {
  const products = ref([])
  const currentProduct = ref(null)
  const loading = ref(false)
  const error = ref(null)
  const pagination = ref({ page: 0, size: 10, totalElements: 0, totalPages: 0, last: true })

  const hasProducts = computed(() => products.value.length > 0)

  function clearError() {
    error.value = null
  }

  async function fetchProducts(page = 0) {
    loading.value = true
    error.value = null
    try {
      const pageResponse = await productService.getAll(page, pagination.value.size)
      products.value = pageResponse.content
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
    pagination,
    hasProducts,
    clearError,
    fetchProducts,
    fetchProduct,
    createProduct
  }
})
