package auth.service.xflow_auth_service.utils.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AnonymousRateLimiter {

    private final RedisTemplate<String, Integer> redisTemplate;
    private static final String REDIS_PREFIX = "rate_limit:anon:";

    @Value("${jwt.anon-max-attempts}")
    private int maxAttempts;

    @Value("${jwt.anon-lockout-duration-ms}")
    private int lockoutDurationMs;

    public boolean isAllowed(String fingerprint) {
        Integer attempts = redisTemplate.opsForValue().get(REDIS_PREFIX + fingerprint);
        return attempts == null || attempts < maxAttempts;
    }

    public void recordAttempt(String fingerprint) {
        String key = REDIS_PREFIX + fingerprint;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, lockoutDurationMs, TimeUnit.MILLISECONDS);
        }
    }
}