package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for property response")
/*
 * PropertyResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a property in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class PropertyResponseDTO {
    @Schema(example = "1")
    private Long id;
    private UserResponseDTO owner;
    @Schema(example = "Beautiful Beach House")
    private String title;
    @Schema(example = "A beautiful beach house with stunning ocean views.")
    private String description;
    @Schema(example = "HOUSE")
    private String propertyType;
    @Schema(example = "100.0")
    private Double area;
    @Schema(example = "2")
    private Integer numberOfRooms;
    @Schema(example = "100.0")
    private BigDecimal pricePerDay;
    @Schema(example = "true")
    private Boolean availability;
    @Schema(example = "2023-10-01T12:00:00")
    private LocalDateTime creationDate;
    private AddressResponseDTO address;
    private List<ImageResponseDTO> images;
    private List<ReviewResponseDTO> reviews;
}
