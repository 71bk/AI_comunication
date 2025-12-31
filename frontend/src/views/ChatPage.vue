<script setup lang="ts">
import { onMounted } from 'vue'
import { useChatStore } from '@/stores/chat.store'
import Sidebar from '@/components/chat/Sidebar.vue'
import Thread from '@/components/chat/Thread.vue'
import Composer from '@/components/chat/Composer.vue'

const chatStore = useChatStore()

// 載入對話列表
onMounted(async () => {
  await chatStore.loadChats()

  // 如果有對話，自動選擇第一個
  if (chatStore.chats.length > 0) {
    await chatStore.selectChat(chatStore.chats[0].id)
  }
})
</script>

<template>
  <div class="app-layout">
    <!-- Sidebar -->
    <Sidebar />

    <!-- Main Content -->
    <main class="main-content">
      <div class="chat-container">
        <!-- Thread -->
        <Thread />

        <!-- Composer -->
        <Composer />
      </div>
    </main>
  </div>
</template>

<style lang="scss" scoped>
// 使用 layout.scss 中定義的類別，無需額外樣式
</style>
