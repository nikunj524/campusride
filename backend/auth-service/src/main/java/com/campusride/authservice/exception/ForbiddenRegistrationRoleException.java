package com.campusride.authservice.exception;

public class ForbiddenRegistrationRoleException extends RuntimeException {

    public ForbiddenRegistrationRoleException() {
        super("ADMIN accounts cannot be registered through this endpoint");
    }
}
