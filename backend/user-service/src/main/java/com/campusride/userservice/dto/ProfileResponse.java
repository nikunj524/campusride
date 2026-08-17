package com.campusride.userservice.dto;

import com.campusride.userservice.enums.Role;

public record ProfileResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Role role
) {
}
