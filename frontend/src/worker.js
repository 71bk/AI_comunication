/**
 * Cloudflare Workers - 安全頭部設置
 * 將此內容部署到 Cloudflare Workers，或在服務器配置中設置這些頭部
 */

export default {
  async fetch(request, env) {
    const response = await env.ASSETS.fetch(request);
    
    // 複製原始響應並添加安全頭部
    const newResponse = new Response(response.body, response);
    
    // Content Security Policy
    newResponse.headers.set(
      'Content-Security-Policy',
      "default-src 'self'; " +
      "script-src 'self' 'unsafe-inline' 'unsafe-eval' cdn.jsdelivr.net; " +
      "style-src 'self' 'unsafe-inline' cdn.jsdelivr.net fonts.googleapis.com; " +
      "font-src 'self' fonts.gstatic.com; " +
      "img-src 'self' data: https:; " +
      "connect-src 'self' https://api.yourdomain.com; " +
      "frame-ancestors 'none'; " +
      "base-uri 'self'; " +
      "form-action 'self';"
    );
    
    // Prevent MIME type sniffing
    newResponse.headers.set('X-Content-Type-Options', 'nosniff');
    
    // Prevent clickjacking
    newResponse.headers.set('X-Frame-Options', 'DENY');
    
    // Enable XSS protection
    newResponse.headers.set('X-XSS-Protection', '1; mode=block');
    
    // Referrer policy
    newResponse.headers.set('Referrer-Policy', 'strict-origin-when-cross-origin');
    
    // Permissions policy (formerly Feature-Policy)
    newResponse.headers.set(
      'Permissions-Policy',
      'accelerometer=(), camera=(), microphone=(), geolocation=(), magnetometer=(), payment=(), usb=()'
    );
    
    // CORS header (if needed)
    // newResponse.headers.set('Access-Control-Allow-Origin', 'https://api.yourdomain.com');
    
    // Cache control for static assets
    if (request.url.includes('/dist/')) {
      newResponse.headers.set('Cache-Control', 'public, max-age=31536000, immutable');
    } else {
      newResponse.headers.set('Cache-Control', 'public, max-age=3600, must-revalidate');
    }
    
    return newResponse;
  },
};
