package org.example.rentify.repository;

import org.example.rentify.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * BookingRepository interface for managing Booking entities.
 * This interface extends JpaRepository to provide CRUD operations.
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Finds bookings by the property ID.
     *
     * @param propertyId the ID of the property
     * @return a list of bookings connected to the specified property
     */
    List<Booking> findByPropertyId(Long propertyId);

    /**
     * Finds bookings by the user ID.
     *
     * @param userId the ID of the user
     * @return a list of bookings made by the specified user
     */
    List<Booking> findByUserId(Long userId);

    /**
     * Finds bookings by the booking status.
     * @param bookingStatus the status of the booking
     * @return a list of bookings with the specified status
     */
    List<Booking> findByBookingStatus(Booking.BookingStatus bookingStatus);
}
