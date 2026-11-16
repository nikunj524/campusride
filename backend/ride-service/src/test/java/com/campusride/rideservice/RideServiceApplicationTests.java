package com.campusride.rideservice;

import com.campusride.rideservice.dto.SearchRideRequest;
import com.campusride.rideservice.enums.RideType;
import com.campusride.rideservice.security.AuthenticatedUser;
import com.campusride.rideservice.service.RideService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RideServiceApplicationTests {

    @Autowired
    private RideService rideService;

	@Test
	void contextLoads() {
	}

    @Test
    void actualConfiguredDatabaseReturnsNoGvpRidesForFourOrMoreAvailableSeats() {
        var request = new SearchRideRequest(RideType.OFFERING, "gvp", null, null, 4, null, null, null, null);
        var result = rideService.searchRides(request, new AuthenticatedUser(999L, "search-test@campusride.local", "STUDENT"));
        assertTrue(result.isEmpty(), "The configured database has no eligible gvp ride with four available seats");
    }

}
