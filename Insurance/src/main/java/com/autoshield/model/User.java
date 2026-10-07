package com.autoshield.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
/**
 * Provides the User functionality.
 */

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User {
/** The id value. */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
/** The fullName value. */

    @Column(name = "full_name", nullable = false)
    private String fullName;
/** The email value. */

    @Column(nullable = false, unique = true)
    private String email;
/** The password value. */

    @Column(nullable = false)
    private String password;
/** The phoneNumber value. */

    @Column(name = "phone_number")
    private String phoneNumber;
/** The nicNumber value. */

    @Column(name = "nic_number")
    private String nicNumber;
/** The profilePhoto value. */

    @Lob
    @Column(name = "profile_photo", columnDefinition = "LONGBLOB")
    private byte[] profilePhoto;
/** The profilePhotoContentType value. */

    @Column(name = "profile_photo_content_type")
    private String profilePhotoContentType;
/** The role value. */

    @Column(nullable = false)
    private String role = "ROLE_USER";

    // Epic 1, US-07: admin can activate/deactivate an account. A deactivated
    // account can no longer log in (enforced in UserServiceImpl.loadUserByUsername).
    @Column(nullable = false)
    private boolean active = true;

    // Epic 1, US-03: forgot-password token, valid for a short window.
    @Column(name = "reset_token")
    private String resetToken;
/** The resetTokenExpiry value. */

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    // A user can register several vehicles.
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicle> vehicles = new ArrayList<>();
/**
 * Executes the User operation.
 */

    public User() {
    }
/**
 * Executes the User operation.
 */

    public User(String fullName, String email, String password, String phoneNumber) {
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNicNumber() {
        return nicNumber;
    }

    public void setNicNumber(String nicNumber) {
        this.nicNumber = nicNumber;
    }

    public byte[] getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(byte[] profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public String getProfilePhotoContentType() {
        return profilePhotoContentType;
    }

    public void setProfilePhotoContentType(String profilePhotoContentType) {
        this.profilePhotoContentType = profilePhotoContentType;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public LocalDateTime getResetTokenExpiry() {
        return resetTokenExpiry;
    }

    public void setResetTokenExpiry(LocalDateTime resetTokenExpiry) {
        this.resetTokenExpiry = resetTokenExpiry;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public void setVehicles(List<Vehicle> vehicles) {
        this.vehicles = vehicles;
    }
}