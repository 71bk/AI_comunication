package tw.bk.ai.service.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tw.bk.ai.config.AppProperties;
import tw.bk.ai.exception.RateLimitException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory rate limiter.
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final AppProperties appProperties;
    private final Map<Long, Counter> counters = new ConcurrentHashMap<>();

    public void check(Long userId) {
        AppProperties.RateLimit config = appProperties.getRateLimit();
        if (!config.isEnabled()) {
            return;
        }

        long now = System.currentTimeMillis();
        Counter counter = counters.computeIfAbsent(userId, id -> new Counter(now));

        synchronized (counter) {
            if (now - counter.windowStart >= config.getWindowSeconds() * 1000L) {
                counter.windowStart = now;
                counter.count = 0;
            }
            counter.count++;
            if (counter.count > config.getMaxRequests()) {
                throw new RateLimitException();
            }
        }
    }

    private static class Counter {
        private long windowStart;
        private int count;

        private Counter(long windowStart) {
            this.windowStart = windowStart;
        }
    }
}
