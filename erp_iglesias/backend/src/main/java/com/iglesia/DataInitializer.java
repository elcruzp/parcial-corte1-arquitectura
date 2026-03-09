package com.iglesia;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!appUserRepository.existsByEmailIgnoreCase("admin@parroquia.com")) {
            AppUser admin = new AppUser();
            admin.setEmail("admin@parroquia.com");
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            admin.setRole(UserRole.ADMIN);
            appUserRepository.save(admin);
        }
    }
}
