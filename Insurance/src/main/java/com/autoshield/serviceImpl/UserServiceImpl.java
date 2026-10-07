package com.autoshield.serviceImpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.autoshield.model.User;
import com.autoshield.repository.UserRepository;
import com.autoshield.service.UserService;
/**
 * Provides the UserServiceImpl functionality.
 */

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final Set<String> VALID_ROLES = Set.of("ROLE_USER", "ROLE_OFFICER", "ROLE_SURVEYOR", "ROLE_FINANCE", "ROLE_ADMIN");
    private static final long RESET_TOKEN_VALID_MINUTES = 30;
    private static final Pattern NIC_PATTERN = Pattern.compile("^(?:\\d{9} V|\\d{12})$");
/** The userRepository value. */

    private final UserRepository userRepository;
/** The passwordEncoder value. */
    private final PasswordEncoder passwordEncoder;
/**
 * Executes the UserServiceImpl operation.
 */

    public UserServiceImpl(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
/**
 * Executes the registerNewUser operation.
 */

    @Override
    public User registerNewUser(User user, MultipartFile profilePhoto) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("There is an account with that email address: " + user.getEmail());
        }
        if (user.getNicNumber() == null || !NIC_PATTERN.matcher(user.getNicNumber().trim()).matches()) {
            throw new IllegalArgumentException("NIC must be 9 digits followed by a space and V, or exactly 12 digits.");
        }
        user.setNicNumber(user.getNicNumber().trim());
        attachProfilePhoto(user, profilePhoto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER");
        return userRepository.save(user);
    }

    private void attachProfilePhoto(User user, MultipartFile profilePhoto) {
        if (profilePhoto == null || profilePhoto.isEmpty()) {
            return;
        }
        if (profilePhoto.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("Profile photo must be 5 MB or smaller.");
        }
        String contentType = profilePhoto.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Profile photo must be an image.");
        }
        try {
            user.setProfilePhoto(profilePhoto.getBytes());
            user.setProfilePhotoContentType(contentType);
        } catch (IOException e) {
            throw new IllegalArgumentException("The profile photo could not be uploaded.", e);
        }
    }
/**
 * Executes the registerNewUserWithRole operation.
 */

    @Override
    public User registerNewUserWithRole(User user, String role) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalArgumentException("There is an account with that email address: " + user.getEmail());
        }
        if (role == null || !VALID_ROLES.contains(role)) {
            throw new IllegalArgumentException("Invalid role selected.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(role);
        return userRepository.save(user);
    }
/**
 * Retrieves the findByEmail operation.
 */

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
/**
 * Retrieves the loadUserByUsername operation.
 */

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("No user found with email: " + email);
        }
        User user = userOpt.get();
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(user.getRole())))
                // Epic 1, US-07: a deactivated account is rejected at login,
                // not just hidden from the UI.
                .disabled(!user.isActive())
                .build();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
/**
 * Processes the deleteUser operation.
 */

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
/**
 * Processes the updateProfile operation.
 */

    @Override
    public User updateProfile(Long userId, String fullName, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            user.setPhoneNumber(phoneNumber);
        }
        return userRepository.save(user);
    }
/**
 * Executes the changePassword operation.
 */

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
/**
 * Executes the toggleActiveStatus operation.
 */

    @Override
    public User toggleActiveStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }
/**
 * Executes the initiatePasswordReset operation.
 */

    @Override
    public String initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Deliberately silent: don't reveal whether the email exists.
            return null;
        }
        User user = userOpt.get();
        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(RESET_TOKEN_VALID_MINUTES));
        userRepository.save(user);
        return token;
    }
/**
 * Executes the resetPassword operation.
 */

    @Override
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("This reset link is invalid."));
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This reset link has expired. Please request a new one.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("New password must be at least 6 characters.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
    }
}