package org.example.rentify.repository;

import org.example.rentify.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for Address entity.
 * This interface extends JpaRepository to provide CRUD operations for Address.
 */
public interface AddressRepository extends JpaRepository <Address, Long> {
}
