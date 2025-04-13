package org.example.rentify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
 * ImageResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for an image in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class ImageResponseDTO {

    private Long id;
    private Long propertyId;
    private String imageUrl;
    private String description;
    private LocalDateTime uploadDate;
}
