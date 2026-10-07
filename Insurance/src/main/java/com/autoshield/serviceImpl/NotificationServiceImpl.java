package com.autoshield.serviceImpl;

import com.autoshield.enums.NotificationType;
import com.autoshield.model.Notification;
import com.autoshield.model.User;
import com.autoshield.repository.NotificationRepository;
import com.autoshield.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;
/**
 * Provides the NotificationServiceImpl functionality.
 */

@Service
public class NotificationServiceImpl implements NotificationService {
/** The notificationRepository value. */

    private final NotificationRepository notificationRepository;
/**
 * Executes the NotificationServiceImpl operation.
 */

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }
/**
 * Executes the notify operation.
 */

    @Override
    public Notification notify(User user, NotificationType type, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        return notificationRepository.save(notification);
    }

    @Override
    public List<Notification> getForUser(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
/**
 * Executes the markAsRead operation.
 */

    @Override
    public void markAsRead(Long id, User user) {
        notificationRepository.findById(id).ifPresent(n -> {
            // Ownership check: a customer can only mark their own notifications read.
            if (n.getUser().getId().equals(user.getId())) {
                n.setRead(true);
                notificationRepository.save(n);
            }
        });
    }
/**
 * Executes the markAllAsRead operation.
 */

    @Override
    public void markAllAsRead(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        notifications.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(notifications);
    }
}