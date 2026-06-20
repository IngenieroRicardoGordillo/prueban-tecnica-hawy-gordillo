import { inventoryApi } from './api.js'

export const inventoryService = {
  async getAll(page = 0, size = 10) {
    const { data } = await inventoryApi.get('/api/v1/inventory', { params: { page, size } })
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
