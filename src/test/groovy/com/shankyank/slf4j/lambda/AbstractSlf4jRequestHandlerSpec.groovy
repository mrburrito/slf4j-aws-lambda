package com.shankyank.slf4j.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import spock.lang.Specification

class AbstractSlf4jRequestHandlerSpec extends Specification {
    private Context context
    private LambdaLogger logger

    def 'setup'() {
        context = Mock()
        logger = Mock()
        context.getLogger() >> logger
    }

    def 'injects LambdaLogger into factory when handling request'() {
        given:
        AbstractSlf4jRequestHandler handler = new LogCapturingRequestHandler()

        when: 'request is handled'
        LambdaLogger before = getLogger()
        handler.handleRequest(null, context)
        LambdaLogger after = getLogger()

        then: 'logger is not configured outside of run context, but exists while request is handled'
        !before
        handler.during == logger
        !after
    }

    def 'LambdaLogger is cleared even when exception is thrown'() {
        given:
        AbstractSlf4jRequestHandler handler = new LogCapturingRequestHandler(true)

        when: 'request is handled'
        LambdaLogger before = getLogger()
        handler.handleRequest(null, context)
        LambdaLogger after = getLogger()

        then: 'logger is not configured outside of run context, but exists while request is handled'
        !before
        handler.during == logger
        thrown(RuntimeException)
        !after
    }

    def getLogger = { LambdaLoggerAdapter.@LOGGER.get() }

    class LogCapturingRequestHandler extends AbstractSlf4jRequestHandler {
        LambdaLogger during

        private boolean error

        LogCapturingRequestHandler(boolean err=false) {
            error = err
        }

        @Override
        protected Object doHandle(Object input, Context context) {
            during = getLogger()
            if (error) {
                throw new RuntimeException("Error!")
            }
        }
    }
}
