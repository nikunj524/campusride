package com.campusride.userservice;

import com.campusride.userservice.dto.VehicleCreateRequest;
import com.campusride.userservice.dto.VehicleUpdateRequest;
import com.campusride.userservice.enums.OwnershipType;
import com.campusride.userservice.enums.VehicleType;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class VehicleControllerIntegrationTests {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @BeforeEach
    void setUp() {
        this.mockMvc = org.springframework.test.web.servlet.setup.MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void cleanDatabase() {
        jdbcTemplate.update("DELETE FROM vehicles");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    void driverCanRegisterReadUpdateAndDeleteVehicle() throws Exception {
        String email = uniqueEmail("driver");
        insertUser(email, "DRIVER");
        String token = tokenFor(email);

        VehicleCreateRequest request = new VehicleCreateRequest(
                "KA01AB1234",
                VehicleType.BIKE,
                "Honda Activa",
                "Blue",
                2,
                OwnershipType.OWN
        );

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleNumber").value("KA01AB1234"))
                .andExpect(jsonPath("$.vehicleType").value("BIKE"))
                .andExpect(jsonPath("$.vehicleModel").value("Honda Activa"))
                .andExpect(jsonPath("$.ownershipType").value("OWN"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(get("/api/vehicles/my").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleNumber").value("KA01AB1234"));

        VehicleUpdateRequest updateRequest = new VehicleUpdateRequest("Honda Activa 6G", "Navy Blue", 2, OwnershipType.FAMILY);
        mockMvc.perform(put("/api/vehicles/my")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vehicleModel").value("Honda Activa 6G"))
                .andExpect(jsonPath("$.vehicleColor").value("Navy Blue"))
                .andExpect(jsonPath("$.ownershipType").value("FAMILY"))
                .andExpect(jsonPath("$.vehicleNumber").value("KA01AB1234"))
                .andExpect(jsonPath("$.vehicleType").value("BIKE"));

        mockMvc.perform(delete("/api/vehicles/my").header("Authorization", bearer(token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/vehicles/my").header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    @Test
    void studentGetsForbiddenForVehicleEndpoints() throws Exception {
        String email = uniqueEmail("student");
        insertUser(email, "STUDENT");
        String token = tokenFor(email);

        mockMvc.perform(get("/api/vehicles/my").header("Authorization", bearer(token)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new VehicleCreateRequest(
                                "KA01ZZ0001",
                                VehicleType.CAR,
                                "Maruti Swift",
                                "White",
                                4,
                                OwnershipType.OWN
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void driverInStudentModeGetsForbiddenForVehicleEndpoints() throws Exception {
        String email = uniqueEmail("driver-student-mode");
        insertUser(email, "DRIVER");

        mockMvc.perform(get("/api/vehicles/my").header("Authorization", bearer(tokenFor(email, "STUDENT"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void studentCannotUseDriverModeTokenForVehicleEndpoints() throws Exception {
        String email = uniqueEmail("student-driver-mode");
        insertUser(email, "STUDENT");

        mockMvc.perform(get("/api/vehicles/my").header("Authorization", bearer(tokenFor(email, "DRIVER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsSecondVehicleForSameDriver() throws Exception {
        String email = uniqueEmail("driver-one");
        insertUser(email, "DRIVER");
        String token = tokenFor(email);

        VehicleCreateRequest request = new VehicleCreateRequest(
                "KA02AB1111",
                VehicleType.CAR,
                "Hyundai i20",
                "Red",
                4,
                OwnershipType.OWN
        );

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Driver %s already has a registered vehicle".formatted(findUserId(email))));
    }

    @Test
    void rejectsDuplicateVehicleNumberAcrossDrivers() throws Exception {
        String firstEmail = uniqueEmail("driver-two");
        String secondEmail = uniqueEmail("driver-three");
        insertUser(firstEmail, "DRIVER");
        insertUser(secondEmail, "DRIVER");

        VehicleCreateRequest firstRequest = new VehicleCreateRequest(
                "KA03AB2222",
                VehicleType.BIKE,
                "TVS Jupiter",
                "Black",
                2,
                OwnershipType.OWN
        );

        VehicleCreateRequest duplicateNumberRequest = new VehicleCreateRequest(
                "KA03AB2222",
                VehicleType.CAR,
                "Toyota Glanza",
                "Grey",
                4,
                OwnershipType.FAMILY
        );

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", bearer(tokenFor(firstEmail)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", bearer(tokenFor(secondEmail)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateNumberRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Vehicle number KA03AB2222 is already registered"));
    }

    @Test
    void validationErrorsReturnBadRequest() throws Exception {
        String email = uniqueEmail("driver-four");
        insertUser(email, "DRIVER");

        mockMvc.perform(post("/api/vehicles")
                        .header("Authorization", bearer(tokenFor(email)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehicleNumber": "",
                                  "vehicleType": "BIKE",
                                  "vehicleModel": "",
                                  "vehicleColor": "",
                                  "totalSeats": 0,
                                  "ownershipType": "OWN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    private void insertUser(String email, String role) {
        jdbcTemplate.update(
                "INSERT INTO users (first_name, last_name, email, password, phone_number, role, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)",
                "Test",
                "User",
                email,
                "password",
                "9999999999",
                role,
                Timestamp.valueOf(LocalDateTime.now())
        );
    }

    private Long findUserId(String email) {
        return jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);
    }

    private String tokenFor(String email) {
        String activeRole = jdbcTemplate.queryForObject("SELECT role FROM users WHERE email = ?", String.class, email);
        return tokenFor(email, activeRole);
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
