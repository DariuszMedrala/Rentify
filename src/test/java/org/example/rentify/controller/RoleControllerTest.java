package org.example.rentify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentify.dto.request.RoleRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllerTestConfig.class)
@WebMvcTest(RoleController.class)
@DisplayName("RoleController Integration Tests")
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoleService roleService;

    @Autowired
    private ObjectMapper objectMapper;

    private RoleRequestDTO roleRequestDTO;
    private RoleResponseDTO roleResponseDTOForSetup;
    private UserResponseDTO userResponseDTOForSetup;


    @BeforeEach
    void setUp() {
        roleRequestDTO = new RoleRequestDTO();
        roleRequestDTO.setName("TEST_ROLE");
        roleRequestDTO.setDescription("A test role");

        roleResponseDTOForSetup = new RoleResponseDTO();
        roleResponseDTOForSetup.setId(1L);
        roleResponseDTOForSetup.setName("TEST_ROLE");
        roleResponseDTOForSetup.setDescription("A test role");

        Set<RoleResponseDTO> userRolesSet = new HashSet<>();
        RoleResponseDTO userRoleForDto = new RoleResponseDTO(2L, "ROLE_USER", "Standard user role");
        userRolesSet.add(userRoleForDto);

        userResponseDTOForSetup = new UserResponseDTO(
                1L, "testUser", "test@example.com", "Test", "User",
                "1234567890", LocalDate.now(), null, userRolesSet
        );
    }

    @Nested
    @DisplayName("GET /api/roles/all")
    class FindAllRolesTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 OK and a page of roles for ADMIN")
        void whenFindAllRolesAsAdmin_thenReturnsPageOfRoles() throws Exception {
            Page<RoleResponseDTO> rolePage = new PageImpl<>(List.of(roleResponseDTOForSetup), PageRequest.of(0, 10), 1);
            when(roleService.findAllRoles(any(Pageable.class))).thenReturn(rolePage);

            mockMvc.perform(get("/api/roles/all")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].name").value(roleResponseDTOForSetup.getName()))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser(roles = "USER")
        @DisplayName("should return 403 Forbidden when non-ADMIN tries to find all roles")
        void whenFindAllRolesAsUser_thenReturns403() throws Exception {
            mockMvc.perform(get("/api/roles/all"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden when anonymous user tries to find all roles")
        void whenFindAllRolesAsAnonymous_thenReturns403() throws Exception {
            mockMvc.perform(get("/api/roles/all"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/roles/name/{name}")
    class FindRoleByNameTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 OK and role details for ADMIN for valid name")
        void whenFindRoleByNameAsAdmin_thenReturnsRole() throws Exception {
            when(roleService.findRoleByName("TEST_ROLE")).thenReturn(roleResponseDTOForSetup);

            mockMvc.perform(get("/api/roles/name/TEST_ROLE"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.name").value("TEST_ROLE"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 404 Not Found when role does not exist (ADMIN)")
        void whenFindNonExistingRoleByNameAsAdmin_thenReturns404() throws Exception {
            String roleName = "UNKNOWN";
            String expectedMessage = "Role " + roleName + " not found.";
            when(roleService.findRoleByName(roleName))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, expectedMessage));

            mockMvc.perform(get("/api/roles/name/" + roleName))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(expectedMessage));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for role name too short")
        void whenFindRoleByNameWithTooShortName_thenReturns400() throws Exception {
            String invalidName = "A";
            String expectedValidationMessage = "Validation Error: name: Role name must be between 2 and 50 characters";

            mockMvc.perform(get("/api/roles/name/" + invalidName))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(expectedValidationMessage));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for role name too long")
        void whenFindRoleByNameWithTooLongName_thenReturns400() throws Exception {
            String invalidName = "A".repeat(51);
            String expectedValidationMessage = "Validation Error: name: Role name must be between 2 and 50 characters";

            mockMvc.perform(get("/api/roles/name/" + invalidName))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(expectedValidationMessage));
        }
    }

    @Nested
    @DisplayName("GET /api/roles/id/{id}")
    class FindRoleByIdTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 OK and role details for ADMIN")
        void whenFindRoleByIdAsAdmin_thenReturnsRole() throws Exception {
            when(roleService.findRoleById(1L)).thenReturn(roleResponseDTOForSetup);

            mockMvc.perform(get("/api/roles/id/1"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 404 Not Found when role ID does not exist")
        void whenFindRoleByIdNonExistent_thenReturns404() throws Exception {
            long nonExistentId = 999L;
            String expectedMessage = "Role with id " + nonExistentId + " not found";
            when(roleService.findRoleById(nonExistentId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, expectedMessage));

            mockMvc.perform(get("/api/roles/id/" + nonExistentId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(expectedMessage));
        }
    }


    @Nested
    @DisplayName("POST /api/roles/create")
    class CreateRoleTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 OK and success message on successful creation for ADMIN")
        void whenCreateRoleAsAdmin_thenReturnsSuccessMessage() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Role created successfully: TEST_ROLE");
            ArgumentCaptor<RoleRequestDTO> captor = ArgumentCaptor.forClass(RoleRequestDTO.class);
            when(roleService.createRole(captor.capture())).thenReturn(successResponse);

            mockMvc.perform(post("/api/roles/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(roleRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Role created successfully: TEST_ROLE"));

            assertThat(captor.getValue().getName()).isEqualTo(roleRequestDTO.getName());
            assertThat(captor.getValue().getDescription()).isEqualTo(roleRequestDTO.getDescription());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for invalid RoleRequestDTO for ADMIN")
        void whenCreateRoleWithInvalidDtoAsAdmin_thenReturns400() throws Exception {
            RoleRequestDTO invalidDto = new RoleRequestDTO();
            invalidDto.setName("T");
            invalidDto.setDescription("");

            MvcResult result = mockMvc.perform(post("/api/roles/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("Validation Error:");
            assertThat(responseBody).contains("name: Role name must be between 2 and 50 characters");
            assertThat(responseBody).contains("description: Role description cannot be blank");
        }
    }

    @Nested
    @DisplayName("DELETE /api/roles/delete/{name}")
    class DeleteRoleByNameTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 OK and success message on successful deletion for ADMIN")
        void whenDeleteRoleAsAdmin_thenReturnsSuccessMessage() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Role DELETED_ROLE deleted successfully.");
            when(roleService.deleteRoleByName("DELETED_ROLE")).thenReturn(successResponse);

            mockMvc.perform(delete("/api/roles/delete/DELETED_ROLE"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Role DELETED_ROLE deleted successfully."));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 Bad Request when service throws IllegalArgumentException (e.g. deleting ADMIN role)")
        void whenDeleteAdminRoleAsAdmin_thenReturns400FromServiceException() throws Exception {
            String roleToDelete = "ADMIN";
            String serviceErrorMessage = "Cannot delete core ADMIN role.";
            when(roleService.deleteRoleByName(roleToDelete))
                    .thenThrow(new IllegalArgumentException(serviceErrorMessage));

            mockMvc.perform(delete("/api/roles/delete/" + roleToDelete))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(serviceErrorMessage));
        }
    }

    @Nested
    @DisplayName("PUT /api/roles/update/{id}")
    class UpdateRoleTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 OK and success message on successful update for ADMIN")
        void whenUpdateRoleAsAdmin_thenReturnsSuccessMessage() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Role updated successfully.");
            when(roleService.updateRole(eq(1L), any(RoleRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/roles/update/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(roleRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Role updated successfully."));
        }
    }

    @Nested
    @DisplayName("GET /api/roles/users/{roleName}")
    class FindAllUsersByRoleNameTests {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 200 OK and page of users for ADMIN")
        void whenFindAllUsersByRoleNameAsAdmin_thenReturnsPageOfUsers() throws Exception {
            Page<UserResponseDTO> userPage = new PageImpl<>(List.of(userResponseDTOForSetup), PageRequest.of(0, 5), 1);
            when(roleService.findAllUsersByRoleName(eq("TEST_ROLE"), any(Pageable.class))).thenReturn(userPage);

            mockMvc.perform(get("/api/roles/users/TEST_ROLE")
                            .param("page", "0")
                            .param("size", "5"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].username").value(userResponseDTOForSetup.getUsername()))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for invalid role name (too short) when finding users")
        void whenFindAllUsersByRoleNameWithTooShortName_thenReturns400() throws Exception {
            String invalidName = "A";
            String expectedValidationMessage = "Validation Error: roleName: Role name must be between 2 and 50 characters";

            mockMvc.perform(get("/api/roles/users/" + invalidName))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(expectedValidationMessage));
        }
    }
}