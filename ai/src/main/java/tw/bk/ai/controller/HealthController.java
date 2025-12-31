package tw.bk.ai.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tw.bk.ai.result.Result;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康檢查控制器
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 健康檢查端點
     * GET /api/health
     */
    @GetMapping
    public ResponseEntity<Result<Map<String, Object>>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", OffsetDateTime.now());

        return ResponseEntity.ok(Result.ok(data));
    }

    /**
     * 簡易 ping 端點
     * GET /api/health/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
