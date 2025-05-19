package org.example.rentify.mapper;

import org.example.rentify.dto.request.RoleRequestDTO;
import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RoleMapper Unit Tests")
class RoleMapperTest {

    private RoleMapper roleMapper;

    private RoleRequestDTO roleRequestDTO;
    private Role roleEntity;

    @BeforeEach
    void setUp() {
        roleMapper = Mappers.getMapper(RoleMapper.class);

        roleRequestDTO = new RoleRequestDTO();
        roleRequestDTO.setName("TEST_ADMIN");
        roleRequestDTO.setDescription("Test administrator role");

        roleEntity = new Role();
        roleEntity.setId(1L);
        roleEntity.setName("EXISTING_USER");
        roleEntity.setDescription("Existing standard user role");
    }

    @Nested
    @DisplayName("roleRequestDtoToRole Tests")
    class RoleRequestDtoToRoleTests {

        @Test
        @DisplayName("Should map RoleRequestDTO to Role entity correctly")
        void shouldMapDtoToEntity() {
            Role mappedRole = roleMapper.roleRequestDtoToRole(roleRequestDTO);

            assertNotNull(mappedRole);
            assertEquals(roleRequestDTO.getName(), mappedRole.getName());
            assertEquals(roleRequestDTO.getDescription(), mappedRole.getDescription());

            assertNull(mappedRole.getId(), "ID should be ignored and thus null");
            assertNull(mappedRole.getUsers(), "Users collection should be ignored and thus null");
        }

        @Test
        @DisplayName("Should handle null RoleRequestDTO gracefully")
        void shouldHandleNullDto() {
            Role mappedRole = roleMapper.roleRequestDtoToRole(null);
            assertNull(mappedRole, "Mapping a null DTO should result in a null entity");
        }

        @Test
        @DisplayName("Should map DTO with null fields to entity with null fields")
        void shouldMapDtoWithNullFields() {
            RoleRequestDTO dtoWithNulls = new RoleRequestDTO();
            Role mappedRole = roleMapper.roleRequestDtoToRole(dtoWithNulls);

            assertNotNull(mappedRole);
            assertNull(mappedRole.getName());
            assertNull(mappedRole.getDescription());
            assertNull(mappedRole.getId());
            assertNull(mappedRole.getUsers());
        }
    }

    @Nested
    @DisplayName("roleToRoleResponseDto Tests")
    class RoleToRoleResponseDtoTests {

        @Test
        @DisplayName("Should map Role entity to RoleResponseDTO correctly")
        void shouldMapEntityToDto() {
            RoleResponseDTO mappedDto = roleMapper.roleToRoleResponseDto(roleEntity);

            assertNotNull(mappedDto);
            assertEquals(roleEntity.getId(), mappedDto.getId());
            assertEquals(roleEntity.getName(), mappedDto.getName());
            assertEquals(roleEntity.getDescription(), mappedDto.getDescription());
        }

        @Test
        @DisplayName("Should handle null Role entity gracefully")
        void shouldHandleNullEntity() {
            RoleResponseDTO mappedDto = roleMapper.roleToRoleResponseDto(null);
            assertNull(mappedDto, "Mapping a null entity should result in a null DTO");
        }

        @Test
        @DisplayName("Should map entity with null description to DTO with null description")
        void shouldMapEntityWithNullDescription() {
            roleEntity.setDescription(null);
            RoleResponseDTO mappedDto = roleMapper.roleToRoleResponseDto(roleEntity);

            assertNotNull(mappedDto);
            assertEquals(roleEntity.getId(), mappedDto.getId());
            assertEquals(roleEntity.getName(), mappedDto.getName());
            assertNull(mappedDto.getDescription());
        }
    }

    @Nested
    @DisplayName("updateRoleFromDto Tests")
    class UpdateRoleFromDtoTests {

        @Test
        @DisplayName("Should update existing Role entity from DTO with non-null DTO fields")
        void shouldUpdateEntityFromDto_NonNullFields() {
            Role targetRole = new Role();
            targetRole.setId(5L);
            targetRole.setName("OLD_NAME");
            targetRole.setDescription("Old Description");

            roleMapper.updateRoleFromDto(roleRequestDTO, targetRole);

            assertEquals(roleRequestDTO.getName(), targetRole.getName());
            assertEquals(roleRequestDTO.getDescription(), targetRole.getDescription());
            assertEquals(5L, targetRole.getId(), "ID should not be changed");
            assertNull(targetRole.getUsers(), "Users collection should remain ignored/null");
        }

        @Test
        @DisplayName("Should ignore null fields from DTO during update due to NullValuePropertyMappingStrategy.IGNORE")
        void shouldIgnoreNullFieldsFromDtoDuringUpdate() {
            Role targetRole = new Role();
            targetRole.setId(6L);
            targetRole.setName("ORIGINAL_NAME");
            targetRole.setDescription("ORIGINAL_DESCRIPTION");

            RoleRequestDTO updateDtoWithNulls = new RoleRequestDTO();
            updateDtoWithNulls.setName(null);
            updateDtoWithNulls.setDescription("New Description Only");

            roleMapper.updateRoleFromDto(updateDtoWithNulls, targetRole);

            assertEquals("ORIGINAL_NAME", targetRole.getName(), "Name should not be updated for null DTO field");
            assertEquals("New Description Only", targetRole.getDescription(), "Description should be updated");
            assertEquals(6L, targetRole.getId());
        }

        @Test
        @DisplayName("Should not update ignored fields like id and users")
        void shouldNotUpdateIgnoredFields() {
            Role targetRole = new Role();
            targetRole.setId(7L);
            targetRole.setName("InitialName");
            targetRole.setUsers(Collections.emptySet());


            RoleRequestDTO updateDto = new RoleRequestDTO();
            updateDto.setName("UpdatedNameOnly");
            updateDto.setDescription("UpdatedDescription");


            roleMapper.updateRoleFromDto(updateDto, targetRole);

            assertEquals("UpdatedNameOnly", targetRole.getName());
            assertEquals("UpdatedDescription", targetRole.getDescription());
            assertEquals(7L, targetRole.getId(), "ID should remain unchanged");
            assertNotNull(targetRole.getUsers(), "Users collection should remain unchanged (not set to null by mapper if it was not null)");
            assertTrue(targetRole.getUsers().isEmpty(), "Users collection should remain as it was (empty set)");
        }
    }
}