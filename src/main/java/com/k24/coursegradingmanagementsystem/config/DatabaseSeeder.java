package com.k24.coursegradingmanagementsystem.config;

import com.k24.coursegradingmanagementsystem.entity.User;
import com.k24.coursegradingmanagementsystem.enums.Role;
import com.k24.coursegradingmanagementsystem.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        seedUser("admin", "admin@coursehub.com", "Admin@123", "System Administrator", Role.ADMIN);
        seedUser("lecturer01", "lecturer01@coursehub.com", "Lecturer@123", "Course Lecturer 01", Role.LECTURER);
        seedUser("student01", "student01@coursehub.com", "Student@123", "Course Student 01", Role.STUDENT);
    }

    private void seedUser(String username, String email, String password, String fullName, Role role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(passwordEncoder.encode(password))
                    .fullName(fullName)
                    .role(role)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            log.info("Successfully seeded default user: {} with role: {}", username, role);
        } else {
            log.debug("Default user {} already exists. Skipping seeding.", username);
        }
    }
}
