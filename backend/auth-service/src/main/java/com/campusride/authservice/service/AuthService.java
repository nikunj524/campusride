package com.campusride.authservice.service;

import com.campusride.authservice.dto.AuthenticationRequest;
import com.campusride.authservice.dto.AuthenticationResponse;
import com.campusride.authservice.dto.RegisterRequest;
import com.campusride.authservice.dto.UserProfileResponse;

public interface AuthService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse login(AuthenticationRequest request);

    UserProfileResponse getProfile(String email);
}
