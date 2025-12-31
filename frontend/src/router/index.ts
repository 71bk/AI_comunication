import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

// 路由配置
const routes: RouteRecordRaw[] = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/LoginPage.vue'),
        meta: { requiresGuest: true },
    },
    {
        path: '/',
        name: 'Chat',
        component: () => import('@/views/ChatPage.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/',
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

// 路由守衛
router.beforeEach(async (to, _from, next) => {
    const authStore = useAuthStore()

    // 等待初始化完成
    if (!authStore.initialized) {
        await authStore.init()
    }

    const isAuthenticated = authStore.isAuthenticated
    const requiresAuth = to.meta.requiresAuth
    const requiresGuest = to.meta.requiresGuest

    // 需要登入但未登入 -> 跳轉登入頁
    if (requiresAuth && !isAuthenticated) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
    }

    // 需要訪客但已登入 -> 跳轉首頁
    if (requiresGuest && isAuthenticated) {
        next({ name: 'Chat' })
        return
    }

    next()
})

export default router
