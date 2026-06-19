import { describe, it, expect, beforeEach } from 'vitest'
import MockAdapter from 'axios-mock-adapter'
import { inventoryApi } from '@/services/api.js'
import { inventoryService } from '@/services/inventoryService.js'

const mock = new MockAdapter(inventoryApi)

describe('inventoryService', () => {
  beforeEach(() => mock.reset())

  it('getAll - retorna lista de inventario', async () => {
    const inventory = [{ id: 'inv-1', productoId: 'p-1', cantidad: 50 }]
    mock.onGet('/api/v1/inventory').reply(200, { success: true, data: inventory })

    const result = await inventoryService.getAll()

    expect(result).toHaveLength(1)
    expect(result[0].cantidad).toBe(50)
  })

  it('update - envía PUT con los datos correctos', async () => {
    const updated = { id: 'inv-1', productoId: 'p-1', cantidad: 75 }
    mock.onPut('/api/v1/inventory/p-1').reply(200, { success: true, data: updated })

    const result = await inventoryService.update('p-1', { cantidad: 75 })

    expect(result.cantidad).toBe(75)
  })

  it('purchase - retorna resultado de compra exitosa', async () => {
    const purchaseResult = {
      purchaseId: 'buy-1',
      productoId: 'p-1',
      productoNombre: 'Laptop',
      cantidad: 2,
      precioUnitario: 1500,
      total: 3000,
      status: 'COMPLETED'
    }
    mock.onPost('/api/v1/purchases').reply(200, { success: true, data: purchaseResult })

    const result = await inventoryService.purchase({ productoId: 'p-1', cantidad: 2 })

    expect(result.status).toBe('COMPLETED')
    expect(result.total).toBe(3000)
  })

  it('purchase - lanza error cuando stock insuficiente (409)', async () => {
    mock.onPost('/api/v1/purchases').reply(409, {
      success: false,
      message: 'Stock insuficiente. Solicitado: 100, Disponible: 5'
    })

    await expect(inventoryService.purchase({ productoId: 'p-1', cantidad: 100 }))
      .rejects.toThrow('Stock insuficiente')
  })

  it('purchase - lanza error cuando servicio no disponible (503)', async () => {
    mock.onPost('/api/v1/purchases').reply(503, {
      success: false,
      message: 'Servicio de productos no disponible'
    })

    await expect(inventoryService.purchase({ productoId: 'p-1', cantidad: 1 }))
      .rejects.toThrow('Servicio de productos no disponible')
  })
})
