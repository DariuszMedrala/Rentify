package org.example.rentify.service;

import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.UserRequestDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.Role;
import org.example.rentify.entity.User;
import org.example.rentify.exception.ResourceNotFoundException;
import org.example.rentify.mapper.UserMapper;
import org.example.rentify.repository.RoleRepository;
import org.example.rentify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserMapper userMapper; // UserMapper is still needed here

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper) { // Inject UserMapper
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
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
     * @return the UserResponseDTO of the registered user
     */
    @Transactional
    public UserResponseDTO registerNewUser(UserRegistrationDTO registrationDto) {

        if (existsByUsername(registrationDto.getUsername())) {
            throw new IllegalArgumentException("Error: Username is already taken!");
        }

        if (existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("Error: Email is already in use!");
        }

        User newUser = userMapper.userRegistrationDtoToUser(registrationDto);
        newUser.setPassword(passwordEncoder.encode(registrationDto.getPassword()));

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
        User savedUser = userRepository.save(newUser);
        return userMapper.userToUserResponseDto(savedUser);
    }

    /**
     * Finds a user by their ID and returns as DTO.
     *
     * @param id The ID of the user to find.
     * @return The UserResponseDTO of the found user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserDtoById(Long id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
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
        Page<User> userPage = userRepository.findAll(pageable);
        return userPage.map(userMapper::userToUserResponseDto);
    }

    /**
     * Finds a user by their username and returns as DTO.
     *
     * @param username The username of the user to find.
     * @return The UserResponseDTO of the found user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserDtoByUsername(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * Finds a user by their email and returns as DTO.
     *
     * @param email The email of the user to find.
     * @return The UserResponseDTO of the found user.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserDtoByEmail(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * Updates an existing user's information.
     *
     * @param id The ID of the user to update.
     * @param userRequestDTO DTO containing the fields to update.
     * @return The UserResponseDTO of the updated user.
     * @throws ResourceNotFoundException if the user to update is not found.
     * @throws IllegalArgumentException if the new email is already in use by another user.
     */
    @Transactional
    public UserResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        User existingUser = userRepository.findUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        if (userRequestDTO.getEmail() != null && !userRequestDTO.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
                throw new IllegalArgumentException("Error: Email is already in use by another account!");
            }
        }
        userMapper.updateUserFromDto(userRequestDTO, existingUser);

        User updatedUser = userRepository.save(existingUser);
        return userMapper.userToUserResponseDto(updatedUser);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @throws ResourceNotFoundException if the user to delete is not found.
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id + ". Could not delete.");
        }
        userRepository.deleteById(id);
    }

    /**
     * Finds a user entity by their ID. (For internal service use if needed)
     *
     * @param id The ID of the user to find.
     * @return The found User entity.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional(readOnly = true)
    public User findUserEntityById(Long id) {
        return userRepository.findUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    /**
     * Finds a user entity by their username. (For internal service use if needed)
     *
     * @param username The username of the user to find.
     * @return The found User entity.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional(readOnly = true)
    public User findUserEntityByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Finds a user entity by their email. (For internal service use if needed)
     *
     * @param email The email of the user to find.
     * @return The found User entity.
     * @throws ResourceNotFoundException if the user is not found.
     */
    @Transactional(readOnly = true)
    public User findUserEntityByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }
}