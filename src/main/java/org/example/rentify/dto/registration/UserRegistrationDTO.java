package org.example.rentify.dto.registration;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.rentify.dto.request.AddressRequestDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Data Transfer Object for user registration")
/*
 * UserRegistrationDTO is a Data Transfer Object (DTO) that represents the request
 * for user registration in the Rentify application. It is used to transfer data between
 * the client and server.
 */
public class UserRegistrationDTO {

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Username can only contain letters, numbers, and underscores")
    @Schema(example = "johndoe")
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character")
    @Schema(example = "P@ssw0rd123")
    private String password;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must be less than 100 characters")
    @Schema(example = "example@gmail.com")
    private String email;

    @NotBlank(message = "First name cannot be blank")
    @Size(max = 50, message = "First name must be less than 50 characters")
    @Schema(example = "John")
    private String firstName;

    @NotBlank(message = "Last name cannot be blank")
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