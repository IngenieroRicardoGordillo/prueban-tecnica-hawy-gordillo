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

  it('estado inicial es vacío y sin errores', () => {
    const store = useProductStore()

    expect(store.products).toEqual([])
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
    expect(store.hasProducts).toBe(false)
  })

  it('fetchProducts - carga la lista y activa loading durante la llamada', async () => {
    const mockProducts = [
      { id: 'p-1', nombre: 'Laptop', precio: 2000 },
      { id: 'p-2', nombre: 'Monitor', precio: 500 }
    ]
    productService.getAll.mockResolvedValue(mockProducts)

    const store = useProductStore()
    const promise = store.fetchProducts()

    expect(store.loading).toBe(true)
    await promise

    expect(store.products).toEqual(mockProducts)
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
