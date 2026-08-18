package com.campusride.authservice.exception;

public class InvalidPasswordResetOtpException extends RuntimeException {

    public InvalidPasswordResetOtpException() {
        super("Invalid or expired password reset OTP");
    }
}
