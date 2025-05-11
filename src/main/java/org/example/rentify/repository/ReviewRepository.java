package org.example.rentify.repository;

import org.example.rentify.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
* ReviewRepository interface for managing Review entities.
* This interface extends JpaRepository to provide CRUD operations.
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Finds reviews by the user ID.
     *
     * @param userId the ID of the user
     * @return a list of reviews made by the specified user
     */
    List<Review> findByUserId(Long userId);

    /**
     * Finds reviews by the property ID.
     *
     * @param propertyId the ID of the property
     * @return a list of reviews for the specified property
     */
    List<Review> findByPropertyId(Long propertyId);

    /**
     * Finds reviews by the booking ID.
     *
     * @param bookingId the ID of the booking
     * @return a list of reviews for the specified booking
     */
    Review findByBookingId(Long bookingId);

    /**
     * Deletes reviews by the booking ID.
     *
     * @param reviewId the ID of the booking
     */
    void deleteReviewById(Long reviewId);
}
