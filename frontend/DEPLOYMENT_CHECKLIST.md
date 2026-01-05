# 前端上線部署檢查清單

## 上線前準備

### 1. 環境配置
- [x] 創建 `.env.production` 文件
- [ ] 修改 `VITE_API_BASE_URL` 為實際生產後端地址 (例: `https://api.yourdomain.com`)
- [ ] 修改 HTML 中的 OG URL 為實際域名

### 2. 構建驗證
```bash
# 安裝依賴
npm install

# 開發環境測試
npm run dev

# 生產構建
npm run build

# 預覽構建結果
npm run preview
```

### 3. 構建輸出檢查
- [ ] 檢查 `dist/` 目錄是否存在
- [ ] 確認 JS 文件已壓縮（console 已移除）
- [ ] 驗證代碼分割是否正確（`vue-core.js`, `ui-lib.js` 等）
- [ ] 檢查 source map 已禁用（`.map` 文件不應存在）

### 4. Cloudflare Workers 部署
```bash
# 登入 Cloudflare
npx wrangler login

# 部署
npx wrangler deploy
```

部署前修改 `wrangler.jsonc`：
- [ ] 將 `yourdomain.com` 替換為實際域名
- [ ] 確認 `env.production.vars.API_BASE_URL` 指向正確的後端

### 5. 域名 CORS 配置

確保後端 SecurityConfig.java 中的 CORS 配置包含生產域名：
```java
config.setAllowedOrigins(List.of(
    "https://yourdomain.com",
    "https://www.yourdomain.com"
));
```

### 6. 瀏覽器測試
- [ ] 訪問 https://yourdomain.com
- [ ] 測試登入功能
- [ ] 測試聊天功能
- [ ] 檢查瀏覽器控制台 (F12) 無錯誤
- [ ] 驗證 Cookie 已設置（Application > Cookies）
- [ ] 檢查 Network 請求正確路由到後端

### 7. 性能檢查
- [ ] 使用 Lighthouse 檢查頁面評分
- [ ] 檢查首屏加載時間（<3s）
- [ ] 驗證資源文件已使用 CDN 緩存

### 8. 安全檢查
- [ ] 確認使用 HTTPS
- [ ] 驗證安全頭部已正確設置
  - `X-Content-Type-Options: nosniff`
  - `X-Frame-Options: DENY`
  - `Content-Security-Policy` 已配置
- [ ] 確認無敏感信息在瀏覽器存儲
- [ ] 驗證 API 調用使用 HTTPS

### 9. 監控和日誌
- [ ] 配置 Cloudflare Analytics
- [ ] 設置錯誤告警
- [ ] 檢查日誌是否正確記錄

## 部署後驗證

- [ ] 前端可訪問
- [ ] 後端 API 可調用
- [ ] 認證流程正常
- [ ] 聊天功能正常
- [ ] 無 CORS 錯誤
- [ ] 無控制台錯誤

## 常見問題排查

### CORS 錯誤
- 檢查後端 `SecurityConfig.java` 的 `allowedOrigins`
- 確認前端域名已添加到允許列表

### API 調用失敗
- 確認 `VITE_API_BASE_URL` 正確
- 檢查後端是否在線
- 驗證網絡連接

### Cookie 未設置
- 檢查後端是否正確設置 Cookie
- 確認前端 `withCredentials: true`
- 驗證 `cookie-secure` 和 `cookie-same-site` 設置

---

完成所有檢查後，您的前端就可以上線了！
