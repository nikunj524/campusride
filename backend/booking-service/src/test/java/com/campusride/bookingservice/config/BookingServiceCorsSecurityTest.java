package com.campusride.bookingservice.config;

import com.campusride.bookingservice.controller.BookingController;
import com.campusride.bookingservice.dto.BookingResponse;
import com.campusride.bookingservice.security.AuthenticatedUser;
import com.campusride.bookingservice.security.JwtAuthenticationFilter;
import com.campusride.bookingservice.security.JwtUtil;
import com.campusride.bookingservice.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookingController.class)
@Import({CorsConfig.class, SecurityConfig.class, JwtAuthenticationFilter.class})
class BookingServiceCorsSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void allowsVitePreflightBeforeJwtAuthentication() throws Exception {
        mockMvc.perform(options("/api/bookings/ride/6")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        org.hamcrest.Matchers.containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        org.hamcrest.Matchers.containsString("authorization")));
    }

    @Test
    void allowsAuthenticatedRideBookingsGetWithoutWeakeningAuthorization() throws Exception {
        AuthenticatedUser user = new AuthenticatedUser(10L, "student@example.test", "STUDENT");
        UsernamePasswordAuthenticationToken authenticated = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
        when(bookingService.getBookingsByRideId(eq(6L), eq(user)))
                .thenReturn(List.of(BookingResponse.builder().id(1L).rideId(6L).passengerId(10L).build()));

        mockMvc.perform(get("/api/bookings/ride/6").with(authentication(authenticated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].rideId").value(6));

        verify(bookingService).getBookingsByRideId(6L, user);
    }
}
