package com.campusride.bookingservice.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class RestTemplateConfigTest {

    private static final String TOKEN = "Bearer original-user-token";

    @AfterEach
    void clearRequestContext() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void forwardsIncomingBearerTokenForRideGetAndPatchRequests() {
        MockHttpServletRequest incomingRequest = new MockHttpServletRequest();
        incomingRequest.addHeader(HttpHeaders.AUTHORIZATION, TOKEN);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(incomingRequest));

        RestTemplate restTemplate = new RestTemplateConfig().restTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        server.expect(once(), requestTo("http://ride-service/api/rides/7"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andRespond(withSuccess("{}", org.springframework.http.MediaType.APPLICATION_JSON));
        server.expect(once(), requestTo("http://ride-service/api/rides/7/accept?passengerId=42"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(HttpHeaders.AUTHORIZATION, TOKEN))
                .andRespond(withSuccess());

        restTemplate.getForEntity("http://ride-service/api/rides/7", String.class);
        restTemplate.exchange(
                "http://ride-service/api/rides/7/accept?passengerId=42",
                HttpMethod.PATCH,
                null,
                Void.class
        );

        server.verify();
    }
}
