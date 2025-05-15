package org.example.rentify.service;

import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.LoginRequestDTO;
import org.example.rentify.dto.request.UserRequestDTO;
import org.example.rentify.dto.response.JwtResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.Role;
import org.example.rentify.entity.User;
import org.example.rentify.mapper.UserMapper;
import org.example.rentify.repository.RoleRepository;
import org.example.rentify.repository.UserRepository;
import org.example.rentify.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserService class for managing users in the system.
 * This class provides methods to interact with the UserRepository.
 * It throws specific exceptions for error conditions, to be handled by a global exception handler.
 */
@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       UserMapper userMapper,
                       AuthenticationManager authenticationManager,
                        JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registers a new user with the provided registration details.
     *
     * @param registrationDto the user registration details
     * @return the UserResponseDTO of the registered user
     * @throws IllegalArgumentException if username/email is taken or DTO is invalid
     */
    @Transactional
    public MessageResponseDTO registerNewUser(UserRegistrationDTO registrationDto) {
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User role not found"));
        newUser.setRoles(new HashSet<>(Set.of(userRole)));
        userRepository.save(newUser);
        return new MessageResponseDTO("User registered successfully: " + newUser.getUsername());
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
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
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
     * @param username The username of the user to find.
     * @return The UserResponseDTO of the found user.
     * @throws IllegalArgumentException if the username is blank
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserDtoByUsername(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with username: " + username));
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * Finds a user by their email and returns as DTO.
     *
     * @param email The email of the user to find.
     * @return The UserResponseDTO of the found user.
     * @throws IllegalArgumentException if the email is blank
     */
    @Transactional(readOnly = true)
    public UserResponseDTO findUserDtoByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("Email to find cannot be blank.");
        }
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with email: " + email));
        return userMapper.userToUserResponseDto(user);
    }

    /**
     * Updates an existing user with the provided user request DTO.
     *
     * @param id The ID of the user to update.
     * @param userRequestDTO The user request DTO containing updated user details.
     * @return A MessageResponseDTO indicating success.
     * @throws IllegalArgumentException if id is null or not positive, or if userRequestDTO is null
     * @throws ResponseStatusException if the user to update is not found, or if email is already in use
     */
    @Transactional
    public MessageResponseDTO updateUser(Long id, UserRequestDTO userRequestDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID for update must be a positive number.");
        }
        if (userRequestDTO == null) {
            throw new IllegalArgumentException("User request DTO cannot be null for update.");
        }
        User existingUser = userRepository.findUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        if (StringUtils.hasText(userRequestDTO.getEmail()) && !userRequestDTO.getEmail().equalsIgnoreCase(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userRequestDTO.getEmail())) {
                throw new IllegalArgumentException("Error: Email '" + userRequestDTO.getEmail() + "' is already in use by another account!");
            }
        }

        userMapper.updateUserFromDto(userRequestDTO, existingUser);
        userRepository.save(existingUser);
        return new MessageResponseDTO("User updated successfully: " + existingUser.getUsername());
    }

     /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @return A MessageResponseDTO indicating success.
     * @throws IllegalArgumentException if id is null or not positive
     * @throws ResponseStatusException if the user is not found
     */
    @Transactional
    public MessageResponseDTO deleteUser(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("User ID for deletion must be a positive number.");
        }
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id + ". Could not delete.");
        }
        userRepository.deleteById(id);
        return new MessageResponseDTO("User deleted successfully with id: " + id);
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

    /**
     * Authenticates a user with the provided login credentials.
     *
     * @param loginRequest The login request containing username and password.
     * @return A ResponseEntity containing the JWT response or an error message.
     */
    @Transactional(readOnly = true)
    public ResponseEntity<?> authenticateUser(LoginRequestDTO loginRequest) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtil.generateToken(authentication);
            Object principal = authentication.getPrincipal();
            String username;
            Long userId;
            String email;

            if (principal instanceof User castedUser) {
                username = castedUser.getUsername();
                userId = castedUser.getId();
                email = castedUser.getEmail();
            } else if (principal instanceof UserDetails springUser) {
                username = springUser.getUsername();
                User appUser = findUserEntityByUsername(username);
                userId = appUser.getId();
                email = appUser.getEmail();
            } else {
                throw new IllegalStateException("Unexpected principal type in authentication object");
            }
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(new JwtResponseDTO(
                    jwt,
                    userId,
                    username,
                    email,
                    roles
            ));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDTO("Error: Invalid username or password!"));
        }
    }
}