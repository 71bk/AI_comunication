package tw.bk.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 應用程式自訂屬性
 * 從 application.yml 讀取 app.* 配置
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Llm llm = new Llm();
    private RateLimit rateLimit = new RateLimit();
    private Cors cors = new Cors();

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long expiration = 86400000; // 24 hours
        private long refreshExpiration = 604800000; // 7 days
        private String cookieName = "access_token";
        private String cookiePath = "/";
        private String cookieDomain; // 新增：支援跨子網域
        private String cookieSameSite = "Lax";
        private boolean cookieSecure = false;
        private boolean cookieHttpOnly = true;
    }

    @Getter
    @Setter
    public static class Cors {
        private String[] allowedOrigins = {"http://localhost:5173", "http://localhost:3000"};
    }

    @Getter
    @Setter
    public static class Llm {
        private String provider = "openai";
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-4o-mini";
        private double temperature = 0.7;
        private double topP = 1.0;
        private String reasoningEffort = "medium";
        private Integer maxCompletionTokens;
        private int maxTokens = 4096;
    }

    @Getter
    @Setter
    public static class RateLimit {
        private boolean enabled = true;
        private int maxRequests = 30;
        private int windowSeconds = 60;
    }
}
