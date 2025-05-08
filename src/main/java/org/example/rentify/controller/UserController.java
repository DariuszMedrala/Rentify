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
import org.springframework.web.server.ResponseStatusException;


/*
 * UserController is a REST controller that handles user-related operations.
 * It provides endpoints for retrieving user details, creating new users, updating user information
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
     * @return The current user's details.
     */
    @Operation(summary = "Get current authenticated user's details", description = "Retrieves the profile information of the currently logged-in user.")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated.");
        }
        UserResponseDTO userResponseDTO = userService.findUserDtoByUsername(userDetails.getUsername());
        return ResponseEntity.ok(userResponseDTO);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The user's details.
     */
    @Operation(summary = "Get user by ID", description = "Retrieves a specific user's details by their ID. Requires ADMIN role or for the user to be fetching their own data.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and @userService.findUserEntityById(#id).username == principal.username)")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID of the user to retrieve") @PathVariable Long id) {
        UserResponseDTO userResponseDTO = userService.findUserDtoById(id);
        return ResponseEntity.ok(userResponseDTO);
    }

    /**
     * Retrieves a user by their username.
     *
     * @param username The username of the user to retrieve.
     * @return The user's details.
     */
    @Operation(summary = "Get user by username", description = "Retrieves a specific user's details by their username.")
    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and #username == principal.username)")
    public ResponseEntity<UserResponseDTO> getUserByUsername(
            @Parameter(description = "Username of the user to retrieve") @PathVariable String username) {
        UserResponseDTO userResponseDTO = userService.findUserDtoByUsername(username);
        return ResponseEntity.ok(userResponseDTO);
    }

    /**
     * Retrieves a user by their email.
     *
     * @param email The email of the user to retrieve.
     * @return The user's details.
     */
    @Operation(summary = "Get user by email", description = "Retrieves a specific user's details by their email.")
    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and @userService.findUserEntityByEmail(#email).username == principal.username)")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @Parameter(description = "Email of the user to retrieve") @PathVariable String email) {
        UserResponseDTO userResponseDTO = userService.findUserDtoByEmail(email);
        return ResponseEntity.ok(userResponseDTO);
    }

    /**
     * Retrieves all users in a paginated format.
     *
     * @param pageable Pagination information.
     * @return A paginated list of users.
     */
    @Operation(summary = "Get all users", description = "Retrieves a paginated list of all users. Requires ADMIN role.")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsers(@Parameter(
            name = "pageable",
            description = "Pageable object containing pagination information",
            example = "{\"page\": 0, \"size\": 10, \"sort\": \"username,asc\"}") @PageableDefault Pageable pageable) {
        Page<UserResponseDTO> userResponseDTOPage = userService.findAllUsers(pageable);
        return ResponseEntity.ok(userResponseDTOPage);
    }

    /**
     * Creates a new user account (by an Admin).
     *
     * @param userRegistrationDTO The registration details of the new user.
     * @return The created user's details.
     */
    @Operation(summary = "Admin Create New User", description = "Allows an administrator to create a new user account. The user will be created with default role.")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> adminCreateUser(@Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        UserResponseDTO userResponseDTO = userService.registerNewUser(userRegistrationDTO);
        logger.info("Admin created new user successfully: {}", userRegistrationDTO.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    /**
     * Updates an existing user's details.
     *
     * @param id The ID of the user to update.
     * @param userRequestDTO The updated user details.
     * @return The updated user's details.
     */
    @Operation(summary = "Update user by ID", description = "Updates an existing user's details. Requires ADMIN role or for the user to be updating their own data.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (isAuthenticated() and @userService.findUserEntityById(#id).username == principal.username)")
    public ResponseEntity<UserResponseDTO> updateUser(
            @Parameter(description = "ID of the user to update") @PathVariable Long id,
            @Valid @RequestBody UserRequestDTO userRequestDTO) {
        UserResponseDTO updatedUserResponseDTO = userService.updateUser(id, userRequestDTO);
        logger.info("User with ID {} updated successfully.", id);
        return ResponseEntity.ok(updatedUserResponseDTO);
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @return A success message.
     */
    @Operation(summary = "Delete user by ID", description = "Deletes a user by their ID. Requires ADMIN role.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponseDTO> deleteUser(
            @Parameter(description = "ID of the user to delete") @PathVariable Long id) {
        userService.deleteUser(id);
        logger.info("User with ID {} deleted successfully.", id);
        return ResponseEntity.ok(new MessageResponseDTO("User with ID " + id + " deleted successfully."));
    }
}