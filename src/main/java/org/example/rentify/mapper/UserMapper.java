package org.example.rentify.mapper;

import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.UserRequestDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.util.List;

/*
* UserMapper interface for mapping between User entity and User DTOs.
* This interface uses MapStruct to generate the implementation code for mapping.
 */
@Mapper(componentModel = "spring",
        uses = {RoleMapper.class, AddressMapper.class, PropertyMapper.class, BookingMapper.class, ReviewMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Maps a User entity to a UserResponseDTO.
     *
     * @param user the User entity to map
     * @return the mapped UserResponseDTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "username", source = "username")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "phoneNumber", source = "phoneNumber")
    @Mapping(target = "registrationDate", source = "registrationDate")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "roles", source = "roles")
    UserResponseDTO toUserResponseDto(User user);

    /**
     * Maps List of User entities to List of UserResponseDTOs.
     *
     * @param users the List of User entities to map
     * @return UserResponseDTOs mapped from the List of User entities
     */
    List<UserResponseDTO> toUserResponseDtoList(List<User> users);

    /**
     * Maps a UserRegistrationDTO to a User entity.
     *
     * @param registrationDTO the UserRegistrationDTO to map
     * @return the mapped User entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "address", source = "address")
    @Mapping(target = "properties", source = "properties")
    @Mapping(target = "bookings", source = "bookings")
    @Mapping(target = "reviews", source = "reviews")
    @Mapping(target = "authorities", source = "authorities")
    User fromUserRegistrationDto(UserRegistrationDTO registrationDTO);

    /**
     * Updates a User entity from a UserRequestDTO.
     * @param dto the UserRequestDTO to map
     * @param entity the User entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "accountNonExpired", ignore = true)
    @Mapping(target = "accountNonLocked", ignore = true)
    @Mapping(target = "credentialsNonExpired", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "properties", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserRequestDTO dto, @MappingTarget User entity);
}