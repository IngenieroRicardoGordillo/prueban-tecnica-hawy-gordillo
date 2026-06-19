import { describe, it, expect, beforeEach, vi } from 'vitest'
import axios from 'axios'
import MockAdapter from 'axios-mock-adapter'
import { productsApi } from '@/services/api.js'
import { productService } from '@/services/productService.js'

const mock = new MockAdapter(productsApi)

describe('productService', () => {
  beforeEach(() => mock.reset())

  it('getAll - retorna lista de productos', async () => {
    const products = [
      { id: 'uuid-1', nombre: 'Laptop', precio: 1500, descripcion: 'Desc' }
    ]
    mock.onGet('/api/v1/products').reply(200, { success: true, data: products })

    const result = await productService.getAll()

    expect(result).toEqual(products)
  })

  it('getById - retorna el producto correcto', async () => {
    const product = { id: 'uuid-1', nombre: 'Monitor', precio: 300, descripcion: '' }
    mock.onGet('/api/v1/products/uuid-1').reply(200, { success: true, data: product })

    const result = await productService.getById('uuid-1')

    expect(result.nombre).toBe('Monitor')
  })

  it('create - envía los datos correctos y retorna el producto', async () => {
    const payload = { nombre: 'Teclado', precio: 80, descripcion: 'RGB' }
    const created = { id: 'uuid-2', ...payload }
    mock.onPost('/api/v1/products').reply(201, { success: true, data: created })

    const result = await productService.create(payload)

    expect(result.id).toBe('uuid-2')
    expect(result.nombre).toBe('Teclado')
  })

  it('getAll - lanza error cuando el servidor falla', async () => {
    mock.onGet('/api/v1/products').reply(500, { success: false, message: 'Error interno' })

    await expect(productService.getAll()).rejects.toThrow('Error interno')
  })

  it('getById - lanza error 404 con mensaje del servidor', async () => {
    mock.onGet('/api/v1/products/no-existe').reply(404, {
      success: false,
      message: 'Producto no encontrado'
    })

    await expect(productService.getById('no-existe')).rejects.toThrow('Producto no encontrado')
  })
})
