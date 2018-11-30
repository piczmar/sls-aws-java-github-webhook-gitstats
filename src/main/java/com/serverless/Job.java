package com.serverless;

import static com.serverless.HmacSha1Signature.calculateRFC2104HMAC;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.serverless.github.PushEvent;
import com.serverless.github.PushEventProcessor;

public class Job implements RequestHandler<Map<String, Object>, String> {

    private static final Logger LOG = Logger.getLogger(Job.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String WEB_HOOK_TOKEN = System.getenv("GITHUB_WEBHOOK_SECRET");
    private static final String PERSONAL_ACCESS_TOKEN = System.getenv("PERSONAL_ACCESS_TOKEN");

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        LOG.info("received: " + input);

        validate(notEmpty(WEB_HOOK_TOKEN), "Must provide a 'GITHUB_WEBHOOK_SECRET' env variable");
        validate(notEmpty(PERSONAL_ACCESS_TOKEN), "Must provide a 'PERSONAL_ACCESS_TOKEN' env variable");

        Map<String, Object> headers = (Map<String, Object>) input.get("headers");
        String sig = (String) headers.get("X-Hub-Signature");
        String githubEvent = (String) headers.get("X-GitHub-Event");
        String id = (String) headers.get("X-GitHub-Delivery");
        String body = (String) input.get("body");

        validate(notEmpty(sig), "No X-Hub-Signature found on request");
        validate(notEmpty(githubEvent), "No X-Github-Event found on request");
        validate(notEmpty(id), "No X-Github-Delivery found on request");

        try {
            String calculatedSig = "sha1=" + calculateRFC2104HMAC(body, WEB_HOOK_TOKEN);
            LOG.info("calculatedSig=" + calculatedSig);

            validate(sig.equals(calculatedSig), "X-Hub-Signature incorrect. Github webhook webHookToken doesn't match");

            PushEvent pushEvent = OBJECT_MAPPER.readValue(body, PushEvent.class);
            PushEventProcessor processor = new PushEventProcessor(PERSONAL_ACCESS_TOKEN);
            processor.process(pushEvent);

        } catch (NoSuchAlgorithmException | InvalidKeyException | UnirestException | GitAPIException | IOException e) {
            LOG.error("Unexpected error", e);
            throw new RuntimeException(e);
        }

        return "Successfully processed";
    }

    private static void validate(boolean condition, String errorMessage) {
        if (!condition) {
            throw new RuntimeException(errorMessage);
        }
    }

    private static boolean notEmpty(String value) {
        return value != null && !"".equals(value);
    }
}
