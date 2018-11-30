package com.serverless.github;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class PushEventConversionTest {

    @Test
    public void testConversion() throws IOException {
        //given
        URL url = this.getClass().getResource("/hook_push.json");
        ObjectMapper mapper = new ObjectMapper();

        //when
        PushEvent dto = mapper.readValue(url, PushEvent.class);

        //then
        assertEquals("refs/heads/test-1", dto.getRef());

    }

}
