import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import MainLayout from '@/layouts/MainLayout.vue'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/DashboardView.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'prisoners',
        name: 'Prisoners',
        component: () => import('@/views/PrisonerList.vue'),
        meta: { title: '服刑人员管理', icon: 'UserFilled' }
      },
      {
        path: 'guards',
        name: 'Guards',
        component: () => import('@/views/GuardList.vue'),
        meta: { title: '警员管理', icon: 'Avatar' }
      },
      {
        path: 'prison-areas',
        name: 'PrisonAreas',
        component: () => import('@/views/PrisonAreaList.vue'),
        meta: { title: '监区管理', icon: 'OfficeBuilding' }
      },
      {
        path: 'cells',
        name: 'Cells',
        component: () => import('@/views/CellList.vue'),
        meta: { title: '监舍管理', icon: 'HomeFilled' }
      },
      {
        path: 'patrols',
        name: 'Patrols',
        component: () => import('@/views/PatrolList.vue'),
        meta: { title: '巡查管理', icon: 'AlarmClock' }
      },
      {
        path: 'incidents',
        name: 'Incidents',
        component: () => import('@/views/IncidentList.vue'),
        meta: { title: '事件管理', icon: 'WarningFilled' }
      },
      {
        path: 'visitors',
        name: 'Visitors',
        component: () => import('@/views/VisitorList.vue'),
        meta: { title: '访客管理', icon: 'User' }
      },
      {
        path: 'medical',
        name: 'Medical',
        component: () => import('@/views/MedicalRecordList.vue'),
        meta: { title: '医疗记录', icon: 'FirstAidKit' }
      },
      {
        path: 'users',
        name: 'Users',
        component: () => import('@/views/UserList.vue'),
        meta: { title: '用户管理', icon: 'Management', roles: ['ADMIN'] }
      },
      {
        path: 'roles',
        name: 'Roles',
        component: () => import('@/views/RoleList.vue'),
        meta: { title: '角色管理', icon: 'Setting', roles: ['ADMIN'] }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: () => import('@/views/LogList.vue'),
        meta: { title: '系统日志', icon: 'Document', roles: ['ADMIN'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      next()
    }
  }
})

export default router