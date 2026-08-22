package com.campusride.userservice.controller;

import com.campusride.userservice.dto.VehicleCreateRequest;
import com.campusride.userservice.dto.VehicleResponse;
import com.campusride.userservice.dto.VehicleUpdateRequest;
import com.campusride.userservice.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(
        origins = "http://localhost:5173",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS},
        allowedHeaders = {"Authorization", "Content-Type"}
)
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    public ResponseEntity<VehicleResponse> createVehicle(
            Authentication authentication,
            @Valid @RequestBody VehicleCreateRequest request
    ) {
        return ResponseEntity.ok(vehicleService.createVehicle(authentication.getName(), request));
    }

    @GetMapping("/my")
    public ResponseEntity<VehicleResponse> getMyVehicle(Authentication authentication) {
        return ResponseEntity.ok(vehicleService.getMyVehicle(authentication.getName()));
    }

    @PutMapping("/my")
    public ResponseEntity<VehicleResponse> updateMyVehicle(
            Authentication authentication,
            @Valid @RequestBody VehicleUpdateRequest request
    ) {
        return ResponseEntity.ok(vehicleService.updateMyVehicle(authentication.getName(), request));
    }

    @DeleteMapping("/my")
    public ResponseEntity<Void> deleteMyVehicle(Authentication authentication) {
        vehicleService.deleteMyVehicle(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
