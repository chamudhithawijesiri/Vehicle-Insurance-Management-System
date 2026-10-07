package com.autoshield.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.autoshield.model.InsurancePlan;
import com.autoshield.model.User;
import com.autoshield.repository.InsurancePlanRepository;
import com.autoshield.repository.UserRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final InsurancePlanRepository insurancePlanRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Value("${app.wallpaper.base-path:}")
    private String wallpaperBasePath;

    public DataInitializer(UserRepository userRepository,
                           InsurancePlanRepository insurancePlanRepository,
                           PasswordEncoder passwordEncoder,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.insurancePlanRepository = insurancePlanRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        migrateLegacyUserSchema();

        // 1. Seed the four insurance plans if the table is empty
        if (insurancePlanRepository.count() == 0) {
            insurancePlanRepository.save(new InsurancePlan("Motor Third Party",
                    "Covers damage/injury you cause to third parties only.", 3000.0));
            insurancePlanRepository.save(new InsurancePlan("Motor Comprehensive",
                    "Covers your own vehicle plus third-party liability.", 85000.0));
            insurancePlanRepository.save(new InsurancePlan("Motor Full Insurance",
                    "Comprehensive cover plus additional add-ons.", 110000.0));
            insurancePlanRepository.save(new InsurancePlan("Commercial Vehicle Insurance",
                    "Cover for vehicles used for commercial/business purposes.", 125000.0));
            System.out.println("Seeded default insurance plans.");
        }

        // 2. Initialize or repair the default admin account.
        ensureStaffAccount("admin@gmail.com", "admin1234",
            "Auto Shield Admin", "ROLE_ADMIN", "0000000000");

            // 3. Initialize the role accounts used by the staff dashboards.
            ensureStaffAccount("insuranceofficer@gmail.com", "insuranceofficer123",
                "Insurance Officer", "ROLE_OFFICER", "0000000001");
            ensureStaffAccount("vehiclesurveyor@gmail.com", "vehiclesurveyor123",
                "Vehicle Surveyor", "ROLE_SURVEYOR", "0000000002");
            ensureStaffAccount("financeofficer@gmail.com", "financeofficer123",
                "Finance Officer", "ROLE_FINANCE", "0000000003");

            // 4. Optionally copy generated wallpapers to static resources if configured
        if (wallpaperBasePath != null && !wallpaperBasePath.isBlank()) {
            File base = new File(wallpaperBasePath);
            if (base.exists() && base.isDirectory()) {
                copyWallpaper(new File(base, "liquid_glass_car_1785757397513.png"), new File("src/main/resources/static/images/vehicle_wallpaper.png"));
                copyWallpaper(new File(base, "liquid_glass_suv_1785757736856.png"), new File("src/main/resources/static/images/suv_wallpaper.png"));
                copyWallpaper(new File(base, "liquid_glass_moto_1785757758270.png"), new File("src/main/resources/static/images/moto_wallpaper.png"));
            }
        }
    }

    private void ensureStaffAccount(String email, String password, String fullName,
                                    String role, String phoneNumber) {
        User user = userRepository.findByEmail(email).orElseGet(User::new);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setPhoneNumber(phoneNumber);
        user.setRole(role);
        user.setActive(true);
        userRepository.save(user);
        System.out.println("Staff account ready: " + email + " (" + role + ")");
    }

    private void migrateLegacyUserSchema() {
        Integer legacyColumnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns "
                        + "WHERE table_schema = DATABASE() AND table_name = 'users' "
                        + "AND column_name = 'policy_plan'",
                Integer.class);
        if (legacyColumnCount != null && legacyColumnCount > 0) {
            jdbcTemplate.execute("ALTER TABLE users MODIFY COLUMN policy_plan VARCHAR(255) NULL");
            System.out.println("Updated legacy users.policy_plan column to allow current account registration.");
        }
    }

    private void copyWallpaper(File srcFile, File destFile) {
        if (srcFile.exists()) {
            try {
                destFile.getParentFile().mkdirs();
                Files.copy(srcFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Wallpaper copied successfully to: " + destFile.getName());
            } catch (IOException | SecurityException e) {
                System.err.println("Could not copy wallpaper " + destFile.getName() + " automatically: " + e.getMessage());
            }
        }
    }
}