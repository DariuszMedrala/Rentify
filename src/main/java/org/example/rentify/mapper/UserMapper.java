package org.example.rentify.mapper;

import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.UserRequestDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * UserMapper interface for mapping between User entity and DTOs.
 * Uses MapStruct for automatic implementation generation.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {AddressMapper.class, RoleMapper.class})
public interface UserMapper {

    /**
     * Converts a UserRegistrationDTO to a User entity.
     *
     * @param userRegistrationDTO the UserRegistrationDTO to convert
     * @return the converted User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "enabled", constant = "true")
    @Mapping(target = "accountNonExpired", constant = "true")
    @Mapping(target = "accountNonLocked", constant = "true")
    @Mapping(target = "credentialsNonExpired", constant = "true")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "payments", ignore = true)
    User userRegistrationDtoToUser(UserRegistrationDTO userRegistrationDTO);

    /**
     * Converts a UserRequestDTO to a User entity.
     *
     * @param userRequestDTO the UserRequestDTO to convert
     * @return the converted User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "payments", ignore = true)
    User userRequestDtoToUser(UserRequestDTO userRequestDTO);

    /**
     * Updates an existing User entity with data from a UserRequestDTO.
     *
     * @param userRequestDTO the DTO containing update data
     * @param user the User entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "payments", ignore = true)
    void updateUserFromDto(UserRequestDTO userRequestDTO, @MappingTarget User user);

    /**
     * Converts a User entity to a UserResponseDTO.
     *
     * @param user the User entity to convert
     * @return the converted UserResponseDTO
     */
    UserResponseDTO userToUserResponseDto(User user);
}
