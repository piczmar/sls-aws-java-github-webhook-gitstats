package com.serverless.github;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

class GithubUtils {

    private static final Logger LOG = Logger.getLogger(GithubUtils.class);

    static {
        // Only one time
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Does github api call analog to this: curl - X POST - H 'Authorization: token {personalApiToken}' \
     * https://api.github.com/repos/{githubUser}/{githubRepo}/pulls/{pullRequestNumber}/reviews \ -d '{"event" :
     * "COMMENT", "body" : "{reviewContent}"}'
     */
    static void publishReview(String githubUser, String githubRepo, String pullRequestNumber,
                              String personalApiToken,
                              String reviewContent) throws UnirestException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "token " + personalApiToken);

        Map<String, String> body = new HashMap<>();
        body.put("event", "COMMENT");
        body.put("body", reviewContent);

        HttpResponse<String> response = Unirest
            .post("https://api.github.com/repos/" + githubUser + "/" + githubRepo + "/pulls/" + pullRequestNumber
                + "/reviews")
            .headers(headers)
            .body(body)
            .asString();
        LOG.info("response code = " + response.getStatus());
        LOG.info("response status text = " + response.getStatusText());
        LOG.info("response body = " + response.getBody());
    }

    /**
     * Does the query analog to this: curl -X GET -H 'Authorization: token {personalApiToken}' \
     * 'https://api.github.com/repos/{githubUser}/{githubRepo}/pulls?state=open&head={head}'
     *
     * @return a list of pull requests
     */
    static List<PullRequestDto> getPullRequests(String githubUser, String githubRepo,
                                                String personalApiToken, String head) throws UnirestException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "token " + personalApiToken);

        HttpResponse<PullRequestDto[]> response = Unirest
            .get("https://api.github.com/repos/" + githubUser + "/" + githubRepo + "/pulls")
            .headers(headers)
            .queryString("head", head)
            .queryString("state", "open")
            .asObject(PullRequestDto[].class);

        LOG.info("response code = " + response.getStatus());
        LOG.info("response status text = " + response.getStatusText());
        LOG.info("response body = " + response.getBody());
        return Arrays.asList(response.getBody());
    }


}
