package org.example.rentify.service;

import org.example.rentify.dto.request.RoleRequestDTO;
import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.Role;
import org.example.rentify.entity.User;
import org.example.rentify.mapper.RoleMapper;
import org.example.rentify.mapper.UserMapper;
import org.example.rentify.repository.RoleRepository;
import org.example.rentify.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

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
            throw new IllegalArgumentException("Role name to find cannot be null or empty.");
        }
        logger.debug("Finding role by name: {}", name);
        Role role = roleRepository.findRoleByName(name)
                .orElseThrow(() -> {
                    logger.warn("Role not found with name: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with name: " + name);
                });
        return roleMapper.roleToRoleResponseDto(role);
    }

    /**
     * Finds a role by its ID.
     *
     * @param id the ID of the role to find
     * @return the role response DTO
     * @throws IllegalArgumentException if id is null or not positive
     * @throws ResponseStatusException if the role not found
     */
    @Transactional(readOnly = true)
    public RoleResponseDTO findRoleById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Role ID must be a positive number.");
        }
        logger.debug("Finding role by ID: {}", id);
        Role role = roleRepository.findRoleById(id)
                .orElseThrow(() -> {
                    logger.warn("Role not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + id);
                });
        return roleMapper.roleToRoleResponseDto(role);
    }

    /**
     * Creates a new role in the system.
     *
     * @param roleRequestDTO the role request DTO containing role details
     * @return the created role response DTO
     * @throws IllegalArgumentException if the role name already exists or the request is invalid
     */
    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO) {
        if (roleRequestDTO == null || !StringUtils.hasText(roleRequestDTO.getName())) {
            throw new IllegalArgumentException("Role name cannot be null or empty for creation.");
        }
        logger.info("Attempting to create role: {}", roleRequestDTO.getName());
        if(roleRepository.existsByName(roleRequestDTO.getName())) {
            throw new IllegalArgumentException("Error: A role with the name '" + roleRequestDTO.getName() + "' already exists.");
        }
        Role role = roleMapper.roleRequestDtoToRole(roleRequestDTO);
        Role savedRole = roleRepository.save(role);
        logger.info("Role created successfully: {}", savedRole.getName());
        return roleMapper.roleToRoleResponseDto(savedRole);
    }

    /**
     * Retrieves all roles in a paginated format as DTOs.
     * @param pageable Pagination information
     * @return A page of RoleResponseDTOs
     */
    @Transactional(readOnly = true)
    public Page<RoleResponseDTO> findAllRoles(Pageable pageable) {
        logger.debug("Fetching all roles, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Role> rolesPage = roleRepository.findAll(pageable);
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
        logger.debug("Finding users for role name: {}, page: {}, size: {}", roleName, pageable.getPageNumber(), pageable.getPageSize());
        if (!roleRepository.existsByName(roleName)) {
            logger.warn("Attempted to find users for a non-existent role: {}", roleName);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with name: " + roleName + ", cannot retrieve users.");
        }
        Page<User> usersPage = userRepository.findAllByRolesName(roleName, pageable);
        return usersPage.map(userMapper::userToUserResponseDto);
    }

    /**
     * Deletes a role by its name.
     *
     * @param name the name of the role to delete
     * @throws IllegalArgumentException if the name is blank, or if trying to delete ADMIN/USER roles
     * @throws ResponseStatusException if the role not found
     */
    @Transactional
    public void deleteRoleByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Role name to delete cannot be null or empty.");
        }
        logger.info("Attempting to delete role by name: {}", name);
        Role role = roleRepository.findRoleByName(name)
                .orElseThrow(() -> {
                    logger.warn("Attempted to delete non-existent role: {}", name);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with name: " + name);
                });
        if ("ADMIN".equalsIgnoreCase(role.getName()) || "USER".equalsIgnoreCase(role.getName())) {
            logger.warn("Attempted to delete protected role: {}", role.getName());
            throw new IllegalArgumentException("Cannot delete the ADMIN or USER role.");
        }
        roleRepository.delete(role);
        logger.info("Role deleted successfully: {}", name);
    }

    /**
     * Updates an existing role.
     *
     * @param id the ID of the role to update
     * @param roleRequestDTO DTO containing update information
     * @return updated RoleResponseDTO
     * @throws IllegalArgumentException if id is invalid, request DTO is invalid, or trying to update ADMIN/USER roles
     * @throws ResponseStatusException if the role not found
     */
    @Transactional
    public RoleResponseDTO updateRole(Long id, RoleRequestDTO roleRequestDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Role ID for update must be a positive number.");
        }
        if (roleRequestDTO == null || !StringUtils.hasText(roleRequestDTO.getName())) {
            throw new IllegalArgumentException("Role details for update cannot be null or have an empty name.");
        }
        logger.info("Attempting to update role with ID: {}", id);

        Role existingRole = roleRepository.findRoleById(id)
                .orElseThrow(() -> {
                    logger.warn("Attempted to update non-existent role with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found with ID: " + id);
                });

        if ("ADMIN".equalsIgnoreCase(existingRole.getName()) || "USER".equalsIgnoreCase(existingRole.getName())) {
            logger.warn("Attempted to update protected role: {}", existingRole.getName());
            throw new IllegalArgumentException("Error: Cannot update the ADMIN or USER role.");
        }

        if (StringUtils.hasText(roleRequestDTO.getName()) && !existingRole.getName().equals(roleRequestDTO.getName())) {
            if(roleRepository.existsByName(roleRequestDTO.getName())) {
                throw new IllegalArgumentException("Error: Another role with the name '" + roleRequestDTO.getName() + "' already exists.");
            }
            existingRole.setName(roleRequestDTO.getName());
        }

        if (roleRequestDTO.getDescription() != null) {
            existingRole.setDescription(roleRequestDTO.getDescription());
        }

        Role updatedRole = roleRepository.save(existingRole);
        logger.info("Role updated successfully: {}", updatedRole.getName());
        return roleMapper.roleToRoleResponseDto(updatedRole);
    }
}