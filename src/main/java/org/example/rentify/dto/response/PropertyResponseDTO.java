package org.example.rentify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * PropertyResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a property in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class PropertyResponseDTO {

    private Long id;
    private UserResponseDTO owner;
    private String title;
    private String description;
    private String propertyType;
    private Double area;
    private Integer numberOfRooms;
    private BigDecimal pricePerDay;
    private Boolean availability;
    private LocalDateTime creationDate;
    private AddressResponseDTO address;
    private List<ImageResponseDTO> images;
    private List<ReviewResponseDTO> reviews;
}
