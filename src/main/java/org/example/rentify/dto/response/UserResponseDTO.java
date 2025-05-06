package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Data Transfer Object for user response")
/*
 * UserResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a user in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class UserResponseDTO {

    @Schema(example = "1")
    private Long id;
    @Schema(example = "john_doe")
    private String username;
    @Schema(example = "example@gmail.com")
    private String email;
    @Schema(example = "John")
    private String firstName;
    @Schema(example = "Doe")
    private String lastName;
    @Schema(example = "1234567890")
    private String phoneNumber;
    @Schema(example = "2024-10-01")
    private LocalDate registrationDate;
    private AddressResponseDTO address;
    private Set<RoleResponseDTO> roles;
}
