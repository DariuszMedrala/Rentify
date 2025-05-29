package org.example.rentify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.AddressRequestDTO;
import org.example.rentify.dto.request.UserRequestDTO;
import org.example.rentify.dto.response.*;
import org.example.rentify.entity.User;
import org.example.rentify.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Import(ControllerTestConfig.class)
@WebMvcTest(UserController.class)
@DisplayName("UserController Integration Tests")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    private UserResponseDTO userResponseDTO;
    private UserRequestDTO validUserRequestDTO;
    private UserRegistrationDTO validUserRegistrationDTO;

    private final Long selfUserId = 1L;
    private final String selfUsername = "selfUser";
    private final String selfUserEmail = "self@example.com";

    private final Long otherUserId = 2L;
    private final String otherUsername = "otherUser";


    private final String adminUsername = "admin";
    private User mockUserForSpell;

    @BeforeEach
    void setUp() {
        AddressRequestDTO validAddressRequestDTO = new AddressRequestDTO("123 Main St", "Anytown", "AnyState", "CountryLand", "12345");
        AddressResponseDTO addressResponseDTO = new AddressResponseDTO(1L, "123 Main St", "Anytown", "AnyState", "CountryLand", "12345");
        RoleResponseDTO roleUserResponseDTO = new RoleResponseDTO(1L, "ROLE_USER" , "Standard user role");

        userResponseDTO = new UserResponseDTO(
                selfUserId, selfUsername, selfUserEmail, "Self", "User", "1234567890",
                LocalDate.now().minusMonths(1), addressResponseDTO, Set.of(roleUserResponseDTO)
        );

        validUserRequestDTO = new UserRequestDTO(
                "updatedUser", "update@example.com", "Updated", "User", "+11234567890", validAddressRequestDTO
        );

        validUserRegistrationDTO = new UserRegistrationDTO(
                "newUser", "newPassword123!", "newuser@example.com", "New", "User", "+19876543210", validAddressRequestDTO
        );

        mockUserForSpell = new User();
        mockUserForSpell.setId(selfUserId);
        mockUserForSpell.setUsername(selfUsername);
        mockUserForSpell.setEmail(selfUserEmail);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(userService);
    }

    @Nested
    @DisplayName("GET /api/users/me")
    class GetMyDetailsTests {
        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 200 OK and current user's details")
        void whenAuthenticated_thenReturnsOwnDetails() throws Exception {
            when(userService.findUserDtoByUsername(selfUsername)).thenReturn(userResponseDTO);

            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(selfUsername))
                    .andExpect(jsonPath("$.id").value(selfUserId));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden for anonymous user")
        void whenAnonymous_thenReturns403() throws Exception {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserByIdTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN requests any user by ID")
        void whenAdminRequestsById_thenReturnsUser() throws Exception {
            when(userService.findUserDtoById(otherUserId)).thenReturn(new UserResponseDTO(otherUserId, otherUsername, null, null, null, null, null, null, null));
            mockMvc.perform(get("/api/users/{id}", otherUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(otherUserId));
        }

        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 200 OK when user requests their own details by ID")
        void whenUserRequestsOwnId_thenReturnsUser() throws Exception {
            when(userService.findUserEntityById(selfUserId)).thenReturn(mockUserForSpell);
            when(userService.findUserDtoById(selfUserId)).thenReturn(userResponseDTO);

            mockMvc.perform(get("/api/users/{id}", selfUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(selfUserId))
                    .andExpect(jsonPath("$.username").value(selfUsername));
        }

        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 403 Forbidden when user requests another user's details by ID")
        void whenUserRequestsOtherId_thenReturns403() throws Exception {
            User otherMockUser = new User();
            otherMockUser.setId(otherUserId);
            otherMockUser.setUsername(otherUsername);
            when(userService.findUserEntityById(otherUserId)).thenReturn(otherMockUser);

            mockMvc.perform(get("/api/users/{id}", otherUserId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 404 Not Found when requesting non-existent user ID")
        void whenUserNotFoundById_thenReturns404() throws Exception {
            Long nonExistentId = 999L;
            String errorMessage = "User not found with ID: " + nonExistentId;
            when(userService.findUserDtoById(nonExistentId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(get("/api/users/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("GET /api/users/username/{username}")
    class GetUserByUsernameTests {

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN requests any user by username")
        void whenAdminRequestsByUsername_thenReturnsUser() throws Exception {
            when(userService.findUserDtoByUsername(otherUsername)).thenReturn(new UserResponseDTO(otherUserId, otherUsername, null, null, null, null, null, null, null));
            mockMvc.perform(get("/api/users/username/{username}", otherUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(otherUsername));
        }

        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 200 OK when user requests their own details by username")
        void whenUserRequestsOwnUsername_thenReturnsUser() throws Exception {
            when(userService.findUserDtoByUsername(selfUsername)).thenReturn(userResponseDTO);
            mockMvc.perform(get("/api/users/username/{username}", selfUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(selfUsername));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 403 Forbidden for username too short (assuming @Size is used)")
        void whenUsernameTooShort_thenReturns400() throws Exception {
            String shortUsername = "ab";

            mockMvc.perform(get("/api/users/username/{username}", shortUsername))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("Username must be between 3 and 50 characters")));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 403 Forbidden for username too long (assuming @Size is used)")
        void whenUsernameTooLong_thenReturns400() throws Exception {
           String longUsername = "a".repeat(51);
            mockMvc.perform(get("/api/users/username/{username}", longUsername))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("Username must be between 3 and 50 characters")));
        }
    }


    @Nested
    @DisplayName("GET /api/users/email/{email}")
    class GetUserByEmailTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN requests user by email")
        void whenAdminRequestsByEmail_thenReturnsUser() throws Exception {
            String otherUserEmail = "other@example.com";
            when(userService.findUserDtoByEmail(otherUserEmail)).thenReturn(new UserResponseDTO(otherUserId, otherUsername, otherUserEmail, null, null, null, null, null, null));
            mockMvc.perform(get("/api/users/email/{email}", otherUserEmail))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(otherUserEmail));
        }

        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 200 OK when user requests their own details by email")
        void whenUserRequestsOwnEmail_thenReturnsUser() throws Exception {
            when(userService.findUserEntityByEmail(selfUserEmail)).thenReturn(mockUserForSpell);
            when(userService.findUserDtoByEmail(selfUserEmail)).thenReturn(userResponseDTO);
            mockMvc.perform(get("/api/users/email/{email}", selfUserEmail))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value(selfUserEmail));
        }
    }

    @Nested
    @DisplayName("GET /api/users/all")
    class GetAllUsersTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK and page of users for ADMIN")
        void whenAdminRequestsAllUsers_thenReturnsPage() throws Exception {
            Page<UserResponseDTO> userPage = new PageImpl<>(List.of(userResponseDTO), PageRequest.of(0, 1), 1);
            when(userService.findAllUsers(any(Pageable.class))).thenReturn(userPage);

            mockMvc.perform(get("/api/users/all").param("page", "0").param("size", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].username").value(selfUsername));
        }

        @Test
        @WithMockUser(username = selfUsername, roles = "USER")
        @DisplayName("should return 403 Forbidden for non-ADMIN user")
        void whenNonAdminRequestsAllUsers_thenReturns403() throws Exception {
            mockMvc.perform(get("/api/users/all"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/users/create (Admin Create User)")
    class AdminCreateUserTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN creates user with valid DTO")
        void whenAdminCreatesUserWithValidDto_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("User registered successfully!");
            when(userService.registerNewUser(any(UserRegistrationDTO.class))).thenReturn(successResponse);

            mockMvc.perform(post("/api/users/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUserRegistrationDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for invalid UserRegistrationDTO")
        void whenAdminCreatesUserWithInvalidDto_thenReturns400() throws Exception {
            UserRegistrationDTO invalidDto = new UserRegistrationDTO();
            invalidDto.setPassword("ValidPass1!");
            invalidDto.setEmail("bad");

            mockMvc.perform(post("/api/users/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("Validation Error:")));
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUserTests {
        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 200 OK when user updates their own details with valid DTO")
        void whenUserUpdatesOwnDetails_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("User updated successfully.");
            when(userService.findUserEntityById(selfUserId)).thenReturn(mockUserForSpell);
            when(userService.updateUser(eq(selfUserId), any(UserRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/users/{id}", selfUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUserRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN updates any user with valid DTO")
        void whenAdminUpdatesUser_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("User updated successfully.");
            when(userService.updateUser(eq(otherUserId), any(UserRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/users/{id}", otherUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validUserRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 400 when user updates with invalid DTO (e.g. invalid email)")
        void whenUserUpdatesWithInvalidDto_thenReturns400() throws Exception {
            UserRequestDTO invalidUpdateDto = new UserRequestDTO();
            invalidUpdateDto.setEmail("invalid-email");
            invalidUpdateDto.setUsername("validUser");

            when(userService.findUserEntityById(selfUserId)).thenReturn(mockUserForSpell);

            mockMvc.perform(put("/api/users/{id}", selfUserId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidUpdateDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("Validation Error: email: Invalid email format")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUserTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN deletes user")
        void whenAdminDeletesUser_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("User deleted successfully.");
            when(userService.deleteUser(otherUserId)).thenReturn(successResponse);

            mockMvc.perform(delete("/api/users/{id}", otherUserId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = selfUsername, roles = "USER")
        @DisplayName("should return 403 Forbidden when non-ADMIN tries to delete user")
        void whenNonAdminDeletesUser_thenReturns403() throws Exception {
            mockMvc.perform(delete("/api/users/{id}", otherUserId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/update/password")
    class ChangePasswordTests {
        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 200 OK for valid password change")
        void whenValidPasswordChange_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Password changed successfully.");
            when(userService.changePassword(eq(selfUsername), eq("NewValidPass1!"), eq("OldValidPass1!")))
                    .thenReturn(successResponse);

            mockMvc.perform(patch("/api/users/update/password")
                            .param("newPassword", "NewValidPass1!")
                            .param("oldPassword", "OldValidPass1!"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = selfUsername)
        @DisplayName("should return 400 Bad Request for invalid new password format")
        void whenNewPasswordInvalidFormat_thenReturns400() throws Exception {
            mockMvc.perform(patch("/api/users/update/password")
                            .param("newPassword", "short")
                            .param("oldPassword", "OldValidPass1!"))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("Validation Error: newPassword: Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden for anonymous user")
        void whenAnonymousChangesPassword_thenReturns403() throws Exception {
            mockMvc.perform(patch("/api/users/update/password")
                            .param("newPassword", "NewValidPass1!")
                            .param("oldPassword", "OldValidPass1!"))
                    .andExpect(status().isForbidden());
        }
    }
}