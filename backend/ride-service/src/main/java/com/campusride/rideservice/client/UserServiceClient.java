package com.campusride.rideservice.client;

import com.campusride.rideservice.dto.VehicleInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    
    @Value("${user.service.url:http://localhost:8082}")
    private String userServiceUrl;

    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public VehicleInfo getDriverVehicle(Long driverId) {
        try {
            String url = userServiceUrl + "/api/vehicles/user/" + driverId;
            return restTemplate.getForObject(url, VehicleInfo.class);
        } catch (Exception e) {
            return null; // Return null if vehicle not found or error
        }
    }
}
