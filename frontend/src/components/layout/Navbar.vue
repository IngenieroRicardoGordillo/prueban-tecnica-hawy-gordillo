<template>
  <nav class="navbar">
    <div class="navbar-brand">
      <RouterLink to="/products" class="brand-link">
        <span class="brand-icon">🏪</span>
        <span class="brand-name">Linktic Shop</span>
      </RouterLink>
    </div>

    <button class="nav-toggle" @click="menuOpen = !menuOpen" aria-label="Toggle menu">
      <span></span><span></span><span></span>
    </button>

    <ul class="nav-links" :class="{ open: menuOpen }">
      <li v-for="link in navLinks" :key="link.to">
        <RouterLink
          :to="link.to"
          class="nav-link"
          active-class="nav-link--active"
          @click="menuOpen = false"
        >
          {{ link.label }}
        </RouterLink>
      </li>
    </ul>
  </nav>
</template>

<script setup>
import { ref } from 'vue'

const menuOpen = ref(false)

const navLinks = [
  { to: '/products', label: 'Productos' },
  { to: '/inventory', label: 'Inventario' },
  { to: '/purchase', label: 'Comprar' }
]
</script>

<style scoped>
.navbar {
  background: var(--color-white);
  box-shadow: var(--shadow);
  padding: 0 1.5rem;
  height: var(--navbar-height);
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 100;
}
.navbar-brand { display: flex; align-items: center; }
.brand-link {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  font-weight: 700;
  font-size: 1.125rem;
  color: var(--color-primary);
}
.brand-icon { font-size: 1.5rem; }
.nav-links {
  display: flex;
  list-style: none;
  gap: 0.25rem;
}
.nav-link {
  display: block;
  padding: 0.5rem 0.875rem;
  border-radius: var(--radius);
  text-decoration: none;
  color: var(--color-gray-700);
  font-weight: 500;
  font-size: 0.875rem;
  transition: background var(--transition), color var(--transition);
}
.nav-link:hover { background: var(--color-gray-100); color: var(--color-primary); }
.nav-link--active { background: var(--color-primary-light); color: var(--color-primary); }

.nav-toggle {
  display: none;
  flex-direction: column;
  gap: 5px;
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
}
.nav-toggle span {
  display: block;
  width: 22px;
  height: 2px;
  background: var(--color-gray-700);
  border-radius: 2px;
}

@media (max-width: 640px) {
  .nav-toggle { display: flex; }
  .nav-links {
    display: none;
    position: absolute;
    top: var(--navbar-height);
    left: 0;
    right: 0;
    background: var(--color-white);
    flex-direction: column;
    padding: 1rem;
    box-shadow: var(--shadow-md);
    gap: 0.25rem;
  }
  .nav-links.open { display: flex; }
}
</style>
