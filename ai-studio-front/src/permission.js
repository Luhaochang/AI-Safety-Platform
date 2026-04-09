import router, { constantRoutes } from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import useUserStore from '@/store/modules/user'
import useSettingsStore from '@/store/modules/settings'
import usePermissionStore from '@/store/modules/permission'
import defAva from '@/assets/icons/svg/checkbox.png'

NProgress.configure({ showSpinner: false });

// ===== 免登录模式：Mock用户信息，直接加载侧边栏 =====
let mockInited = false

router.beforeEach((to, from, next) => {
  NProgress.start()
  to.meta.title && useSettingsStore().setTitle(to.meta.title)

  // 首次访问时初始化Mock用户和侧边栏
  if (!mockInited) {
    mockInited = true
    const userStore = useUserStore()
    userStore.roles = ['admin']
    userStore.permissions = ['*:*:*']
    userStore.name = 'admin'
    userStore.avatar = defAva

    const permissionStore = usePermissionStore()
    permissionStore.setSidebarRouters(constantRoutes)
    permissionStore.setRoutes([])
    permissionStore.setDefaultRoutes(constantRoutes)
    permissionStore.setTopbarRoutes(constantRoutes)
  }

  // 拦截登录页，直接跳首页
  if (to.path === '/login') {
    next({ path: '/' })
    NProgress.done()
    return
  }
  next()
})

router.afterEach(() => {
  NProgress.done()
})
