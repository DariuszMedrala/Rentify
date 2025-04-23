package org.example.rentify.config;

import org.example.rentify.entity.Role;
import org.example.rentify.entity.User;
import org.example.rentify.repository.RoleRepository;
import org.example.rentify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * DataInitializer is a CommandLineRunner that initializes the database with default roles and an admin user.
 * It runs when the application starts.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

    }

    /**
     * This method is called when the application starts.
     * It initializes the database with default roles and an admin user if they do not exist.
     * @param args command line arguments
     * @throws Exception if an error occurs during initialization
     */
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ADMIN");
                    newRole.setDescription("Administrator role with full access");
                    return roleRepository.save(newRole);
                });

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("USER");
                    newRole.setDescription("Default user role");
                    return roleRepository.save(newRole);
                });

        String adminUsername = "admin";
        if (!userRepository.existsByUsername(adminUsername)) {
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setPassword(passwordEncoder.encode("AdminPassword123!"));
            adminUser.setEmail("admin@rentify.com");
            adminUser.setFirstName("Admin");
            adminUser.setLastName("User");
            adminUser.setPhoneNumber("0000000000");
            adminUser.setRegistrationDate(LocalDate.now());
            adminUser.setEnabled(true);
            adminUser.setAccountNonExpired(true);
            adminUser.setAccountNonLocked(true);
            adminUser.setCredentialsNonExpired(true);

            Set<Role> adminRoles = new HashSet<>();
            adminRoles.add(adminRole);
            adminRoles.add(userRole);
            adminUser.setRoles(adminRoles);

            userRepository.save(adminUser);
        }
    }
}