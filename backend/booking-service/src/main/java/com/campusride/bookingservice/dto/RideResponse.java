package com.campusride.bookingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {
    private Long id;
    private Long driverId;
    private Long vehicleId;
    private String source;
    private String destination;
    private LocalDateTime departureTime;
    private Integer availableSeats;
    private BigDecimal pricePerSeat;
    private String status;
}
