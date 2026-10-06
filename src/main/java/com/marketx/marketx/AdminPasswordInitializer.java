package com.marketx.marketx;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminPasswordInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminPasswordInitializer(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        Admin admin = adminRepository
                .findByUsername("admin")
                .orElse(null);

        if (admin == null) {
            return;
        }

        String password = admin.getPassword();

        // Only encode if the password is not already BCrypt
        if (password != null &&
            !password.startsWith("$2a$") &&
            !password.startsWith("$2b$") &&
            !password.startsWith("$2y$")) {

            admin.setPassword(
                    passwordEncoder.encode(password)
            );

            adminRepository.save(admin);

            System.out.println(
                    "Admin password converted to BCrypt."
            );
        }
    }
}