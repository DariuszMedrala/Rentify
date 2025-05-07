package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for simple message response")
/*
 * MessageResponseDTO is a Data Transfer Object (DTO) that represents a simple message response
 * in the Rentify application. It is used to transfer data between the client and server.
 */
public class MessageResponseDTO {

    @Schema(example = "Action completed successfully")
    private String message;
}