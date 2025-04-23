package org.example.rentify.service;

import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.entity.Address;
import org.example.rentify.entity.Role;
import org.example.rentify.entity.User;
import org.example.rentify.repository.RoleRepository;
import org.example.rentify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * UserService class for managing users in the system.
 * This class provides methods to interact with the UserRepository.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Checks if a user with the given username exists.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if the email exists, false otherwise
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Registers a new user with the provided registration details.
     *
     * @param registrationDto the user registration details
     * @return the registered user
     */
    @Transactional
    public User registerNewUser(UserRegistrationDTO registrationDto) {
        if (existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setEmail(registrationDto.getEmail());
        newUser.setFirstName(registrationDto.getFirstName());
        newUser.setLastName(registrationDto.getLastName());
        newUser.setPhoneNumber(registrationDto.getPhoneNumber());
        newUser.setRegistrationDate(LocalDate.now());

        newUser.setEnabled(true);
        newUser.setAccountNonExpired(true);
        newUser.setAccountNonLocked(true);
        newUser.setCredentialsNonExpired(true);

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {

                    Role newRole = new Role();
                    newRole.setName("USER");
                    newRole.setDescription("Default user role");
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);

        if (registrationDto.getAddress() != null && registrationDto.getAddress().getStreetAddress() != null) {
            Address address = new Address();
            address.setStreetAddress(registrationDto.getAddress().getStreetAddress());
            address.setCity(registrationDto.getAddress().getCity());
            address.setStateOrProvince(registrationDto.getAddress().getStateOrProvince());
            address.setPostalCode(registrationDto.getAddress().getPostalCode());
            address.setCountry(registrationDto.getAddress().getCountry());
            newUser.setAddress(address);
        }

        return userRepository.save(newUser);
    }
}
