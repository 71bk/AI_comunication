<script setup lang="ts">
import { computed, ref, nextTick } from 'vue'
import { ElIcon, ElButton, ElPopconfirm, ElMessage } from 'element-plus'
import { Plus, Delete, ChatDotRound, Edit } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat.store'
import { useAuthStore } from '@/stores/auth.store'

const chatStore = useChatStore()
const authStore = useAuthStore()

const chats = computed(() => chatStore.chats)
const currentChatId = computed(() => chatStore.currentChatId)
const loading = computed(() => chatStore.loading)

// 編輯標題相關狀態
const editingChatId = ref<number | null>(null)
const editingTitle = ref('')

// 建立新對話
async function handleCreateChat() {
  await chatStore.createChat()
}

// 選擇對話
async function handleSelectChat(chatId: number) {
  // 如果正在編輯，不切換
  if (editingChatId.value === chatId) return
  await chatStore.selectChat(chatId)
}

// 刪除對話
async function handleDeleteChat(chatId: number) {
  await chatStore.deleteChat(chatId)
}

// 開始編輯標題
function handleEditTitle(chatId: number, currentTitle: string, event: Event) {
  event.stopPropagation()
  editingChatId.value = chatId
  editingTitle.value = currentTitle
  nextTick(() => {
    // 在 v-for 中無法直接用 ref，改用 querySelector
    const input = document.querySelector('.title-input') as HTMLInputElement
    input?.focus()
    input?.select()
  })
}

// 保存標題
async function handleSaveTitle() {
  if (!editingChatId.value) return
  
  const newTitle = editingTitle.value.trim()
  if (!newTitle) {
    ElMessage.warning('標題不能為空')
    return
  }
  
  try {
    await chatStore.updateChatTitle(editingChatId.value, newTitle)
    ElMessage.success('標題已更新')
  } catch (error) {
    ElMessage.error('更新標題失敗')
  } finally {
    editingChatId.value = null
    editingTitle.value = ''
  }
}

// 取消編輯
function handleCancelEdit() {
  editingChatId.value = null
  editingTitle.value = ''
}

// 處理鍵盤事件
function handleTitleKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter') {
    handleSaveTitle()
  } else if (event.key === 'Escape') {
    handleCancelEdit()
  }
}

// 登出
function handleLogout() {
  authStore.logout()
}

// 格式化時間
function formatTime(dateStr: string): string {
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  if (diff < 60 * 1000) return '剛剛'
  if (diff < 60 * 60 * 1000) return `${Math.floor(diff / 60000)} 分鐘前`
  if (diff < 24 * 60 * 60 * 1000) return `${Math.floor(diff / 3600000)} 小時前`
  if (diff < 7 * 24 * 60 * 60 * 1000) return `${Math.floor(diff / 86400000)} 天前`

  return date.toLocaleDateString()
}
</script>

