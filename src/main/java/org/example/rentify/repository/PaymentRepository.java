package org.example.rentify.repository;

import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Payment;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;

/*
 * PaymentRepository interface for managing Payment entities.
 * This interface extends JpaRepository to provide CRUD operations.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds a payment by its booking ID.
     *
     * @param bookingId the booking ID of the payment
     * @return the payment with the specified booking ID, or null if not found
     */
    Payment findByBookingId(Long bookingId);

    /**
     * Finds all payments associated with a specific user.
     *
     * @param user the user whose payments are to be found
     * @return a list of payments associated with the specified user
     */
    List<Payment> findByUser(User user);
}
