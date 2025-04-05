package org.example.rentify.repository;

import org.example.rentify.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * ImageRepository interface for managing Image entities.
 * This interface extends JpaRepository to provide CRUD operations.
 */
public interface ImageRepository extends JpaRepository<Image, Long> {

    /**
     * Finds images by the property ID.
     *
     * @param propertyId the ID of the property
     * @return a list of images associated with the specified property
     */
    List<Image> findByPropertyId(Long propertyId);
}
