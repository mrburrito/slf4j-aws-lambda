package com.shankyank.slf4j.lambda;

/**
 * Log levels for LambdaLoggers. The LambdaLogger does not natively
 * support level distinctions so we need to manage these internally.
 */
public enum LambdaLevel {
    TRACE,
    DEBUG,
    INFO,
    WARN,
    ERROR;

    /**
     * Indicates if logging is enabled for the provided level.
     * @param level the target level
     * @return true if logging is enabled
     */
    public boolean isLoggingEnabled(final LambdaLevel level) {
        return level != null && this.compareTo(level) <= 0;
    }
}
