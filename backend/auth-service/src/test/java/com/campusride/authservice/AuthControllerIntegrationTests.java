package com.campusride.authservice;

import com.campusride.authservice.dto.AuthenticationRequest;
import com.campusride.authservice.dto.RegisterRequest;
import com.campusride.authservice.enums.Role;
import com.campusride.authservice.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String testEmail;

    @AfterEach
    void removeTestUser() {
        if (testEmail != null) {
            userRepository.findByEmail(testEmail).ifPresent(userRepository::delete);
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

        mockMvc.perform(get("/auth/profile").header("Authorization", "Bearer " + loginToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(testEmail))
                .andExpect(jsonPath("$.role").value("STUDENT"));

        mockMvc.perform(get("/auth/profile"))
                .andExpect(status().isForbidden());

        assert registrationToken != null;
    }
}
