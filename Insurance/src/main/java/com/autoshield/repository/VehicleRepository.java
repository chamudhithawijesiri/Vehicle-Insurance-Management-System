package com.autoshield.repository;

import com.autoshield.model.User;
import com.autoshield.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/**
 * Provides the VehicleRepository functionality.
 */

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
/**
     * Executes the findByOwner operation.
     */
    List<Vehicle> findByOwner(User owner);
/**
     * Executes the findByVehicleNumber operation.
     */
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
/**
     * Executes the existsByVehicleNumber operation.
     */
    boolean existsByVehicleNumber(String vehicleNumber);
/**
     * Executes the existsByChassisNumber operation.
     */
    boolean existsByChassisNumber(String chassisNumber);
}