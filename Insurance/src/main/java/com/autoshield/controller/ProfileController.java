package com.autoshield.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.autoshield.model.User;
import com.autoshield.service.UserService;

/**
 * Epic 1, US-04/US-05 (view/update profile) and US-09 (change password).
 * Both actions redirect back to the dashboard's Profile tab either way, with
 * a query flag the template uses to show a success or error banner.
 */
/**
 * Provides the ProfileController functionality.
 */
@Controller
public class ProfileController {
/** The userService value. */

    private final UserService userService;
/**
 * Executes the ProfileController operation.
 */

    public ProfileController(UserService userService) {
        this.userService = userService;
    }
/**
 * Processes the updateProfile operation.
 */

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                @RequestParam("fullName") String fullName,
                                @RequestParam("phoneNumber") String phoneNumber,
                                Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());
        userService.updateProfile(user.getId(), fullName, phoneNumber);
        return "redirect:/dashboard?tab=profile&profileUpdated=true";
    }
/**
 * Executes the changePassword operation.
 */

    @PostMapping("/profile/change-password")
    public String changePassword(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmPassword") String confirmPassword) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        User user = userService.findByEmail(userDetails.getUsername());

        if (!newPassword.equals(confirmPassword)) {
            return "redirect:/dashboard?tab=profile&passwordError=" + encode("New password and confirmation don't match.");
        }
        try {
            userService.changePassword(user.getId(), currentPassword, newPassword);
            return "redirect:/dashboard?tab=profile&passwordChanged=true";
        } catch (IllegalArgumentException e) {
            return "redirect:/dashboard?tab=profile&passwordError=" + encode(e.getMessage());
        }
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}