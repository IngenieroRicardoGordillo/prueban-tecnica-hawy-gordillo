import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useProductStore } from '@/stores/productStore.js'
import { productService } from '@/services/productService.js'

vi.mock('@/services/productService.js', () => ({
  productService: {
    getAll: vi.fn(),
    getById: vi.fn(),
    create: vi.fn()
  }
}))

describe('productStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('estado inicial es vacío, sin errores y con paginación en página 0', () => {
    const store = useProductStore()

    expect(store.products).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.hasProducts).toBe(false)
    expect(store.pagination.page).toBe(0)
    expect(store.pagination.totalElements).toBe(0)
  })

  it('fetchProducts - carga la página y actualiza metadatos de paginación', async () => {
    const mockProducts = [
      { id: 'p-1', nombre: 'Laptop', precio: 2000 },
      { id: 'p-2', nombre: 'Monitor', precio: 500 }
    ]
    const pageResponse = {
      content: mockProducts, page: 0, size: 10,
      totalElements: 2, totalPages: 1, last: true
    }
    productService.getAll.mockResolvedValue(pageResponse)

    const store = useProductStore()
    const promise = store.fetchProducts()

    expect(store.loading).toBe(true)
    await promise

    expect(store.products).toEqual(mockProducts)
    expect(store.pagination.totalElements).toBe(2)
    expect(store.pagination.last).toBe(true)
    expect(store.loading).toBe(false)
    expect(store.hasProducts).toBe(true)
  })

  it('fetchProducts - guarda el error y apaga loading cuando el servicio falla', async () => {
    productService.getAll.mockRejectedValue(new Error('Sin conexión'))

    const store = useProductStore()
    await store.fetchProducts()

    expect(store.error).toBe('Sin conexión')
    expect(store.loading).toBe(false)
    expect(store.products).toEqual([])
  })

  it('createProduct - agrega el producto al inicio de la lista', async () => {
    const existente = { id: 'p-1', nombre: 'Monitor', precio: 500 }
    const nuevo = { id: 'p-2', nombre: 'Laptop', precio: 2000 }
    productService.create.mockResolvedValue(nuevo)

    const store = useProductStore()
    store.products = [existente]
    await store.createProduct({ nombre: 'Laptop', precio: 2000 })

    expect(store.products[0]).toEqual(nuevo)
    expect(store.products).toHaveLength(2)
  })

  it('createProduct - propaga el error para que la vista lo maneje', async () => {
    productService.create.mockRejectedValue(new Error('Nombre duplicado'))

    const store = useProductStore()
    await expect(store.createProduct({ nombre: 'X', precio: 10 })).rejects.toThrow('Nombre duplicado')
    expect(store.error).toBe('Nombre duplicado')
  })

  it('clearError - limpia el error sin afectar el resto del estado', async () => {
    productService.getAll.mockRejectedValue(new Error('Fallo'))
    const store = useProductStore()
    await store.fetchProducts()

    store.clearError()

    expect(store.error).toBeNull()
    expect(store.products).toEqual([])
  })
})