<template>
  <aside class="sidebar">
    <!-- Header -->
    <div class="sidebar-header">
      <h2 class="logo">AI Chat</h2>
      <el-button
        type="primary"
        :icon="Plus"
        @click="handleCreateChat"
        :loading="loading"
      >
        新對話
      </el-button>
    </div>

    <!-- Chat List -->
    <div class="chat-list">
      <div v-if="chats.length === 0" class="empty-state">
        <el-icon :size="40" class="empty-icon"><ChatDotRound /></el-icon>
        <p>還沒有對話</p>
        <p class="hint">點擊上方按鈕開始</p>
      </div>

      <div
        v-for="chat in chats"
        :key="chat.id"
        class="chat-item"
        :class="{ active: chat.id === currentChatId }"
        @click="handleSelectChat(chat.id)"
      >
        <div class="chat-info">
          <!-- 編輯模式 -->
          <template v-if="editingChatId === chat.id">
            <input
              v-model="editingTitle"
              class="title-input"
              @keydown="handleTitleKeydown"
              @blur="handleSaveTitle"
              @click.stop
            />
          </template>
          <!-- 顯示模式 -->
          <template v-else>
            <span class="chat-title">{{ chat.title }}</span>
            <span class="chat-time">{{ formatTime(chat.updated_at) }}</span>
          </template>
        </div>

        <div class="chat-actions">
          <!-- 編輯按鈕 -->
          <el-button
            v-if="editingChatId !== chat.id"
            class="action-btn edit-btn"
            :icon="Edit"
            size="small"
            link
            @click="handleEditTitle(chat.id, chat.title, $event)"
          />
          
          <!-- 刪除按鈕 -->
          <el-popconfirm
            title="確定要刪除這個對話嗎？"
            confirm-button-text="刪除"
            cancel-button-text="取消"
            @confirm="handleDeleteChat(chat.id)"
          >
            <template #reference>
              <el-button
                class="action-btn delete-btn"
                :icon="Delete"
                size="small"
                link
                @click.stop
              />
            </template>
          </el-popconfirm>
        </div>
      </div>
    </div>

    <!-- Footer -->
    <div class="sidebar-footer">
      <div class="user-info">
        <div class="avatar">{{ authStore.displayName.charAt(0).toUpperCase() }}</div>
        <span class="username">{{ authStore.displayName }}</span>
      </div>
      <el-button link size="small" @click="handleLogout">登出</el-button>
    </div>
  </aside>
</template>

<style lang="scss" scoped>
.sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.sidebar-header {
  padding: $spacing-md;
  border-bottom: 1px solid $dark-border;

  .logo {
    font-size: $font-size-xl;
    font-weight: 700;
    margin-bottom: $spacing-md;
    background: linear-gradient(135deg, $primary-light, $primary);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .el-button {
    width: 100%;
  }
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: $spacing-sm;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: $spacing-2xl $spacing-md;
  color: $gray-500;
  text-align: center;

  .empty-icon {
    color: $gray-600;
    margin-bottom: $spacing-md;
  }

  p {
    margin-bottom: $spacing-xs;
  }

  .hint {
    font-size: $font-size-sm;
    color: $gray-600;
  }
}

.chat-item {
  display: flex;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  border-radius: $radius-md;
  cursor: pointer;
  transition: background-color $transition-fast;
  margin-bottom: $spacing-xs;

  &:hover {
    background-color: $dark-surface-2;

    .chat-actions .action-btn {
      opacity: 1;
    }
  }

  &.active {
    background-color: rgba($primary, 0.15);
    border: 1px solid rgba($primary, 0.3);
  }

  .chat-info {
    flex: 1;
    min-width: 0;

    .chat-title {
      display: block;
      font-size: $font-size-sm;
      color: $gray-200;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .chat-time {
      font-size: $font-size-xs;
      color: $gray-500;
    }

    .title-input {
      width: 100%;
      padding: $spacing-xs $spacing-sm;
      background-color: $dark-surface;
      border: 1px solid $primary;
      border-radius: $radius-sm;
      color: $gray-100;
      font-size: $font-size-sm;
      outline: none;

      &:focus {
        box-shadow: 0 0 0 2px rgba($primary, 0.3);
      }
    }
  }

  .chat-actions {
    display: flex;
    gap: $spacing-xs;
    flex-shrink: 0;

    .action-btn {
      opacity: 0;
      color: $gray-500;
      transition: opacity $transition-fast, color $transition-fast;

      &:hover {
        color: $gray-200;
      }
    }

    .edit-btn:hover {
      color: $primary;
    }

    .delete-btn:hover {
      color: $error;
    }
  }
}

.sidebar-footer {
  padding: $spacing-md;
  border-top: 1px solid $dark-border;
  display: flex;
  align-items: center;
  justify-content: space-between;

  .user-info {
    display: flex;
    align-items: center;
    gap: $spacing-sm;

    .avatar {
      width: 32px;
      height: 32px;
      border-radius: $radius-full;
      background: linear-gradient(135deg, $primary, $primary-dark);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 600;
      font-size: $font-size-sm;
      color: white;
    }

    .username {
      font-size: $font-size-sm;
      color: $gray-300;
    }
  }
}
</style>
