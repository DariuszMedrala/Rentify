package org.example.rentify.mapper;

import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.PropertyResponseDTO;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.mapstruct.*;

/**
 * PropertyMapper interface for mapping between Property entity and DTOs.
 * Uses MapStruct for automatic implementation generation.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {AddressMapper.class, UserMapper.class, ReviewMapper.class, ImageMapper.class, BookingMapper.class})
public interface PropertyMapper {

    /**
     * Converts a PropertyRequestDTO to a Property entity.
     *
     * @param propertyRequestDTO the PropertyRequestDTO to convert
     * @return the converted Property entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "availability", source = "availability")
    @Mapping(target = "bookings", ignore = true)
    Property propertyRequestDtoToProperty(PropertyRequestDTO propertyRequestDTO);

    /**
     * Converts a Property entity to a PropertyResponseDTO.
     *
     * @param property the Property entity to convert
     * @return the converted PropertyResponseDTO
     */
    PropertyResponseDTO propertyToPropertyResponseDto(Property property);

    /**
     * Updates an existing Property entity with data from a PropertyRequestDTO.
     *
     * @param propertyRequestDTO the DTO containing update data
     * @param property the Property entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    void updatePropertyFromDto(PropertyRequestDTO propertyRequestDTO, @MappingTarget Property property);
}