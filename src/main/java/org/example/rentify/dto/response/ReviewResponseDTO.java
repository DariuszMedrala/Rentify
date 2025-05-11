package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for review response")
/*
 * ReviewResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a review in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class ReviewResponseDTO {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "2")
    private Long propertyId;
    @Schema(example = "1")
    private Long bookingId;
    @Schema(example = "5")
    private int rating;
    @Schema(example = "This is a great property!")
    private String comment;
    @Schema(example = "2025-10-01T12:00:00")
    private LocalDateTime reviewDate;
}
