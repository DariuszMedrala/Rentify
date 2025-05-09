package org.example.rentify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for booking request")
/*
 * BookingRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for a booking in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class BookingRequestDTO {

    @NotNull(message = "Property ID cannot be null")
    @Schema(example = "1")
    private Long propertyId;

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be in the present or future")
    @Schema(example = "2025-10-01")
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @FutureOrPresent(message = "End date must be in the present or future")
    @Schema(example = "2025-10-10")
    private LocalDate endDate;
}
