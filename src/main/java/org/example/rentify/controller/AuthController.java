package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.LoginRequestDTO;
import org.example.rentify.dto.response.JwtResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.User;
import org.example.rentify.security.jwt.JwtUtil;
import org.example.rentify.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AuthController is a REST controller that handles authentication and registration requests.
 * It provides endpoints for user login and registration.
 */
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager,
                          UserService userService,
                          JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param loginRequest The login request containing username and password.
     * @return A JWT token if authentication is successful, or an error message if it fails.
     */
    @Operation(summary = "User Login", description = "Authenticates a user based on their username and password.")
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequestDTO loginRequest) {
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
                User appUser = userService.findUserEntityByUsername(username);
                userId = appUser.getId();
                email = appUser.getEmail();
            } else {
                logger.error("Unexpected principal type: {}", principal.getClass().getName());
                throw new IllegalStateException("Unexpected principal type in authentication object");
            }

            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            logger.info("User '{}' logged in successfully.", username);

            return ResponseEntity.ok(new JwtResponseDTO(
                    jwt,
                    userId,
                    username,
                    email,
                    roles
            ));
        } catch (BadCredentialsException e) {
            logger.warn("Login attempt failed for user '{}': Invalid credentials", loginRequest.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new MessageResponseDTO("Error: Invalid username or password!"));
        }
    }

    /**
     * Registers a new user with the provided details.
     *
     * @param signUpRequest The UserRegistrationDTO containing user details.
     * @return The registered user's details or an error message if registration fails.
     */
    @Operation(summary = "User Registration", description = "Registers a new user based on their provided details.")
    @PostMapping("/register")
    public ResponseEntity<UserResponseDTO> registerUser(@Valid @RequestBody UserRegistrationDTO signUpRequest) {
        UserResponseDTO registeredUserDto = userService.registerNewUser(signUpRequest);
        logger.info("User registered successfully: {}", registeredUserDto.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).body(registeredUserDto);
    }
}