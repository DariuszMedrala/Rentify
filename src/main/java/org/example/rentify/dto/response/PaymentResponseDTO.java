package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Data Transfer Object for payment response")
/*
 * PaymentResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a payment in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class PaymentResponseDTO {
    @Schema(example = "1")
    private Long id;
    private BookingResponseDTO booking;
    @Schema(example = "2025-10-01T10:00:00")
    private LocalDateTime paymentDate;
    @Schema(example = "100.00")
    private BigDecimal amount;
    @Schema(example = "CREDIT_CARD")
    private PaymentMethod paymentMethod;
    @Schema(example = "COMPLETED")
    private PaymentStatus paymentStatus;
    @Schema(example = "txn_1234567890")
    private String transactionId;
}
