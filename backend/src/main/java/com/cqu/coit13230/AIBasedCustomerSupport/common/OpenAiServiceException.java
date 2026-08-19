package com.cqu.coit13230.AIBasedCustomerSupport.common;

public class OpenAiServiceException extends RuntimeException {
    private final int upstreamStatus;

    public OpenAiServiceException(String message) {
        this(message, 0, null);
    }

    public OpenAiServiceException(String message, Throwable cause) {
        this(message, 0, cause);
    }

    public OpenAiServiceException(String message, int upstreamStatus) {
        this(message, upstreamStatus, null);
    }

    private OpenAiServiceException(
            String message,
            int upstreamStatus,
            Throwable cause
    ) {
        super(message, cause);
        this.upstreamStatus = upstreamStatus;
    }

    public int getUpstreamStatus() {
        return upstreamStatus;
    }
}
