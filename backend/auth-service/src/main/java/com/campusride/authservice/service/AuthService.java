package com.campusride.authservice.service;

import com.campusride.authservice.dto.AuthenticationRequest;
import com.campusride.authservice.dto.AuthenticationResponse;
import com.campusride.authservice.dto.ForgotPasswordRequest;
import com.campusride.authservice.dto.RegisterRequest;
import com.campusride.authservice.dto.ResetPasswordRequest;
import com.campusride.authservice.dto.UserProfileResponse;
import com.campusride.authservice.dto.VerifyPasswordResetOtpRequest;
import com.campusride.authservice.dto.WorkspaceSwitchRequest;

public interface AuthService {

    AuthenticationResponse register(RegisterRequest request);

    AuthenticationResponse login(AuthenticationRequest request);

    AuthenticationResponse switchWorkspace(String email, WorkspaceSwitchRequest request);

    UserProfileResponse getProfile(String email);

    void requestPasswordReset(ForgotPasswordRequest request);

    void verifyPasswordResetOtp(VerifyPasswordResetOtpRequest request);

    void resetPassword(ResetPasswordRequest request);
}
