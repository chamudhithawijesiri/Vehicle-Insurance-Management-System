package com.autoshield.serviceImpl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autoshield.model.User;
import com.autoshield.model.Vehicle;
import com.autoshield.repository.VehicleRepository;
import com.autoshield.service.VehicleService;
/**
 * Provides the VehicleServiceImpl functionality.
 */

@Service
public class VehicleServiceImpl implements VehicleService {
/** The vehicleRepository value. */

    private final VehicleRepository vehicleRepository;
/**
 * Executes the VehicleServiceImpl operation.
 */

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }
/**
 * Executes the registerVehicle operation.
 */

    @Override
    public Vehicle registerVehicle(User owner, String vehicleNumber, String vehicleModel,
                                   Integer vehicleYear,
                                   String fuelType,
                                   String vehicleCategory, String vehicleColor,
                                   String chassisNumber, String vehicleImages) {
        int currentYear = java.time.Year.now().getValue();
        if (vehicleYear == null || vehicleYear < 1900 || vehicleYear > currentYear) {
            throw new IllegalArgumentException("Please select a valid vehicle year.");
        }
        String normalizedFuelType = fuelType == null ? "" : fuelType.trim();
        if (!java.util.Set.of("Petrol", "Diesel", "Hybrid", "Electric", "CNG").contains(normalizedFuelType)) {
            throw new IllegalArgumentException("Please select a valid fuel type.");
        }
        String normalizedVehicleNumber = vehicleNumber == null ? "" : vehicleNumber.trim().toUpperCase();
        if (!normalizedVehicleNumber.matches("^(?:[A-Z]{2,3}|[0-9]{2,3})-\\d{4}$")) {
            throw new IllegalArgumentException("Vehicle number must contain 2-3 letters or digits, a hyphen, and exactly 4 digits.");
        }
        if (vehicleRepository.existsByVehicleNumber(normalizedVehicleNumber)) {
            throw new IllegalArgumentException("A vehicle with this number is already registered: " + normalizedVehicleNumber);
        }
        String normalizedChassisNumber = chassisNumber == null ? "" : chassisNumber.trim().toUpperCase();
        if (!normalizedChassisNumber.matches("^[A-Z0-9]+$")) {
            throw new IllegalArgumentException("Chassis number may contain only letters and digits.");
        }
        if (vehicleRepository.existsByChassisNumber(normalizedChassisNumber)) {
            throw new IllegalArgumentException("A vehicle with this chassis number is already registered: " + normalizedChassisNumber);
        }

        Vehicle vehicle = new Vehicle();
        vehicle.setOwner(owner);
        vehicle.setVehicleNumber(normalizedVehicleNumber);
        vehicle.setVehicleModel(vehicleModel);
        vehicle.setVehicleYear(vehicleYear);
        vehicle.setFuelType(normalizedFuelType);
        vehicle.setVehicleCategory(vehicleCategory);
        vehicle.setVehicleColor(vehicleColor);
        vehicle.setChassisNumber(normalizedChassisNumber);
        vehicle.setVehicleImages(vehicleImages);

        return vehicleRepository.save(vehicle);
    }

    @Override
    public List<Vehicle> getVehiclesForUser(User owner) {
        return vehicleRepository.findByOwner(owner);
    }

    @Override
    public Vehicle getById(Long id) {
        return vehicleRepository.findById(id).orElse(null);
    }
}