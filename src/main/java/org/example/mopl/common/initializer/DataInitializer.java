package org.example.mopl.common.initializer;

import lombok.RequiredArgsConstructor;
import org.example.mopl.user.service.RoleService;
import org.example.mopl.user.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer {

    public static final String ADMIN_PASSWORD = "admin";
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final RoleService roleService;

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            roleService.addDefaultRole();
            userService.addAdmin(passwordEncoder.encode(ADMIN_PASSWORD), roleService.getAdminRole());
        };
    }
}
