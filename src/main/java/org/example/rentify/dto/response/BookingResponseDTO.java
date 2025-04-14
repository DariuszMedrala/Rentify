package org.example.rentify.dto.response;

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
/*
 * BookingResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a booking in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class BookingResponseDTO {

    private Long id;
    private PropertyResponseDTO property;
    private UserResponseDTO user;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalPrice;
    private LocalDateTime bookingDate;
    private BookingStatus bookingStatus;
    private PaymentResponseDTO payment;
}
