package com.campusride.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class DriverRequestControllerIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM driver_requests");
        jdbcTemplate.update("DELETE FROM vehicles");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void studentRequestCanBeApprovedWithoutChangingPrimaryRole() throws Exception {
        String studentEmail = uniqueEmail("student");
        String adminEmail = uniqueEmail("admin");
        insertUser(studentEmail, "STUDENT", false);
        insertUser(adminEmail, "ADMIN", false);

        String studentToken = tokenFor(studentEmail, "STUDENT");
        String adminToken = tokenFor(adminEmail, "STUDENT");

        mockMvc.perform(post("/api/driver-requests").header("Authorization", bearer(studentToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.email").value(studentEmail));

        mockMvc.perform(post("/api/driver-requests").header("Authorization", bearer(studentToken)))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/admin/driver-requests/{id}/approve", 1L)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());

        String requestId = mockMvc.perform(get("/api/admin/driver-requests")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long id = objectMapper.readTree(requestId).get(0).get("id").asLong();

        mockMvc.perform(post("/api/admin/driver-requests/{id}/approve", id)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        assertEquals("STUDENT", jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE email = ?", String.class, studentEmail));
        assertEquals(1, jdbcTemplate.queryForObject(
                "SELECT CASE WHEN driver_eligible THEN 1 ELSE 0 END FROM users WHERE email = ?",
                Integer.class,
                studentEmail));

        mockMvc.perform(get("/api/admin/driver-requests")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].reviewedAt").isNotEmpty())
                .andExpect(jsonPath("$[0].reviewedBy").isNotEmpty());

        mockMvc.perform(get("/api/vehicles/my").header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/vehicles/my").header("Authorization", bearer(tokenFor(studentEmail, "DRIVER"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/admin/driver-requests/{id}/revoke", id)
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/admin/driver-requests/{id}/revoke", id)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVOKED"));

        assertEquals("STUDENT", jdbcTemplate.queryForObject(
                "SELECT role FROM users WHERE email = ?", String.class, studentEmail));
        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT CASE WHEN driver_eligible THEN 1 ELSE 0 END FROM users WHERE email = ?",
                Integer.class,
                studentEmail));

        mockMvc.perform(get("/api/admin/driver-requests")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REVOKED"))
                .andExpect(jsonPath("$[0].reviewedAt").isNotEmpty())
                .andExpect(jsonPath("$[0].reviewedBy").isNotEmpty());

        mockMvc.perform(get("/api/vehicles/my").header("Authorization", bearer(tokenFor(studentEmail, "DRIVER"))))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/driver-requests").header("Authorization", bearer(studentToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void rejectedRequestDoesNotEnableDriverModeAndCanBeResubmitted() throws Exception {
        String studentEmail = uniqueEmail("student-reject");
        String adminEmail = uniqueEmail("admin-reject");
        insertUser(studentEmail, "STUDENT", false);
        insertUser(adminEmail, "ADMIN", false);

        String studentToken = tokenFor(studentEmail, "STUDENT");
        String adminToken = tokenFor(adminEmail, "STUDENT");

        String requestBody = mockMvc.perform(post("/api/driver-requests")
                        .header("Authorization", bearer(studentToken)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        Long requestId = objectMapper.readTree(requestBody).get("id").asLong();

        mockMvc.perform(post("/api/admin/driver-requests/{id}/reject", requestId)
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        assertEquals(0, jdbcTemplate.queryForObject(
                "SELECT CASE WHEN driver_eligible THEN 1 ELSE 0 END FROM users WHERE email = ?",
                Integer.class,
                studentEmail));

        mockMvc.perform(get("/api/admin/driver-requests")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("REJECTED"))
                .andExpect(jsonPath("$[0].reviewedAt").isNotEmpty())
                .andExpect(jsonPath("$[0].reviewedBy").isNotEmpty());

        mockMvc.perform(post("/api/driver-requests").header("Authorization", bearer(studentToken)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    private void insertUser(String email, String role, boolean driverEligible) {
        jdbcTemplate.update(
                "INSERT INTO users (first_name, last_name, email, password, phone_number, role, driver_eligible, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                "Test",
                "User",
                email,
                "password",
                "9999999999",
                role,
                driverEligible,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    private String tokenFor(String email, String activeRole) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        return Jwts.builder()
                .subject(email)
                .claim("activeRole", activeRole)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 86_400_000L))
                .signWith(key)
                .compact();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID() + "@campusride.test";
    }
}
