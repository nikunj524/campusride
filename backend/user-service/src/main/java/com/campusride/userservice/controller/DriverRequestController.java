package com.campusride.userservice.controller;

import com.campusride.userservice.dto.DriverRequestResponse;
import com.campusride.userservice.service.DriverRequestService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin(
        origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = {"Authorization", "Content-Type"},
        allowCredentials = "true"
)
public class DriverRequestController {

    private final DriverRequestService driverRequestService;

    public DriverRequestController(DriverRequestService driverRequestService) {
        this.driverRequestService = driverRequestService;
    }

    @PostMapping("/api/driver-requests")
    public ResponseEntity<DriverRequestResponse> createRequest(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(driverRequestService.createRequest(authentication.getName()));
    }

    @GetMapping("/api/driver-requests/me")
    public ResponseEntity<DriverRequestResponse> getMyRequest(Authentication authentication) {
        return ResponseEntity.ok(driverRequestService.getMyRequest(authentication.getName()));
    }

    @GetMapping("/api/driver-requests/my-status")
    public ResponseEntity<java.util.Map<String, Object>> getMyRequestStatus(Authentication authentication) {
        try {
            DriverRequestResponse request = driverRequestService.getMyRequest(authentication.getName());
            return ResponseEntity.ok(java.util.Map.of(
                "status", request.status(),
                "canSwitchToDriver", request.status().equals("APPROVED")
            ));
        } catch (Exception e) {
            // No request found
            return ResponseEntity.ok(java.util.Map.of(
                "status", "NONE",
                "canSwitchToDriver", false
            ));
        }
    }

    @GetMapping("/api/admin/driver-requests")
    public ResponseEntity<List<DriverRequestResponse>> getAllRequests() {
        return ResponseEntity.ok(driverRequestService.getAllRequests());
    }

    @PostMapping("/api/admin/driver-requests/{requestId}/approve")
    public ResponseEntity<DriverRequestResponse> approveRequest(
            @PathVariable Long requestId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(driverRequestService.approveRequest(requestId, authentication.getName()));
    }

    @PostMapping("/api/admin/driver-requests/{requestId}/reject")
    public ResponseEntity<DriverRequestResponse> rejectRequest(
            @PathVariable Long requestId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(driverRequestService.rejectRequest(requestId, authentication.getName()));
    }

    @PostMapping("/api/admin/driver-requests/{requestId}/revoke")
    public ResponseEntity<DriverRequestResponse> revokeRequest(
            @PathVariable Long requestId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(driverRequestService.revokeRequest(requestId, authentication.getName()));
    }
}
