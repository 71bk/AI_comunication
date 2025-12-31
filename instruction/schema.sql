-- ============================================
-- GPT-like 全端聊天網頁 - 資料庫結構 (MySQL 8)
-- 版本：v1.0
-- 日期：2025-12-30
-- ============================================

-- 設定字元集
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- ============================================
-- A 版 (MVP) - 核心資料表
-- ============================================

-- -------------------------------------------
-- 1. 使用者資料表 (users)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `users` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '使用者 ID',
    `email` VARCHAR(255) NOT NULL COMMENT '電子郵件（唯一）',
    `password_hash` VARCHAR(255) NOT NULL COMMENT '密碼雜湊值',
    `display_name` VARCHAR(100) NOT NULL COMMENT '顯示名稱',
    `status` ENUM('ACTIVE', 'INACTIVE', 'BANNED') NOT NULL DEFAULT 'ACTIVE' COMMENT '帳號狀態',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`),
    INDEX `idx_users_status` (`status`),
    INDEX `idx_users_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者資料表';

-- -------------------------------------------
-- 2. 對話資料表 (chats)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `chats` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '對話 ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '使用者 ID',
    `title` VARCHAR(255) DEFAULT NULL COMMENT '對話標題',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (`id`),
    INDEX `idx_chats_user_id` (`user_id`),
    INDEX `idx_chats_updated_at` (`updated_at`),
    CONSTRAINT `fk_chats_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='對話資料表';

-- -------------------------------------------
-- 3. 訊息資料表 (messages)
-- 已預留 B 版 (RAG) 擴充欄位
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `messages` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '訊息 ID',
    `chat_id` BIGINT UNSIGNED NOT NULL COMMENT '對話 ID',
    `role` ENUM('system', 'user', 'assistant') NOT NULL COMMENT '角色類型',
    `content` TEXT NOT NULL COMMENT '訊息內容',
    `provider` VARCHAR(50) DEFAULT NULL COMMENT 'LLM 供應商（如 openai, anthropic）',
    `model` VARCHAR(100) DEFAULT NULL COMMENT '模型名稱（如 gpt-4, claude-3）',
    `token_in` INT UNSIGNED DEFAULT NULL COMMENT '輸入 token 數',
    `token_out` INT UNSIGNED DEFAULT NULL COMMENT '輸出 token 數',
    `metadata_json` JSON DEFAULT NULL COMMENT '擴充資料（B 版 citations、tool output 等）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (`id`),
    INDEX `idx_messages_chat_id` (`chat_id`),
    INDEX `idx_messages_role` (`role`),
    INDEX `idx_messages_created_at` (`created_at`),
    CONSTRAINT `fk_messages_chat_id` FOREIGN KEY (`chat_id`) REFERENCES `chats` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='訊息資料表';

-- ============================================
-- B 版 (RAG) - 擴充資料表
-- 可在升級時再執行以下 SQL
-- ============================================

-- -------------------------------------------
-- 4. 知識庫資料表 (knowledge_bases)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `knowledge_bases` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '知識庫 ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '擁有者 ID',
    `name` VARCHAR(255) NOT NULL COMMENT '知識庫名稱',
    `description` TEXT DEFAULT NULL COMMENT '知識庫描述',
    `status` ENUM('ACTIVE', 'INACTIVE', 'PROCESSING') NOT NULL DEFAULT 'ACTIVE' COMMENT '狀態',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (`id`),
    INDEX `idx_knowledge_bases_user_id` (`user_id`),
    INDEX `idx_knowledge_bases_status` (`status`),
    CONSTRAINT `fk_knowledge_bases_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知識庫資料表';

-- -------------------------------------------
-- 5. 文件資料表 (documents)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `documents` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '文件 ID',
    `knowledge_base_id` BIGINT UNSIGNED NOT NULL COMMENT '知識庫 ID',
    `file_name` VARCHAR(255) NOT NULL COMMENT '原始檔案名稱',
    `file_path` VARCHAR(500) NOT NULL COMMENT '儲存路徑（S3/R2/本機）',
    `file_size` BIGINT UNSIGNED DEFAULT NULL COMMENT '檔案大小（bytes）',
    `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME 類型',
    `status` ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING' COMMENT '處理狀態',
    `chunk_count` INT UNSIGNED DEFAULT 0 COMMENT 'Chunk 數量',
    `metadata_json` JSON DEFAULT NULL COMMENT '擴充資料',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新時間',
    PRIMARY KEY (`id`),
    INDEX `idx_documents_knowledge_base_id` (`knowledge_base_id`),
    INDEX `idx_documents_status` (`status`),
    CONSTRAINT `fk_documents_knowledge_base_id` FOREIGN KEY (`knowledge_base_id`) REFERENCES `knowledge_bases` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件資料表';

-- -------------------------------------------
-- 6. 文件區塊資料表 (document_chunks)
-- -------------------------------------------
CREATE TABLE IF NOT EXISTS `document_chunks` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'Chunk ID',
    `document_id` BIGINT UNSIGNED NOT NULL COMMENT '文件 ID',
    `chunk_index` INT UNSIGNED NOT NULL COMMENT 'Chunk 順序索引',
    `content` TEXT NOT NULL COMMENT 'Chunk 內容',
    `token_count` INT UNSIGNED DEFAULT NULL COMMENT 'Token 數量',
    `page_number` INT UNSIGNED DEFAULT NULL COMMENT '對應頁碼（PDF 適用）',
    `embedding_vector` JSON DEFAULT NULL COMMENT '向量嵌入（若不使用外部向量庫）',
    `metadata_json` JSON DEFAULT NULL COMMENT '擴充資料（段落索引等）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (`id`),
    INDEX `idx_document_chunks_document_id` (`document_id`),
    INDEX `idx_document_chunks_chunk_index` (`chunk_index`),
    CONSTRAINT `fk_document_chunks_document_id` FOREIGN KEY (`document_id`) REFERENCES `documents` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件區塊資料表';

-- ============================================
-- 可選：用量追蹤資料表 (usage_logs)
-- ============================================
CREATE TABLE IF NOT EXISTS `usage_logs` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '記錄 ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '使用者 ID',
    `chat_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '對話 ID',
    `message_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '訊息 ID',
    `provider` VARCHAR(50) NOT NULL COMMENT 'LLM 供應商',
    `model` VARCHAR(100) NOT NULL COMMENT '模型名稱',
    `token_in` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '輸入 token 數',
    `token_out` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '輸出 token 數',
    `cost_usd` DECIMAL(10, 6) DEFAULT NULL COMMENT '預估成本（USD）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '建立時間',
    PRIMARY KEY (`id`),
    INDEX `idx_usage_logs_user_id` (`user_id`),
    INDEX `idx_usage_logs_created_at` (`created_at`),
    CONSTRAINT `fk_usage_logs_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用量追蹤資料表';
