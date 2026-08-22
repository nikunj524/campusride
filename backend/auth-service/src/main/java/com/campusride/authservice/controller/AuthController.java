package com.campusride.authservice.controller;

import com.campusride.authservice.dto.AuthenticationRequest;
import com.campusride.authservice.dto.AuthenticationResponse;
import com.campusride.authservice.dto.ForgotPasswordRequest;
import com.campusride.authservice.dto.MessageResponse;
import com.campusride.authservice.dto.RegisterRequest;
import com.campusride.authservice.dto.ResetPasswordRequest;
import com.campusride.authservice.dto.UserProfileResponse;
import com.campusride.authservice.dto.VerifyPasswordResetOtpRequest;
import com.campusride.authservice.dto.WorkspaceSwitchRequest;
import com.campusride.authservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/workspace")
    public ResponseEntity<AuthenticationResponse> switchWorkspace(
            Authentication authentication,
            @Valid @RequestBody WorkspaceSwitchRequest request
    ) {
        return ResponseEntity.ok(authService.switchWorkspace(authentication.getName(), request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(new MessageResponse(
                "If an account exists for this email, a password reset OTP has been sent"
        ));
    }

    @PostMapping("/verify-reset-otp")
    public ResponseEntity<MessageResponse> verifyResetOtp(@Valid @RequestBody VerifyPasswordResetOtpRequest request) {
        authService.verifyPasswordResetOtp(request);
        return ResponseEntity.ok(new MessageResponse("OTP verified successfully"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("Password reset successfully"));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> profile(Authentication authentication) {
        return ResponseEntity.ok(authService.getProfile(authentication.getName()));
    }
}
