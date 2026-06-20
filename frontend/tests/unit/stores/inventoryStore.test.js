import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useInventoryStore } from '@/stores/inventoryStore.js'
import { inventoryService } from '@/services/inventoryService.js'

vi.mock('@/services/inventoryService.js', () => ({
  inventoryService: {
    getAll: vi.fn(),
    update: vi.fn(),
    purchase: vi.fn()
  }
}))

describe('inventoryStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('estado inicial es vacío y sin errores', () => {
    const store = useInventoryStore()

    expect(store.inventoryList).toEqual([])
    expect(store.purchaseResult).toBeNull()
    expect(store.loading).toBe(false)
    expect(store.error).toBeNull()
  })

  it('fetchInventory - carga la lista correctamente', async () => {
    const mockList = [
      { productoId: 'p-1', cantidad: 10 },
      { productoId: 'p-2', cantidad: 25 }
    ]
    inventoryService.getAll.mockResolvedValue(mockList)

    const store = useInventoryStore()
    await store.fetchInventory()

    expect(store.inventoryList).toEqual(mockList)
    expect(store.loading).toBe(false)
  })

  it('updateInventory - actualiza el registro existente en la lista', async () => {
    const updated = { productoId: 'p-1', cantidad: 99 }
    inventoryService.update.mockResolvedValue(updated)

    const store = useInventoryStore()
    store.inventoryList = [{ productoId: 'p-1', cantidad: 10 }]
    await store.updateInventory('p-1', 99)

    expect(store.inventoryList[0].cantidad).toBe(99)
  })

  it('updateInventory - agrega el registro si no existía en la lista', async () => {
    const nuevo = { productoId: 'p-nuevo', cantidad: 50 }
    inventoryService.update.mockResolvedValue(nuevo)

    const store = useInventoryStore()
    store.inventoryList = []
    await store.updateInventory('p-nuevo', 50)

    expect(store.inventoryList).toHaveLength(1)
    expect(store.inventoryList[0]).toEqual(nuevo)
  })

  it('purchase - guarda el resultado y limpia loading', async () => {
    const resultado = { status: 'COMPLETED', total: 3000, cantidad: 2 }
    inventoryService.purchase.mockResolvedValue(resultado)

    const store = useInventoryStore()
    await store.purchase('p-1', 2)

    expect(store.purchaseResult).toEqual(resultado)
    expect(store.loading).toBe(false)
  })

  it('purchase - guarda el error y propaga la excepción cuando falla', async () => {
    inventoryService.purchase.mockRejectedValue(new Error('Stock insuficiente'))

    const store = useInventoryStore()
    await expect(store.purchase('p-1', 999)).rejects.toThrow('Stock insuficiente')
    expect(store.error).toBe('Stock insuficiente')
    expect(store.purchaseResult).toBeNull()
  })

  it('clearPurchaseResult - limpia solo el resultado de compra', async () => {
    const resultado = { status: 'COMPLETED', total: 1500 }
    inventoryService.purchase.mockResolvedValue(resultado)

    const store = useInventoryStore()
    await store.purchase('p-1', 1)
    store.clearPurchaseResult()

    expect(store.purchaseResult).toBeNull()
  })
})
