package com.serverless;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import org.apache.log4j.Logger;

import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ApiGatewayHandler implements RequestStreamHandler {

    private static final String FUNCTION_NAME = System.getenv("ENTRY_FUNCTION_NAME");
    private static final Logger LOG = Logger.getLogger(ApiGatewayHandler.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {

        try {
            String payload = IOUtils.toString(inputStream);
            LOG.info("received: " + payload);
            LOG.info("Invoking function");

            AWSLambdaClient lambdaClient = new AWSLambdaClient();
            InvokeRequest request = new InvokeRequest()
                .withFunctionName(FUNCTION_NAME)
                .withInvocationType(InvocationType.Event)
                .withPayload(payload);
            lambdaClient.invoke(request);

            LOG.info("Invoked function");

        } catch (Exception e) {
            LOG.error("Failed to invoke processing function", e);
            OBJECT_MAPPER.writeValue(outputStream, getResponse(e.getMessage(), 500));
        }

        OBJECT_MAPPER.writeValue(outputStream, getResponse("Successfully processed", 200));

    }

    private static ApiGatewayResponse getResponse(Object objectBody, int statusCode) {
        LOG.info("Response: " + objectBody);
        return ApiGatewayResponse.builder()
            .setStatusCode(statusCode)
            .setObjectBody(objectBody)
            .setHeaders(Collections.singletonMap("Content-Type", "text/plain"))
            .build();
    }
}
