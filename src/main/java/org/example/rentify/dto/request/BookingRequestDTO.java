package org.example.rentify.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * BookingRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for a booking in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class BookingRequestDTO {

    @NotNull(message = "Property ID cannot be null")
    private Long propertyId;

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be in the present or future")
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @FutureOrPresent(message = "End date must be in the present or future")
    private LocalDate endDate;
}
