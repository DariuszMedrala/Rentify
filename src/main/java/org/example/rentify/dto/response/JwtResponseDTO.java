package org.example.rentify.dto.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
/*
 * JwtResponseDTO is a Data Transfer Object (DTO) that represents the response
 * for a JWT (JSON Web Token) in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class JwtResponseDTO {

    private String token;
    private Long id;
    private String username;
    private String email;
    private List<String> roles;
    private String type = "Bearer";

    public JwtResponseDTO(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
}

