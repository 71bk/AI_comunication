import { post, get } from './http'

// ============================================
// Types - 欄位名稱使用 snake_case 以匹配後端 Jackson 設定
// ============================================

export interface RegisterReq {
    email: string
    password: string
    display_name: string
}

export interface LoginReq {
    email: string
    password: string
}

export interface AuthMeResp {
    id: number
    email: string
    display_name: string
    status: string
    // token 已移除 - 使用 HttpOnly Cookie 認證，不再回傳 token
    created_at?: string
}

// ============================================
// API Functions
// ============================================

/**
 * 使用者註冊
 */
export function register(req: RegisterReq): Promise<AuthMeResp> {
    return post<AuthMeResp>('/auth/register', req)
}

/**
 * 使用者登入
 */
export function login(req: LoginReq): Promise<AuthMeResp> {
    return post<AuthMeResp>('/auth/login', req)
}

/**
 * 使用者登出
 */
export function logout(): Promise<void> {
    return post<void>('/auth/logout')
}

/**
 * 取得當前使用者資訊
 */
export function getMe(): Promise<AuthMeResp> {
    return get<AuthMeResp>('/auth/me')
}
