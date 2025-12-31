import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

// 配置 markdown-it
const md = new MarkdownIt({
    html: false,
    linkify: true,
    typographer: true,
    highlight: (str, lang) => {
        if (lang && hljs.getLanguage(lang)) {
            try {
                const highlighted = hljs.highlight(str, { language: lang, ignoreIllegals: true }).value
                return `<pre class="hljs"><code class="language-${lang}">${highlighted}</code></pre>`
            } catch {
                // 忽略錯誤
            }
        }
        // 無法識別的語言，使用純文字
        const escaped = md.utils.escapeHtml(str)
        return `<pre class="hljs"><code>${escaped}</code></pre>`
    },
})

/**
 * 將 Markdown 轉換為 HTML
 */
export function renderMarkdown(text: string): string {
    return md.render(text)
}

/**
 * 將 Markdown 轉換為內聯 HTML（不包含段落標籤）
 */
export function renderMarkdownInline(text: string): string {
    return md.renderInline(text)
}

export default md
