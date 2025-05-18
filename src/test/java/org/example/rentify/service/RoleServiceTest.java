package org.example.rentify.service;

import org.example.rentify.dto.request.RoleRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.Role;
import org.example.rentify.entity.User;
import org.example.rentify.mapper.RoleMapper;
import org.example.rentify.mapper.UserMapper;
import org.example.rentify.repository.RoleRepository;
import org.example.rentify.repository.UserRepository;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService Unit Tests")
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;

    private Role role;
    private RoleRequestDTO roleRequestDTO;
    private RoleResponseDTO roleResponseDTO;
    private User user;
    private UserResponseDTO userResponseDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(1L);
        role.setName("TEST_ROLE");
        role.setDescription("A role for testing purposes");

        roleRequestDTO = new RoleRequestDTO();
        roleRequestDTO.setName("NEW_TEST_ROLE");
        roleRequestDTO.setDescription("New description");

        roleResponseDTO = new RoleResponseDTO();
        roleResponseDTO.setId(1L);
        roleResponseDTO.setName("TEST_ROLE");
        roleResponseDTO.setDescription("A role for testing purposes");

        user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1L);
        userResponseDTO.setUsername("testUser");

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("findRoleByName Tests")
    class FindRoleByNameTests {

        @Test
        @DisplayName("Should return role when found by name")
        void findRoleByName_whenRoleExists_shouldReturnRoleResponseDTO() {
            when(roleRepository.findRoleByName("TEST_ROLE")).thenReturn(Optional.of(role));
            when(roleMapper.roleToRoleResponseDto(role)).thenReturn(roleResponseDTO);

            RoleResponseDTO result = roleService.findRoleByName("TEST_ROLE");

            assertNotNull(result);
            assertEquals("TEST_ROLE", result.getName());
            verify(roleRepository).findRoleByName("TEST_ROLE");
            verify(roleMapper).roleToRoleResponseDto(role);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name is null")
        void findRoleByName_whenNameIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.findRoleByName(null));
            assertEquals("Role name cannot be null or empty.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name is empty")
        void findRoleByName_whenNameIsEmpty_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.findRoleByName(""));
            assertEquals("Role name cannot be null or empty.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when name is blank")
        void findRoleByName_whenNameIsBlank_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.findRoleByName("   "));
            assertEquals("Role name cannot be null or empty.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when role not found by name")
        void findRoleByName_whenRoleNotFound_shouldThrowResponseStatusException() {
            when(roleRepository.findRoleByName("UNKNOWN_ROLE")).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> roleService.findRoleByName("UNKNOWN_ROLE"));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("User not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("findRoleById Tests")
    class FindRoleByIdTests {
        @Test
        @DisplayName("Should return role when found by ID")
        void findRoleById_whenRoleExists_shouldReturnRoleResponseDTO() {
            when(roleRepository.findRoleById(1L)).thenReturn(Optional.of(role));
            when(roleMapper.roleToRoleResponseDto(role)).thenReturn(roleResponseDTO);

            RoleResponseDTO result = roleService.findRoleById(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(roleRepository).findRoleById(1L);
            verify(roleMapper).roleToRoleResponseDto(role);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is null")
        void findRoleById_whenIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.findRoleById(null));
            assertEquals("Role ID must be a positive number.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is zero")
        void findRoleById_whenIdIsZero_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.findRoleById(0L));
            assertEquals("Role ID must be a positive number.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is negative")
        void findRoleById_whenIdIsNegative_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.findRoleById(-1L));
            assertEquals("Role ID must be a positive number.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when role not found by ID")
        void findRoleById_whenRoleNotFound_shouldThrowResponseStatusException() {
            when(roleRepository.findRoleById(99L)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> roleService.findRoleById(99L));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Role not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("createRole Tests")
    class CreateRoleTests {
        @Test
        @DisplayName("Should create role successfully when name is valid and does not exist")
        void createRole_whenValidAndNameNotExists_shouldReturnSuccessMessage() {
            when(roleRepository.existsByName(roleRequestDTO.getName())).thenReturn(false);
            when(roleMapper.roleRequestDtoToRole(roleRequestDTO)).thenReturn(role);
            when(roleRepository.save(role)).thenReturn(role);

            MessageResponseDTO response = roleService.createRole(roleRequestDTO);

            assertNotNull(response);
            assertEquals("Role created successfully: " + roleRequestDTO.getName(), response.getMessage());
            verify(roleRepository).existsByName(roleRequestDTO.getName());
            verify(roleMapper).roleRequestDtoToRole(roleRequestDTO);
            verify(roleRepository).save(role);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when DTO is null")
        void createRole_whenDtoIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.createRole(null));
            assertEquals("Role name cannot be null or empty for creation.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when role name in DTO is null")
        void createRole_whenRoleNameInDtoIsNull_shouldThrowIllegalArgumentException() {
            roleRequestDTO.setName(null);
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.createRole(roleRequestDTO));
            assertEquals("Role name cannot be null or empty for creation.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when role name in DTO is empty")
        void createRole_whenRoleNameInDtoIsEmpty_shouldThrowIllegalArgumentException() {
            roleRequestDTO.setName("");
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.createRole(roleRequestDTO));
            assertEquals("Role name cannot be null or empty for creation.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when role with same name already exists")
        void createRole_whenRoleNameExists_shouldThrowIllegalArgumentException() {
            when(roleRepository.existsByName(roleRequestDTO.getName())).thenReturn(true);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.createRole(roleRequestDTO));
            assertEquals("A role with the name '" + roleRequestDTO.getName() + "' already exists.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("findAllRoles Tests")
    class FindAllRolesTests {
        @Test
        @DisplayName("Should return page of roles when roles exist")
        void findAllRoles_whenRolesExist_shouldReturnPageOfRoleResponseDTO() {
            Page<Role> rolesPage = new PageImpl<>(List.of(role), pageable, 1);
            when(roleRepository.findAll(pageable)).thenReturn(rolesPage);
            when(roleMapper.roleToRoleResponseDto(role)).thenReturn(roleResponseDTO);

            Page<RoleResponseDTO> result = roleService.findAllRoles(pageable);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.getTotalElements());
            assertEquals(roleResponseDTO.getName(), result.getContent().getFirst().getName());
            verify(roleRepository).findAll(pageable);
            verify(roleMapper).roleToRoleResponseDto(role);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when pageable is null")
        void findAllRoles_whenPageableIsNull_shouldThrowResponseStatusException() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> roleService.findAllRoles(null));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Invalid pagination parameters.", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when pageable is unpaged")
        void findAllRoles_whenPageableIsUnpaged_shouldThrowResponseStatusException() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> roleService.findAllRoles(Pageable.unpaged()));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Invalid pagination parameters.", exception.getReason());
        }


        @Test
        @DisplayName("Should throw ResponseStatusException when no roles found")
        void findAllRoles_whenNoRolesFound_shouldThrowResponseStatusException() {
            Page<Role> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(roleRepository.findAll(pageable)).thenReturn(emptyPage);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> roleService.findAllRoles(pageable));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No roles found.", exception.getReason());
        }
    }

    @Nested
    @DisplayName("findAllUsersByRoleName Tests")
    class FindAllUsersByRoleNameTests {
        String validRoleName = "EXISTING_ROLE";

        @Test
        @DisplayName("Should return page of users for existing role")
        void findAllUsersByRoleName_whenRoleExists_shouldReturnPageOfUserResponseDTO() {
            Page<User> usersPage = new PageImpl<>(List.of(user), pageable, 1);
            when(roleRepository.existsByName(validRoleName)).thenReturn(true);
            when(userRepository.findAllByRolesName(validRoleName, pageable)).thenReturn(usersPage);
            when(userMapper.userToUserResponseDto(user)).thenReturn(userResponseDTO);

            Page<UserResponseDTO> result = roleService.findAllUsersByRoleName(validRoleName, pageable);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.getTotalElements());
            assertEquals(userResponseDTO.getUsername(), result.getContent().getFirst().getUsername());
            verify(roleRepository).existsByName(validRoleName);
            verify(userRepository).findAllByRolesName(validRoleName, pageable);
            verify(userMapper).userToUserResponseDto(user);
        }

        @Test
        @DisplayName("Should return empty page when role exists but has no users")
        void findAllUsersByRoleName_whenRoleExistsButNoUsers_shouldReturnEmptyPage() {
            Page<User> emptyUsersPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            when(roleRepository.existsByName(validRoleName)).thenReturn(true);
            when(userRepository.findAllByRolesName(validRoleName, pageable)).thenReturn(emptyUsersPage);

            Page<UserResponseDTO> result = roleService.findAllUsersByRoleName(validRoleName, pageable);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            verify(roleRepository).existsByName(validRoleName);
            verify(userRepository).findAllByRolesName(validRoleName, pageable);
        }


        @Test
        @DisplayName("Should throw IllegalArgumentException when roleName is null")
        void findAllUsersByRoleName_whenRoleNameIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.findAllUsersByRoleName(null, pageable));
            assertEquals("Role name to find users for cannot be null or empty.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when role does not exist")
        void findAllUsersByRoleName_whenRoleNotExists_shouldThrowResponseStatusException() {
            String nonExistingRoleName = "NON_EXISTING_ROLE";
            when(roleRepository.existsByName(nonExistingRoleName)).thenReturn(false);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> roleService.findAllUsersByRoleName(nonExistingRoleName, pageable));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Role not found with name: " + nonExistingRoleName + ", cannot retrieve users.", exception.getReason());
        }
    }

    @Nested
    @DisplayName("deleteRoleByName Tests")
    class DeleteRoleByNameTests {
        @Test
        @DisplayName("Should delete role successfully when not ADMIN or USER")
        void deleteRoleByName_whenRoleNotAdminOrUser_shouldReturnSuccessMessage() {
            role.setName("CUSTOM_ROLE");
            when(roleRepository.findRoleByName("CUSTOM_ROLE")).thenReturn(Optional.of(role));
            doNothing().when(roleRepository).delete(role);

            MessageResponseDTO response = roleService.deleteRoleByName("CUSTOM_ROLE");

            assertNotNull(response);
            assertEquals("Role deleted successfully: CUSTOM_ROLE", response.getMessage());
            verify(roleRepository).findRoleByName("CUSTOM_ROLE");
            verify(roleRepository).delete(role);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when role name is null")
        void deleteRoleByName_whenNameIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.deleteRoleByName(null));
            assertEquals("Role name to delete cannot be null or empty.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when role not found")
        void deleteRoleByName_whenRoleNotFound_shouldThrowResponseStatusException() {
            when(roleRepository.findRoleByName("UNKNOWN_ROLE")).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> roleService.deleteRoleByName("UNKNOWN_ROLE"));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Role not found with name: UNKNOWN_ROLE", exception.getReason());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when deleting ADMIN role")
        void deleteRoleByName_whenDeletingAdminRole_shouldThrowIllegalArgumentException() {
            role.setName("ADMIN");
            when(roleRepository.findRoleByName("ADMIN")).thenReturn(Optional.of(role));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.deleteRoleByName("ADMIN"));
            assertEquals("Cannot delete the ADMIN or USER role.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when deleting USER role")
        void deleteRoleByName_whenDeletingUserRole_shouldThrowIllegalArgumentException() {
            role.setName("USER");
            when(roleRepository.findRoleByName("USER")).thenReturn(Optional.of(role));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.deleteRoleByName("USER"));
            assertEquals("Cannot delete the ADMIN or USER role.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("updateRole Tests")
    class UpdateRoleTests {
        Long roleIdToUpdate = 2L;
        Role existingRole;
        RoleRequestDTO updateRequestDTO;

        @BeforeEach
        void updateSetup() {
            existingRole = new Role();
            existingRole.setId(roleIdToUpdate);
            existingRole.setName("UPDATABLE_ROLE");
            existingRole.setDescription("Old Description");

            updateRequestDTO = new RoleRequestDTO();
            updateRequestDTO.setName("UPDATED_NAME");
            updateRequestDTO.setDescription("New Description For Update");
        }

        @Test
        @DisplayName("Should update role successfully when valid and not ADMIN/USER and new name not exists")
        void updateRole_whenValidAndNotAdminUserAndNewNameNotExists_shouldSucceed() {
            when(roleRepository.findRoleById(roleIdToUpdate)).thenReturn(Optional.of(existingRole));
            when(roleRepository.existsByName("UPDATED_NAME")).thenReturn(false);
            when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

            MessageResponseDTO response = roleService.updateRole(roleIdToUpdate, updateRequestDTO);

            assertNotNull(response);
            assertEquals("Role updated successfully: UPDATED_NAME", response.getMessage());
            assertEquals("UPDATED_NAME", existingRole.getName());
            assertEquals("New Description For Update", existingRole.getDescription());
            verify(roleRepository).save(existingRole);
        }

        @Test
        @DisplayName("Should update role successfully when only description changes")
        void updateRole_whenOnlyDescriptionChanges_shouldSucceed() {
            updateRequestDTO.setName("UPDATABLE_ROLE");
            when(roleRepository.findRoleById(roleIdToUpdate)).thenReturn(Optional.of(existingRole));
            when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0));

            MessageResponseDTO response = roleService.updateRole(roleIdToUpdate, updateRequestDTO);

            assertNotNull(response);
            assertEquals("Role updated successfully: UPDATABLE_ROLE", response.getMessage());
            assertEquals("UPDATABLE_ROLE", existingRole.getName());
            assertEquals("New Description For Update", existingRole.getDescription());
            verify(roleRepository).save(existingRole);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when DTO name is null during update")
        void updateRole_whenDtoNameIsNull_shouldThrowIllegalArgumentException() {
            RoleRequestDTO dtoWithNullName = new RoleRequestDTO();
            dtoWithNullName.setName(null);
            dtoWithNullName.setDescription("Some Description");
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.updateRole(roleIdToUpdate, dtoWithNullName));

            assertEquals("Role ID for update must be a positive number, and role name cannot be null or empty", exception.getMessage());

            verify(roleRepository, never()).findRoleById(anyLong());
            verify(roleRepository, never()).save(any(Role.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when ID is null")
        void updateRole_whenIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.updateRole(null, updateRequestDTO));
            assertEquals("Role ID for update must be a positive number, and role name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when RoleRequestDTO is null")
        void updateRole_whenDtoIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.updateRole(roleIdToUpdate, null));
            assertEquals("Role ID for update must be a positive number, and role name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when RoleRequestDTO name is empty")
        void updateRole_whenDtoNameIsEmpty_shouldThrowIllegalArgumentException() {
            updateRequestDTO.setName("");
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.updateRole(roleIdToUpdate, updateRequestDTO));
            assertEquals("Role ID for update must be a positive number, and role name cannot be null or empty", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when role to update is not found")
        void updateRole_whenRoleNotFound_shouldThrowResponseStatusException() {
            when(roleRepository.findRoleById(roleIdToUpdate)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> roleService.updateRole(roleIdToUpdate, updateRequestDTO));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Role not found with ID: " + roleIdToUpdate, exception.getReason());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating ADMIN role")
        void updateRole_whenUpdatingAdminRole_shouldThrowIllegalArgumentException() {
            existingRole.setName("ADMIN");
            when(roleRepository.findRoleById(roleIdToUpdate)).thenReturn(Optional.of(existingRole));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.updateRole(roleIdToUpdate, updateRequestDTO));
            assertEquals("Error: Cannot update the ADMIN or USER role.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating USER role")
        void updateRole_whenUpdatingUserRole_shouldThrowIllegalArgumentException() {
            existingRole.setName("USER");
            when(roleRepository.findRoleById(roleIdToUpdate)).thenReturn(Optional.of(existingRole));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.updateRole(roleIdToUpdate, updateRequestDTO));
            assertEquals("Error: Cannot update the ADMIN or USER role.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when new role name already exists")
        void updateRole_whenNewNameAlreadyExists_shouldThrowIllegalArgumentException() {
            when(roleRepository.findRoleById(roleIdToUpdate)).thenReturn(Optional.of(existingRole));
            updateRequestDTO.setName("EXISTING_OTHER_ROLE");
            when(roleRepository.existsByName("EXISTING_OTHER_ROLE")).thenReturn(true);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> roleService.updateRole(roleIdToUpdate, updateRequestDTO));
            assertEquals("Error: Another role with the name 'EXISTING_OTHER_ROLE' already exists.", exception.getMessage());
        }
    }
}