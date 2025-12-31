<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth.store'
import { AppError } from '@/api/error'

const authStore = useAuthStore()

// 登入/註冊模式切換
const isLogin = ref(true)

// 表單資料
const form = reactive({
  email: '',
  password: '',
  displayName: '',
})

// 表單錯誤
const errors = reactive({
  email: '',
  password: '',
  displayName: '',
})

// 提交中
const submitting = ref(false)

// 切換模式
function toggleMode() {
  isLogin.value = !isLogin.value
  clearErrors()
}

// 清除錯誤
function clearErrors() {
  errors.email = ''
  errors.password = ''
  errors.displayName = ''
}

// 驗證表單
function validate(): boolean {
  clearErrors()
  let valid = true

  if (!form.email) {
    errors.email = '請輸入 Email'
    valid = false
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email)) {
    errors.email = '請輸入有效的 Email'
    valid = false
  }

  if (!form.password) {
    errors.password = '請輸入密碼'
    valid = false
  } else if (form.password.length < 8) {
    errors.password = '密碼至少 8 個字元'
    valid = false
  }

  if (!isLogin.value && !form.displayName) {
    errors.displayName = '請輸入顯示名稱'
    valid = false
  }

  return valid
}

// 提交表單
async function handleSubmit() {
  if (!validate()) return

  submitting.value = true
  clearErrors()

  try {
    if (isLogin.value) {
      await authStore.login(form.email, form.password)
      ElMessage.success('登入成功')
    } else {
      await authStore.register(form.email, form.password, form.displayName)
      ElMessage.success('註冊成功')
    }
  } catch (error) {
    if (error instanceof AppError) {
      // 處理欄位錯誤
      if (error.isValidationError() && error.fieldErrors) {
        for (const fieldError of error.fieldErrors) {
          if (fieldError.field in errors) {
            (errors as Record<string, string>)[fieldError.field] = fieldError.reason
          }
        }
      } else {
        ElMessage.error(error.message)
      }
    } else {
      ElMessage.error('發生未知錯誤')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="auth-layout">
    <div class="auth-card">
      <div class="auth-header">
        <h1>AI Chat</h1>
        <p>{{ isLogin ? '歡迎回來' : '建立新帳號' }}</p>
      </div>

      <form @submit.prevent="handleSubmit" class="auth-form">
        <!-- Email -->
        <div class="form-group">
          <label for="email">Email</label>
          <input
            id="email"
            v-model="form.email"
            type="email"
            placeholder="your@email.com"
            :class="{ 'has-error': errors.email }"
            autocomplete="email"
          />
          <span v-if="errors.email" class="error-message">{{ errors.email }}</span>
        </div>

        <!-- Display Name (Register only) -->
        <div v-if="!isLogin" class="form-group">
          <label for="displayName">顯示名稱</label>
          <input
            id="displayName"
            v-model="form.displayName"
            type="text"
            placeholder="您的名稱"
            :class="{ 'has-error': errors.displayName }"
            autocomplete="name"
          />
          <span v-if="errors.displayName" class="error-message">{{ errors.displayName }}</span>
        </div>

        <!-- Password -->
        <div class="form-group">
          <label for="password">密碼</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="至少 8 個字元"
            :class="{ 'has-error': errors.password }"
            autocomplete="current-password"
          />
          <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
        </div>

        <!-- Submit Button -->
        <button type="submit" class="submit-btn" :disabled="submitting">
          <span v-if="submitting" class="loading-spinner"></span>
          {{ isLogin ? '登入' : '註冊' }}
        </button>
      </form>

      <!-- Toggle Mode -->
      <div class="auth-footer">
        <span>{{ isLogin ? '還沒有帳號？' : '已有帳號？' }}</span>
        <button type="button" class="link-btn" @click="toggleMode">
          {{ isLogin ? '立即註冊' : '立即登入' }}
        </button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.auth-form {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;

  label {
    font-size: $font-size-sm;
    color: $gray-300;
    font-weight: 500;
  }

  input {
    width: 100%;
    padding: $spacing-sm $spacing-md;
    background-color: $dark-surface-2;
    border: 1px solid $dark-border;
    border-radius: $radius-md;
    color: $gray-100;
    font-size: $font-size-base;
    transition: all $transition-fast;

    &::placeholder {
      color: $gray-500;
    }

    &:focus {
      border-color: $primary;
      box-shadow: 0 0 0 3px rgba($primary, 0.2);
    }

    &.has-error {
      border-color: $error;

      &:focus {
        box-shadow: 0 0 0 3px rgba($error, 0.2);
      }
    }
  }

  .error-message {
    font-size: $font-size-xs;
    color: $error;
  }
}

.submit-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: $spacing-sm;
  width: 100%;
  padding: $spacing-sm $spacing-lg;
  background: linear-gradient(135deg, $primary, $primary-dark);
  color: white;
  font-size: $font-size-base;
  font-weight: 600;
  border-radius: $radius-md;
  margin-top: $spacing-sm;
  transition: all $transition-fast;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: $shadow-glow;
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.auth-footer {
  margin-top: $spacing-xl;
  text-align: center;
  font-size: $font-size-sm;
  color: $gray-400;

  .link-btn {
    color: $primary-light;
    background: none;
    padding: 0;
    margin-left: $spacing-xs;
    font-size: inherit;

    &:hover {
      color: $primary;
      text-decoration: underline;
    }
  }
}
</style>
