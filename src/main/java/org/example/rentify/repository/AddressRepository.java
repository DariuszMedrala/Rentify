package org.example.rentify.repository;

import org.example.rentify.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * AddressRepository interface for managing Address entities.
 * This interface extends JpaRepository to provide CRUD operations.
 */
public interface AddressRepository extends JpaRepository <Address, Long> {
}
