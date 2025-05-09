package org.example.rentify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for image creation")
/*
 * ImageRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for an image in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class ImageRequestDTO {

    @NotBlank(message = "Image URL cannot be blank")
    @URL(message = "Invalid image URL format")
    @Size(max = 2048, message = "Image URL too long")
    @Schema(example = "https://example.com/image")
    private String imageUrl;

    @Size(max = 255, message = "Description must be less than 255 characters")
    @Schema(example = "Beautiful view of the property")
    private String description;
}
