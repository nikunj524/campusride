package com.campusride.userservice.controller;

import com.campusride.userservice.dto.AdminSettingsDTO;
import com.campusride.userservice.service.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/settings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminSettingsController {
    
    private final AdminSettingsService adminSettingsService;
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSettingsDTO> getSettings() {
        AdminSettingsDTO settings = adminSettingsService.getSettings();
        return ResponseEntity.ok(settings);
    }
    
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminSettingsDTO> updateSettings(
            @RequestBody AdminSettingsDTO settingsDTO,
            Authentication authentication) {
        String adminEmail = authentication.getName();
        AdminSettingsDTO updatedSettings = adminSettingsService.updateSettings(settingsDTO, adminEmail);
        return ResponseEntity.ok(updatedSettings);
    }
}
