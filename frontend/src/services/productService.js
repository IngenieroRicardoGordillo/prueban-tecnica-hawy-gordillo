import { productsApi } from './api.js'

export const productService = {
  async getAll(page = 0, size = 10) {
    const { data } = await productsApi.get('/api/v1/products', { params: { page, size } })
    return data.data
  },

  async getById(id) {
    const { data } = await productsApi.get(`/api/v1/products/${id}`)
    return data.data
  },

  async create(product) {
    const { data } = await productsApi.post('/api/v1/products', product)
    return data.data
  }
}
