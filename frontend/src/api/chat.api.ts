import { get, post, put, del } from './http'

// ============================================
// Types - 欄位名稱使用 snake_case 以匹配後端 Jackson 設定
// ============================================

export interface ChatCreateReq {
    title?: string
}

export interface ChatResp {
    id: number
    title: string
    created_at: string
    updated_at: string
}

export interface MessageResp {
    id: number
    role: 'SYSTEM' | 'USER' | 'ASSISTANT'
    content: string
    provider?: string
    model?: string
    token_in?: number
    token_out?: number
    metadata?: Record<string, unknown>
    created_at: string
}

export interface ChatDetailResp {
    id: number
    title: string
    messages: MessageResp[]
    created_at: string
    updated_at: string
}

export interface MessageSendReq {
    content: string
    model?: string
    temperature?: number
    max_tokens?: number
}

export interface ChatTitleUpdateReq {
    title: string
}

// ============================================
// API Functions
// ============================================

/**
 * 建立新對話
 */
export function createChat(req?: ChatCreateReq): Promise<ChatResp> {
    return post<ChatResp>('/chats', req || {})
}

/**
 * 取得對話列表
 */
export function getChatList(): Promise<ChatResp[]> {
    return get<ChatResp[]>('/chats')
}

/**
 * 取得對話詳情（含訊息）
 */
export function getChatDetail(chatId: number): Promise<ChatDetailResp> {
    return get<ChatDetailResp>(`/chats/${chatId}`)
}

/**
 * 刪除對話
 */
export function deleteChat(chatId: number): Promise<void> {
    return del(`/chats/${chatId}`)
}

/**
 * 更新對話標題
 */
export function updateChatTitle(chatId: number, title: string): Promise<ChatResp> {
    return put<ChatResp>(`/chats/${chatId}/title`, { title })
}

/**
 * 取得 SSE 串流 URL
 */
export function getStreamUrl(chatId: number): string {
    return `/api/chats/${chatId}/messages:stream`
}
