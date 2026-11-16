package com.campusride.bookingservice.service.impl;

import com.campusride.bookingservice.dto.RideResponse;
import com.campusride.bookingservice.exception.RideNotAvailableException;
import com.campusride.bookingservice.service.RideServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideServiceClientImpl implements RideServiceClient {

    private final RestTemplate restTemplate;

    @Value("${ride.service.url}")
    private String rideServiceUrl;

    @Override
    public RideResponse getRideById(Long rideId) {
        try {
            String url = rideServiceUrl + "/api/rides/" + rideId;
            log.info("Fetching ride details from: {}", url);
            
            ResponseEntity<RideResponse> response = restTemplate.getForEntity(
                    url,
                    RideResponse.class
            );
            
            if (response.getBody() == null) {
                throw new RideNotAvailableException("Ride not found with ID: " + rideId);
            }
            
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Ride not found: {}", rideId);
            throw new RideNotAvailableException("Ride not found with ID: " + rideId);
        } catch (Exception e) {
            log.error("Error fetching ride details: {}", e.getMessage());
            throw new RideNotAvailableException("Failed to fetch ride details: " + e.getMessage());
        }
    }

    @Override
    public void reserveBookingSeats(Long rideId, Long bookingId, Long passengerId, Integer seats) {
        try {
            String url = rideServiceUrl + "/api/rides/" + rideId + "/reservations/" + bookingId
                    + "?passengerId=" + passengerId + "&seats=" + seats;
            log.info("Reserving seats for booking {} at: {}", bookingId, url);
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(
                    url,
                    HttpMethod.PATCH,
                    entity,
                    Void.class
            );
            
            log.info("Reserved seats for booking {} on ride {}", bookingId, rideId);
        } catch (Exception e) {
            log.error("Error reserving seats: {}", e.getMessage());
            throw new RideNotAvailableException("Failed to reserve seats: " + e.getMessage());
        }
    }

    @Override
    public void releaseBookingSeats(Long rideId, Long bookingId) {
        try {
            String url = rideServiceUrl + "/api/rides/" + rideId + "/reservations/" + bookingId + "/release";
            restTemplate.exchange(url, HttpMethod.PATCH, new HttpEntity<>(new HttpHeaders()), Void.class);
        } catch (Exception e) {
            log.error("Error releasing seats: {}", e.getMessage());
            throw new RideNotAvailableException("Failed to release seats: " + e.getMessage());
        }
    }
}
