package org.example.rentify.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@Schema(description = "Data Transfer Object for JWT response")
/*
 * JwtResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a JWT (JSON Web Token) in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class JwtResponseDTO {
    @Schema(example = "Bearer <KEY>")
    private String token;
    @Schema(example = "1")
    private Long id;
    @Schema(example = "john_doe")
    private String username;
    @Schema(example = "example@gmail.com")
    private String email;
    private List<String> roles;
    @Schema(example = "Bearer")
    private String type = "Bearer";

    public JwtResponseDTO(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}

