package org.example.rentify.repository;

import org.example.rentify.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * PropertyRepository interface for managing Property entities.
 * It extends JpaRepository to provide CRUD operations and custom query methods.
 */
public interface PropertyRepository extends JpaRepository<Property, Long> {

    /**
     * Finds properties by their availability status.
     *
     * @param availability the availability status of the properties
     * @return a list of properties with the specified availability status
     */
    List<Property> findByAvailability(Boolean availability);

    /**
     * Finds properties by the username of their owner.
     *
     * @param username the username of the property owner
     * @return a list of properties owned by the specified user
     */
    List<Property> findByOwnerUsername(String username);

    /**
     * Finds properties by their city.
     *
     * @param city the city where the properties are located
     * @return a list of properties located in the specified city
     */
    List<Property> findByCity(String city);

    /**
     * Finds properties by their city and availability status.
     *
     * @param city the city where the properties are located
     * @param availability the availability status of the properties
     * @return a list of properties located in the specified city with the specified availability status
     */
    List<Property> findByCityAndAvailability(String city, Boolean availability);

    /**
     * Finds properties by their property type and availability status.
     *
     * @param propertyType the type of the properties
     * @param availability the availability status of the properties
     * @return a list of properties with the specified property type and availability status
     */
    List<Property> findByPropertyTypeAndAvailability(Property.PropertyType propertyType, Boolean availability);
}
