package com.autoshield.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.autoshield.model.User;
/**
 * Provides the UserService functionality.
 */

public interface UserService {
    // Public self-registration: always creates a ROLE_USER account.
/**
     * Executes the registerNewUser operation.
     */
    User registerNewUser(User user, MultipartFile profilePhoto);

    // Admin-only: creates an account with the role the admin selected
    // (customer, Insurance Officer, or Administrator). Epic 1, US-10.
/**
     * Executes the registerNewUserWithRole operation.
     */
    User registerNewUserWithRole(User user, String role);

/**
     * Executes the findByEmail operation.
     */
    User findByEmail(String email);

    // Used by the Officer's "view customer" detail page during policy/claim review.
/**
     * Executes the getById operation.
     */
    User getById(Long id);

/**
     * Executes the getAllUsers operation.
     */
    List<User> getAllUsers();

/**
     * Executes the deleteUser operation.
     */
    void deleteUser(Long id);

    // Epic 1, US-04/US-05: customer edits their own contact details.
/**
     * Executes the updateProfile operation.
     */
    User updateProfile(Long userId, String fullName, String phoneNumber);

    // Epic 1, US-09: customer changes their own password. Throws
    // IllegalArgumentException if the current password doesn't match.
/**
     * Executes the changePassword operation.
     */
    void changePassword(Long userId, String currentPassword, String newPassword);

    // Epic 1, US-07: admin toggles whether an account can log in.
/**
     * Executes the toggleActiveStatus operation.
     */
    User toggleActiveStatus(Long userId);

    // Epic 1, US-03: forgot-password flow. Generates a time-limited token for
    // the given email (silently no-ops if the email doesn't exist, to avoid
    // leaking which emails are registered) and returns it so the controller
    // can build a reset link. Returns null if no account matched.
/**
     * Executes the initiatePasswordReset operation.
     */
    String initiatePasswordReset(String email);

    // Validates the token + expiry and sets the new password, clearing the
    // token afterward so it can't be reused. Throws IllegalArgumentException
    // if the token is invalid or expired.
/**
     * Executes the resetPassword operation.
     */
    void resetPassword(String token, String newPassword);
}