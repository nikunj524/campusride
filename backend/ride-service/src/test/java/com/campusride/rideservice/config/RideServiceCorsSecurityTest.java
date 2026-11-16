package com.campusride.rideservice.config;

import com.campusride.rideservice.controller.RideController;
import com.campusride.rideservice.security.JwtAuthenticationFilter;
import com.campusride.rideservice.security.JwtUtil;
import com.campusride.rideservice.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RideController.class)
@Import({CorsConfig.class, SecurityConfig.class, JwtAuthenticationFilter.class})
class RideServiceCorsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RideService rideService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void allowsUnauthenticatedPreflightFromViteOrigin() throws Exception {
        mockMvc.perform(options("/api/rides/search")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("POST")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, org.hamcrest.Matchers.containsString("authorization")));
    }

    @Test
    void stillRejectsUnauthenticatedProtectedSearchRequest() throws Exception {
        mockMvc.perform(post("/api/rides/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OFFERING\",\"status\":\"PENDING\"}"))
                .andExpect(status().isUnauthorized());
    }
}
