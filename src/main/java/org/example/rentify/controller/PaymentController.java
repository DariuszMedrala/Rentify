package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.request.PaymentRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.PaymentResponseDTO;
import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;
import org.example.rentify.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * PaymentController is a REST controller that handles payment-related operations.
 * It provides endpoints for managing payments for bookings.
 */
@RestController()
@RequestMapping("/api/bookings/payments")
@Tag(name = "Payment Management", description = "Endpoints for managing booking payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Retrieves all payments made by the authenticated user.
     *
     * @return a ResponseEntity containing a list of all payments made by the user
     */
    @Operation(summary = "Get all payments", description = "Retrieves all payments made by the authenticated user.")
    @GetMapping("/me/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments(Authentication authentication) {

        return ResponseEntity.ok(paymentService.getAllPayments(((UserDetails) authentication.getPrincipal()).getUsername()));
    }

    /**
     * Makes a payment for a specific booking.
     *
     * @param bookingId the ID of the booking for which the payment is being made
     * @param paymentResponseDTO the DTO containing payment details
     * @return a MessageResponseDTO indicating the result of the payment operation
     */
    @Operation(summary = "Make a payment for a booking", description = "Processes a payment for a specific booking.")
    @PostMapping("/{bookingId}/pay")
    @PreAuthorize("isAuthenticated() and (hasRole('Admin') or @bookingService.isBookingOwner(#bookingId, principal.username))")
    public MessageResponseDTO makePayment(@Parameter(description = "Booking ID", in = ParameterIn.PATH)
                                          @PathVariable Long bookingId,@Parameter(description = "Payment DTO")
                                          @Valid PaymentRequestDTO paymentResponseDTO) {

        return paymentService.makePayment(bookingId, paymentResponseDTO);
    }

    /**
     * Retrieves a payment connected to a specific booking.
     *
     * @param bookingId the ID of the booking for which payment is being retrieved
     * @return a ResponseEntity containing the PaymentResponseDTO for the specified booking
     */
    @Operation(summary = "Get payment by booking ID", description = "Retrieves payment details for a specific booking.")
    @GetMapping("/{bookingId}")
    @PreAuthorize("isAuthenticated() and (hasRole('Admin') or @bookingService.isBookingOwner(#bookingId, principal.username))")
    public ResponseEntity<PaymentResponseDTO> getPaymentByBookingId(@Parameter(description = "Booking ID", in = ParameterIn.PATH)
                                                                      @PathVariable Long bookingId) {
        return ResponseEntity.ok(paymentService.getPaymentByBookingId(bookingId));
    }

    /**
     * Deletes a payment associated with a specific booking.
     *
     * @param bookingId the ID of the booking for which the payment is being deleted
     * @return a MessageResponseDTO indicating the result of the deletion operation
     */
    @Operation(summary = "Delete payment by booking ID", description = "Deletes a payment associated with a specific booking.")
    @DeleteMapping("/{bookingId}/delete")
    @PreAuthorize("isAuthenticated() and (hasRole('Admin') or @bookingService.isBookingOwner(#bookingId, principal.username))")
    public MessageResponseDTO deletePaymentByBookingId(@Parameter(description = "Booking ID", in = ParameterIn.PATH)
                                                        @PathVariable Long bookingId) {
        return paymentService.deletePaymentByBookingId(bookingId);
    }

    /**
     * Retrieves all payments made by a specific user.
     *
     * @param username the username of the user whose payments are to be retrieved
     * @return a ResponseEntity containing a list of PaymentResponseDTOs for the specified user
     */
    @Operation(summary = "Get all payments by user", description = "Retrieves all payments made by a specific user.")
    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<PaymentResponseDTO>> getAllPaymentsByUser(@Parameter(description = "Username of the user", in = ParameterIn.PATH)
                                                                           @PathVariable String username) {
        return ResponseEntity.ok(paymentService.getAllPaymentsByUser(username));
    }

    /**
     * Retrieves the amount of money paid by a specific user.
     *
     * @param username the username of the user whose payment amount is to be retrieved
     * @return a ResponseEntity containing the total amount paid by the user
     */
    @Operation(summary = "Get total amount paid by user", description = "Retrieves the total amount of money paid by a specific user.")
    @GetMapping("/user/{username}/total")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Double> getTotalAmountPaidByUser(@Parameter(description = "Username of the user", in = ParameterIn.PATH)
                                                            @PathVariable String username) {
        return ResponseEntity.ok(paymentService.getTotalAmountPaidByUser(username));
    }

    /**
     * Retrieves the total amount of money paid by all users.
     *
     * @return a ResponseEntity containing the total amount paid by all users
     */
    @Operation(summary = "Get total amount paid by all users", description = "Retrieves the total amount of money paid by all users.")
    @GetMapping("/total")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<BigDecimal> getTotalAmountPaidByAllUsers() {
        return ResponseEntity.ok(paymentService.getTotalAmountPaidByAllUsers());
    }

    /**
     * Retrieves the total amount of money paid by all users for a specific property.
     *
     * @param propertyId the ID of the property for which the total payment amount is to be retrieved
     * @return a ResponseEntity containing the total amount paid for the specified property
     */
    @Operation(summary = "Get total amount paid by all users for a property", description = "Retrieves the total amount of money paid by all users for a specific property.")
    @GetMapping("/property/{propertyId}/total")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<BigDecimal> getTotalAmountPaidByAllUsersForProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH)
                                                                                @PathVariable Long propertyId) {
        return ResponseEntity.ok(paymentService.getTotalAmountPaidByAllUsersForProperty(propertyId));
    }

    /**
     * Updates the payment status for a specific payment.
     *
     * @param paymentId the ID of the payment to update
     * @param paymentStatus the new status to set for the payment
     * @return a MessageResponseDTO indicating the result of the update operation
     */
    @Operation(summary = "Update payment status", description = "Updates the status of a specific payment.")
    @PatchMapping("/{paymentId}/{paymentStatus}/update-status")
    @PreAuthorize("hasRole('Admin')")
    public MessageResponseDTO updatePaymentStatus(@Parameter(description = "Payment ID", in = ParameterIn.PATH)
                                                  @PathVariable Long paymentId,
                                                  @Parameter(description = "New payment status", in = ParameterIn.PATH)
                                                  @Valid @PathVariable PaymentStatus paymentStatus) {
        return paymentService.updatePaymentStatus(paymentId, paymentStatus);
    }

    /**
     * Updates the payment method for a specific payment.
     *
     * @param paymentId the ID of the payment to update
     * @param paymentMethod the new payment method to set for the payment
     * @return a MessageResponseDTO indicating the result of the update operation
     */
    @Operation(summary = "Update payment method", description = "Updates the payment method for a specific payment.")
    @PatchMapping("/{paymentId}/{paymentMethod}/update-method")
    @PreAuthorize("hasRole('Admin')")
    public MessageResponseDTO updatePaymentMethod(@Parameter(description = "Payment ID", in = ParameterIn.PATH)
                                                  @PathVariable Long paymentId,
                                                  @Parameter(description = "New payment method", in = ParameterIn.PATH)
                                                  @Valid @PathVariable PaymentMethod paymentMethod) {
        return paymentService.updatePaymentMethod(paymentId, paymentMethod);
    }

    /**
     * Updates entire payment details for a specific payment.
     *
     * @param paymentId the ID of the payment to update
     * @param paymentRequestDTO the DTO containing updated payment details
     * @return a MessageResponseDTO indicating the result of the update operation
     */
    @Operation(summary = "Update payment details", description = "Updates the entire payment details for a specific payment.")
    @PutMapping("/{paymentId}/update")
    @PreAuthorize("isAuthenticated() and (hasRole('Admin') or @paymentService.isPaymentOwner(#paymentId, principal.username))")
    public MessageResponseDTO updatePayment(@Parameter(description = "Payment ID", in = ParameterIn.PATH)
                                            @PathVariable Long paymentId,
                                            @Parameter(description = "Payment Request DTO")
                                            @Valid @RequestBody PaymentRequestDTO paymentRequestDTO) {
        return paymentService.updatePayment(paymentId, paymentRequestDTO);
    }

}
