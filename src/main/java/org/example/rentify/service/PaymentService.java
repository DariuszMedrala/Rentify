package org.example.rentify.service;

import jakarta.validation.Valid;
import org.example.rentify.dto.request.PaymentRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.PaymentResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Payment;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;
import org.example.rentify.mapper.PaymentMapper;
import org.example.rentify.repository.BookingRepository;
import org.example.rentify.repository.PaymentRepository;
import org.example.rentify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentMapper paymentMapper;
    private final BookingRepository bookingRepository;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository, PaymentMapper paymentMapper,
                          BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.paymentMapper = paymentMapper;
        this.bookingRepository = bookingRepository;
    }

    /**
     * Retrieves all payments made by the user identified by the given username.
     *
     * @param username the username of the user whose payments are to be retrieved
     * @return a list of PaymentResponseDTOs representing the user's payments
     * @throws ResponseStatusException if the user is not found or has no payments
     */
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> getAllPayments(String username) {
        User user = userRepository.findUserByUsername(username).
                orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Payment> payments = paymentRepository.findByUser(user);
        if (payments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No payments found for user");
        }
        return payments.stream()
                .map(paymentMapper::paymentToPaymentResponseDto)
                .toList();
    }


    /**
     * Processes a payment for a booking identified by the given booking ID.
     *
     * @param bookingId the ID of the booking for which the payment is being made
     * @param paymentRequestDTO the DTO containing payment details
     * @return a MessageResponseDTO indicating the result of the payment operation
     * @throws ResponseStatusException if the booking is not found, payment already exists, or payment amount does not match booking total price
     */
    public MessageResponseDTO makePayment(Long bookingId, PaymentRequestDTO paymentRequestDTO) {
        if (bookingId == null || bookingId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking ID cannot be null or negative");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        Payment payment = paymentRepository.findByBookingId(bookingId);
        if (payment != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment already exists for this booking");
        }
        if (!Objects.equals(booking.getTotalPrice(), paymentRequestDTO.getAmount())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount does not match booking total price");
        }
        payment = paymentMapper.paymentRequestDtoToPayment(paymentRequestDTO);
        payment.setBooking(booking);
        payment.setUser(booking.getUser());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
        return new MessageResponseDTO("Payment created successfully for booking ID: " + bookingId);
    }

    /**
     * Retrieves the payment associated with a specific booking ID.
     *
     * @param bookingId the ID of the booking for which the payment is being retrieved
     * @return a PaymentResponseDTO containing the payment details
     * @throws ResponseStatusException if the booking ID is null or negative, or if no payment is found for the booking ID
     */
    public PaymentResponseDTO getPaymentByBookingId(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking ID cannot be null or negative");
        }
        Payment payment = paymentRepository.findByBookingId(bookingId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for booking ID: " + bookingId);
        }
        return paymentMapper.paymentToPaymentResponseDto(payment);
    }

    /**
     * Deletes the payment associated with a specific booking ID.
     *
     * @param bookingId the ID of the booking for which the payment is being deleted
     * @return a MessageResponseDTO indicating the result of the deletion operation
     * @throws ResponseStatusException if the booking ID is null or negative, or if no payment is found for the booking ID
     */
    public MessageResponseDTO deletePaymentByBookingId(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking ID can not be null or negative");
        }
        Payment payment = paymentRepository.findByBookingId(bookingId);
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for booking ID: " + bookingId);
        }
        paymentRepository.delete(payment);
        return new MessageResponseDTO("Payment deleted successfully for booking ID: " + bookingId);
    }

    /**
     * Retrieves all payments made by a specific user.
     *
     * @param username the username of the user whose payments are to be retrieved
     * @return a list of PaymentResponseDTOs representing the user's payments
     * @throws ResponseStatusException if the user is not found or has no payments
     */
    public List<PaymentResponseDTO> getAllPaymentsByUser(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested User was not found"));

        List<Payment> payments = paymentRepository.findByUser(user);
        if (payments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No payments found for user");
        }
        return payments.stream()
                .map(paymentMapper::paymentToPaymentResponseDto)
                .toList();
    }

    /**
     * Calculates the total amount paid by a specific user.
     *
     * @param username the username of the user whose total payment amount is to be calculated
     * @return the total amount paid by the user
     * @throws ResponseStatusException if the user is not found or has no payments
     */
    public Double getTotalAmountPaidByUser(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested User was not found"));

        List<Payment> payments = paymentRepository.findByUser(user);
        if (payments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No payments found for user");
        }
        return payments.stream()
                .map(Payment::getAmount)
                .mapToDouble(BigDecimal::doubleValue)
                .sum();
    }

    /**
     * Calculates the total amount paid by all users.
     *
     * @return the total amount paid by all users
     * @throws ResponseStatusException if no payments are found
     */
    public BigDecimal getTotalAmountPaidByAllUsers() {
        List<Payment> payments = paymentRepository.findAll();
        if (payments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No payments found");
        }
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the total amount paid by all users for a specific property.
     *
     * @param propertyId the ID of the property for which the total payment amount is to be calculated
     * @return the total amount paid by all users for the specified property
     * @throws ResponseStatusException if the property ID is null or negative, or if no bookings are found for the property
     */
    public BigDecimal getTotalAmountPaidByAllUsersForProperty(Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property ID cannot be null or negative");
        }
        List<Booking> bookings = bookingRepository.findByPropertyId(propertyId);
        if (bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No bookings found for property ID: " + propertyId);
        }
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Booking booking : bookings) {
            Payment payment = paymentRepository.findByBookingId(booking.getId());
            if (payment != null) {
                totalAmount = totalAmount.add(payment.getAmount());
            }
        }
        return totalAmount;
    }

    /**
     * Updates the payment status for a specific payment ID.
     *
     * @param paymentId the ID of the payment to be updated
     * @param paymentStatus the new payment status to be set
     * @return a MessageResponseDTO indicating the result of the update operation
     * @throws ResponseStatusException if the payment ID is null or negative, or if no payment is found for the ID
     */
    public MessageResponseDTO updatePaymentStatus(Long paymentId, @Valid PaymentStatus paymentStatus) {
        if (paymentId == null || paymentId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment ID cannot be null or negative");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for ID: " + paymentId));
        payment.setPaymentStatus(paymentStatus);
        paymentRepository.save(payment);
        return new MessageResponseDTO("Payment status updated successfully for payment ID: " + paymentId);
    }

    /**
     * Updates the payment method for a specific payment ID.
     *
     * @param paymentId the ID of the payment to be updated
     * @param paymentMethod the new payment method to be set
     * @return a MessageResponseDTO indicating the result of the update operation
     * @throws ResponseStatusException if the payment ID is null or negative, or if no payment is found for the ID
     */
    public MessageResponseDTO updatePaymentMethod(Long paymentId, @Valid PaymentMethod paymentMethod) {
        if (paymentId == null || paymentId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment ID cannot be null or negative");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for ID: " + paymentId));
        payment.setPaymentMethod(paymentMethod);
        paymentRepository.save(payment);
        return new MessageResponseDTO("Payment method updated successfully for payment ID: " + paymentId);
    }

    public MessageResponseDTO updatePayment(Long paymentId, @Valid PaymentRequestDTO paymentRequestDTO) {
        if (paymentId == null || paymentId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment ID cannot be null or negative");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for ID: " + paymentId));

        paymentMapper.updatePaymentFromDto(paymentRequestDTO, payment);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        paymentRepository.save(payment);
        return new MessageResponseDTO("Payment updated successfully for payment ID: " + paymentId);
    }


    /**
     * Checks if the user is the owner of the payment associated with the given payment ID.
     *
     * @param paymentId the ID of the payment to check
     * @param username the username of the user to check ownership for
     * @return true if the user is the owner of the payment, false otherwise
     * @throws ResponseStatusException if the payment ID is null or negative, or if no payment is found for the ID
     */
    public boolean isPaymentOwner(Long paymentId, String username) {
        if (paymentId == null || paymentId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment ID cannot be null or negative");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found for ID: " + paymentId));
        return payment.getUser().getUsername().equals(username);

    }
}

