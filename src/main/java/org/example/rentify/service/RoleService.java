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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * RoleService class for managing roles in the system.
 * This class provides methods to interact with the RoleRepository and uses RoleMapper for DTO conversion.
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
     * Checks if a role with the given name exists.
     *
     * @param name the name of the role to check
     * @return true if the role exists, false otherwise
     */
    public boolean existsByName(String name) {
        return roleRepository.existsByName(name);
    }

    /**
     * Finds a role by its name.
     *
     * @param name the name of the role to find
     * @return the role response DTO
     */
    @Transactional(readOnly = true)
    public RoleResponseDTO findRoleByName(String name) {
        Role role = roleRepository.findRoleByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        return roleMapper.roleToRoleResponseDto(role);
    }

    /**
     * Finds a role by its ID.
     *
     * @param id the ID of the role to find
     * @return the role response DTO
     */
    @Transactional(readOnly = true)
    public RoleResponseDTO findRoleById(Long id) {
        Role role = roleRepository.findRoleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        return roleMapper.roleToRoleResponseDto(role);
    }

    /**
     * Creates a new role in the system.
     *
     * @param roleRequestDTO the role request DTO containing role details
     * @return the created role response DTO
     */
    @Transactional
    public RoleResponseDTO createRole(RoleRequestDTO roleRequestDTO) {

        if(existsByName(roleRequestDTO.getName())) {
            throw new IllegalArgumentException("Error: A role with that name already exists");
        }
        Role role = roleMapper.roleRequestDtoToRole(roleRequestDTO);
        return roleMapper.roleToRoleResponseDto(roleRepository.save(role));
    }

    /**
     * Retrieves all roles in a paginated format as DTOs.
     * @param pageable Pagination information
     * @return A page of RoleResponseDTOs
     */
    @Transactional(readOnly = true)
    public Page<RoleResponseDTO> findAllRoles(Pageable pageable) {
        Page<Role> rolesPage = roleRepository.findAll(pageable);
        return rolesPage.map(roleMapper::roleToRoleResponseDto);
    }

    /**
     * Retrieves all users associated with a specific role name.
     *
     * @param roleName the name of the role
     * @return a list of user response DTOs associated with the role
     */
    @Transactional(readOnly = true)
    public Page<UserResponseDTO> findAllUsersByRoleName(String roleName, Pageable pageable) {
        if (roleRepository.findRoleByName(roleName).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found");
        }
        Page<User> usersPage = userRepository.findAllByRolesName(roleName, pageable);
        return usersPage.map(userMapper::userToUserResponseDto);
    }

    /**
     * Deletes a role by its name.
     *
     * @param name the name of the role to delete
     */
    @Transactional
    public void deleteRoleByName(String name) {
        Role role = roleRepository.findRoleByName(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        if (role.getName().equals("ADMIN") || role.getName().equals("USER")) {
            throw new IllegalArgumentException("Cannot delete the ADMIN or USER role");
        }
        roleRepository.delete(role);
    }

    /**
     * Updates an existing role.
     *
     * @param id the ID of the role to update
     */
    @Transactional
    public RoleResponseDTO updateRole(Long id, RoleRequestDTO roleRequestDTO) {
        Role existingRole = roleRepository.findRoleById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Role not found"));
        if (existingRole.getName().equals("ADMIN") || existingRole.getName().equals("USER")) {
            throw new IllegalArgumentException("Error: Cannot update the ADMIN or USER role");
        }
        if (roleRequestDTO.getName() != null) {
            existingRole.setName(roleRequestDTO.getName());
        }
        if (roleRequestDTO.getDescription() != null) {
            existingRole.setDescription(roleRequestDTO.getDescription());
        }
        Role updatedRole = roleRepository.save(existingRole);
        return roleMapper.roleToRoleResponseDto(updatedRole);
    }
}