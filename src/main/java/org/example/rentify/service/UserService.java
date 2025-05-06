package org.example.rentify.service;

import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.UserRequestDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.Role;
import org.example.rentify.entity.User;
import org.example.rentify.mapper.UserMapper;
import org.example.rentify.repository.RoleRepository;
import org.example.rentify.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * UserService class for managing users in the system.
 * This class provides methods to interact with the UserRepository.
 * It throws specific exceptions for error conditions, to be handled by a global exception handler.
 */
@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }
    /**
     * Registers a new user with the provided registration details.
     *
     * @param registrationDto the user registration details
     * @return the UserResponseDTO of the registered user
     * @throws IllegalArgumentException if username/email is taken or DTO is invalid
     */
    @Transactional
    public UserResponseDTO registerNewUser(UserRegistrationDTO registrationDto) {

        if (registrationDto == null) {
            throw new IllegalArgumentException("Registration DTO cannot be null.");
        }

        if (!StringUtils.hasText(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Username cannot be blank.");
        }
        if (!StringUtils.hasText(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }
        if (!StringUtils.hasText(registrationDto.getPassword())) {
            throw new IllegalArgumentException("Password cannot be blank.");
        }

        logger.info("Attempting to register new user: {}", registrationDto.getUsername());

        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Error: Username '" + registrationDto.getUsername() + "' is already taken!");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Error: Email '" + registrationDto.getEmail() + "' is already in use!");
        }

        User newUser = userMapper.userRegistrationDtoToUser(registrationDto);
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        newUser.setRegistrationDate(LocalDate.now());

        Role userRole = roleRepository.findRoleByName("USER")
                .orElseGet(() -> {
                    logger.info("USER role not found, creating it.");
                    Role newRole = new Role();
                    newRole.setName("USER");
                    newRole.setDescription("Default user role");
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        newUser.setRoles(roles);
        User savedUser = userRepository.save(newUser);
        logger.info("User registered successfully: {}", savedUser.getUsername());
        return userMapper.userToUserResponseDto(savedUser);
    }

    /**
     * Finds a user by their ID and returns as DTO.
     *
     * @param id The ID of the user to find.
     * @return The UserResponseDTO of the found user.
     * @throws IllegalArgumentException if id is null or not positive
     * @throws ResponseStatusException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserDtoById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID must be a positive number.");
        }
        logger.debug("Finding user by ID: {}", id);
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id);
                });
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * Retrieves all users in a paginated format as DTOs.
     *
     * @param pageable Pagination information.
     * @return A Page of UserResponseDTO.
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllUsers(Pageable pageable) {
        logger.debug("Fetching all users, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::userToUserResponseDto);
    }

    /**
     * Finds a user by their username and returns as DTO.
     *
     * @param username The username of the user to find.
     * @return The UserResponseDTO of the found user.
     * @throws IllegalArgumentException if the username is blank
     * @throws ResponseStatusException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserDtoByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username to find cannot be blank.");
        }
        logger.debug("Finding user by username: {}", username);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with username: " + username);
                });
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * Finds a user by their email and returns as DTO.
     *
     * @param email The email of the user to find.
     * @return The UserResponseDTO of the found user.
     * @throws IllegalArgumentException if the email is blank
     * @throws ResponseStatusException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserDtoByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email to find cannot be blank.");
        }
        logger.debug("Finding user by email: {}", email);
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email);
                });
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * Updates an existing user's information.
     *
     * @param id The ID of the user to update.
     * @param userRequestDTO DTO containing the fields to update.
     * @return The UserResponseDTO of the updated user.
     * @throws IllegalArgumentException if id is invalid or new email is already in use
     * @throws ResponseStatusException if the user to update is not found.
     */
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID for update must be a positive number.");
        }
        if (userRequestDTO == null) {
            throw new IllegalArgumentException("User request DTO cannot be null for update.");
        }
        logger.info("Attempting to update user with ID: {}", id);
        User existingUser = userRepository.findUserById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found for update with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id);
                });

        if (StringUtils.hasText(userRequestDTO.getEmail()) && !userRequestDTO.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
                throw new IllegalArgumentException("Error: Email '" + userRequestDTO.getEmail() + "' is already in use by another account!");
            }
        }
        userMapper.updateUserFromDto(userRequestDTO, existingUser);

        User updatedUser = userRepository.save(existingUser);
        logger.info("User updated successfully: {}", updatedUser.getUsername());
        return userMapper.userToUserResponseDto(updatedUser);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @throws IllegalArgumentException if id is null or not positive
     * @throws ResponseStatusException if the user to delete is not found.
     */
    @Transactional
    public void deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID for deletion must be a positive number.");
        }
        logger.info("Attempting to delete user with ID: {}", id);
        if (!userRepository.existsById(id)) {
            logger.warn("User not found for deletion with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id + ". Could not delete.");
        }
        userRepository.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }

    /**
     * Finds a user by their ID and returns the entity.
     *
     * @param id The ID of the user to find.
     * @return The User entity of the found user.
     * @throws IllegalArgumentException if id is null or not positive
     * @throws ResponseStatusException if the user is not found.
     */
    @Transactional(readOnly = true)
    public User findUserEntityById(Long id) {
        if (id == null || id <= 0) {
            // This check is useful even for internal methods if IDs are passed around.
            throw new IllegalArgumentException("User ID must be a positive number for entity lookup.");
        }
        return userRepository.findUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User entity not found with id: " + id));
    }

    /**
     * Finds a user by their username and returns the entity.
     *
     * @param username The username of the user to find.
     * @return The User entity of the found user.
     * @throws IllegalArgumentException if the username is blank
     * @throws ResponseStatusException if the user is not found.
     */
    @Transactional(readOnly = true)
    public User findUserEntityByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username for entity lookup cannot be blank.");
        }
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"User entity not found with username: " + username));
    }

    /**
     * Finds a user by their email and returns the entity.
     *
     * @param email The email of the user to find.
     * @return The User entity of the found user.
     * @throws IllegalArgumentException if the email is blank
     * @throws ResponseStatusException if the user is not found.
     */
    @Transactional(readOnly = true)
    public User findUserEntityByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email for entity lookup cannot be blank.");
        }
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User entity not found with email: " + email));
    }
}