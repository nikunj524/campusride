package com.campusride.authservice.dto;

public record AuthenticationResponse(
        String token,
        String tokenType,
        UserProfileResponse user
) {
}
