package org.example.rentify.mapper;

import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.Set;

/*
 * RoleMapper interface for mapping Role entities to RoleResponseDTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface RoleMapper {

    RoleMapper INSTANCE = Mappers.getMapper(RoleMapper.class);

    /**
     * Maps a Role entity to a RoleResponseDTO.
     *
     * @param role the Role entity to map
     * @return the mapped RoleResponseDTO
     */
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "description", source = "description")
    RoleResponseDTO toDto(Role role);

    /**
     * Maps a set of Role entities to a set of RoleResponseDTOs.
     *
     * @param roles the set of Role entities to map
     * @return the mapped set of RoleResponseDTOs
     */
    Set<RoleResponseDTO> toDtoSet(Set<Role> roles);
}
