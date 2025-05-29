package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.example.rentify.dto.request.RoleRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/*
 * RoleController is a REST controller for managing roles in the system.
 * It provides endpoints for creating, updating, deleting, and retrieving roles.
 */
@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles Management", description = "Endpoints for managing roles")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class RoleController {

    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Retrieves all roles in a paginated format.
     *
     * @param pageable the pagination information
     * @return a paginated list of roles
     */
    @Operation(summary = "Get all roles", description = "Retrieve a paginated list of all roles. Requires ADMIN role.")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<RoleResponseDTO>> findAllRoles(@Parameter(
            name = "pageable",
            description = "Pageable object containing pagination information",
            example = "{\"page\": 0, \"size\": 10, \"sort\": \"name,asc\"}") Pageable pageable) {

        return ResponseEntity.ok(roleService.findAllRoles(pageable));
    }

    /**
     * Retrieves a role by its name.
     *
     * @param name the name of the role
     * @return the role details
     */
    @Operation(summary = "Get role by name", description = "Retrieve a specific role's details by its name. Requires ADMIN role.")
    @GetMapping("name/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDTO> findRoleByName(
            @Parameter(description = "Name of the role to retrieve", in = ParameterIn.PATH)
            @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
            @PathVariable String name) {

        return ResponseEntity.ok(roleService.findRoleByName(name));
    }

    /**
     * Retrieves a role by its ID.
     *
     * @param id the ID of the role
     * @return the role details
     */
    @Operation(summary = "Get role by ID", description = "Retrieve a specific role's details by its ID. Requires ADMIN role.")
    @GetMapping("/id/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDTO> findRoleById(
            @Parameter(description = "ID of the role to retrieve", in = ParameterIn.PATH) @PathVariable Long id) {

        return ResponseEntity.ok(roleService.findRoleById(id));
    }

    /**
     * Creates a new role.
     *
     * @param roleRequestDTO DTO containing the role details
     * @return a message indicating the result of the creation
     */
    @Operation(summary = "Create New Role", description = "Allows the creation of a new role. Requires ADMIN role.")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponseDTO createRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO) {

        return roleService.createRole(roleRequestDTO);
    }

    /**     * Deletes a role by its name.
     *
     * @param name the name of the role to delete
     * @return a message indicating the result of the deletion
     */
    @Operation(summary = "Delete Role", description = "Allows the deletion of a role by its name. Requires ADMIN role. Does not allow deletion of USER and ADMIN roles.")
    @DeleteMapping("/delete/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponseDTO deleteRoleByName(
            @Parameter(description = "Name of the role to delete", in = ParameterIn.PATH)
            @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
            @PathVariable String name) {

        return roleService.deleteRoleByName(name);
    }

    /**
     * Updates an existing role by its ID.
     *
     * @param id the ID of the role to update
     * @param roleRequestDTO DTO containing the updated role details
     * @return a message indicating the result of the update
     */
    @Operation(summary = "Update Role", description = "Allows the update of an existing role. Requires ADMIN role. Does not allow changes in USER and ADMIN roles.")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MessageResponseDTO updateRole(
            @Parameter(description = "ID of the role to update", in = ParameterIn.PATH) @PathVariable Long id,
            @Valid @RequestBody RoleRequestDTO roleRequestDTO) {

        return  roleService.updateRole(id, roleRequestDTO);
    }

    /**
     * Retrieves all users associated with a specific role name.
     *
     * @param roleName the name of the role to retrieve users for
     * @param pageable the pagination information
     * @return a paginated list of users associated with the specified role
     */
    @Operation(summary = "Get all users by role name", description = "Retrieve a paginated list of all users associated with a specific role name. Requires ADMIN role.")
    @GetMapping("/users/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> findAllUsersByRoleName(
            @Parameter(description = "Name of the role to retrieve users for", in = ParameterIn.PATH)
            @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
            @PathVariable String roleName, Pageable pageable) {

        return ResponseEntity.ok(roleService.findAllUsersByRoleName(roleName, pageable));
    }
}