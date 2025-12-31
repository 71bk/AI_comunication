import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth.api'
import type { AuthMeResp } from '@/api/auth.api'
import router from '@/router'

export const useAuthStore = defineStore('auth', () => {
    // ============================================
    // State
    // ============================================
    const user = ref<AuthMeResp | null>(null)
    const loading = ref(false)
    const initialized = ref(false)

    // ============================================
    // Getters
    // ============================================
    const isAuthenticated = computed(() => !!user.value)
    const displayName = computed(() => user.value?.display_name || '')

    // ============================================
    // Actions
    // ============================================

    /**
     * 初始化 - 嘗試透過 Cookie 取得使用者資訊
     * Cookie 由瀏覽器自動攜帶，不需要手動管理 token
     */
    async function init() {
        try {
            user.value = await authApi.getMe()
        } catch (error) {
            // Cookie 無效或未登入
            user.value = null
        }
        initialized.value = true
    }

    /**
     * 註冊
     * 後端會設定 HttpOnly Cookie，前端只需保存 user 資訊
     */
    async function register(email: string, password: string, displayName: string) {
        loading.value = true
        try {
            const resp = await authApi.register({ email, password, display_name: displayName })
            user.value = resp
            router.push('/')
        } finally {
            loading.value = false
        }
    }

    /**
     * 登入
     * 後端會設定 HttpOnly Cookie，前端只需保存 user 資訊
     */
    async function login(email: string, password: string) {
        loading.value = true
        try {
            const resp = await authApi.login({ email, password })
            user.value = resp
            router.push('/')
        } finally {
            loading.value = false
        }
    }

    /**
     * 登出
     * 後端會清除 HttpOnly Cookie
     */
    async function logout() {
        try {
            await authApi.logout()
        } catch (error) {
            // 忽略登出錯誤
        } finally {
            user.value = null
            router.push('/login')
        }
    }

    /**
     * 重新取得使用者資訊
     */
    async function fetchUser() {
        try {
            user.value = await authApi.getMe()
        } catch (error) {
            user.value = null
        }
    }

    return {
        // State
        user,
        loading,
        initialized,
        // Getters
        isAuthenticated,
        displayName,
        // Actions
        init,
        register,
        login,
        logout,
        fetchUser,
    }
})

