package com.tracker.config;

import com.tracker.model.ApprovalStatus;
import com.tracker.model.Role;
import com.tracker.model.User;
import com.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Creates the fixed administrator account on startup and keeps its credentials in sync with configuration. */
@Component
public class AdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.email}") String adminEmail,
                            @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        Optional<User> existing = userRepository.findByEmailIgnoreCase(adminEmail);
        if (existing.isEmpty()) {
            User admin = new User();
            admin.setFullName("System Administrator");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setApprovalStatus(ApprovalStatus.APPROVED);
            admin.setDepartment("Administration");
            userRepository.save(admin);
            return;
        }
        User admin = existing.get();
        boolean changed = false;
        if (admin.getRole() != Role.ADMIN) {
            admin.setRole(Role.ADMIN);
            changed = true;
        }
        if (!passwordEncoder.matches(adminPassword, admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(adminPassword));
            changed = true;
        }
        if (changed) {
            userRepository.save(admin);
        }
    }
}