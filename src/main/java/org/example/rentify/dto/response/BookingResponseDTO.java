package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rentify.entity.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for booking response")
/*
 * BookingResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a booking in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class BookingResponseDTO {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "2025-10-01")
    private LocalDate startDate;
    @Schema(example = "2025-10-10")
    private LocalDate endDate;
    @Schema(example = "1000.00")
    private BigDecimal totalPrice;
    @Schema(example = "2025-09-01T10:00:00")
    private LocalDateTime bookingDate;
    @Schema(example = "PENDING")
    private BookingStatus bookingStatus;
}
