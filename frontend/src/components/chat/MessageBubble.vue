<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { renderMarkdown } from '@/utils/markdown'
import type { MessageResp } from '@/api/chat.api'

const props = defineProps<{
  message: MessageResp
  isStreaming?: boolean
}>()

// 渲染 Markdown 內容
const renderedContent = computed(() => {
  if (!props.message.content) return ''
  return renderMarkdown(props.message.content)
})

// 是否為使用者訊息
const isUser = computed(() => props.message.role === 'USER')

// 複製訊息內容
async function copyContent() {
  try {
    await navigator.clipboard.writeText(props.message.content)
    ElMessage.success('已複製到剪貼簿')
  } catch {
    ElMessage.error('複製失敗')
  }
}
</script>

<template>
  <div class="message-bubble" :class="{ 'is-user': isUser, 'is-assistant': !isUser }">
    <!-- Avatar -->
    <div class="avatar">
      <template v-if="isUser">👤</template>
      <template v-else>🤖</template>
    </div>

    <!-- Content -->
    <div class="content-wrapper">
      <div class="role-label">{{ isUser ? '你' : 'AI' }}</div>
      <div class="content" v-html="renderedContent"></div>

      <!-- Streaming Indicator -->
      <div v-if="isStreaming" class="streaming-indicator">
        <span class="dot"></span>
        <span class="dot"></span>
        <span class="dot"></span>
      </div>

      <!-- Actions -->
      <div class="actions" v-if="!isStreaming && !isUser">
        <button class="action-btn" @click="copyContent" title="複製">
          📋
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.message-bubble {
  display: flex;
  gap: $spacing-md;
  padding: $spacing-lg 0;

  &:not(:last-child) {
    border-bottom: 1px solid $dark-border;
  }

  &.is-user {
    .avatar {
      background: linear-gradient(135deg, $secondary, $secondary-dark);
    }
  }

  &.is-assistant {
    .avatar {
      background: linear-gradient(135deg, $primary, $primary-dark);
    }
  }
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: $radius-md;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.content-wrapper {
  flex: 1;
  min-width: 0;
}

.role-label {
  font-size: $font-size-xs;
  color: $gray-500;
  margin-bottom: $spacing-xs;
  text-transform: uppercase;
  font-weight: 600;
}

.content {
  color: $gray-100;
  line-height: $line-height-relaxed;

  :deep(p) {
    margin-bottom: $spacing-md;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(code) {
    font-family: $font-mono;
    font-size: 0.875em;
    background-color: $dark-surface-2;
    padding: 2px 6px;
    border-radius: $radius-sm;
  }

  :deep(pre) {
    background-color: $dark-surface;
    border: 1px solid $dark-border;
    border-radius: $radius-md;
    padding: $spacing-md;
    overflow-x: auto;
    margin: $spacing-md 0;

    code {
      background: none;
      padding: 0;
      font-size: $font-size-sm;
    }
  }

  :deep(ul),
  :deep(ol) {
    padding-left: $spacing-lg;
    margin-bottom: $spacing-md;
  }

  :deep(li) {
    margin-bottom: $spacing-xs;
  }

  :deep(blockquote) {
    border-left: 4px solid $primary;
    padding-left: $spacing-md;
    margin: $spacing-md 0;
    color: $gray-400;
    font-style: italic;
  }

  :deep(a) {
    color: $primary-light;

    &:hover {
      text-decoration: underline;
    }
  }

  :deep(table) {
    width: 100%;
    border-collapse: collapse;
    margin: $spacing-md 0;

    th,
    td {
      border: 1px solid $dark-border;
      padding: $spacing-sm $spacing-md;
      text-align: left;
    }

    th {
      background-color: $dark-surface-2;
      font-weight: 600;
    }
  }
}

.streaming-indicator {
  display: flex;
  gap: 4px;
  margin-top: $spacing-sm;

  .dot {
    width: 8px;
    height: 8px;
    background-color: $primary;
    border-radius: 50%;
    animation: pulse 1.4s infinite ease-in-out;

    &:nth-child(1) {
      animation-delay: -0.32s;
    }
    &:nth-child(2) {
      animation-delay: -0.16s;
    }
  }
}

@keyframes pulse {
  0%,
  80%,
  100% {
    transform: scale(0.6);
    opacity: 0.4;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.actions {
  display: flex;
  gap: $spacing-sm;
  margin-top: $spacing-sm;
  opacity: 0;
  transition: opacity $transition-fast;

  .message-bubble:hover & {
    opacity: 1;
  }
}

.action-btn {
  background: none;
  padding: $spacing-xs;
  font-size: 14px;
  color: $gray-500;
  border-radius: $radius-sm;
  transition: all $transition-fast;

  &:hover {
    background-color: $dark-surface-2;
    color: $gray-200;
  }
}
</style>
