package com.campusride.userservice.service;

import com.campusride.userservice.dto.VehicleCreateRequest;
import com.campusride.userservice.dto.VehicleResponse;
import com.campusride.userservice.dto.VehicleUpdateRequest;

public interface VehicleService {

    VehicleResponse createVehicle(String email, VehicleCreateRequest request);

    VehicleResponse getMyVehicle(String email);

    VehicleResponse updateMyVehicle(String email, VehicleUpdateRequest request);

    void deleteMyVehicle(String email);
}
