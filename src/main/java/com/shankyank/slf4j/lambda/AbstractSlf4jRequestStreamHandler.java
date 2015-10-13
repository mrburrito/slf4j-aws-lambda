package com.shankyank.slf4j.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A base implementation of RequestStreamHandler that injects the LambdaLogger
 * found in the request Context into the Slf4j adapter.
 */
public abstract class AbstractSlf4jRequestStreamHandler implements RequestStreamHandler {
    @Override
    public void handleRequest(final InputStream input, final OutputStream output, final Context context)
            throws IOException
    {
        LambdaLoggerAdapter.setLambdaLogger(context.getLogger());
        try {
            doHandle(input, output, context);
        } finally {
            LambdaLoggerAdapter.clearLambdaLogger();
        }
    }

    /**
     * Handle the request, reading parameters from the input stream and writing
     * any output to the output stream.
     * @param input the input stream
     * @param output the output stream
     * @param context the Lambda function context
     * @throws IOException if an error occurs during processing
     */
    protected abstract void doHandle(final InputStream input, final OutputStream output, final Context context)
            throws IOException;
}
