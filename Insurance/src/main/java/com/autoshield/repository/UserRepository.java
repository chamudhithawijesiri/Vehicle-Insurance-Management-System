package com.autoshield.repository;

import com.autoshield.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
/**
 * Provides the UserRepository functionality.
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
/**
     * Executes the findByEmail operation.
     */
    Optional<User> findByEmail(String email);

/**
     * Executes the findByRoleAndActiveTrue operation.
     */
    List<User> findByRoleAndActiveTrue(String role);

    // Epic 1, US-03: look up the account currently holding a given reset token.
/**
     * Executes the findByResetToken operation.
     */
    Optional<User> findByResetToken(String resetToken);
}