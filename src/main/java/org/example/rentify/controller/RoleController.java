package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.request.RoleRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/*
 * RoleController is a REST controller for managing roles in the system.
 * It provides endpoints for creating, updating, deleting, and retrieving roles.
 */
@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles Management", description = "Endpoints for managing roles")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private static final Logger logger = LoggerFactory.getLogger(RoleController.class);

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
    public ResponseEntity<Page<RoleResponseDTO>> getAllRoles(@Parameter(
            name = "pageable",
            description = "Pageable object containing pagination information",
            example = "{\"page\": 0, \"size\": 10, \"sort\": \"name,asc\"}") Pageable pageable) {
        Page<RoleResponseDTO> roleResponseDTOPage = roleService.findAllRoles(pageable);
        return ResponseEntity.ok(roleResponseDTOPage);
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
    public ResponseEntity<RoleResponseDTO> getRoleByName(
            @Parameter(description = "Name of the role to retrieve") @PathVariable String name) {
        RoleResponseDTO roleResponseDTO = roleService.findRoleByName(name);
        return ResponseEntity.ok(roleResponseDTO);
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
    public ResponseEntity<RoleResponseDTO> getRoleById(
            @Parameter(description = "ID of the role to retrieve") @PathVariable Long id) {
        RoleResponseDTO roleResponseDTO = roleService.findRoleById(id);
        return ResponseEntity.ok(roleResponseDTO);
    }

    /**
     * Creates a new role.
     *
     * @param roleRequestDTO The role request DTO containing role details
     * @return the created role's details or an error message if creation fails
     */
    @Operation(summary = "Create New Role", description = "Allows the creation of a new role. Requires ADMIN role.")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDTO> adminCreateRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        RoleResponseDTO createdRole = roleService.createRole(roleRequestDTO);
        logger.info("Admin created role successfully: {}", createdRole.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
    }

    /**
     * Deletes a role by its name.
     *
     * @param name the name of the role to delete
     * @return a message indicating the result of the deletion
     */
    @Operation(summary = "Delete Role", description = "Allows the deletion of a role by its name. Requires ADMIN role. Does not allow deletion of USER and ADMIN roles.")
    @DeleteMapping("/delete/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponseDTO> deleteRole(
            @Parameter(description = "Name of the role to delete") @PathVariable String name) {
        roleService.deleteRoleByName(name);
        logger.info("Admin deleted role successfully: {}", name);
        return ResponseEntity.ok(new MessageResponseDTO("Role '" + name + "' deleted successfully."));
    }

    /**
     * Updates an existing role.
     *
     * @param id the ID of the role to update
     * @param roleRequestDTO DTO containing update details
     * @return the updated role's details or an error message if the update fails
     */
    @Operation(summary = "Update Role", description = "Allows the update of an existing role. Requires ADMIN role. Does not allow changes in USER and ADMIN roles.")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoleResponseDTO> updateRole(
            @Parameter(description = "ID of the role to update") @PathVariable Long id,
            @Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        RoleResponseDTO updatedRole = roleService.updateRole(id, roleRequestDTO);
        logger.info("Admin updated role successfully: {}", updatedRole.getName());
        return ResponseEntity.ok(updatedRole);
    }

    /**
     * Retrieves all users associated with a specific role name.
     *
     * @param roleName the name of the role
     * @param pageable pagination information
     * @return a paginated list of users associated with the role
     */
    @Operation(summary = "Get all users by role name", description = "Retrieve a paginated list of all users associated with a specific role name. Requires ADMIN role.")
    @GetMapping("/users/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDTO>> getAllUsersByRoleName(
            @Parameter(description = "Name of the role to retrieve users for") @PathVariable String roleName, Pageable pageable) {
        Page<UserResponseDTO> userResponseDTOPage = roleService.findAllUsersByRoleName(roleName, pageable);
        return ResponseEntity.ok(userResponseDTOPage);
    }
}