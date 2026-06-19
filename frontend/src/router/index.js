import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/products'
  },
  {
    path: '/products',
    name: 'products',
    component: () => import('@/views/ProductsView.vue'),
    meta: { title: 'Productos' }
  },
  {
    path: '/inventory',
    name: 'inventory',
    component: () => import('@/views/InventoryView.vue'),
    meta: { title: 'Inventario' }
  },
  {
    path: '/purchase',
    name: 'purchase',
    component: () => import('@/views/PurchaseView.vue'),
    meta: { title: 'Comprar' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.afterEach(to => {
  document.title = `Linktic - ${to.meta.title || 'App'}`
})

export default router
