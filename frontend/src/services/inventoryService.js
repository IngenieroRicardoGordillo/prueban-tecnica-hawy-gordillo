import { inventoryApi } from './api.js'

export const inventoryService = {
  async getAll() {
    const { data } = await inventoryApi.get('/api/v1/inventory')
    return data.data
  },

  async getByProductId(productoId) {
    const { data } = await inventoryApi.get(`/api/v1/inventory/${productoId}`)
    return data.data
  },

  async update(productoId, payload) {
    const { data } = await inventoryApi.put(`/api/v1/inventory/${productoId}`, payload)
    return data.data
  },

  async purchase(payload) {
    const { data } = await inventoryApi.post('/api/v1/purchases', payload)
    return data.data
  }
}
