<script setup lang="ts">
import { ref, computed, shallowRef } from 'vue'
import { ElButton, ElMessage } from 'element-plus'
import { useChatStore } from '@/stores/chat.store'
import { getStreamUrl } from '@/api/chat.api'
import { createSseConnection, abortSseConnection } from '@/utils/sse'

const chatStore = useChatStore()

const content = ref('')
const textareaRef = ref<HTMLTextAreaElement | null>(null)
// 使用 shallowRef 確保響應性（不需要深層追蹤）
const abortController = shallowRef<AbortController | null>(null)

const isStreaming = computed(() => chatStore.isStreaming)
const canSend = computed(() => content.value.trim().length > 0 && !isStreaming.value)
const currentChatId = computed(() => chatStore.currentChatId)

// 自動調整 textarea 高度
function adjustHeight() {
  const textarea = textareaRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = Math.min(textarea.scrollHeight, 200) + 'px'
  }
}

// 處理按鍵
function handleKeydown(e: KeyboardEvent) {
  // Enter 送出（Shift+Enter 換行）
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

// 送出訊息
async function handleSend() {
  if (!canSend.value || !currentChatId.value) return

  const message = content.value.trim()
  content.value = ''
  adjustHeight()

  // 樂觀更新 - 新增使用者訊息
  chatStore.addUserMessage(message)

  // 開始串流
  chatStore.startStreaming()

  try {
    abortController.value = await createSseConnection({
      url: getStreamUrl(currentChatId.value),
      body: { content: message },
      callbacks: {
        onDelta: (delta) => {
          chatStore.appendStreamingContent(delta)
        },
        onDone: () => {
          chatStore.endStreaming()
          abortController.value = null
        },
        onError: (error) => {
          chatStore.endStreaming()
          ElMessage.error(error.message || '發送失敗')
          abortController.value = null
        },
      },
    })
  } catch (error) {
    chatStore.endStreaming()
    ElMessage.error('連線失敗')
  }
}

// 停止生成
function handleStop() {
  if (abortController.value) {
    abortSseConnection(abortController.value)
    chatStore.endStreaming()
    abortController.value = null
  }
}
</script>

<template>
  <div class="chat-composer">
    <div class="chat-composer-inner">
      <div class="composer-container">
        <textarea
          ref="textareaRef"
          v-model="content"
          class="composer-input"
          placeholder="輸入訊息..."
          rows="1"
          :disabled="!currentChatId"
          @input="adjustHeight"
          @keydown="handleKeydown"
        ></textarea>

        <div class="composer-actions">
          <template v-if="isStreaming">
            <el-button type="danger" size="small" @click="handleStop">
              停止生成
            </el-button>
          </template>
          <template v-else>
            <el-button
              type="primary"
              size="small"
              :disabled="!canSend"
              @click="handleSend"
            >
              發送
            </el-button>
          </template>
        </div>
      </div>

      <p class="hint">
        按 Enter 發送，Shift + Enter 換行
      </p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.chat-composer {
  padding: $spacing-md $spacing-lg $spacing-lg;
  border-top: 1px solid $dark-border;
  background-color: $dark-bg;
}

.chat-composer-inner {
  max-width: $max-content-width;
  margin: 0 auto;
  width: 100%;
}

.composer-container {
  display: flex;
  gap: $spacing-md;
  align-items: flex-end;
  background-color: $dark-surface;
  border: 1px solid $dark-border;
  border-radius: $radius-lg;
  padding: $spacing-sm $spacing-md;
  transition: border-color $transition-fast, box-shadow $transition-fast;

  &:focus-within {
    border-color: $primary;
    box-shadow: 0 0 0 3px rgba($primary, 0.15);
  }
}

.composer-input {
  flex: 1;
  background: none;
  color: $gray-100;
  font-size: $font-size-base;
  line-height: $line-height-normal;
  resize: none;
  min-height: 24px;
  max-height: 200px;
  padding: $spacing-xs 0;

  &::placeholder {
    color: $gray-500;
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.composer-actions {
  flex-shrink: 0;
}

.hint {
  margin-top: $spacing-sm;
  font-size: $font-size-xs;
  color: $gray-600;
  text-align: center;
  margin-bottom: 0;
}
</style>
