package org.example.rentify.mapper;

import org.example.rentify.dto.request.RoleRequestDTO;
import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * RoleMapper interface for mapping between Role entity and DTOs.
 * Uses MapStruct for automatic implementation generation.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface RoleMapper {

    /**
     * Converts a RoleRequestDTO to a Role entity.
     *
     * @param roleRequestDTO the RoleRequestDTO to convert
     * @return the converted Role entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    Role roleRequestDtoToRole(RoleRequestDTO roleRequestDTO);

    /**
     * Converts a Role entity to a RoleResponseDTO.
     *
     * @param role the Role entity to convert
     * @return the converted RoleResponseDTO
     */
    RoleResponseDTO roleToRoleResponseDto(Role role);

    /**
     * Updates an existing Role entity with data from a RoleRequestDTO.
     *
     * @param roleRequestDTO the DTO containing update data
     * @param role the Role entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "users", ignore = true)
    void updateRoleFromDto(RoleRequestDTO roleRequestDTO, @MappingTarget Role role);
}