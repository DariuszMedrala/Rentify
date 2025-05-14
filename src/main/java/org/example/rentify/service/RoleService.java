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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

/**
 * RoleService class for managing roles in the system.
 * This class provides methods to interact with the RoleRepository and uses RoleMapper for DTO conversion.
 * It throws specific exceptions for error conditions, to be handled by a global exception handler.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserRepository userRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository, RoleMapper roleMapper, UserMapper userMapper, UserRepository userRepository) {
        this.roleRepository = roleRepository;
        this.roleMapper = roleMapper;
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    /**
     * Finds a role by its name.
     *
     * @param name the name of the role to find
     * @return the role response DTO
     * @throws IllegalArgumentException if the name is blank
     * @throws ResponseStatusException if the role not found
     */
    @Transactional(readOnly = true)
    public RoleResponseDTO findRoleByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Role name cannot be null or empty.");
        }
        Role role = roleRepository.findRoleByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return roleMapper.roleToRoleResponseDto(role);
    }

    /**
     * Finds a role by its ID.
     *
     * @param id the ID of the role to find
     * @throws IllegalArgumentException if the ID is null or not positive
     * @throws ResponseStatusException if the role not found
     * @return the role response DTO
     */
    @Transactional(readOnly = true)
    public RoleResponseDTO findRoleById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Role ID must be a positive number.");
        }
        Role role = roleRepository.findRoleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        return roleMapper.roleToRoleResponseDto(role);
    }

    /**
     * Creates a new role.
     *
     * @param roleRequestDTO the role request DTO containing the role details
     * @return a message response DTO indicating success
     * @throws IllegalArgumentException if the roleRequestDTO is null or has an empty name, or if a role with the same name already exists
     */
    @Transactional
    public MessageResponseDTO createRole(RoleRequestDTO roleRequestDTO) {
        if (roleRequestDTO == null || !StringUtils.hasText(roleRequestDTO.getName())) {
            throw new IllegalArgumentException("Role name cannot be null or empty for creation.");
        }
        if(roleRepository.existsByName(roleRequestDTO.getName())) {
            throw new IllegalArgumentException("A role with the name '" + roleRequestDTO.getName() + "' already exists.");
        }
        roleRepository.save(roleMapper.roleRequestDtoToRole(roleRequestDTO));
        return new MessageResponseDTO("Role created successfully: " + roleRequestDTO.getName());
    }

    /**
     * Retrieves all roles in a paginated format.
     * @param pageable the pagination information
     * @throws ResponseStatusException if no roles are found, or if the pageable is null or invalid.
     * @return a page of role response DTOs
     */
    @Transactional(readOnly = true)
    public Page<RoleResponseDTO> findAllRoles(Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid pagination parameters.");
        }
        Page<Role> rolesPage = roleRepository.findAll(pageable);
        if (rolesPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No roles found.");
        }
        return rolesPage.map(roleMapper::roleToRoleResponseDto);
    }

    /**
     * Retrieves all users associated with a specific role name.
     *
     * @param roleName the name of the role
     * @param pageable pagination information
     * @return a page of user response DTOs associated with the role
     * @throws IllegalArgumentException if the roleName is blank
     * @throws ResponseStatusException if the role not found
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllUsersByRoleName(String roleName, Pageable pageable) {
        if (!StringUtils.hasText(roleName)) {
            throw new IllegalArgumentException("Role name to find users for cannot be null or empty.");
        }
        if (!roleRepository.existsByName(roleName)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with name: " + roleName + ", cannot retrieve users.");
        }
        Page<User> usersPage = userRepository.findAllByRolesName(roleName, pageable);
        return usersPage.map(userMapper::userToUserResponseDto);
    }

   /**     * Deletes a role by its name.
     *
     * @param name the name of the role to delete
     * @return a message response DTO indicating success
     * @throws IllegalArgumentException if the name is blank or trying to delete ADMIN/USER roles
     * @throws ResponseStatusException if the role not found
     */
    @Transactional
    public MessageResponseDTO deleteRoleByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Role name to delete cannot be null or empty.");
        }
        Role role = roleRepository.findRoleByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with name: " + name));

        if ("ADMIN".equalsIgnoreCase(role.getName()) || "USER".equalsIgnoreCase(role.getName())) {
            throw new IllegalArgumentException("Cannot delete the ADMIN or USER role.");
        }
        roleRepository.delete(role);
        return new MessageResponseDTO("Role deleted successfully: " + name);
    }

    /**
     * Updates an existing role by its ID.
     *
     * @param id the ID of the role to update
     * @param roleRequestDTO the role request DTO containing the updated role details
     * @return a message response DTO indicating success
     * @throws IllegalArgumentException if the ID is null or not positive, or if the roleRequestDTO is null or has an empty name
     * @throws ResponseStatusException if the role not found, or if trying to update ADMIN/USER roles
     */
    @Transactional
    public MessageResponseDTO updateRole(Long id, RoleRequestDTO roleRequestDTO) {
        if (id == null || id <= 0 || roleRequestDTO == null || !StringUtils.hasText(roleRequestDTO.getName())) {
            throw new IllegalArgumentException("Role ID for update must be a positive number, and role name cannot be null or empty");
        }


        Role existingRole = roleRepository.findRoleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + id));

        if ("ADMIN".equalsIgnoreCase(existingRole.getName()) || "USER".equalsIgnoreCase(existingRole.getName())) {
            throw new IllegalArgumentException("Error: Cannot update the ADMIN or USER role.");
        }

        if (StringUtils.hasText(roleRequestDTO.getName()) && !existingRole.getName().equals(roleRequestDTO.getName())) {
            if(roleRepository.existsByName(roleRequestDTO.getName())) {
                throw new IllegalArgumentException("Error: Another role with the name '" + roleRequestDTO.getName() + "' already exists.");
            }
            existingRole.setName(roleRequestDTO.getName());
        }
        existingRole.setDescription(roleRequestDTO.getDescription());
        roleRepository.save(existingRole);
        return new MessageResponseDTO("Role updated successfully: " + existingRole.getName());
    }
}