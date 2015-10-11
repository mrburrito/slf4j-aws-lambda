package com.shankyank.slf4j.lambda;

import static com.shankyank.slf4j.lambda.LambdaLevel.*;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;

/**
 * A wrapper around {@link com.amazonaws.services.lambda.runtime.LambdaLogger}
 * in conforming to the {@link org.slf4j.Logger} interface.
 *
 * Lambda loggers can be configured with an output pattern using the following
 * symbols:
 *
 *
 * @author Gordon Shankman
 */
public class LambdaLoggerAdapter extends MarkerIgnoringBase implements Logger {
    /**
     * There should be a single LambdaLogger per Lambda function execution.
     * Since each Lambda function is single-threaded, we can use a static
     * ThreadLocal to manage the logger, caching any log messages received
     * before the logger is initialized.
     */
    private static final ThreadLocal<LambdaLogger> LOGGER = new ThreadLocal<LambdaLogger>() {
        @Override
        public void set(final LambdaLogger value) {
            super.set(value);
            if (value != null) {
                List<String> queue = LOG_QUEUE.get();
                LOG_QUEUE.remove();
                queue.forEach(msg -> value.log(msg));
            }
        }
    };

    /**
     * The cache of log messages queued up while waiting for the LambdaLogger
     * to be initialized.
     */
    private static final ThreadLocal<List<String>> LOG_QUEUE = new ThreadLocal<List<String>>() {
        @Override
        protected List<String> initialValue() {
            return new LinkedList<>();
        }
    };

    /**
     * Configure the LambdaLogger for the current thread.
     * @param logger the LambdaLogger
     */
    public static void setLambdaLogger(final LambdaLogger logger) {
        if (logger != null) {
            LOGGER.set(logger);
        } else {
            clearLambdaLogger();
        }
    }

    /**
     * Clear the LambdaLogger for the current thread.
     */
    public static void clearLambdaLogger() {
        LOGGER.remove();
    }

    /**
     * The name of this logger.
     */
    private final String name;

    /**
     * The outLevel of this logger.
     */
    private final LambdaLevel outLevel;

    /**
     * Adapter constructor limited to package scope so only LambdaLoggerFactory
     * can create one.
     *
     * @param _name   the name of this logger
     * @param _outLevel  the level of this logger
     */
    LambdaLoggerAdapter(final String _name, final LambdaLevel _outLevel) {
        name = _name;
        outLevel = _outLevel;
    }

    /**
     * @param level the level to query
     * @return true if logging at the indicated level is turned on for this logger
     */
    private boolean isLoggingEnabled(final LambdaLevel level) {
        return outLevel != null && outLevel.isLoggingEnabled(level);
    }

    /**
     * Log a simple message at the specified level.
     * @param level the log level
     * @param msg the message to log
     */
    private void log(final LambdaLevel level, final String msg) {
        log(level, msg, null, null);
    }

    /**
     * Log a formatted message with a single argument.
     * @param level the log level
     * @param format the message format
     * @param arg the argument
     */
    private void log(final LambdaLevel level, final String format, final Object arg) {
        log(level, format, new Object[] { arg }, null);
    }

    /**
     * Log a formatted message with two arguments.
     * @param level the log level
     * @param format the message format
     * @param arg1 the first argument
     * @param arg2 the second argument
     */
    private void log(final LambdaLevel level, final String format, final Object arg1, final Object arg2) {
        log(level, format, new Object[] { arg1, arg2 }, null);
    }

    /**
     * Log a formatted message with a variable number of arguments.
     * @param level the log level
     * @param format the message format
     * @param args the arguments
     */
    private void log(final LambdaLevel level, final String format, final Object... args) {
        log(level, format, args, null);
    }

    /**
     * Writes a message to the log at the specified level. If args are provided, the message
     * is assumed to be a formatted message. If err is provided, the stack trace of the
     * Throwable will be appended to the message following a newline. If no err is provided
     * but the last element of the args array is a Throwable, the Throwable will be treated
     * as though it was the err parameter.
     *
     * All logs are output in the format:
     * [%name] %level  %msg\n%err
     *
     * @param level the level of the message
     * @param format the log message
     * @param args the replacement values for the message if it contains {} placeholders
     * @param err the exception to log with this message
     */
    private void log(final LambdaLevel level, final String format, final Object[] args, Throwable err) {
        if (outLevel != null && outLevel.isLoggingEnabled(level)) {
            // attempt to format the message if args were provided
            String message;
            if (args != null && args.length > 0) {
                // if we have no error and last element of args array is a Throwable,
                // assume it should be logged as a Throwable and still included in
                // the formatting args
                if (err == null && args[args.length - 1] instanceof Throwable) {
                    err = (Throwable) args[args.length - 1];
                }
                message = MessageFormatter.arrayFormat(format, args).getMessage();
            } else {
                message = format;
            }

            // generate stack trace if err was provided
            StringWriter errStr = new StringWriter();
            if (err != null) {
                errStr.append('\n');
                err.printStackTrace(new PrintWriter(errStr));
            }

            // log format: [${logName}] ${LEVEL}  ${message}\n${stackTrace}
            String entry = String.format("[%s] %s  %s%s", name, level, message, errStr.toString());
            LambdaLogger logger = LOGGER.get();
            if (logger != null) {
                logger.log(entry);
            } else {
                LOG_QUEUE.get().add(entry);
            }
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return isLoggingEnabled(TRACE);
    }

    @Override
    public void trace(String msg) {
        log(TRACE, msg);
    }

    @Override
    public void trace(String format, Object arg1) {
        log(TRACE, format, arg1);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        log(TRACE, format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... args) {
        log(TRACE, format, args);
    }

    @Override
    public void trace(String msg, Throwable err) {
        log(TRACE, msg, null, err);
    }

    @Override
    public boolean isDebugEnabled() {
        return isLoggingEnabled(DEBUG);
    }

    @Override
    public void debug(String msg) {
        log(DEBUG, msg);
    }

    @Override
    public void debug(String format, Object arg1) {
        log(DEBUG, format, arg1);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        log(DEBUG, format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... args) {
        log(DEBUG, format, args);
    }

    @Override
    public void debug(String msg, Throwable err) {
        log(DEBUG, msg, null, err);
    }

    @Override
    public boolean isInfoEnabled() {
        return isLoggingEnabled(INFO);
    }

    @Override
    public void info(String msg) {
        log(INFO, msg);
    }

    @Override
    public void info(String format, Object arg1) {
        log(INFO, format, arg1);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        log(INFO, format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... args) {
        log(INFO, format, args);
    }

    @Override
    public void info(String msg, Throwable err) {
        log(INFO, msg, null, err);
    }

    @Override
    public boolean isWarnEnabled() {
        return isLoggingEnabled(WARN);
    }

    @Override
    public void warn(String msg) {
        log(WARN, msg);
    }

    @Override
    public void warn(String format, Object arg1) {
        log(WARN, format, arg1);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        log(WARN, format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... args) {
        log(WARN, format, args);
    }

    @Override
    public void warn(String msg, Throwable err) {
        log(WARN, msg, null, err);
    }

    @Override
    public boolean isErrorEnabled() {
        return isLoggingEnabled(ERROR);
    }

    @Override
    public void error(String msg) {
        log(ERROR, msg);
    }

    @Override
    public void error(String format, Object arg1) {
        log(ERROR, format, arg1);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        log(ERROR, format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... args) {
        log(ERROR, format, args);
    }

    @Override
    public void error(String msg, Throwable err) {
        log(ERROR, msg, null, err);
    }
}
