package com.campusride.authservice.exception;

public class PasswordResetNotVerifiedException extends RuntimeException {

    public PasswordResetNotVerifiedException() {
        super("Password reset verification is invalid or expired");
    }
}
