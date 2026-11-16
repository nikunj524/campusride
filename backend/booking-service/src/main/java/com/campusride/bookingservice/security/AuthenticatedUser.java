package com.campusride.bookingservice.security;

public record AuthenticatedUser(Long userId, String email, String role) {
}
