package org.example.rentify.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rentify.entity.enums.PaymentMethod;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
* PaymentRequestDTO is a Data Transfer Object (DTO) that represents the request
* for a payment in the Rentify application. It is used to transfer data between
* the client and server.
*/
public class PaymentRequestDTO {

    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    @NotNull(message = "Amount cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotNull(message = "Payment method cannot be null")
    private PaymentMethod paymentMethod;

    @Size(max = 255, message = "Transaction ID must be less than 255 characters")
    private String transactionId;
}
