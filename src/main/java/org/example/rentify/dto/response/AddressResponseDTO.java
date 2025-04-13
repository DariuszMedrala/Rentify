package org.example.rentify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

/*
 * AddressResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for an address in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class AddressResponseDTO {
    private Long id;
    private String streetAddress;
    private String city;
    private String stateOrProvince;
    private String country;
    private String postalCode;
}
