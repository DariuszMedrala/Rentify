package org.example.rentify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for role creation")
/*
 * RoleRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for a role in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class RoleRequestDTO {

    @NotBlank(message = "Role name cannot be blank")
    @Size(min = 2, max = 50, message = "Role name must be between 2 and 50 characters")
    @Schema(example = "ROLE")
    private String name;

    @NotBlank(message = "Role description cannot be blank")
    @Size(max = 255, message = "Role description cannot exceed 255 characters")
    @Schema(example = "Example of ROLE description")
    private String description;
}
