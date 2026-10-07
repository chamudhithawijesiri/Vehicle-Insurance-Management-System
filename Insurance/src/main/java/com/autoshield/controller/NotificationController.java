package com.autoshield.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.autoshield.model.User;
import com.autoshield.service.NotificationService;
import com.autoshield.service.UserService;
/**
 * Provides the NotificationController functionality.
 */

@Controller
public class NotificationController {
/** The notificationService value. */

    private final NotificationService notificationService;
/** The userService value. */
    private final UserService userService;
/**
 * Executes the NotificationController operation.
 */

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }
/**
 * Executes the notifications operation.
 */

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        model.addAttribute("currentUser", user);
        model.addAttribute("notifications", notificationService.getForUser(user));
        return "customer/notifications";
    }
/**
 * Executes the markAsRead operation.
 */

    @PostMapping("/notifications/{id}/read")
    public String markAsRead(@PathVariable("id") Long id, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            notificationService.markAsRead(id, user);
        }
        return "redirect:/notifications";
    }
/**
 * Executes the markAllAsRead operation.
 */

    @PostMapping("/notifications/read-all")
    public String markAllAsRead(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());
            notificationService.markAllAsRead(user);
        }
        return "redirect:/notifications";
    }
}
