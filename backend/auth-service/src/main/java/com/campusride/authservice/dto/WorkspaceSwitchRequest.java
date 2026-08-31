package com.campusride.authservice.dto;

import com.campusride.authservice.enums.Role;
import jakarta.validation.constraints.NotNull;

public record WorkspaceSwitchRequest(
        @NotNull(message = "Mode is required") Role mode
) {
}
