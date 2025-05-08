package org.example.rentify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.rentify.entity.enums.PropertyType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for property creation")
/*
 * PropertyRequest DTO is a Data Transfer Object (DTO) that represents the request
 * for a role in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class PropertyRequestDTO {

    @NotBlank(message = "Property title cannot be blank")
    @Size(max = 255, message = "Property title cannot exceed 255 characters")
    @Schema(example = "Beautiful Beach House")
    private String title;

    @Size(message = "Property description cannot exceed 1000 characters", max = 1000)
    @Schema(example = "A beautiful beach house with stunning ocean views.")
    private String description;

    @NotNull(message = "Property type cannot be null")
    @Schema(example = "HOUSE")
    private PropertyType propertyType;

    @NotNull(message = "Property area cannot be null")
    @DecimalMin(value = "1.0", message = "Property area must be greater than 0")
    @Schema(example = "100.0")
    private Double area;

    @Min(message = "Number of rooms cannot be negative", value = 0)
    @Schema(example = "2")
    private Integer numberOfRooms;

    @NotNull(message = "Price cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price per day must be greater than 0")
    @Schema(example = "100.0")
    private Double pricePerDay;

    private boolean availability = true;

    @NotNull(message = "Address cannot be null")
    @Valid
    private AddressRequestDTO address;
}
