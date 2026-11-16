package com.campusride.rideservice.config;

import com.campusride.rideservice.controller.RideController;
import com.campusride.rideservice.security.JwtAuthenticationFilter;
import com.campusride.rideservice.security.JwtUtil;
import com.campusride.rideservice.security.AuthenticatedUser;
import com.campusride.rideservice.service.RideService;
import com.campusride.rideservice.dto.SearchRideRequest;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

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

    @Test
    void mapsFourSeatRequestToSearchDtoAndReturnsServiceResponse() throws Exception {
        AuthenticatedUser student = new AuthenticatedUser(42L, "student@test", "STUDENT");
        when(rideService.searchRides(any(SearchRideRequest.class), eq(student))).thenReturn(java.util.List.of());
        var authenticatedStudent = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                student, null, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_STUDENT")));

        mockMvc.perform(post("/api/rides/search")
                        .with(authentication(authenticatedStudent))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"OFFERING\",\"source\":\"gvp\",\"minimumAvailableSeats\":4}"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(rideService).searchRides(org.mockito.ArgumentMatchers.argThat(request ->
                request.minimumAvailableSeats() == 4 && "gvp".equals(request.source())), eq(student));
    }
}
