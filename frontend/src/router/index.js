import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/Login.vue') },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/Dashboard.vue'), meta: { title: '数据统计' } },
      { path: 'room-type', name: 'RoomType', component: () => import('@/views/RoomType.vue'), meta: { title: '房型管理' } },
      { path: 'room', name: 'Room', component: () => import('@/views/Room.vue'), meta: { title: '客房管理' } },
      { path: 'order', name: 'Order', component: () => import('@/views/Order.vue'), meta: { title: '订单管理' } },
      { path: 'user', name: 'User', component: () => import('@/views/User.vue'), meta: { title: '用户管理' } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
