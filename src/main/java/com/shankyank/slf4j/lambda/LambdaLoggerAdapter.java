package com.shankyank.slf4j.lambda;

import static com.shankyank.slf4j.lambda.LambdaLevel.*;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * A wrapper around {@link com.amazonaws.services.lambda.runtime.LambdaLogger}
 * in conforming to the {@link org.slf4j.Logger} interface.
 *
 * @author Gordon Shankman
 */
public class LambdaLoggerAdapter extends MarkerIgnoringBase implements Logger {
    /**
     * The name of this logger.
     */
    private final String name;

    /**
     * The wrapped logger.
     */
    private final LambdaLogger logger;

    /**
     * The level of this logger.
     */
    private final LambdaLevel level;

    /**
     * Adapter constructor limited to package scope so only LambdaLoggerFactory
     * can create one.
     *
     * @param _name   the name of this logger
     * @param _level  the level of this logger
     * @param _logger the logger to wrap
     */
    LambdaLoggerAdapter(final String _name, final LambdaLevel _level, final LambdaLogger _logger) {
        name = _name;
        level = _level;
        logger = _logger;
    }

    @Override
    public boolean isTraceEnabled() {
        return level != null && level.isLoggingEnabled(TRACE);
    }

    @Override
    public void trace(String s) {
    }

    @Override
    public void trace(String s, Object o) {
    }

    @Override
    public void trace(String s, Object o, Object o1) {
    }

    @Override
    public void trace(String s, Object... objects) {
    }

    @Override
    public void trace(String s, Throwable throwable) {
    }

    @Override
    public boolean isDebugEnabled() {
        return level != null && level.isLoggingEnabled(DEBUG);
    }

    @Override
    public void debug(String s) {

    }

    @Override
    public void debug(String s, Object o) {

    }

    @Override
    public void debug(String s, Object o, Object o1) {

    }

    @Override
    public void debug(String s, Object... objects) {

    }

    @Override
    public void debug(String s, Throwable throwable) {

    }

    @Override
    public boolean isInfoEnabled() {
        return level != null && level.isLoggingEnabled(INFO);
    }

    @Override
    public void info(String s) {

    }

    @Override
    public void info(String s, Object o) {

    }

    @Override
    public void info(String s, Object o, Object o1) {

    }

    @Override
    public void info(String s, Object... objects) {

    }

    @Override
    public void info(String s, Throwable throwable) {

    }

    @Override
    public boolean isWarnEnabled() {
        return level != null && level.isLoggingEnabled(WARN);
    }

    @Override
    public void warn(String s) {

    }

    @Override
    public void warn(String s, Object o) {

    }

    @Override
    public void warn(String s, Object... objects) {

    }

    @Override
    public void warn(String s, Object o, Object o1) {

    }

    @Override
    public void warn(String s, Throwable throwable) {

    }

    @Override
    public boolean isErrorEnabled() {
        return level != null && level.isLoggingEnabled(ERROR);
    }

    @Override
    public void error(String s) {

    }

    @Override
    public void error(String s, Object o) {

    }

    @Override
    public void error(String s, Object o, Object o1) {

    }

    @Override
    public void error(String s, Object... objects) {

    }

    @Override
    public void error(String s, Throwable throwable) {

    }
}
