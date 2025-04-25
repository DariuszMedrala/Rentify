package org.example.rentify.mapper;

import org.example.rentify.dto.request.AddressRequestDTO;
import org.example.rentify.dto.response.AddressResponseDTO;
import org.example.rentify.entity.Address;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.factory.Mappers;

/*
 * AddressMapper is an interface that defines the mapping between Address entity and Address DTOs.
 * It uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface AddressMapper {

    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    /**
     * Converts an Address entity to an AddressResponseDTO.
     *
     * @param address the Address entity to convert
     * @return the converted AddressResponseDTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "streetAddress", source = "streetAddress")
    @Mapping(target = "city", source = "city")
    @Mapping(target = "stateOrProvince", source = "stateOrProvince")
    @Mapping(target = "country", source = "country")
    @Mapping(target = "postalCode", source = "postalCode")
    AddressResponseDTO toDto(Address address);

    /**
     * Converts an AddressRequestDTO to an Address entity.
     *
     * @param addressRequestDTO the AddressRequestDTO to convert
     * @return the converted Address entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    Address toEntity(AddressRequestDTO addressRequestDTO);


    /**
     * Updates an existing Address entity with values from an AddressRequestDTO.
     *
     * @param dto the AddressRequestDTO containing the new values
     * @param entity the existing Address entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(AddressRequestDTO dto, @MappingTarget Address entity);
}