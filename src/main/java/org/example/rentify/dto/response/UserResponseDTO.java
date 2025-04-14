package org.example.rentify.dto.response;

import java.time.LocalDate;
import java.util.Set;

/**
 * UserResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a user in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private LocalDate registrationDate;
    private AddressResponseDTO address;
    private Set<RoleResponseDTO> roles;
}
