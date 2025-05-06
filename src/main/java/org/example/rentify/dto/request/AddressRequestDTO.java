package org.example.rentify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for address creation")
/*
 * AddressRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for an address in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class AddressRequestDTO {

    @NotBlank(message = "Street address cannot be blank")
    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    @Schema(example = "123 Main St")
    private String streetAddress;

    @NotBlank(message = "City cannot be blank")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    @Schema(example = "Springfield")
    private String city;

    @NotBlank(message = "State or province cannot be blank")
    @Size(max = 100, message = "State or province cannot exceed 100 characters")
    @Schema(example = "Illinois")
    private String stateOrProvince;

    @NotBlank(message = "Country cannot be blank")
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Schema(example = "United States")
    private String country;

    @NotBlank(message = "Postal code cannot be blank")
    @Size(max = 20, message = "Postal code cannot exceed 20 characters")
    @Schema(example = "62-704")
    private String postalCode;
}
