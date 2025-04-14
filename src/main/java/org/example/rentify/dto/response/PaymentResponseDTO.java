package org.example.rentify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
 * PaymentResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a payment in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class PaymentResponseDTO {

    private Long id;
    private BookingResponseDTO booking;
    private LocalDateTime paymentDate;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String transactionId;
}
