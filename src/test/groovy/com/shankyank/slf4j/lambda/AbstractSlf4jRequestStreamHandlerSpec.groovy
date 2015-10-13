package com.shankyank.slf4j.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import spock.lang.Specification

class AbstractSlf4jRequestStreamHandlerSpec extends Specification {
    private Context context
    private LambdaLogger logger

    def 'setup'() {
        context = Mock()
        logger = Mock()
        context.getLogger() >> logger
    }

    def 'injects LambdaLogger into factory when handling request'() {
        given:
        AbstractSlf4jRequestStreamHandler handler = new LogCapturingRequestStreamHandler()

        when: 'request is handled'
        LambdaLogger before = getLogger()
        handler.handleRequest(null, null, context)
        LambdaLogger after = getLogger()

        then: 'logger is not configured outside of run context, but exists while request is handled'
        !before
        handler.during == logger
        !after
    }

    def 'logger is cleared even when exception is thrown'() {
        given:
        AbstractSlf4jRequestStreamHandler handler = new LogCapturingRequestStreamHandler(true)

        when: 'request is handled'
        LambdaLogger before = getLogger()
        handler.handleRequest(null, null, context)
        LambdaLogger after = getLogger()

        then: 'logger is not configured outside of run context, but exists while request is handled'
        !before
        handler.during == logger
        thrown(IOException)
        !after
    }

    def getLogger = { LambdaLoggerAdapter.@LOGGER.get() }

    class LogCapturingRequestStreamHandler extends AbstractSlf4jRequestStreamHandler {
        LambdaLogger during

        private boolean error

        LogCapturingRequestStreamHandler(boolean err=false) {
            error = err
        }

        @Override
        protected void doHandle(InputStream input, OutputStream output, Context context) throws IOException {
            during = getLogger()
            if (error) {
                throw new IOException("Error!")
            }
        }
    }
}
