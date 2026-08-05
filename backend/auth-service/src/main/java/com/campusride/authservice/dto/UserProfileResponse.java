package com.campusride.authservice.dto;

import com.campusride.authservice.enums.Role;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role role,
        LocalDateTime createdAt
) {
}
