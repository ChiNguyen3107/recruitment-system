package com.recruitment.system.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cấu hình Rate Limiting để chống brute force attacks
 */
@Configuration
@Slf4j
public class RateLimitConfig {

    @Value("${security.rate-limit.login.requests:5}")
    private int loginRequests;

    @Value("${security.rate-limit.login.window:300}")
    private int loginWindow;

    @Value("${security.rate-limit.register.requests:3}")
    private int registerRequests;

    @Value("${security.rate-limit.register.window:3600}")
    private int registerWindow;

    @Value("${security.rate-limit.refresh.requests:10}")
    private int refreshRequests;

    @Value("${security.rate-limit.refresh.window:300}")
    private int refreshWindow;

    @Bean
    public Map<String, Bucket> loginBuckets() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<String, Bucket> registerBuckets() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Map<String, Bucket> refreshBuckets() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Tạo bucket cho rate limiting login
     */
    public Bucket createLoginBucket(String key) {
        Bandwidth limit = Bandwidth.classic(loginRequests, Refill.intervally(loginRequests, Duration.ofSeconds(loginWindow)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Tạo bucket cho rate limiting register
     */
    public Bucket createRegisterBucket(String key) {
        Bandwidth limit = Bandwidth.classic(registerRequests, Refill.intervally(registerRequests, Duration.ofSeconds(registerWindow)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Tạo bucket cho rate limiting refresh token
     */
    public Bucket createRefreshBucket(String key) {
        Bandwidth limit = Bandwidth.classic(refreshRequests, Refill.intervally(refreshRequests, Duration.ofSeconds(refreshWindow)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Kiểm tra và tạo bucket cho login
     */
    public boolean tryConsumeLogin(String key) {
        Bucket bucket = loginBuckets().computeIfAbsent(key, this::createLoginBucket);
        return bucket.tryConsume(1);
    }

    /**
     * Kiểm tra và tạo bucket cho register
     */
    public boolean tryConsumeRegister(String key) {
        Bucket bucket = registerBuckets().computeIfAbsent(key, this::createRegisterBucket);
        return bucket.tryConsume(1);
    }

    /**
     * Kiểm tra và tạo bucket cho refresh token
     */
    public boolean tryConsumeRefresh(String key) {
        Bucket bucket = refreshBuckets().computeIfAbsent(key, this::createRefreshBucket);
        return bucket.tryConsume(1);
    }

    /**
     * Lấy thời gian còn lại trước khi có thể thử lại
     */
    public long getWaitTimeLogin(String key) {
        Bucket bucket = loginBuckets().get(key);
        if (bucket != null) {
            return bucket.getAvailableTokens();
        }
        return 0;
    }

    /**
     * Lấy thời gian còn lại trước khi có thể thử lại register
     */
    public long getWaitTimeRegister(String key) {
        Bucket bucket = registerBuckets().get(key);
        if (bucket != null) {
            return bucket.getAvailableTokens();
        }
        return 0;
    }

    /**
     * Lấy thời gian còn lại trước khi có thể thử lại refresh token
     */
    public long getWaitTimeRefresh(String key) {
        Bucket bucket = refreshBuckets().get(key);
        if (bucket != null) {
            return bucket.getAvailableTokens();
        }
        return 0;
    }
}








