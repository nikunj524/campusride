package com.campusride.bookingservice.dto;

import com.campusride.bookingservice.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingUpdateRequest {

    @NotNull(message = "Status is required")
    private BookingStatus status;
}
