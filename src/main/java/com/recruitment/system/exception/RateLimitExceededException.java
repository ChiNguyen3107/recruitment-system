package com.recruitment.system.exception;

/**
 * Exception được ném khi vượt quá giới hạn rate limit
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final long waitTime;
    private final String operation;
    
    public RateLimitExceededException(String operation, long waitTime) {
        super(String.format("Rate limit exceeded for %s. Please try again in %d seconds.", operation, waitTime));
        this.operation = operation;
        this.waitTime = waitTime;
    }
    
    public long getWaitTime() {
        return waitTime;
    }
    
    public String getOperation() {
        return operation;
    }
}













