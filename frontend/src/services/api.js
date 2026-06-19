import axios from 'axios'

const PRODUCTS_API_KEY = import.meta.env.VITE_PRODUCTS_API_KEY || 'products-api-key-change-in-production'
const INVENTORY_API_KEY = import.meta.env.VITE_INVENTORY_API_KEY || 'inventory-api-key-change-in-production'

export const productsApi = axios.create({
  baseURL: import.meta.env.VITE_PRODUCTS_API_URL || 'http://localhost:8081',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': PRODUCTS_API_KEY
  }
})

export const inventoryApi = axios.create({
  baseURL: import.meta.env.VITE_INVENTORY_API_URL || 'http://localhost:8082',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
    'X-API-Key': INVENTORY_API_KEY
  }
})

function attachInterceptors(instance) {
  instance.interceptors.response.use(
    response => response,
    error => {
      if (error.response) {
        const message = error.response.data?.message || 'Error en la respuesta del servidor'
        return Promise.reject(new Error(message))
      }
      if (error.request) {
        return Promise.reject(new Error('Servicio no disponible. Verifica tu conexión.'))
      }
      return Promise.reject(error)
    }
  )
}

attachInterceptors(productsApi)
attachInterceptors(inventoryApi)
