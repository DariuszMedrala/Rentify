package org.example.rentify.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
/*
 * LoginRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for a login in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class LoginRequestDTO {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    private String password;
}
