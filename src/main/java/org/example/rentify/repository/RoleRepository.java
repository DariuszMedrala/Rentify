package org.example.rentify.repository;

import org.example.rentify.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Role entity.
 * This interface extends JpaRepository to provide CRUD operations for Role.
 */
public interface RoleRepository extends JpaRepository <Role, Long> {
}
