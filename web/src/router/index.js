import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/ProductList.vue') },
  { path: '/login', name: 'login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'register', component: () => import('../views/Register.vue') },
  { path: '/products/new', name: 'productCreate', component: () => import('../views/ProductCreate.vue') },
  { path: '/products/:id', name: 'productDetail', component: () => import('../views/ProductDetail.vue') },
  { path: '/realname', name: 'realname', component: () => import('../views/Realname.vue') },
  { path: '/wallet', name: 'wallet', component: () => import('../views/Wallet.vue') },
  { path: '/orders', name: 'orders', component: () => import('../views/Orders.vue') },
  { path: '/me', name: 'me', component: () => import('../views/Home.vue') }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局守卫：未登录只能访问 login / register
router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (!token && to.name !== 'login' && to.name !== 'register') {
    return { name: 'login' }
  }
})

export default router
