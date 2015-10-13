package com.shankyank.slf4j.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * A base implementation of RequestHandler that injects the LambdaLogger
 * found in the request Context into the Slf4j adapter.
 * @param <I> the input object type
 * @param <O> the output object type
 */
public abstract class AbstractSlf4jRequestHandler<I, O> implements RequestHandler<I, O> {
    @Override
    public final O handleRequest(final I input, final Context context) {
        LambdaLoggerAdapter.setLambdaLogger(context.getLogger());
        try {
            return doHandle(input, context);
        } finally {
            LambdaLoggerAdapter.clearLambdaLogger();
        }
    }

    /**
     * Handle the request, returning the appropriate response based on the input
     * parameters.
     * @param input the input parameters
     * @param context the Lambda context
     * @return the response
     */
    protected abstract O doHandle(final I input, final Context context);
}
