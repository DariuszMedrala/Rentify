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
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

/*
 * RoleController is a REST controller for managing roles in the system.
 * It provides endpoints to retrieve all roles and their details.
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
    public ResponseEntity<?> getAllRoles(@PageableDefault(size = 10, sort = "name")Pageable pageable) {
        try {
            Page<RoleResponseDTO> roleResponseDTOPage = roleService.findAllRoles(pageable);
            return ResponseEntity.ok(roleResponseDTOPage);
        }catch (IllegalArgumentException e) {
            logger.warn("Failed to retrieve roles due to an illegal argument: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: Invalid request parameters. " + e.getMessage()));
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDTO("Error: An unexpected error occurred while trying to retrieve users."));
        }
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
    public ResponseEntity<?> getRoleByName(@Parameter(description = "Name of the role to retrieve") @PathVariable String name) {
        try {
            RoleResponseDTO roleResponseDTO = roleService.findRoleByName(name);
            return ResponseEntity.ok(roleResponseDTO);
        } catch (ResponseStatusException e) {
            logger.warn("Role not found with username: {}", name, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: Role not found with name " + name));

        }
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
    public ResponseEntity<?> getRoleById(@Parameter(description = "ID of the role to retrieve") @PathVariable Long id) {
        try {
            RoleResponseDTO roleResponseDTO = roleService.findRoleById(id);
            return ResponseEntity.ok(roleResponseDTO);
        } catch (ResponseStatusException e) {
            logger.warn("Role not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: Role not found with ID " + id));
        }
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
    public ResponseEntity<?> adminCreateRole(@Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        try {
            RoleResponseDTO createdRole = roleService.createRole(roleRequestDTO);
            logger.info("Admin created role successfully: {}", createdRole.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRole);
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to create role: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("An unexpected error occurred while creating role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDTO("Error: An unexpected error occurred while trying to create the role."));
        }
    }

    /**
     * Deletes a role by its name.
     *
     * @param name the name of the role to delete
     * @return a message indicating the result of the deletion
     */
    @Operation(summary = "Delete Role", description = "Allows the deletion of a role by its name. Requires ADMIN role. Doesnt allow deletion of USER and ADMIN roles.")
    @DeleteMapping("/delete/{name}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRole(@Parameter(description = "Name of the role to delete") @PathVariable String name) {
        try {
            roleService.deleteRoleByName(name);
            logger.info("Admin deleted role successfully: {}", name);
            return ResponseEntity.ok(new MessageResponseDTO("Role " + name + " deleted successfully."));
        } catch (ResponseStatusException e) {
            logger.warn("Role not found with name: {}", name, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: Role not found with name " + name));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to delete role: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("An unexpected error occurred while deleting role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDTO("Error: An unexpected error occurred while trying to delete the role."));
        }
    }

    /**
     * Updates an existing role.
     *
     * @param id the ID of the role to update
     * @return the updated role's details or an error message if the update fails
     */
    @Operation(summary = "Update Role", description = "Allows the update of an existing role. Requires ADMIN role. Doesnt allow changes in USER and ADMIN roles.")
    @PutMapping("/update/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(@Parameter(description = "ID of the role to update") @PathVariable Long id,
                                        @Valid @RequestBody RoleRequestDTO roleRequestDTO) {
        try {
            RoleResponseDTO updatedRole = roleService.updateRole(id, roleRequestDTO);
            logger.info("Admin updated role successfully: {}", updatedRole.getName());
            return ResponseEntity.ok(updatedRole);
        } catch (ResponseStatusException e) {
            logger.warn("Role not found with ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: Role not found with ID " + id));
        } catch (IllegalArgumentException e) {
            logger.warn("Failed to update role: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponseDTO("Error: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("An unexpected error occurred while updating role: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDTO("Error: An unexpected error occurred while trying to update the role."));
        }
    }

    /**
     * Retrieves all users associated with a specific role name.
     *
     * @param roleName the name of the role
     * @return a paginated list of users associated with the role
     */
    @Operation(summary = "Get all users by role name", description = "Retrieve a paginated list of all users associated with a specific role name. Requires ADMIN role.")
    @GetMapping("/users/{roleName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsersByRoleName(@Parameter(description = "Name of the role to retrieve users for") @PathVariable String roleName,
                                                   @PageableDefault(size = 10, sort = "username") Pageable pageable) {
        try {
            Page<UserResponseDTO> userResponseDTOPage = roleService.findAllUsersByRoleName(roleName, pageable);
            return ResponseEntity.ok(userResponseDTOPage);
        } catch (ResponseStatusException e) {
            logger.warn("Role not found with name: {}", roleName, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO("Error: Role not found with name " + roleName));
        } catch (Exception e) {
            logger.error("An unexpected error occurred while retrieving users by role name: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDTO("Error: An unexpected error occurred while trying to retrieve users by role name."));
        }
    }
}

