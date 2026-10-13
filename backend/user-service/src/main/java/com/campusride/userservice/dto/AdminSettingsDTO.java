package com.campusride.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettingsDTO {
    private String siteName;
    private String supportEmail;
    private String supportPhone;
    private Boolean requireDriverApproval;
    private Boolean allowRegistration;
    private Integer maxRidesPerDay;
    private Double minDriverRating;
    private Integer maxPassengersPerRide;
    private Boolean enableEmailNotifications;
    private Boolean enableSmsNotifications;
    private Boolean autoApproveDrivers;
}
