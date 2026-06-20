import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import UpdateInventoryModal from '@/components/inventory/UpdateInventoryModal.vue'

describe('UpdateInventoryModal', () => {
  it('renderiza el ID del producto y el campo de cantidad', () => {
    const wrapper = mount(UpdateInventoryModal, {
      props: { productoId: 'abc-123', currentCantidad: 10 }
    })

    expect(wrapper.text()).toContain('abc-123')
    expect(wrapper.find('#cantidad').exists()).toBe(true)
  })

  it('pre-rellena la cantidad con el valor actual del inventario', () => {
    const wrapper = mount(UpdateInventoryModal, {
      props: { productoId: 'p-1', currentCantidad: 42 }
    })

    expect(wrapper.find('#cantidad').element.value).toBe('42')
  })

  it('muestra error si se intenta guardar con cantidad negativa', async () => {
    const wrapper = mount(UpdateInventoryModal, {
      props: { productoId: 'p-1', currentCantidad: 10 }
    })

    await wrapper.find('#cantidad').setValue('-5')
    await wrapper.find('.btn-primary').trigger('click')

    expect(wrapper.text()).toContain('0 o mayor')
    expect(wrapper.emitted('save')).toBeFalsy()
  })

  it('emite save con la cantidad correcta cuando el valor es válido', async () => {
    const wrapper = mount(UpdateInventoryModal, {
      props: { productoId: 'p-1', currentCantidad: 10 }
    })

    await wrapper.find('#cantidad').setValue('75')
    await wrapper.find('.btn-primary').trigger('click')

    expect(wrapper.emitted('save')).toBeTruthy()
    expect(wrapper.emitted('save')[0][0]).toBe(75)
  })

  it('emite close al hacer click en Cancelar', async () => {
    const wrapper = mount(UpdateInventoryModal, {
      props: { productoId: 'p-1', currentCantidad: 0 }
    })

    await wrapper.find('.btn-outline').trigger('click')

    expect(wrapper.emitted('close')).toBeTruthy()
  })

  it('deshabilita el botón Guardar y muestra "Guardando..." cuando loading es true', () => {
    const wrapper = mount(UpdateInventoryModal, {
      props: { productoId: 'p-1', currentCantidad: 0, loading: true }
    })

    const btn = wrapper.find('.btn-primary')
    expect(btn.element.disabled).toBe(true)
    expect(btn.text()).toBe('Guardando...')
  })
})
