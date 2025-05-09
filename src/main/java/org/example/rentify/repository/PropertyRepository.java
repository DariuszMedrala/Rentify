package org.example.rentify.repository;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.enums.PropertyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
 * PropertyRepository interface for managing Property entities.
 * It extends JpaRepository to provide CRUD operations and custom query methods.
 */
public interface PropertyRepository extends JpaRepository<Property, Long> {

    /**
     * Finds properties by their availability status.
     *
     * @param availability the availability status of the properties
     * @param pageable the pagination information
     * @return a page of properties with the specified availability status
     */
    Page<Property> findByAvailability(Boolean availability, Pageable pageable);

    /**
     * Deletes a property by its ID.
     * @param id the ID of the property to delete
     */
    void deletePropertyById(Long id);


    /**
     * Finds properties by their type and availability status.
     *
     * @param propertyType the type of the properties
     * @param availability the availability status of the properties
     * @param pageable     the pagination information
     * @return aa page of properties with the specified type and availability status
     */
    Page<Property> findByPropertyTypeAndAvailability(PropertyType propertyType, Boolean availability, Pageable pageable);

    /**
     * Finds properties by country, city and availability status.
     *
     * @param addressCountry the country of the properties
     * @param addressCity    the city of the properties
     * @param availability   the availability status of the properties
     * @param pageable       the pagination information
     * @return a page of properties with the specified country, city and availability status
     */
    Page<Property> findAllByAddress_CountryAndAddress_CityAndAvailability(String addressCountry, String addressCity, Boolean availability, Pageable pageable);

    /**
     * Finds a property by its ID.
     *
     * @param id the ID of the property to find
     * @return an Optional containing the property if found, or empty if not found.
     */
    Optional<Property> findPropertyById(Long id);

}
