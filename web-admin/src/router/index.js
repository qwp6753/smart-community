import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'system/users',
        name: 'SystemUsers',
        component: () => import('@/views/system/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system/roles',
        name: 'SystemRoles',
        component: () => import('@/views/system/role/index.vue'),
        meta: { title: '角色管理', icon: 'UserFilled' }
      },
      {
        path: 'system/menus',
        name: 'SystemMenus',
        component: () => import('@/views/system/menu/index.vue'),
        meta: { title: '菜单管理', icon: 'Menu' }
      },
      {
        path: 'system/dict',
        name: 'SystemDict',
        component: () => import('@/views/system/dict/index.vue'),
        meta: { title: '数据字典', icon: 'Document' }
      },
      {
        path: 'property/communities',
        name: 'PropertyCommunities',
        component: () => import('@/views/property/community/index.vue'),
        meta: { title: '小区管理', icon: 'House' }
      },
      {
        path: 'property/cameras',
        name: 'PropertyCameras',
        component: () => import('@/views/property/camera/index.vue'),
        meta: { title: '摄像头管理', icon: 'VideoCamera' }
      },
      {
        path: 'property/persons',
        name: 'PropertyPersons',
        component: () => import('@/views/property/person/index.vue'),
        meta: { title: '居民管理', icon: 'Avatar' }
      },
      {
        path: 'access/records',
        name: 'AccessRecords',
        component: () => import('@/views/access/record/index.vue'),
        meta: { title: '出入记录', icon: 'Tickets' }
      },
      {
        path: 'access/visitors',
        name: 'AccessVisitors',
        component: () => import('@/views/access/visitor/index.vue'),
        meta: { title: '访客登记', icon: 'Notebook' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
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
