package com.autoshield.service;

import com.autoshield.enums.NotificationType;
import com.autoshield.model.Notification;
import com.autoshield.model.User;

import java.util.List;
/**
 * Provides the NotificationService functionality.
 */

public interface NotificationService {
/**
     * Executes the notify operation.
     */
    Notification notify(User user, NotificationType type, String title, String message);

/**
     * Executes the getForUser operation.
     */
    List<Notification> getForUser(User user);

/**
     * Executes the getUnreadCount operation.
     */
    long getUnreadCount(User user);

/**
     * Executes the markAsRead operation.
     */
    void markAsRead(Long id, User user);

/**
     * Executes the markAllAsRead operation.
     */
    void markAllAsRead(User user);
}