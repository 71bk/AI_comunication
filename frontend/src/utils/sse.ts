import type { MessageSendReq } from '@/api/chat.api'

/**
 * SSE 事件類型
 */
export interface SseEvent {
    type: 'delta' | 'meta' | 'done' | 'error'
    delta?: string
    usage?: { inputTokens: number; outputTokens: number }
    citations?: Array<{ docId: number; chunkId: number; title: string; page?: number }>
    error?: {
        success: boolean
        code: string
        message: string
        traceId?: string
    }
}

/**
 * SSE 回調函數
 */
export interface SseCallbacks {
    onDelta?: (delta: string) => void
    onMeta?: (event: SseEvent) => void
    onDone?: (usage?: { inputTokens: number; outputTokens: number }) => void
    onError?: (error: { code: string; message: string }) => void
}

/**
 * SSE 連線選項
 */
export interface SseOptions {
    url: string
    body: MessageSendReq
    callbacks: SseCallbacks
}

/**
 * 建立 SSE 串流連線
 * 使用 fetch + ReadableStream 實現 POST SSE
 */
export async function createSseConnection(options: SseOptions): Promise<AbortController> {
    const { url, body, callbacks } = options
    const controller = new AbortController()

    try {
        const response = await fetch(url, {
            method: 'POST',
            credentials: 'include',  // 🔑 關鍵：啟用跨域 Cookie
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
            signal: controller.signal,
        })

        if (!response.ok) {
            callbacks.onError?.({
                code: `HTTP_${response.status}`,
                message: `請求失敗: ${response.statusText}`,
            })
            return controller
        }

        const reader = response.body?.getReader()
        if (!reader) {
            callbacks.onError?.({
                code: 'NO_BODY',
                message: '無法讀取回應',
            })
            return controller
        }

        const decoder = new TextDecoder()
        let buffer = ''

        // 事件狀態需要在多次 read() 之間保持
        let eventType = ''
        let eventData = ''

        // 處理 SSE 事件的函數
        function processEvents(lines: string[]) {
            for (const line of lines) {
                if (line.startsWith('event:')) {
                    eventType = line.slice(6).trim()
                } else if (line.startsWith('data:')) {
                    eventData = line.slice(5).trim()
                } else if (line === '' && eventType && eventData) {
                    // 事件結束，解析並處理
                    try {
                        const event = JSON.parse(eventData) as SseEvent

                        switch (eventType) {
                            case 'delta':
                                if (event.delta) {
                                    callbacks.onDelta?.(event.delta)
                                }
                                break
                            case 'meta':
                                callbacks.onMeta?.(event)
                                break
                            case 'done':
                                callbacks.onDone?.(event.usage)
                                break
                            case 'error':
                                if (event.error) {
                                    callbacks.onError?.(event.error)
                                }
                                break
                        }
                    } catch {
                        // 解析錯誤，忽略
                    }

                    eventType = ''
                    eventData = ''
                }
            }
        }

        // 讀取串流
        while (true) {
            const { done, value } = await reader.read()

            if (done) {
                // 處理剩餘的 buffer（最後的事件可能還在這裡）
                if (buffer.trim()) {
                    const remainingLines = buffer.split('\n')
                    // 加一個空行來觸發最後一個事件的處理
                    remainingLines.push('')
                    processEvents(remainingLines)
                }
                break
            }

            buffer += decoder.decode(value, { stream: true })

            // 解析 SSE 事件
            const lines = buffer.split('\n')
            buffer = lines.pop() || '' // 保留未完成的行

            processEvents(lines)
        }
    } catch (error) {
        if ((error as Error).name !== 'AbortError') {
            callbacks.onError?.({
                code: 'STREAM_ERROR',
                message: (error as Error).message || '串流連線失敗',
            })
        }
    }

    return controller
}

/**
 * 停止 SSE 連線
 */
export function abortSseConnection(controller: AbortController): void {
    controller.abort()
}
