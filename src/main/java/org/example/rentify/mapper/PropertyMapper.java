package org.example.rentify.mapper;

import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.PropertyResponseDTO;
import org.example.rentify.entity.Property;
import org.mapstruct.*;

import java.util.List;

/*
* PropertyMapper interface for mapping between a Property entity and PropertyRequestDTO/PropertyResponseDTO.
* This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring",
        uses = {UserMapper.class, AddressMapper.class, ImageMapper.class, ReviewMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PropertyMapper {

    /**
     * Maps a Property entity to a PropertyResponseDTO.
     *
     * @param property the Property entity to map
     * @return the mapped PropertyResponseDTO
     */
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "address", source = "address")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "reviews", source = "reviews")
    PropertyResponseDTO toDto(Property property);

    /**
     * Maps a list of Property entities to a list of PropertyResponseDTOs.
     *
     * @param properties the list of Property entities to map
     * @return the list of mapped PropertyResponseDTOs
     */
    List<PropertyResponseDTO> toDtoList(List<Property> properties);

    /**
     * Maps a PropertyRequestDTO to a Property entity.
     *
     * @param propertyRequestDTO the PropertyRequestDTO to map
     * @return the mapped Property entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "address", source = "address")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Property toEntity(PropertyRequestDTO propertyRequestDTO);

    /**
     *  Updates an existing Property entity with values from a PropertyRequestDTO.
     *
     * @param dto  the PropertyRequestDTO containing the new values
     * @param entity the existing Property entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(PropertyRequestDTO dto, @MappingTarget Property entity);
}