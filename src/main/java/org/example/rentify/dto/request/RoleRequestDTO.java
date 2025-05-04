package org.example.rentify.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * RoleRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for a role in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class RoleRequestDTO {

    @NotBlank(message = "Role name cannot be blank")
    @Size(min = 2, max = 50, message = "Role name cannot exceed 50 characters")
    private String name;

    @NotBlank(message = "Role description cannot be blank")
    @Size(max = 255, message = "Role description cannot exceed 255 characters")
    private String description;
}
