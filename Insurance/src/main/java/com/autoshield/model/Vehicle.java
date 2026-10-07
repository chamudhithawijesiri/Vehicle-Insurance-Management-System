package com.autoshield.model;

import jakarta.persistence.*;
/**
 * Provides the Vehicle functionality.
 */

@Entity
@Table(name = "vehicles")
public class Vehicle {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Owner of this vehicle
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
/** The vehicleNumber value. */

    @Column(name = "vehicle_number", nullable = false, unique = true)
    private String vehicleNumber;
/** The vehicleModel value. */

    @Column(name = "vehicle_model", nullable = false)
    private String vehicleModel;
/** The vehicleYear value. */

    @Column(name = "vehicle_year")
    private Integer vehicleYear;
/** The fuelType value. */

    @Column(name = "fuel_type")
    private String fuelType;
/** The vehicleCategory value. */

    @Column(name = "vehicle_category")
    private String vehicleCategory;
/** The vehicleColor value. */

    @Column(name = "vehicle_color")
    private String vehicleColor;
/** The chassisNumber value. */

    @Column(name = "chassis_number", nullable = false, unique = true)
    private String chassisNumber;

    // Bug fix: this used to be @Column(length = 1000), which silently
    // capped what could be stored — nowhere near enough for real base64
    // image data (a single photo alone can be tens of thousands of
    // characters, and this column holds up to 4). LONGTEXT removes that cap.
    @Lob
    @Column(name = "vehicle_images", columnDefinition = "LONGTEXT")
    private String vehicleImages;
/**
 * Executes the Vehicle operation.
 */

    public Vehicle() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public Integer getVehicleYear() {
        return vehicleYear;
    }

    public void setVehicleYear(Integer vehicleYear) {
        this.vehicleYear = vehicleYear;
    }

    public String getFuelType() {
        return fuelType;
    }

    public void setFuelType(String fuelType) {
        this.fuelType = fuelType;
    }

    public String getVehicleCategory() {
        return vehicleCategory;
    }

    public void setVehicleCategory(String vehicleCategory) {
        this.vehicleCategory = vehicleCategory;
    }

    public String getVehicleColor() {
        return vehicleColor;
    }

    public void setVehicleColor(String vehicleColor) {
        this.vehicleColor = vehicleColor;
    }

    public String getChassisNumber() {
        return chassisNumber;
    }

    public void setChassisNumber(String chassisNumber) {
        this.chassisNumber = chassisNumber;
    }

    public String getVehicleImages() {
        return vehicleImages;
    }

    public void setVehicleImages(String vehicleImages) {
        this.vehicleImages = vehicleImages;
    }
}