package com.campusride.userservice.dto;

import com.campusride.userservice.enums.Role;

import java.time.LocalDateTime;

public record ProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role role,
        Boolean driverEligible,
        LocalDateTime createdAt
) {
}
