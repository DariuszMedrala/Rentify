package org.example.rentify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for user creation")
/*
 * UserRequestDTO is a Data Transfer Object (DTO) that represents the request
 * for a user in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class UserRequestDTO {

    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(example = "johndoe")
    private String username;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Schema(example = "example@gmail.com")
    private String email;

    @Size(max = 50, message = "First name must be less than 50 characters")
    @Schema(example = "John")
    private String firstName;

    @Size(max = 50, message = "Last name must be less than 50 characters")
    @Schema(example = "Doe")
    private String lastName;

    @Size(max = 15, message = "Phone number must be less than 15 characters")
    @Pattern(regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Invalid phone number format")
    @Schema(example = "1234567890")
    private String phoneNumber;

    @Valid
    private AddressRequestDTO address;
}
