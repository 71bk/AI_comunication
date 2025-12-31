/**
 * AppError - 統一前端錯誤類別
 * 對應後端 Result 格式
 */
export class AppError extends Error {
    constructor(
        public code: string,
        message: string,
        public traceId?: string,
        public path?: string,
        public fieldErrors?: FieldError[]
    ) {
        super(message)
        this.name = 'AppError'
    }

    /**
     * 是否為驗證錯誤
     */
    isValidationError(): boolean {
        return this.code === 'VALIDATION_FAILED'
    }

    /**
     * 是否為認證錯誤
     */
    isAuthError(): boolean {
        return this.code.startsWith('AUTH_')
    }

    /**
     * 取得特定欄位的錯誤訊息
     */
    getFieldError(field: string): string | undefined {
        return this.fieldErrors?.find(e => e.field === field)?.reason
    }
}

/**
 * 欄位錯誤
 */
export interface FieldError {
    field: string
    reason: string
}

/**
 * 後端 Result 格式
 */
export interface ApiResult<T = unknown> {
    success: boolean
    code: string
    message: string
    data?: T
    path?: string
    timestamp?: string
    traceId?: string
}

/**
 * 將後端 Result 轉換為 AppError
 */
export function toAppError(result: ApiResult): AppError {
    let fieldErrors: FieldError[] | undefined

    // 處理 Validation 錯誤的欄位資訊
    if (result.code === 'VALIDATION_FAILED' && result.data) {
        const data = result.data as { fields?: FieldError[] }
        fieldErrors = data.fields
    }

    return new AppError(
        result.code,
        result.message,
        result.traceId,
        result.path,
        fieldErrors
    )
}

/**
 * 預設錯誤訊息
 */
export const DEFAULT_ERROR_MESSAGES: Record<string, string> = {
    NETWORK_ERROR: '網路連線失敗，請檢查網路狀態',
    TIMEOUT: '請求逾時，請稍後再試',
    INTERNAL_ERROR: '系統發生錯誤，請稍後再試',
    AUTH_INVALID_CREDENTIALS: '帳號或密碼錯誤',
    AUTH_INVALID_TOKEN: '登入已過期，請重新登入',
    AUTH_FORBIDDEN: '您沒有權限執行此操作',
    VALIDATION_FAILED: '輸入資料驗證失敗',
}
