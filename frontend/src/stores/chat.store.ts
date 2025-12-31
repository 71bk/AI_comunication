import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as chatApi from '@/api/chat.api'
import type { ChatResp, ChatDetailResp, MessageResp } from '@/api/chat.api'

export const useChatStore = defineStore('chat', () => {
    // ============================================
    // State
    // ============================================
    const chats = ref<ChatResp[]>([])
    const currentChatId = ref<number | null>(null)
    const currentChat = ref<ChatDetailResp | null>(null)
    const loading = ref(false)
    const isStreaming = ref(false)
    const streamingContent = ref('')

    // ============================================
    // Getters
    // ============================================
    const messages = computed<MessageResp[]>(() => currentChat.value?.messages || [])
    const hasChats = computed(() => chats.value.length > 0)

    // ============================================
    // Actions
    // ============================================

    /**
     * 載入對話列表
     */
    async function loadChats() {
        loading.value = true
        try {
            chats.value = await chatApi.getChatList()
        } finally {
            loading.value = false
        }
    }

    /**
     * 建立新對話
     */
    async function createChat(title?: string) {
        const chat = await chatApi.createChat({ title })
        chats.value.unshift(chat)
        await selectChat(chat.id)
        return chat
    }

    /**
     * 選擇對話
     */
    async function selectChat(chatId: number) {
        if (currentChatId.value === chatId) return

        currentChatId.value = chatId
        loading.value = true
        try {
            currentChat.value = await chatApi.getChatDetail(chatId)
        } finally {
            loading.value = false
        }
    }

    /**
     * 刪除對話
     */
    async function deleteChat(chatId: number) {
        await chatApi.deleteChat(chatId)
        chats.value = chats.value.filter((c: ChatResp) => c.id !== chatId)

        // 如果刪除的是當前對話，切換到第一個對話或清空
        if (currentChatId.value === chatId) {
            if (chats.value.length > 0) {
                await selectChat(chats.value[0].id)
            } else {
                currentChatId.value = null
                currentChat.value = null
            }
        }
    }

    /**
     * 更新對話標題
     */
    async function updateChatTitle(chatId: number, title: string) {
        const updated = await chatApi.updateChatTitle(chatId, title)

        // 更新列表中的標題
        const chat = chats.value.find((c: ChatResp) => c.id === chatId)
        if (chat) {
            chat.title = updated.title
        }

        // 更新當前對話的標題
        if (currentChat.value && currentChat.value.id === chatId) {
            currentChat.value.title = updated.title
        }

        return updated
    }

    /**
     * 新增使用者訊息（樂觀更新）
     */
    function addUserMessage(content: string) {
        if (!currentChat.value) return

        const message: MessageResp = {
            id: Date.now(), // 臨時 ID
            role: 'USER',
            content,
            created_at: new Date().toISOString(),
        }

        currentChat.value.messages.push(message)
    }

    /**
     * 開始串流回覆
     */
    function startStreaming() {
        if (!currentChat.value) return

        isStreaming.value = true
        streamingContent.value = ''

        // 新增一個空的 assistant 訊息
        const message: MessageResp = {
            id: Date.now(),
            role: 'ASSISTANT',
            content: '',
            created_at: new Date().toISOString(),
        }

        currentChat.value.messages.push(message)
    }

    /**
     * 追加串流內容
     */
    function appendStreamingContent(delta: string) {
        if (!currentChat.value) return

        streamingContent.value += delta

        // 更新最後一則 assistant 訊息的內容
        const msgs = currentChat.value.messages
        const lastMessage = msgs[msgs.length - 1]
        if (lastMessage && lastMessage.role === 'ASSISTANT') {
            lastMessage.content = streamingContent.value
        }
    }

    /**
     * 結束串流
     */
    function endStreaming() {
        isStreaming.value = false
        streamingContent.value = ''
    }

    /**
     * 清空當前對話
     */
    function clearCurrentChat() {
        currentChatId.value = null
        currentChat.value = null
    }

    return {
        // State
        chats,
        currentChatId,
        currentChat,
        loading,
        isStreaming,
        streamingContent,
        // Getters
        messages,
        hasChats,
        // Actions
        loadChats,
        createChat,
        selectChat,
        deleteChat,
        updateChatTitle,
        addUserMessage,
        startStreaming,
        appendStreamingContent,
        endStreaming,
        clearCurrentChat,
    }
})
