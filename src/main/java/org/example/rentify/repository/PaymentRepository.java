package org.example.rentify.repository;

import org.example.rentify.entity.Payment;
import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;
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
     * Finds all payments with the specified payment status.
     *
     * @param paymentStatus the payment status of the payments
     * @return a list of payments with the specified payment status
     */
    List<Payment> findByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Finds all payments with the specified payment method.
     *
     * @param paymentMethod the payment method of the payments
     * @return a list of payments with the specified payment method
     */
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
}
