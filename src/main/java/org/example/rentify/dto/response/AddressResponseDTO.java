package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for address response")
/*
 * AddressResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for an address in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class AddressResponseDTO {
    @Schema(example = "1")
    private Long id;
    @Schema(example = "123 Main St")
    private String streetAddress;
    @Schema(example = "Springfield")
    private String city;
    @Schema(example = "Illinois")
    private String stateOrProvince;
    @Schema(example = "United States")
    private String country;
    @Schema(example = "62-704")
    private String postalCode;
}
