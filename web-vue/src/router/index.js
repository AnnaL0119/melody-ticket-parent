import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import Home from '../views/Home.vue'
import ConcertDetail from '../views/ConcertDetail.vue'
import UserOrders from '../views/UserOrders.vue'
import AdminConcerts from '../views/AdminConcerts.vue'
import MusicPlayer from '../views/MusicPlayer.vue'

const routes = [
  { path: '/login', name: 'Login', component: Login },
  { path: '/register', name: 'Register', component: Register },
  { path: '/', name: 'Home', component: Home, meta: { requiresAuth: true } },
  { path: '/concert/:id', name: 'ConcertDetail', component: ConcertDetail, meta: { requiresAuth: true } },
  { path: '/orders', name: 'UserOrders', component: UserOrders, meta: { requiresAuth: true } },
  { path: '/music', name: 'MusicPlayer', component: MusicPlayer, meta: { requiresAuth: true } },
  { path: '/admin/concerts', name: 'AdminConcerts', component: AdminConcerts, meta: { requiresAuth: true, role: 'ADMIN' } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    next('/login')
    return
  }

  if (to.meta.role && to.meta.role !== role) {
    ElMessage.error('无权限访问')
    next('/')
    return
  }

  next()
})

export default router
