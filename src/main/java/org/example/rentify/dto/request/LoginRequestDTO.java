package org.example.rentify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for user login")
/*
 * LoginRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for a login in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class LoginRequestDTO {

    @NotBlank(message = "Username cannot be blank")
    @Schema(example = "john_doe")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Schema(example = "passWord123@")
    private String password;
}
