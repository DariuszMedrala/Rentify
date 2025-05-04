package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.UserRequestDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.exception.ResourceNotFoundException;
import org.example.rentify.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/*
 * UserController is a REST controller that handles user-related operations.
 * It provides endpoints for user management, including retrieving user details,
 * creating new users, updating user information, and deleting users.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for managing user accounts")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Retrieves the current authenticated user's details.
     *
     * @param authentication The authentication object containing user details.
     * @return The current user's details or an error message if not authenticated.
     */
    @Operation(summary = "Get current authenticated user's details", description = "Retrieves the profile information of the currently logged-in user.")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDTO("Error: User not authenticated."));
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        try {
            UserResponseDTO userResponseDTO = userService.findUserDtoByUsername(userDetails.getUsername());
            return ResponseEntity.ok(userResponseDTO);
        } catch (ResourceNotFoundException e) {
            logger.warn("Could not find authenticated user: {}", userDetails.getUsername(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: User not found."));
        }
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The user's details or an error message if not found.
     */
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user's details by their ID. Requires ADMIN role or for the user to be fetching their own data.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and @userService.findUserEntityById(#id).username == principal.username)")
    public ResponseEntity<?> getUserById(@Parameter(description = "ID of the user to retrieve") @PathVariable Long id) {
        try {
            UserResponseDTO userResponseDTO = userService.findUserDtoById(id);
            return ResponseEntity.ok(userResponseDTO);
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: User not found with id " + id));
        }
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve.
     * @return The user's details or an error message if not found.
     */
    @Operation(summary = "Get user by username", description = "Retrieves a specific user's details by their username.")
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and @userService.findUserEntityByUsername(#username).username == principal.username)")
    public ResponseEntity<?> getUserByUsername(@Parameter(description = "Username of the user to retrieve") @PathVariable String username) {
        try {
            UserResponseDTO userResponseDTO = userService.findUserDtoByUsername(username);
            return ResponseEntity.ok(userResponseDTO);
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found with username: {}", username, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: User not found with username " + username));
        }
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email The email of the user to retrieve.
     * @return The user's details or an error message if not found.
     */
    @Operation(summary = "Get user by email", description = "Retrieves a specific user's details by their email.")
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and @userService.findUserEntityByEmail(#email).username == principal.username)")
    public ResponseEntity<?> getUserByEmail(@Parameter(description = "Email of the user to retrieve") @PathVariable String email) {
        try {
            UserResponseDTO userResponseDTO = userService.findUserDtoByEmail(email);
            return ResponseEntity.ok(userResponseDTO);
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found with email: {}", email, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: User not found with email " + email));
        }
    }

    /**
     * Retrieves all users in a paginated format.
     *
     * @param pageable Pagination information.
     * @return A paginated list of users or an error message if retrieval fails.
     */
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users. Requires ADMIN role.")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(@PageableDefault(size = 10, sort = "username") Pageable pageable) {
        try {
            Page<UserResponseDTO> userResponseDTOPage = userService.findAllUsers(pageable);
            return ResponseEntity.ok(userResponseDTOPage);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve users due to an illegal argument: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Invalid request parameters. " + e.getMessage()));
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving users: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error: An unexpected error occurred while trying to retrieve users."));
        }
    }

    /**
     * Creates a new user account.
     *
     * @param userRegistrationDTO The registration details of the new user.
     * @return The created user's details or an error message if creation fails.
     */
    @Operation(summary = "Create New User", description = "Allows an administrator to create a new user account. The user will be created with default role.")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> adminCreateUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        try {
            UserResponseDTO userResponseDTO = userService.registerNewUser(userRegistrationDTO);
            logger.info("Admin created new user successfully: {}", userRegistrationDTO.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Admin user creation failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during admin user creation: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error: An unexpected error occurred during user creation."));
        }
    }

    /**
     * Updates an existing user's details.
     *
     * @param id The ID of the user to update.
     * @param userRequestDTO The updated user details.
     * @return The updated user's details or an error message if update fails.
     */
    @Operation(summary = "Update user by ID", description = "Updates an existing user's details. Requires ADMIN role or for the user to be updating their own data.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and @userService.findUserEntityById(#id).username == principal.username)")
    public ResponseEntity<?> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        try {
            UserResponseDTO updatedUserResponseDTO = userService.updateUser(id, userRequestDTO);
            return ResponseEntity.ok(updatedUserResponseDTO);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid data for updating user with id {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: " + e.getMessage()));
        } catch (ResourceNotFoundException e) {
            logger.error("Error updating user with id {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: User not found with id " + id));
        }
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @return A success message or an error message if deletion fails.
     */
    @Operation(summary = "Delete user by ID", description = "Deletes a user by their ID. Requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponseDTO> deleteUser(@Parameter(description = "ID of the user to delete") @PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new MessageResponseDTO("User with ID " + id + " deleted successfully."));
        } catch (ResourceNotFoundException e) {
            logger.warn("User not found for deletion with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: User not found with id " + id + ". Could not delete."));
        }
    }
}