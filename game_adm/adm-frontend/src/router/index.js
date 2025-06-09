import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import GMTools from '../views/GMTools.vue'
import System from '../views/System.vue'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: Dashboard
  },
  {
    path: '/gm',
    name: 'GMTools',
    component: GMTools
  },
  {
    path: '/system',
    name: 'System',
    component: System
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router