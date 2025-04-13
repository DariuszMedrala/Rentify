package org.example.rentify.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
/*
 * RoleResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a role in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class RoleResponseDTO {

    private Long id;
    private String name;
    private String description;
}
