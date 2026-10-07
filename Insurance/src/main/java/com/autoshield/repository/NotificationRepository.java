package com.autoshield.repository;

import com.autoshield.model.Notification;
import com.autoshield.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
/**
 * Provides the NotificationRepository functionality.
 */

public interface NotificationRepository extends JpaRepository<Notification, Long> {
/**
     * Executes the findByUserOrderByCreatedAtDesc operation.
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
/**
     * Executes the countByUserAndIsReadFalse operation.
     */
    long countByUserAndIsReadFalse(User user);

    // Used to avoid sending the same "policy expiring soon" reminder twice
    // for the same policy (see NotificationServiceImpl's scheduled check).
/**
     * Executes the existsByUserAndTitleContaining operation.
     */
    boolean existsByUserAndTitleContaining(User user, String titleFragment);
}