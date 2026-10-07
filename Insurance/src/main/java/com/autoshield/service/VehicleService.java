package com.autoshield.service;

import com.autoshield.model.User;
import com.autoshield.model.Vehicle;

import java.util.List;
/**
 * Provides the VehicleService functionality.
 */

public interface VehicleService {
    Vehicle registerVehicle(User owner, String vehicleNumber, String vehicleModel,
                            Integer vehicleYear,
                            String fuelType,
                            String vehicleCategory, String vehicleColor,
                            String chassisNumber, String vehicleImages);

/**
     * Executes the getVehiclesForUser operation.
     */
    List<Vehicle> getVehiclesForUser(User owner);

/**
     * Executes the getById operation.
     */
    Vehicle getById(Long id);
}