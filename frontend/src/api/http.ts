import axios, { AxiosError, type AxiosInstance, type AxiosResponse } from 'axios'
import { AppError, toAppError, type ApiResult, DEFAULT_ERROR_MESSAGES } from './error'
import router from '@/router'

/**
 * Axios 實例配置
 * 使用 Cookie 認證方案：withCredentials 讓瀏覽器自動攜帶 HttpOnly Cookie
 */
const http: AxiosInstance = axios.create({
    baseURL: '/api',
    timeout: 30000,
    withCredentials: true,  // 🔑 關鍵：啟用跨域 Cookie
    headers: {
        'Content-Type': 'application/json',
    },
})

// 不再需要 Request Interceptor 手動附加 JWT

/**
 * Response Interceptor - 統一錯誤處理
 */
http.interceptors.response.use(
    (response: AxiosResponse<ApiResult>) => {
        const result = response.data

        // 後端回傳 success: false 視為錯誤
        if (result && result.success === false) {
            throw toAppError(result)
        }

        return response
    },
    (error: AxiosError<ApiResult>) => {
        // 處理 HTTP 錯誤
        if (error.response) {
            const { status, data } = error.response

            // 有後端回傳的錯誤格式
            if (data && data.code) {
                const appError = toAppError(data)

                // 401 自動導向登入（Cookie 由後端管理，前端不需清除）
                if (status === 401) {
                    router.push('/login')
                }

                throw appError
            }

            // 無格式的 HTTP 錯誤
            throw new AppError(
                `HTTP_${status}`,
                getHttpErrorMessage(status)
            )
        }

        // 網路錯誤
        if (error.code === 'ECONNABORTED') {
            throw new AppError('TIMEOUT', DEFAULT_ERROR_MESSAGES.TIMEOUT)
        }

        throw new AppError('NETWORK_ERROR', DEFAULT_ERROR_MESSAGES.NETWORK_ERROR)
    }
)

/**
 * 取得 HTTP 狀態碼對應的錯誤訊息
 */
function getHttpErrorMessage(status: number): string {
    const messages: Record<number, string> = {
        400: '請求參數錯誤',
        401: '未授權，請重新登入',
        403: '沒有權限執行此操作',
        404: '請求的資源不存在',
        500: '伺服器內部錯誤',
        502: '伺服器暫時無法處理請求',
        503: '服務暫時不可用',
    }
    return messages[status] || `請求失敗 (${status})`
}

/**
 * 封裝 GET 請求
 */
export async function get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
    const response = await http.get<ApiResult<T>>(url, { params })
    return response.data.data as T
}

/**
 * 封裝 POST 請求
 */
export async function post<T>(url: string, data?: unknown): Promise<T> {
    const response = await http.post<ApiResult<T>>(url, data)
    return response.data.data as T
}

/**
 * 封裝 PUT 請求
 */
export async function put<T>(url: string, data?: unknown): Promise<T> {
    const response = await http.put<ApiResult<T>>(url, data)
    return response.data.data as T
}

/**
 * 封裝 DELETE 請求
 */
export async function del<T = void>(url: string): Promise<T> {
    const response = await http.delete<ApiResult<T>>(url)
    return response.data.data as T
}

export default http
