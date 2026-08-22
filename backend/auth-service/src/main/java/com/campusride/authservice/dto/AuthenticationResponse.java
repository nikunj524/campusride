package com.campusride.authservice.dto;

import com.campusride.authservice.enums.Role;

public record AuthenticationResponse(
        String token,
        String tokenType,
        UserProfileResponse user,
        Role activeRole,
        boolean driverEligible
) {
}
