package org.example.rentify.repository;

import org.example.rentify.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
 * PaymentRepository interface for managing Payment entities.
 * This interface extends JpaRepository to provide CRUD operations.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Finds a payment by its transaction ID.
     *
     * @param transactionId the transaction ID of the payment
     * @return the payment with the specified transaction ID, or null if not found
     */
    Payment findByTransactionId(String transactionId);

    /**
     * Finds a payment by its booking ID.
     *
     * @param bookingId the booking ID of the payment
     * @return the payment with the specified booking ID, or null if not found
     */
    Payment findByBookingId(Long bookingId);

    /**
     * Finds a payment by its payment status.
     *
     * @param paymentStatus the payment status of the payment
     * @return the payment with the specified payment status, or null if not found
     */
    List<Payment> findByPaymentStatus(Payment.PaymentStatus paymentStatus);

    /**
     * Finds a payment by its payment method.
     *
     * @param paymentMethod the payment method of the payment
     * @return the payment with the specified payment method, or null if not found
     */
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
}
