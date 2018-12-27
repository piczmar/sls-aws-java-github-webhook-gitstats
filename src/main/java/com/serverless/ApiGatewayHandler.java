package com.serverless;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;

public class ApiGatewayHandler implements RequestStreamHandler {

    private static final Logger LOG = Logger.getLogger(ApiGatewayHandler.class);

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LOG.info("Invoked function");
    }
}
