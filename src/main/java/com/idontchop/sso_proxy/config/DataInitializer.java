package com.idontchop.sso_proxy.config;

import com.idontchop.sso_proxy.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;

    @Override
    public void run(String... args) throws Exception {
        // Create default admin user for testing
        if (!userService.userExists("admin")) {
            userService.createUser("admin", "password", "admin@example.com");
            log.info("Default admin user created - Username: admin, Password: password");
        }

        // Create default test user
        if (!userService.userExists("user")) {
            userService.createUser("user", "password", "user@example.com");
            log.info("Default test user created - Username: user, Password: password");
        }

        log.info("=".repeat(50));
        log.info("SSO Proxy Application Started Successfully!");
        log.info("Login URL: http://localhost:8080/login");
        log.info("Test Credentials:");
        log.info("  Username: admin | Password: password");
        log.info("  Username: user  | Password: password");
        log.info("H2 Console: http://localhost:8080/h2-console");
        log.info("=".repeat(50));
    }
}