package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for image response")
/*
 * ImageResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for an image in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class ImageResponseDTO {

    @Schema(example = "1")
    private Long id;
    private PropertyResponseDTO property;
    @Schema(example = "https://example.com/image")
    private String imageUrl;
    @Schema(example = "Beautiful view of the property")
    private String description;
    @Schema(example = "2023-15-01T12:00:00")
    private LocalDateTime uploadDate;
}
