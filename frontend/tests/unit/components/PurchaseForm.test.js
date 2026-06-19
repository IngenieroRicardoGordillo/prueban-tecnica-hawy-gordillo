import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import PurchaseForm from '@/components/purchase/PurchaseForm.vue'

const mockProducts = [
  { id: 'p-1', nombre: 'Laptop Dell', precio: 1500, descripcion: 'Desc' },
  { id: 'p-2', nombre: 'Monitor LG', precio: 350, descripcion: 'Monitor 4K' }
]

describe('PurchaseForm', () => {
  it('renderiza el selector de producto y el campo de cantidad', () => {
    const wrapper = mount(PurchaseForm, { props: { products: mockProducts } })

    expect(wrapper.find('#product-select').exists()).toBe(true)
    expect(wrapper.find('#cantidad').exists()).toBe(true)
  })

  it('muestra todos los productos en el selector', () => {
    const wrapper = mount(PurchaseForm, { props: { products: mockProducts } })
    const options = wrapper.findAll('option')

    expect(options.length).toBe(mockProducts.length + 1) // +1 por la opción vacía
  })

  it('muestra error si se envía sin seleccionar producto', async () => {
    const wrapper = mount(PurchaseForm, { props: { products: mockProducts } })

    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('Selecciona un producto')
  })

  it('muestra error si la cantidad es menor a 1', async () => {
    const wrapper = mount(PurchaseForm, { props: { products: mockProducts } })
    await wrapper.find('#product-select').setValue('p-1')
    await wrapper.find('#cantidad').setValue('0')

    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('mínima es 1')
  })

  it('emite submit con productoId y cantidad cuando el formulario es válido', async () => {
    const wrapper = mount(PurchaseForm, { props: { products: mockProducts } })
    await wrapper.find('#product-select').setValue('p-1')
    await wrapper.find('#cantidad').setValue('3')

    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')).toBeTruthy()
    const emitted = wrapper.emitted('submit')[0][0]
    expect(emitted.productoId).toBe('p-1')
    expect(emitted.cantidad).toBe(3)
  })

  it('muestra el precio estimado cuando se selecciona un producto', async () => {
    const wrapper = mount(PurchaseForm, { props: { products: mockProducts } })
    await wrapper.find('#product-select').setValue('p-1')
    await wrapper.find('#cantidad').setValue('2')

    await wrapper.vm.$nextTick()
    expect(wrapper.find('.price-preview').exists()).toBe(true)
  })

  it('deshabilita el botón cuando loading es true', () => {
    const wrapper = mount(PurchaseForm, {
      props: { products: mockProducts, loading: true }
    })

    expect(wrapper.find('button[type="submit"]').element.disabled).toBe(true)
    expect(wrapper.find('button[type="submit"]').text()).toBe('Procesando...')
  })
})
