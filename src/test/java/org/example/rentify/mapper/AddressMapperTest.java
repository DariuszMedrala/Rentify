package org.example.rentify.mapper;

import org.example.rentify.dto.request.AddressRequestDTO;
import org.example.rentify.dto.response.AddressResponseDTO;
import org.example.rentify.entity.Address;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AddressMapper Unit Tests")
class AddressMapperTest {

    private AddressMapper addressMapper;

    private AddressRequestDTO addressRequestDTO;
    private Address addressEntity;

    @BeforeEach
    void setUp() {
        addressMapper = Mappers.getMapper(AddressMapper.class);

        addressRequestDTO = new AddressRequestDTO();
        addressRequestDTO.setStreetAddress("123 Test St");
        addressRequestDTO.setCity("TestVille");
        addressRequestDTO.setStateOrProvince("TestLand");
        addressRequestDTO.setCountry("TestuAnia");
        addressRequestDTO.setPostalCode("T3S T01");

        addressEntity = new Address();
        addressEntity.setId(1L);
        addressEntity.setStreetAddress("456 Original Ave");
        addressEntity.setCity("OldTown");
        addressEntity.setStateOrProvince("Old Province");
        addressEntity.setCountry("OriginalLand");
        addressEntity.setPostalCode("OLD 123");
    }

    @Nested
    @DisplayName("addressRequestDtoToAddress Tests")
    class AddressRequestDtoToAddressTests {

        @Test
        @DisplayName("Should map AddressRequestDTO to Address entity correctly")
        void shouldMapDtoToEntity() {
            Address mappedAddress = addressMapper.addressRequestDtoToAddress(addressRequestDTO);

            assertNotNull(mappedAddress);
            assertEquals(addressRequestDTO.getStreetAddress(), mappedAddress.getStreetAddress());
            assertEquals(addressRequestDTO.getCity(), mappedAddress.getCity());
            assertEquals(addressRequestDTO.getStateOrProvince(), mappedAddress.getStateOrProvince());
            assertEquals(addressRequestDTO.getCountry(), mappedAddress.getCountry());
            assertEquals(addressRequestDTO.getPostalCode(), mappedAddress.getPostalCode());
            assertNull(mappedAddress.getId());
        }

        @Test
        @DisplayName("Should handle null AddressRequestDTO gracefully")
        void shouldHandleNullDto() {
            Address mappedAddress = addressMapper.addressRequestDtoToAddress(null);
            assertNull(mappedAddress, "Mapping a null DTO should result in a null entity");
        }

        @Test
        @DisplayName("Should map DTO with null fields to entity with null fields")
        void shouldMapDtoWithNullFields() {
            AddressRequestDTO dtoWithNulls = new AddressRequestDTO();

            Address mappedAddress = addressMapper.addressRequestDtoToAddress(dtoWithNulls);

            assertNotNull(mappedAddress);
            assertNull(mappedAddress.getStreetAddress());
            assertNull(mappedAddress.getCity());
            assertNull(mappedAddress.getStateOrProvince());
            assertNull(mappedAddress.getCountry());
            assertNull(mappedAddress.getPostalCode());
            assertNull(mappedAddress.getId());
        }
    }

    @Nested
    @DisplayName("addressToAddressResponseDto Tests")
    class AddressToAddressResponseDtoTests {

        @Test
        @DisplayName("Should map Address entity to AddressResponseDTO correctly")
        void shouldMapEntityToDto() {
            AddressResponseDTO mappedDto = addressMapper.addressToAddressResponseDto(addressEntity);

            assertNotNull(mappedDto);
            assertEquals(addressEntity.getId(), mappedDto.getId());
            assertEquals(addressEntity.getStreetAddress(), mappedDto.getStreetAddress());
            assertEquals(addressEntity.getCity(), mappedDto.getCity());
            assertEquals(addressEntity.getStateOrProvince(), mappedDto.getStateOrProvince());
            assertEquals(addressEntity.getCountry(), mappedDto.getCountry());
            assertEquals(addressEntity.getPostalCode(), mappedDto.getPostalCode());
        }

