package org.example.rentify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.validator.constraints.URL;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
 * ImageRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for an image in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class ImageRequestDTO {

    @NotNull(message = "Property ID cannot be null")
    private Long propertyId;

    @NotBlank(message = "Image URL cannot be blank")
    @URL(message = "Invalid image URL format")
    @Size(max = 2048, message = "Image URL too long")
    private String imageUrl;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;
}
