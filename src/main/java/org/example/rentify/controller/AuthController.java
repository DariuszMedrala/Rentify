package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.LoginRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController is a REST controller that handles authentication and registration requests.
 * It provides endpoints for user login and registration.
 */
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Authenticates a user based on the provided login request.
     *
     * @param loginRequest The LoginRequestDTO containing username and password.
     * @return A ResponseEntity containing JWT token and user details if authentication is successful.
     * @throws BadCredentialsException if the username or password is incorrect.
     */
    @Operation(summary = "User Login", description = "Authenticates a user based on their username and password.")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Parameter(description = "User loging request DTO")
                                                  @Valid @RequestBody LoginRequestDTO loginRequest) {

        return userService.authenticateUser(loginRequest);
    }


    /**
     * Registers a new user based on the provided registration details.
     *
     * @param userRegistrationDTO The UserRegistrationDTO containing user details.
     * @return A MessageResponseDTO indicating the result of the registration.
     */
    @Operation(summary = "User Registration", description = "Registers a new user based on their provided details.")
    @PostMapping("/register")
    public MessageResponseDTO registerNewUser(@Parameter(description = "User Registration DTO") @Valid @RequestBody UserRegistrationDTO userRegistrationDTO) {
        return userService.registerNewUser(userRegistrationDTO);
    }
}