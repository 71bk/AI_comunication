package tw.bk.ai.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseCookie;
import tw.bk.ai.dto.auth.AuthMeResp;
import tw.bk.ai.dto.auth.LoginReq;
import tw.bk.ai.dto.auth.RegisterReq;
import tw.bk.ai.result.Result;
import tw.bk.ai.security.JwtUserPrincipal;
import tw.bk.ai.service.auth.AuthService;
import tw.bk.ai.config.AppProperties;

import java.time.Duration;

/**
 * 認證控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppProperties appProperties;

    /**
     * 使用者註冊
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<Result<AuthMeResp>> register(@Valid @RequestBody RegisterReq req) {
        AuthMeResp resp = authService.register(req);
        ResponseCookie cookie = buildAccessTokenCookie(resp.getToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Result.ok(resp));
    }

    /**
     * 使用者登入
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<Result<AuthMeResp>> login(@Valid @RequestBody LoginReq req) {
        AuthMeResp resp = authService.login(req);
        ResponseCookie cookie = buildAccessTokenCookie(resp.getToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Result.ok(resp));
    }

    /**
     * 登出（前端清除 token 即可，此端點僅供記錄）
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout(@AuthenticationPrincipal JwtUserPrincipal principal) {
        // 無狀態 JWT 不需要伺服器端處理，前端清除 token 即可
        // 若需要黑名單機制，可在此實作
        ResponseCookie cookie = clearAccessTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Result.ok());
    }

    /**
     * 取得當前使用者資訊
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<Result<AuthMeResp>> getCurrentUser(@AuthenticationPrincipal JwtUserPrincipal principal) {
        AuthMeResp resp = authService.getCurrentUser(principal.getId());
        return ResponseEntity.ok(Result.ok(resp));
    }

    private ResponseCookie buildAccessTokenCookie(String token) {
        AppProperties.Jwt jwt = appProperties.getJwt();
        return ResponseCookie.from(jwt.getCookieName(), token)
                .httpOnly(jwt.isCookieHttpOnly())
                .secure(jwt.isCookieSecure())
                .sameSite(jwt.getCookieSameSite())
                .path(jwt.getCookiePath())
                .maxAge(Duration.ofMillis(jwt.getExpiration()))
                .build();
    }

    private ResponseCookie clearAccessTokenCookie() {
        AppProperties.Jwt jwt = appProperties.getJwt();
        return ResponseCookie.from(jwt.getCookieName(), "")
                .httpOnly(jwt.isCookieHttpOnly())
                .secure(jwt.isCookieSecure())
                .sameSite(jwt.getCookieSameSite())
                .path(jwt.getCookiePath())
                .maxAge(Duration.ZERO)
                .build();
    }
}
