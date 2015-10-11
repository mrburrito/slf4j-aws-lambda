package com.shankyank.slf4j.lambda;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory that reads log levels from the classpath and creates
 * LambdaLoggerAdapters for the requested log name. This factory
 * does not cache Loggers and will create a new Logger for each
 * request.
 *
 * Log levels are configured via the lambdalogger.properties file
 * at the root of the classpath. If this file is not found, logging
 * will be disabled. The lambdalogger.properties supports log level
 * configuration similar to log4j 1.2. The following properties may
 * be provided:
 *
 * <table>
 *     <thead>
 *         <tr>
 *             <th>property</th>
 *             <th>value</th>
 *             <th>description</th>
 *             <th>default</th>
 *         </tr>
 *     </thead>
 *     <tbody>
 *         <tr>
 *             <td>lambda.rootLogger</td>
 *             <td><code>TRACE|DEBUG|INFO|WARN|ERROR|OFF</code></td>
 *             <td>
 *                 The base log level of the system. Loggers not explicitly
 *                 defined in the lambdalogger.properties file will inherit
 *                 this level.
 *             </td>
 *             <th><code>OFF</code></th>
 *         </tr>
 *         <tr>
 *             <td>lambda.logger.FOO</td>
 *             <td><code>TRACE|DEBUG|INFO|WARN|ERROR|OFF</code></td>
 *             <td>
 *                 The log level for the logger named <code>FOO</code>. Any loggers
 *                 falling under <code>FOO</code> in the hierarchy (e.g.
 *                 <code>FOO.BAR</code>) will inherit this level if they
 *                 are not explicitly defined.s
 *             </td>
 *             <td><code>${lambda.rootLogger}</code></td>
 *         </tr>
 *     </tbody>
 * </table>
 */
public class LambdaLoggerFactory implements ILoggerFactory {
    /** The path to the configuration file. */
    public static final String LAMBDA_LOGGER_PROPERTIES = "lambdalogger.properties";
    /** The property key for configuring the root log level. */
    public static final String ROOT_LOGGER_KEY = "lambda.rootLogger";
    /** The property key prefix for configuring log levels for a hierarchical logger. */
    public static final String LOGGER_PREFIX = "lambda.logger.";
    /** The placeholder string to indicate logging should be turned OFF. */
    public static final String LEVEL_OFF = "OFF";

    /** The pattern used to match and extract the names of hierarchical loggers. */
    private static final Pattern LOGGER_CONFIG_PTN =
            Pattern.compile("^lambda\\.logger\\.([a-zA-Z]\\w*(?:\\.[a-zA-Z]\\w*)*)$");

    /** The root log level. */
    private final LambdaLevel rootLevel;
    /** The map of logger names to log levels. */
    private final Map<String, LambdaLevel> logLevels;

    /**
     * Initializes the factory, reading log levels from the classpath.
     */
    public LambdaLoggerFactory() {
        LambdaLevel root = null;
        Map<String, LambdaLevel> levelMap = new HashMap<>();
        try {
            InputStream is = LambdaLoggerFactory.class.getClassLoader().getResourceAsStream(LAMBDA_LOGGER_PROPERTIES);
            Properties props = new Properties();
            props.load(is);
            for (String key : props.stringPropertyNames()) {
                String levelStr = props.getProperty(key, LEVEL_OFF).toUpperCase().trim();
                LambdaLevel level;
                if (LEVEL_OFF.equalsIgnoreCase(levelStr)) {
                    level = null;
                } else {
                    level = LambdaLevel.valueOf(levelStr.toUpperCase());
                }
                if (ROOT_LOGGER_KEY.equals(key)) {
                    root = level;
                } else {
                    Matcher matcher = LOGGER_CONFIG_PTN.matcher(levelStr);
                    if (matcher.matches()) {
                        levelMap.put(matcher.group(1), level);
                    }
                }
            }
        } catch (NullPointerException | IllegalArgumentException | IOException ex) {
            // log exceptions to stderr; since we cannot read the log levels, these
            // messages will be picked up by AWS Lambda and written to CloudWatch as
            // a single event per line. This means stack traces will generate multiple
            // events.
            System.err.println(String.format("Unable to load %s. Defaulting to OFF: %s",
                    LAMBDA_LOGGER_PROPERTIES, ex));
            ex.printStackTrace(System.err);
        }

        rootLevel = root;
        levelMap.put(null, rootLevel);
        logLevels = Collections.unmodifiableMap(levelMap);
    }

    @Override
    public Logger getLogger(final String name) {
        return new LambdaLoggerAdapter(name, findLevelForLogger(name));
    }

    /**
     * Walks the hierarchical logger tree, starting at the initial name
     * and stripping off the last part of the path (assuming '.' separation
     * of path components) until a configured log level is found or the
     * name is fully consumed, at which point the root level is used.
     * @param name the logger name
     * @return the configured level for the requested logger
     */
    private LambdaLevel findLevelForLogger(final String name) {
        LambdaLevel level;
        if (logLevels.containsKey(name)) {
            level = logLevels.get(name);
        } else {
            // walk up the logger hierarchy, assuming '.' separation until
            // we find a logger
            int lastDot = name.lastIndexOf('.');
            String parent = lastDot > 0 ? name.substring(0, lastDot) : null;
            level = findLevelForLogger(parent);
        }
        return level;
    }
}
