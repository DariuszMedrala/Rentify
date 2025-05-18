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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private Role userRole;
    private final String testUsername = "testUser";
    private UserRegistrationDTO userRegistrationDTO;
    private UserRequestDTO userRequestDTO;
    private UserResponseDTO userResponseDTO;
    private LoginRequestDTO loginRequestDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setRegistrationDate(LocalDate.now());

        userRole = new Role();
        userRole.setId(1L);
        userRole.setName("USER");
        user.setRoles(new HashSet<>(Set.of(userRole)));

        userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername("newuser");
        userRegistrationDTO.setEmail("new@example.com");
        userRegistrationDTO.setPassword("password123");
        userRegistrationDTO.setFirstName("New");
        userRegistrationDTO.setLastName("User");
        userRegistrationDTO.setPhoneNumber("123456789");

        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setEmail("updated@example.com");
        userRequestDTO.setFirstName("Updated");
        userRequestDTO.setLastName("Name");
        userRequestDTO.setPhoneNumber("987654321");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setUsername("testuser");
        userResponseDTO.setEmail("test@example.com");

        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setUsername("testuser");
        loginRequestDTO.setPassword("password");

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("registerNewUser Tests")
    class RegisterNewUserTests {
        @Test
        @DisplayName("Should register user successfully when username and email not taken")
        void registerNewUser_whenUsernameAndEmailNotTaken_shouldSucceed() {
            when(userRepository.existsByUsername(userRegistrationDTO.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(userRegistrationDTO.getEmail())).thenReturn(false);
            when(userMapper.userRegistrationDtoToUser(userRegistrationDTO)).thenReturn(user);
            when(passwordEncoder.encode(userRegistrationDTO.getPassword())).thenReturn("encodedNewPassword");
            when(roleRepository.findRoleByName("USER")).thenReturn(Optional.of(userRole));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            MessageResponseDTO response = userService.registerNewUser(userRegistrationDTO);

            assertNotNull(response);
            assertEquals("User registered successfully: " + user.getUsername(), response.getMessage());
            verify(userRepository).save(any(User.class));
            assertEquals("encodedNewPassword", user.getPassword());
            assertTrue(user.getRoles().contains(userRole));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when username is taken")
        void registerNewUser_whenUsernameTaken_shouldThrowIllegalArgumentException() {
            when(userRepository.existsByUsername(userRegistrationDTO.getUsername())).thenReturn(true);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> userService.registerNewUser(userRegistrationDTO));
            assertEquals("Error: Username '" + userRegistrationDTO.getUsername() + "' is already taken!", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email is taken")
        void registerNewUser_whenEmailTaken_shouldThrowIllegalArgumentException() {
            when(userRepository.existsByUsername(userRegistrationDTO.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(userRegistrationDTO.getEmail())).thenReturn(true);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> userService.registerNewUser(userRegistrationDTO));
            assertEquals("Error: Email '" + userRegistrationDTO.getEmail() + "' is already in use!", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when USER role not found")
        void registerNewUser_whenUserRoleNotFound_shouldThrowResponseStatusException() {
            when(userRepository.existsByUsername(userRegistrationDTO.getUsername())).thenReturn(false);
            when(userRepository.existsByEmail(userRegistrationDTO.getEmail())).thenReturn(false);
            when(userMapper.userRegistrationDtoToUser(userRegistrationDTO)).thenReturn(new User());
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(roleRepository.findRoleByName("USER")).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userService.registerNewUser(userRegistrationDTO));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("User role not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("findUserDtoById Tests")
    class FindUserDtoByIdTests {
        @Test
        @DisplayName("Should return user DTO when user exists")
        void findUserDtoById_whenUserExists_shouldReturnUserResponseDTO() {
            when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
            when(userMapper.userToUserResponseDto(user)).thenReturn(userResponseDTO);

            UserResponseDTO result = userService.findUserDtoById(1L);

            assertNotNull(result);
            assertEquals(userResponseDTO.getId(), result.getId());
            verify(userRepository).findUserById(1L);
            verify(userMapper).userToUserResponseDto(user);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null ID")
        void findUserDtoById_whenIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> userService.findUserDtoById(null));
            assertEquals("User ID must be a positive number.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found")
        void findUserDtoById_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserById(99L)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> userService.findUserDtoById(99L));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("User not found with id: 99", exception.getReason());
        }
    }

    @Nested
    @DisplayName("findAllUsers Tests")
    class FindAllUsersTests {
        @Test
        @DisplayName("Should return page of user DTOs")
        void findAllUsers_shouldReturnPageOfUserResponseDTO() {
            Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);
            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(userMapper.userToUserResponseDto(user)).thenReturn(userResponseDTO);

            Page<UserResponseDTO> result = userService.findAllUsers(pageable);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.getTotalElements());
            assertEquals(userResponseDTO, result.getContent().getFirst());
            verify(userRepository).findAll(pageable);
            verify(userMapper).userToUserResponseDto(user);
        }

        @Test
        @DisplayName("Should return empty page when no users found")
        void findAllUsers_whenNoUsersFound_shouldReturnEmptyPage() {
            Page<User> emptyPage = Page.empty(pageable);
            when(userRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<UserResponseDTO> result = userService.findAllUsers(pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("findUserDtoByUsername Tests")
    class FindUserDtoByUsernameTests {
        @Test
        @DisplayName("Should return user DTO for existing username")
        void findUserDtoByUsername_whenUserExists_shouldReturnUserResponseDTO() {
            when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
            when(userMapper.userToUserResponseDto(user)).thenReturn(userResponseDTO);

            UserResponseDTO result = userService.findUserDtoByUsername("testuser");
            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found by username")
        void findUserDtoByUsername_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserByUsername("unknown")).thenReturn(Optional.empty());
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.findUserDtoByUsername("unknown"));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertEquals("User not found with username: unknown", ex.getReason());
        }
    }


    @Nested
    @DisplayName("findUserDtoByEmail Tests")
    class FindUserDtoByEmailTests {
        @Test
        @DisplayName("Should return user DTO for existing email")
        void findUserDtoByEmail_whenUserExists_shouldReturnUserResponseDTO() {
            when(userRepository.findUserByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(userMapper.userToUserResponseDto(user)).thenReturn(userResponseDTO);

            UserResponseDTO result = userService.findUserDtoByEmail("test@example.com");
            assertNotNull(result);
            assertEquals("test@example.com", result.getEmail());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when email is blank")
        void findUserDtoByEmail_whenEmailIsBlank_shouldThrowIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.findUserDtoByEmail(" "));
            assertEquals("Email to find cannot be blank.", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found by email")
        void findUserDtoByEmail_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserByEmail("unknown@example.com")).thenReturn(Optional.empty());
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.findUserDtoByEmail("unknown@example.com"));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertEquals("User not found with email: unknown@example.com", ex.getReason());
        }
    }

    @Nested
    @DisplayName("updateUser Tests")
    class UpdateUserTests {
        @Test
        @DisplayName("Should update user successfully")
        void updateUser_whenValidInputAndEmailNotTaken_shouldSucceed() {
            Long userId = 1L;
            when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));

            when(userRepository.existsByEmail(userRequestDTO.getEmail())).thenReturn(false);
            doNothing().when(userMapper).updateUserFromDto(userRequestDTO, user);
            when(userRepository.save(user)).thenReturn(user);

            MessageResponseDTO response = userService.updateUser(userId, userRequestDTO);

            assertNotNull(response);
            assertEquals("User updated successfully: " + user.getUsername(), response.getMessage());
            verify(userMapper).updateUserFromDto(userRequestDTO, user);
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should update user successfully when email is not changed")
        void updateUser_whenEmailNotChanged_shouldSucceed() {
            Long userId = 1L;
            userRequestDTO.setEmail(user.getEmail());
            when(userRepository.findUserById(userId)).thenReturn(Optional.of(user));
            doNothing().when(userMapper).updateUserFromDto(userRequestDTO, user);
            when(userRepository.save(user)).thenReturn(user);

            MessageResponseDTO response = userService.updateUser(userId, userRequestDTO);

            assertNotNull(response);
            assertEquals("User updated successfully: " + user.getUsername(), response.getMessage());
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userMapper).updateUserFromDto(userRequestDTO, user);
            verify(userRepository).save(user);
        }


        @Test
        @DisplayName("Should throw IllegalArgumentException for null ID")
        void updateUser_whenIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUser(null, userRequestDTO));
            assertEquals("User ID for update must be a positive number.", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null DTO")
        void updateUser_whenDtoIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUser(1L, null));
            assertEquals("User request DTO cannot be null for update.", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found")
        void updateUser_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserById(99L)).thenReturn(Optional.empty());
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.updateUser(99L, userRequestDTO));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when new email is already in use")
        void updateUser_whenNewEmailTaken_shouldThrowIllegalArgumentException() {
            userRequestDTO.setEmail("taken@example.com");
            when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.updateUser(1L, userRequestDTO));
            assertEquals("Error: Email 'taken@example.com' is already in use by another account!", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteUser Tests")
    class DeleteUserTests {
        @Test
        @DisplayName("Should delete user successfully")
        void deleteUser_whenUserExists_shouldSucceed() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            MessageResponseDTO response = userService.deleteUser(1L);
            assertEquals("User deleted successfully with id: 1", response.getMessage());
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null ID")
        void deleteUser_whenIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(null));
            assertEquals("User ID for deletion must be a positive number.", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found for deletion")
        void deleteUser_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.existsById(99L)).thenReturn(false);
            ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> userService.deleteUser(99L));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertEquals("User not found with id: 99. Could not delete.", ex.getReason());
        }
    }


    @Nested
    @DisplayName("findUserEntityById Tests")
    class FindUserEntityByIdTests {
        @Test
        @DisplayName("Should return user entity when user exists")
        void findUserEntityById_whenUserExists_shouldReturnUserEntity() {
            when(userRepository.findUserById(1L)).thenReturn(Optional.of(user));
            User result = userService.findUserEntityById(1L);
            assertNotNull(result);
            assertEquals(user.getId(), result.getId());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user entity not found")
        void findUserEntityById_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserById(99L)).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> userService.findUserEntityById(99L));
        }
    }


    @Nested
    @DisplayName("authenticateUser Tests")
    class AuthenticateUserTests {
        @Test
        @DisplayName("Should authenticate user and return JWT when credentials are valid (principal is User)")
        void authenticateUser_whenValidCredentialsAndPrincipalIsUser_shouldReturnJwt() {
            Authentication authentication = mock(Authentication.class);
            when(authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword())))
                    .thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(user);
            when(jwtUtil.generateToken(authentication)).thenReturn("mocked.jwt.token");
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);


            ResponseEntity<?> responseEntity = userService.authenticateUser(loginRequestDTO);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            JwtResponseDTO jwtResponse = (JwtResponseDTO) responseEntity.getBody();
            assertNotNull(jwtResponse);
            assertEquals("mocked.jwt.token", jwtResponse.getToken());
            assertEquals(user.getId(), jwtResponse.getId());
            assertEquals(user.getUsername(), jwtResponse.getUsername());
            assertEquals(user.getEmail(), jwtResponse.getEmail());
            assertTrue(jwtResponse.getRoles().contains("ROLE_USER"));
        }

        @Test
        @DisplayName("Should authenticate user and return JWT when credentials are valid (principal is UserDetails)")
        void authenticateUser_whenValidCredentialsAndPrincipalIsUserDetails_shouldReturnJwt() {
            Authentication authentication = mock(Authentication.class);
            UserDetails springUserDetails = mock(UserDetails.class);

            when(springUserDetails.getUsername()).thenReturn("testuser");
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
            when(authentication.getPrincipal()).thenReturn(springUserDetails);
            when(userRepository.findUserByUsername("testuser")).thenReturn(Optional.of(user));
            when(jwtUtil.generateToken(authentication)).thenReturn("mocked.jwt.token");
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            when(authentication.getAuthorities()).thenAnswer(invocation -> authorities);

            ResponseEntity<?> responseEntity = userService.authenticateUser(loginRequestDTO);

            assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
            JwtResponseDTO jwtResponse = (JwtResponseDTO) responseEntity.getBody();
            assertNotNull(jwtResponse);
            assertEquals("mocked.jwt.token", jwtResponse.getToken());
            assertEquals(user.getId(), jwtResponse.getId());
        }


        @Test
        @DisplayName("Should return Unauthorized when credentials are bad")
        void authenticateUser_whenBadCredentials_shouldReturnUnauthorized() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            ResponseEntity<?> responseEntity = userService.authenticateUser(loginRequestDTO);

            assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
            MessageResponseDTO messageResponse = (MessageResponseDTO) responseEntity.getBody();
            assertNotNull(messageResponse);
            assertEquals("Error: Invalid username or password!", messageResponse.getMessage());
        }
    }

    @Nested
    @DisplayName("changePassword Tests")
    class ChangePasswordTests {
        @Test
        @DisplayName("Should change password successfully when old password matches")
        void changePassword_whenOldPasswordMatches_shouldSucceed() {
            String oldPassword = "oldPassword";
            String newPassword = "newPassword";
            String encodedNewPassword = "encodedNewPassword";

            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(true);
            when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
            when(userRepository.save(user)).thenReturn(user);

            MessageResponseDTO response = userService.changePassword(testUsername, newPassword, oldPassword);
            assertEquals("Password changed successfully for user: " + testUsername, response.getMessage());
            assertEquals(encodedNewPassword, user.getPassword());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if username is blank")
        void changePassword_whenUsernameIsBlank_shouldThrowIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.changePassword(" ", "newPass", "oldPass"));
            assertEquals("Username and new password cannot be blank.", ex.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if new password is blank")
        void changePassword_whenNewPasswordIsBlank_shouldThrowIllegalArgumentException() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> userService.changePassword(testUsername, " ", "oldPass"));
            assertEquals("Username and new password cannot be blank.", ex.getMessage());
        }


        @Test
        @DisplayName("Should throw ResponseStatusException from findUserEntityByUsername if user not found")
        void changePassword_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserByUsername("unknownUser")).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.changePassword("unknownUser", "newPassword", "oldPassword"));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertNotNull(ex.getReason());
            assertTrue(ex.getReason().contains("User entity not found with username: unknownUser"));
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when old password does not match")
        void changePassword_whenOldPasswordMismatch_shouldThrowResponseStatusException() {
            String oldPassword = "wrongOldPassword";
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(oldPassword, user.getPassword())).thenReturn(false);

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> userService.changePassword(testUsername, "newPassword", oldPassword));
            assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatusCode());
            assertEquals("Old password is incorrect.", ex.getReason());
        }
    }
}