        @Test
        @DisplayName("Should handle null Address entity gracefully")
        void shouldHandleNullEntity() {
            AddressResponseDTO mappedDto = addressMapper.addressToAddressResponseDto(null);
            assertNull(mappedDto, "Mapping a null entity should result in a null DTO");
        }

        @Test
        @DisplayName("Should map entity with null fields to DTO with null fields")
        void shouldMapEntityWithNullFields() {
            Address entityWithNulls = new Address();
            entityWithNulls.setId(5L);

            AddressResponseDTO mappedDto = addressMapper.addressToAddressResponseDto(entityWithNulls);

            assertNotNull(mappedDto);
            assertEquals(5L, mappedDto.getId());
            assertNull(mappedDto.getStreetAddress());
            assertNull(mappedDto.getCity());
            assertNull(mappedDto.getStateOrProvince());
            assertNull(mappedDto.getCountry());
            assertNull(mappedDto.getPostalCode());
        }
    }

    @Nested
    @DisplayName("updateAddressFromDto Tests")
    class UpdateAddressFromDtoTests {

        @Test
        @DisplayName("Should update existing Address entity from DTO with non-null DTO fields")
        void shouldUpdateEntityFromDto_NonNullFields() {
            Address targetAddress = new Address();
            targetAddress.setId(10L);
            targetAddress.setStreetAddress("Old Street");
            targetAddress.setCity("Old City");
            targetAddress.setStateOrProvince("Old State");
            targetAddress.setCountry("Old Country");
            targetAddress.setPostalCode("OLD POST");

            addressMapper.updateAddressFromDto(addressRequestDTO, targetAddress);

            assertEquals(addressRequestDTO.getStreetAddress(), targetAddress.getStreetAddress());
            assertEquals(addressRequestDTO.getCity(), targetAddress.getCity());
            assertEquals(addressRequestDTO.getStateOrProvince(), targetAddress.getStateOrProvince());
            assertEquals(addressRequestDTO.getCountry(), targetAddress.getCountry());
            assertEquals(addressRequestDTO.getPostalCode(), targetAddress.getPostalCode());

            assertEquals(10L, targetAddress.getId());
        }

        @Test
        @DisplayName("Should ignore null fields from DTO during update due to NullValuePropertyMappingStrategy.IGNORE")
        void shouldIgnoreNullFieldsFromDtoDuringUpdate() {
            Address targetAddress = new Address();
            targetAddress.setId(11L);
            targetAddress.setStreetAddress("Original Street");
            targetAddress.setCity("Original City");
            targetAddress.setStateOrProvince("Original State");
            targetAddress.setCountry("Original Country");
            targetAddress.setPostalCode("ORIG ZIP");

            AddressRequestDTO updateDtoWithNulls = new AddressRequestDTO();
            updateDtoWithNulls.setStreetAddress(null);
            updateDtoWithNulls.setCity("New City");
            updateDtoWithNulls.setStateOrProvince(null);
            updateDtoWithNulls.setCountry("New Country");

            addressMapper.updateAddressFromDto(updateDtoWithNulls, targetAddress);

            assertEquals("Original Street", targetAddress.getStreetAddress(), "StreetAddress should not be updated due to null in DTO");
            assertEquals("New City", targetAddress.getCity(), "City should be updated");
            assertEquals("Original State", targetAddress.getStateOrProvince(), "StateOrProvince should not be updated");
            assertEquals("New Country", targetAddress.getCountry(), "Country should be updated");
            assertEquals("ORIG ZIP", targetAddress.getPostalCode(), "PostalCode should not be updated due to null in DTO");
            assertEquals(11L, targetAddress.getId());
        }

        @Test
        @DisplayName("Should not update ID field even if present in DTO (due to ignore=true)")
        void shouldNotUpdateIdField() {
            Address targetAddress = new Address();
            targetAddress.setId(12L);
            targetAddress.setCity("CityBeforeUpdate");

            AddressRequestDTO dto = new AddressRequestDTO();
            dto.setCity("CityAfterUpdate");

            addressMapper.updateAddressFromDto(dto, targetAddress);

            assertEquals(12L, targetAddress.getId(), "ID should remain unchanged");
            assertEquals("CityAfterUpdate", targetAddress.getCity());
        }
    }
}