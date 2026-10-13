package com.campusride.userservice.dto;

import com.campusride.userservice.enums.DriverRequestStatus;
import com.campusride.userservice.enums.Role;

import java.time.LocalDateTime;

public record DriverRequestResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role userRole,
        DriverRequestStatus status,
        LocalDateTime requestedAt,
        LocalDateTime reviewedAt,
        Long reviewedBy
) {
}
