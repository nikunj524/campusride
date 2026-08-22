package com.campusride.authservice;

import com.campusride.authservice.dto.AuthenticationRequest;
import com.campusride.authservice.dto.RegisterRequest;
import com.campusride.authservice.dto.WorkspaceSwitchRequest;
import com.campusride.authservice.enums.Role;
import com.campusride.authservice.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestMailConfig.class)
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private String testEmail;
    private String driverEmail;

    @AfterEach
    void removeTestUser() {
        if (testEmail != null) {
            userRepository.findByEmail(testEmail).ifPresent(userRepository::delete);
        }
        if (driverEmail != null) {
            userRepository.findByEmail(driverEmail).ifPresent(userRepository::delete);
        }
    }

    @Test
    void registersLogsInAndReadsProtectedProfile() throws Exception {
        testEmail = "test." + UUID.randomUUID() + "@campusride.test";
        RegisterRequest registerRequest = new RegisterRequest(
                "Test", "Student", testEmail, "securePass123", "+919876543210", Role.STUDENT
        );

        String registrationResponse = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.activeRole").value("STUDENT"))
                .andExpect(jsonPath("$.driverEligible").value(false))
                .andExpect(jsonPath("$.user.email").value(testEmail))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String registrationToken = objectMapper.readTree(registrationResponse).get("token").asText();

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthenticationRequest(testEmail, "securePass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginResponse);
        String loginToken = loginJson.get("token").asText();
        assertClaim(loginToken, "driverEligible", false);

        mockMvc.perform(get("/auth/profile").header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mockMvc.perform(post("/auth/workspace")
                        .header("Authorization", "Bearer " + loginToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceSwitchRequest(Role.DRIVER))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().isForbidden());

        assert registrationToken != null;
    }

    @Test
    void driverEnabledUserCanSwitchModesWithOneLogin() throws Exception {
        driverEmail = "driver." + UUID.randomUUID() + "@campusride.test";
        RegisterRequest registerRequest = new RegisterRequest(
                "Test", "Driver", driverEmail, "securePass123", "+919876543210", Role.DRIVER
        );

        String registrationResponse = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activeRole").value("DRIVER"))
                .andExpect(jsonPath("$.driverEligible").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String studentModeToken = objectMapper.readTree(registrationResponse).get("token").asText();
        assertClaim(studentModeToken, "activeRole", "DRIVER");
        assertClaim(studentModeToken, "driverEligible", true);
        String driverModeResponse = mockMvc.perform(post("/auth/workspace")
                        .header("Authorization", "Bearer " + studentModeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceSwitchRequest(Role.DRIVER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeRole").value("DRIVER"))
                .andExpect(jsonPath("$.driverEligible").value(true))
                .andExpect(jsonPath("$.user.role").value("DRIVER"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String driverModeToken = objectMapper.readTree(driverModeResponse).get("token").asText();
        assertClaim(driverModeToken, "driverEligible", true);
        mockMvc.perform(post("/auth/workspace")
                        .header("Authorization", "Bearer " + driverModeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new WorkspaceSwitchRequest(Role.STUDENT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeRole").value("STUDENT"))
                .andExpect(jsonPath("$.driverEligible").value(true));
    }

    @Test
    void driverLoginStartsInDriverWorkspace() throws Exception {
        driverEmail = "driver-login." + UUID.randomUUID() + "@campusride.test";
        RegisterRequest registerRequest = new RegisterRequest(
                "Test", "Driver", driverEmail, "securePass123", "+919876543210", Role.DRIVER
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthenticationRequest(driverEmail, "securePass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeRole").value("DRIVER"))
                .andExpect(jsonPath("$.driverEligible").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String loginToken = objectMapper.readTree(loginResponse).get("token").asText();
        assertClaim(loginToken, "activeRole", "DRIVER");
        assertClaim(loginToken, "driverEligible", true);
    }

    @Test
    void approvedStudentCanReceiveDriverWorkspaceWithoutChangingRole() throws Exception {
        testEmail = "approved." + UUID.randomUUID() + "@campusride.test";
        RegisterRequest registerRequest = new RegisterRequest(
                "Test", "Approved", testEmail, "securePass123", "+919876543210", Role.STUDENT
        );

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        jdbcTemplate.update("UPDATE users SET driver_eligible = TRUE WHERE email = ?", testEmail);

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuthenticationRequest(testEmail, "securePass123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeRole").value("STUDENT"))
                .andExpect(jsonPath("$.driverEligible").value(true))
                .andExpect(jsonPath("$.user.role").value("STUDENT"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String studentToken = objectMapper.readTree(loginResponse).get("token").asText();
        mockMvc.perform(post("/auth/workspace")
                        .header("Authorization", "Bearer " + studentToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new WorkspaceSwitchRequest(Role.DRIVER))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeRole").value("DRIVER"))
                .andExpect(jsonPath("$.user.role").value("STUDENT"));
    }

    private void assertClaim(String token, String claimName, boolean expectedValue) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        org.junit.jupiter.api.Assertions.assertEquals(expectedValue, claims.get(claimName, Boolean.class));
    }

    private void assertClaim(String token, String claimName, String expectedValue) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        org.junit.jupiter.api.Assertions.assertEquals(expectedValue, claims.get(claimName, String.class));
    }
}
