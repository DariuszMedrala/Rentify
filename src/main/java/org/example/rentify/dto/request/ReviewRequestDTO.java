package org.example.rentify.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * ReviewRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for a review in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class ReviewRequestDTO {

    @NotNull(message = "User ID cannot be null")
    private Long userId;

    @NotNull(message = "Property ID cannot be null")
    private Long propertyId;

    @NotNull(message = "Booking ID cannot be null")
    private Long bookingId;

    @NotNull(message = "Rating cannot be null")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @Size(max = 2000, message = "Comment must be less than 2000 characters")
    private String comment;
}
