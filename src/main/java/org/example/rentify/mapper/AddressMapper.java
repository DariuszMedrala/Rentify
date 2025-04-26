package org.example.rentify.mapper;

import org.example.rentify.dto.request.AddressRequestDTO;
import org.example.rentify.dto.response.AddressResponseDTO;
import org.example.rentify.entity.Address;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/*
 * AddressMapper is an interface that defines the mapping between Address entity and Address DTOs.
 * It uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    /**
     * Converts an AddressRequestDTO to an Address entity.
     *
     * @param addressRequestDTO the AddressRequestDTO to convert
     * @return the converted Address entity
     */
    @Mapping(target = "id", ignore = true)
    Address addressRequestDtoToAddress(AddressRequestDTO addressRequestDTO);

    /**
     * Converts an Address entity to an AddressResponseDTO.
     *
     * @param address the Address entity to convert
     * @return the converted AddressResponseDTO
     */
    AddressResponseDTO addressToAddressResponseDto(Address address);

    /**
     * Updates an existing Address entity with data from an AddressRequestDTO.
     *
     * @param addressRequestDTO the DTO containing update data
     * @param address the Address entity to update
     */
    @Mapping(target = "id", ignore = true)
    void updateAddressFromDto(AddressRequestDTO addressRequestDTO, @MappingTarget Address address);
}