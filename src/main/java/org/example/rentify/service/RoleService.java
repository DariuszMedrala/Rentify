package org.example.rentify.service;

import org.example.rentify.entity.Role;
import org.example.rentify.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service class for managing roles.
 */
@Service
public class RoleService {

    private final RoleRepository roleRepository;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Finds a role by its name.
     *
     * @param name the name of the role
     * @return an Optional containing the role if found, or empty if not found
     */
    Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }
}

