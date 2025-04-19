package org.example.rentify.repository;

import org.example.rentify.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository interface for Role entity.
 * This interface extends JpaRepository to provide CRUD operations for Role.
 */
public interface RoleRepository extends JpaRepository <Role, Long> {

    /**
     * Find a role by its name.
     *
     * @param name The name of the role to find.
     * @return An Optional containing the role if found, or empty if not found.
     */
    Optional<Role> findByName(String name);
}
