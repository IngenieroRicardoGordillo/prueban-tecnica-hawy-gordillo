import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import ProductForm from '@/components/products/ProductForm.vue'

describe('ProductForm', () => {
  it('renderiza los campos de nombre, precio y descripción', () => {
    const wrapper = mount(ProductForm)

    expect(wrapper.find('#nombre').exists()).toBe(true)
    expect(wrapper.find('#precio').exists()).toBe(true)
    expect(wrapper.find('#descripcion').exists()).toBe(true)
  })

  it('muestra error cuando nombre está vacío al enviar', async () => {
    const wrapper = mount(ProductForm)

    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('al menos 2 caracteres')
  })

  it('muestra error cuando precio es 0 o negativo', async () => {
    const wrapper = mount(ProductForm)
    await wrapper.find('#nombre').setValue('Laptop')
    await wrapper.find('#precio').setValue('-10')

    await wrapper.find('form').trigger('submit')

    expect(wrapper.text()).toContain('mayor a 0')
  })

  it('emite evento submit con datos correctos cuando el formulario es válido', async () => {
    const wrapper = mount(ProductForm)
    await wrapper.find('#nombre').setValue('Monitor LG')
    await wrapper.find('#precio').setValue('350')
    await wrapper.find('#descripcion').setValue('Monitor 4K')

    await wrapper.find('form').trigger('submit')

    expect(wrapper.emitted('submit')).toBeTruthy()
    const emittedData = wrapper.emitted('submit')[0][0]
    expect(emittedData.nombre).toBe('Monitor LG')
    expect(emittedData.precio).toBe(350)
    expect(emittedData.descripcion).toBe('Monitor 4K')
  })

  it('limpia el formulario al hacer click en Limpiar', async () => {
    const wrapper = mount(ProductForm)
    await wrapper.find('#nombre').setValue('Producto X')
    await wrapper.find('button[type="button"]').trigger('click')

    expect(wrapper.find('#nombre').element.value).toBe('')
  })

  it('muestra "Guardando..." cuando loading es true', () => {
    const wrapper = mount(ProductForm, { props: { loading: true } })

    expect(wrapper.find('button[type="submit"]').text()).toBe('Guardando...')
    expect(wrapper.find('button[type="submit"]').element.disabled).toBe(true)
  })
})
