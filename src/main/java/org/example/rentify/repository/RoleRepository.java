package org.example.rentify.repository;

import org.example.rentify.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * RoleRepository interface for managing Role entity.
 * This interface extends JpaRepository to provide CRUD operations for Role.
 */
public interface RoleRepository extends JpaRepository <Role, Long> {

    /**
     * Find a role by its name.
     *
     * @param name The name of the role to find.
     * @return An Optional containing the role if found, or empty if not found.
     */
    Optional<Role> findRoleByName(String name);

    /**
     * Find a role by its ID.
     *
     * @param id The ID of the role to find.
     * @return An Optional containing the role if found, or empty if not found.
     */
    Optional<Role> findRoleById(Long id);

    /**
     * Check if a role with the given name exists.
     *
     * @param name The name of the role to check.
     * @return true if a role with the given name exists, false otherwise.
     */
    boolean existsByName(String name);
}
