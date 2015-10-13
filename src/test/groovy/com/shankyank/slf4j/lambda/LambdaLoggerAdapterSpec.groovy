package com.shankyank.slf4j.lambda

import static com.shankyank.slf4j.lambda.LambdaLevel.*

import com.amazonaws.services.lambda.runtime.LambdaLogger
import spock.lang.Specification

class LambdaLoggerAdapterSpec extends Specification {
    private static final String LOG_NAME = 'test.log'

    private LambdaLogger lambdaLogger

    def 'setup'() {
        lambdaLogger = Mock()
        LambdaLoggerAdapter.lambdaLogger = lambdaLogger
    }

    def 'cleanup'() {
        LambdaLoggerAdapter.clearLambdaLogger()
    }

    def 'logs messages at or above TRACE when level is TRACE'() {
        given:
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, TRACE)
        String msg = 'this is a test'

        when:
        logger.trace(msg)
        logger.debug(msg)
        logger.info(msg)
        logger.warn(msg)
        logger.error(msg)

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, TRACE, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, DEBUG, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, msg) })
    }

    def 'logs messages at or above DEBUG when level is DEBUG'() {
        given:
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, DEBUG)
        String msg = 'this is a test'

        when:
        logger.trace(msg)
        logger.debug(msg)
        logger.info(msg)
        logger.warn(msg)
        logger.error(msg)

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, DEBUG, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, msg) })
        0*_
    }

    def 'logs messages at or above INFO when level is INFO'() {
        given:
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, INFO)
        String msg = 'this is a test'

        when:
        logger.trace(msg)
        logger.debug(msg)
        logger.info(msg)
        logger.warn(msg)
        logger.error(msg)

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, msg) })
        0*_
    }

    def 'logs messages at or above WARN when level is WARN'() {
        given:
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, WARN)
        String msg = 'this is a test'

        when:
        logger.trace(msg)
        logger.debug(msg)
        logger.info(msg)
        logger.warn(msg)
        logger.error(msg)

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, msg) })
        0*_
    }

    def 'logs messages at or above ERROR when level is ERROR'() {
        given:
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, ERROR)
        String msg = 'this is a test'

        when:
        logger.trace(msg)
        logger.debug(msg)
        logger.info(msg)
        logger.warn(msg)
        logger.error(msg)

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, msg) })
        0*_
    }

    def 'does not log any messages when level is null (OFF)'() {
        given:
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, null)
        String msg = 'this is a test'

        when:
        logger.trace(msg)
        logger.debug(msg)
        logger.info(msg)
        logger.warn(msg)
        logger.error(msg)

        then:
        0*lambdaLogger.log(_)
    }

    def 'is(.*)Enabled works for all configured levels'() {
        expect:
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, level)
        logger.isTraceEnabled() == trace
        logger.isDebugEnabled() == debug
        logger.isInfoEnabled() == info
        logger.isWarnEnabled() == warn
        logger.isErrorEnabled() == error

        where:
        level || trace | debug | info  | warn  | error
        TRACE || true  | true  | true  | true  | true
        DEBUG || false | true  | true  | true  | true
        INFO  || false | false | true  | true  | true
        WARN  || false | false | false | true  | true
        ERROR || false | false | false | false | true
        null  || false | false | false | false | false
    }

    def 'caches log messages when no LambdaLogger is configured'() {
        given:
        String msg = 'This is a test'
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, INFO)
        LambdaLoggerAdapter.clearLambdaLogger()

        when: 'no LambdaLogger is configured'
        logger.info(msg)
        logger.debug(msg)
        logger.error(msg)
        logger.warn(msg)
        logger.trace(msg)

        then: 'no calls to log are made'
        0*lambdaLogger.log(_)

        when: 'LambdaLogger is assigned'
        LambdaLoggerAdapter.lambdaLogger = lambdaLogger

        then: 'queued messages are posted in the order they were received'
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, msg) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, msg) })
    }

    def 'logs exceptions with a stack trace'() {
        given:
        String msg = 'An error occurred'
        Exception ex = new RuntimeException('Test Exception')
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, TRACE)

        when: 'exception is logged'
        logger.trace(msg, ex)
        logger.debug(msg, ex)
        logger.info(msg, ex)
        logger.warn(msg, ex)
        logger.error(msg, ex)

        then: 'stack trace found in message'
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, TRACE, msg, ex) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, DEBUG, msg, ex) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, msg, ex) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, msg, ex) })

        then:
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, msg, ex) })
    }

    def 'log messages formatted properly with 1 argument'() {
        given:
        String fmt = 'Argument {} Substitution'
        int arg = 42
        String expected = "Argument ${arg} Substitution"
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, TRACE)

        when: 'single argument format provided'
        logger.trace(fmt, arg)
        logger.debug(fmt, arg)
        logger.info(fmt, arg)
        logger.warn(fmt, arg)
        logger.error(fmt, arg)

        then: 'single argument format applied'
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, TRACE, expected) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, DEBUG, expected) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, expected) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, expected) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, expected) })
    }

    def 'log messages formatted properly with 2 arguments'() {
        given:
        String fmt = 'Argument {} Substitution {}'
        String arg1 = 'answer'
        def arg2 = 42
        String expectedOneArg = "Argument ${arg1} Substitution {}"
        String expectedTwoArg = "Argument ${arg1} Substitution ${arg2}"
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, TRACE)

        when: 'two argument format provided'
        logger.trace(fmt, arg1, arg2)
        logger.debug(fmt, arg1, arg2)
        logger.info(fmt, arg1, arg2)
        logger.warn(fmt, arg1, arg2)
        logger.error(fmt, arg1, arg2)

        then: 'two argument format applied'
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, TRACE, expectedTwoArg) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, DEBUG, expectedTwoArg) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, expectedTwoArg) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, expectedTwoArg) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, expectedTwoArg) })

        when: 'last argument is an exception'
        arg2 = new RuntimeException("Test Error")
        logger.trace(fmt, arg1, arg2)
        logger.debug(fmt, arg1, arg2)
        logger.info(fmt, arg1, arg2)
        logger.warn(fmt, arg1, arg2)
        logger.error(fmt, arg1, arg2)

        then: 'single argument is formatted and stack trace is printed'
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, TRACE, expectedOneArg, arg2) && !it.contains(expectedTwoArg) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, DEBUG, expectedOneArg, arg2) && !it.contains(expectedTwoArg) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, expectedOneArg, arg2) && !it.contains(expectedTwoArg) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, expectedOneArg, arg2) && !it.contains(expectedTwoArg) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, expectedOneArg, arg2) && !it.contains(expectedTwoArg) })
    }

    def 'log messages formatted properly with >2 arguments'() {
        given:
        String fmt = 'I have {} variable arguments of types {}, {}, {}.'
        def arg1 = 3
        def arg2 = 'String'
        def arg3 = 'int'
        def arg4 = 'RuntimeException'
        Throwable err = new RuntimeException("Test Error")
        String expected = "I have ${arg1} variable arguments of types ${arg2}, ${arg3}, ${arg4}"
        LambdaLoggerAdapter logger = new LambdaLoggerAdapter(LOG_NAME, TRACE)

        when: 'more than 2 arg format is provided'
        logger.trace(fmt, arg1, arg2, arg3, arg4)
        logger.debug(fmt, arg1, arg2, arg3, arg4)
        logger.info(fmt, arg1, arg2, arg3, arg4)
        logger.warn(fmt, arg1, arg2, arg3, arg4)
        logger.error(fmt, arg1, arg2, arg3, arg4)

        then: 'correct format applied'
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, TRACE, expected) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, DEBUG, expected) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, expected) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, expected) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, expected) })

        when: 'more than 2 arg format is provided with exception'
        logger.trace(fmt, arg1, arg2, arg3, arg4, err)
        logger.debug(fmt, arg1, arg2, arg3, arg4, err)
        logger.info(fmt, arg1, arg2, arg3, arg4, err)
        logger.warn(fmt, arg1, arg2, arg3, arg4, err)
        logger.error(fmt, arg1, arg2, arg3, arg4, err)

        then: 'correct format applied and stack trace is printed'
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, TRACE, expected, err) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, DEBUG, expected, err) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, INFO, expected, err) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, WARN, expected, err) })
        1*lambdaLogger.log({ logContainsAllInfo(it, LOG_NAME, ERROR, expected, err) })
    }

    private boolean logContainsAllInfo(String loggedMsg, String logName, LambdaLevel level,
                                       String msg, Throwable ex=null) {
        String trace = ex?.with {
            StringWriter sw = new StringWriter()
            printStackTrace(new PrintWriter(sw))
            sw.toString()
        }
        loggedMsg?.with {
            contains(logName) &&
                    contains(level.toString()) &&
                    contains(msg) &&
                    (!trace || contains(trace))
        }
    }
}
