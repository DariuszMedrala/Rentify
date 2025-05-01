package org.example.rentify.repository;

import org.example.rentify.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * UserRepository interface for managing User entities in the database.
 * This interface extends JpaRepository to provide CRUD operations and custom query methods.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their username.
     * @param username The username of the user to find.
     * @return An Optional containing the user if found, or empty if not found.
     */
    Optional<User> findUserByUsername(String username);

    /**
     * Find a user by their email address.
     * @param email The email address of the user to find.
     * @return An Optional containing the user if found, or empty if not found.
     */
    Optional<User> findUserByEmail(String email);

    /**
     * Find a user by their ID.
     * @param id The ID of the user to find.
     * @return An Optional containing the user if found, or empty if not found.
     */
    Optional<User> findUserById(Long id);

    /**
     * Check if a user with the given username exists in the database.
     * @param username The username to check.
     * @return true if a user with the given username exists, false otherwise.
     */
    boolean existsByUsername(String username);

    /**
     * Check if a user with the given email exists in the database.
     * @param email The email to check.
     * @return true if a user with the given email exists, false otherwise.
     */
    boolean existsByEmail(String email);

}
