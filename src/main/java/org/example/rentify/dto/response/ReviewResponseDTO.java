package org.example.rentify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * ReviewResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a review in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class ReviewResponseDTO {

    private Long id;
    private UserResponseDTO user;
    private PropertyResponseDTO property;
    private BookingResponseDTO booking;
    private int rating;
    private String comment;
    private LocalDateTime reviewDate;
}
