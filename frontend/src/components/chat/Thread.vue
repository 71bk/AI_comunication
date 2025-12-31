<script setup lang="ts">
import { computed, watch, nextTick, ref } from 'vue'
import { useChatStore } from '@/stores/chat.store'
import MessageBubble from './MessageBubble.vue'

const chatStore = useChatStore()
const threadRef = ref<HTMLElement | null>(null)

const messages = computed(() => chatStore.messages)
const isStreaming = computed(() => chatStore.isStreaming)
const currentChat = computed(() => chatStore.currentChat)

// 自動滾動到底部
function scrollToBottom(smooth = true) {
  nextTick(() => {
    if (threadRef.value) {
      threadRef.value.scrollTo({
        top: threadRef.value.scrollHeight,
        behavior: smooth ? 'smooth' : 'auto',
      })
    }
  })
}

// 監聽訊息變化，自動滾動
watch(
  () => messages.value.length,
  () => {
    scrollToBottom()
  }
)

// 監聽串流內容變化，平滑滾動
watch(
  () => chatStore.streamingContent,
  () => {
    if (isStreaming.value) {
      scrollToBottom(false)
    }
  }
)
</script>

<template>
  <div class="chat-thread" ref="threadRef">
    <div class="chat-thread-inner">
      <!-- Empty State -->
      <div v-if="!currentChat" class="empty-state">
        <div class="welcome-icon">💬</div>
        <h2>歡迎使用 AI Chat</h2>
        <p>選擇一個對話或建立新對話開始聊天</p>
      </div>

      <div v-else-if="messages.length === 0" class="empty-state">
        <div class="welcome-icon">👋</div>
        <h2>開始對話</h2>
        <p>在下方輸入您的問題或想法</p>
      </div>

      <!-- Messages -->
      <template v-else>
        <MessageBubble
          v-for="message in messages"
          :key="message.id"
          :message="message"
          :is-streaming="isStreaming && message === messages[messages.length - 1] && message.role === 'assistant'"
        />
      </template>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.chat-thread {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-lg;
}

.chat-thread-inner {
  max-width: $max-content-width;
  margin: 0 auto;
  width: 100%;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 50vh;
  text-align: center;
  color: $gray-400;

  .welcome-icon {
    font-size: 64px;
    margin-bottom: $spacing-lg;
  }

  h2 {
    font-size: $font-size-2xl;
    color: $gray-200;
    margin-bottom: $spacing-sm;
  }

  p {
    font-size: $font-size-base;
    color: $gray-500;
    margin-bottom: 0;
  }
}
</style>
