package org.example.rentify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.LoginRequestDTO;
import org.example.rentify.dto.response.JwtResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;


@Import(ControllerTestConfig.class)
@WebMvcTest(AuthController.class)
@DisplayName("AuthController Integration Tests")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDTO validLoginRequest;
    private UserRegistrationDTO validUserRegistrationRequest;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequestDTO();
        validLoginRequest.setUsername("testUser");
        validLoginRequest.setPassword("password123");

        validUserRegistrationRequest = new UserRegistrationDTO();
        validUserRegistrationRequest.setUsername("newUser");
        validUserRegistrationRequest.setEmail("newuser@example.com");
        validUserRegistrationRequest.setPassword("ValidPass@123");
        validUserRegistrationRequest.setFirstName("New");
        validUserRegistrationRequest.setLastName("User");
        validUserRegistrationRequest.setPhoneNumber("1234567890");
        validUserRegistrationRequest.setAddress(null);
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpointTests {

        @Test
        @DisplayName("should return 200 OK with JWT on successful login")
        void whenLoginWithValidCredentials_thenReturns200AndJwt() throws Exception {
            JwtResponseDTO jwtResponse = new JwtResponseDTO("fake-jwt-token", 1L, "testUser", "testuser@example.com", List.of("ROLE_USER"));
            when(userService.authenticateUser(any(LoginRequestDTO.class)))
                    .thenAnswer(invocation -> ResponseEntity.ok(jwtResponse));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.token").value("fake-jwt-token"))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.username").value("testUser"))
                    .andExpect(jsonPath("$.email").value("testuser@example.com"))
                    .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
        }

        @Test
        @DisplayName("should return 401 Unauthorized for bad credentials")
        void whenLoginWithInvalidCredentials_thenReturns401() throws Exception {
            MessageResponseDTO errorMsg = new MessageResponseDTO("Error: Invalid username or password!");
            when(userService.authenticateUser(any(LoginRequestDTO.class)))
                    .thenAnswer(invocation -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorMsg));

            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validLoginRequest))
                            .with(csrf()))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Error: Invalid username or password!"));
        }

        @Test
        @DisplayName("should return 400 Bad Request for invalid login request DTO (validation failure)")
        void whenLoginWithInvalidRequestDto_thenReturns400() throws Exception {
            LoginRequestDTO invalidLoginRequest = new LoginRequestDTO();
            invalidLoginRequest.setUsername("");

            MvcResult result = mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidLoginRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            String actualMessage = JsonPath.read(responseBody, "$.message");
            assertThat(actualMessage).startsWith("Validation Error: ");
            assertThat(actualMessage).contains("username: Username cannot be blank");
            assertThat(actualMessage).contains("password: Password cannot be blank");
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpointTests {

        @Test
        @DisplayName("should return 200 OK with success message on successful registration")
        void whenRegisterWithValidDetails_thenReturns200AndSuccessMessage() throws Exception {
            MessageResponseDTO successMessage = new MessageResponseDTO("User registered successfully: " + validUserRegistrationRequest.getUsername());
            when(userService.registerNewUser(any(UserRegistrationDTO.class))).thenReturn(successMessage);

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUserRegistrationRequest))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("User registered successfully: newUser"));
        }

        @Test
        @DisplayName("should return 400 Bad Request when username is already taken")
        void whenRegisterWithExistingUsername_thenReturns400() throws Exception {
            String errorMessage = "Error: Username '" + validUserRegistrationRequest.getUsername() + "' is already taken!";
            when(userService.registerNewUser(any(UserRegistrationDTO.class)))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUserRegistrationRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("should return 400 Bad Request when email is already in use")
        void whenRegisterWithExistingEmail_thenReturns400() throws Exception {
            String errorMessage = "Error: Email '" + validUserRegistrationRequest.getEmail() + "' is already in use!";
            when(userService.registerNewUser(any(UserRegistrationDTO.class)))
                    .thenThrow(new IllegalArgumentException(errorMessage));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUserRegistrationRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("should return 404 Not Found when user role is not found during registration")
        void whenRegisterAndRoleNotFound_thenReturns404() throws Exception {
            String errorMessage = "User role not found";
            when(userService.registerNewUser(any(UserRegistrationDTO.class)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUserRegistrationRequest))
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("should return 400 Bad Request for invalid registration DTO (validation failure)")
        void whenRegisterWithInvalidDto_thenReturns400() throws Exception {
            UserRegistrationDTO invalidRegistrationRequest = new UserRegistrationDTO();
            invalidRegistrationRequest.setEmail("not-an-email");
            invalidRegistrationRequest.setUsername("u");
            MvcResult result = mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRegistrationRequest))
                            .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            String actualMessage = JsonPath.read(responseBody, "$.message");
            assertThat(actualMessage).startsWith("Validation Error: ");
            assertThat(actualMessage).contains("username: Username must be between 3 and 50 characters");
            assertThat(actualMessage).contains("password: Password cannot be blank");
            assertThat(actualMessage).contains("lastName: Last name cannot be blank");
            assertThat(actualMessage).contains("email: Invalid email format");
            assertThat(actualMessage).contains("firstName: First name cannot be blank");
        }
    }
}