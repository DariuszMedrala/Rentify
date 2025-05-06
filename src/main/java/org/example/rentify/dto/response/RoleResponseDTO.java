package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for role response")
/*
 * RoleResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a role in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class RoleResponseDTO {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "ROLE_USER")
    private String name;
    @Schema(example = "User role with basic permissions")
    private String description;
}
