package org.example.rentify.mapper;

import org.example.rentify.dto.request.AddressRequestDTO;
import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.AddressResponseDTO;
import org.example.rentify.dto.response.PropertyResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.Address;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        PropertyMapperImpl.class,
        AddressMapperImpl.class,
        UserMapperImpl.class,
        ReviewMapperImpl.class,
        ImageMapperImpl.class,
        BookingMapperImpl.class,
        PaymentMapperImpl.class,
        RoleMapperImpl.class
})
@DisplayName("PropertyMapper Integration Tests (Spring Context)")
class PropertyMapperTest {

    @Autowired
    private PropertyMapper propertyMapper;

    private PropertyRequestDTO propertyRequestDTO;
    private AddressRequestDTO addressRequestDTO;
    private Property propertyEntity;
    private UserResponseDTO expectedOwnerResponseDTO;
    private AddressResponseDTO expectedAddressResponseDTO;

    @BeforeEach
    void setUp() {
        User ownerEntity = new User();
        ownerEntity.setId(1L);
        ownerEntity.setUsername("testOwner");
        expectedOwnerResponseDTO = new UserResponseDTO();
        expectedOwnerResponseDTO.setId(ownerEntity.getId());
        expectedOwnerResponseDTO.setUsername(ownerEntity.getUsername());

        Address addressEntity = new Address();
        addressEntity.setId(1L);
        addressEntity.setStreetAddress("456 Entity Ave");
        addressEntity.setCity("EntityCity");
        addressEntity.setStateOrProvince("EntityState");
        addressEntity.setCountry("EntityCountry");
        addressEntity.setPostalCode("E1N 2T3");

        expectedAddressResponseDTO = new AddressResponseDTO();
        expectedAddressResponseDTO.setId(addressEntity.getId());
        expectedAddressResponseDTO.setStreetAddress(addressEntity.getStreetAddress());
        expectedAddressResponseDTO.setCity(addressEntity.getCity());
        expectedAddressResponseDTO.setStateOrProvince(addressEntity.getStateOrProvince());
        expectedAddressResponseDTO.setCountry(addressEntity.getCountry());
        expectedAddressResponseDTO.setPostalCode(addressEntity.getPostalCode());


        addressRequestDTO = new AddressRequestDTO();
        addressRequestDTO.setStreetAddress("123 DTO St");
        addressRequestDTO.setCity("DTOTown");
        addressRequestDTO.setStateOrProvince("DTOState");
        addressRequestDTO.setCountry("DTOCountry");
        addressRequestDTO.setPostalCode("D1T 0P0");

        propertyRequestDTO = new PropertyRequestDTO();
        propertyRequestDTO.setTitle("Beach House");
        propertyRequestDTO.setDescription("A beautiful house by the beach.");
        propertyRequestDTO.setPropertyType(PropertyType.HOUSE);
        propertyRequestDTO.setArea(120.50);
        propertyRequestDTO.setNumberOfRooms(4);
        propertyRequestDTO.setPricePerDay(100.0);
        propertyRequestDTO.setAvailability(true);
        propertyRequestDTO.setAddress(addressRequestDTO);


        propertyEntity = new Property();
        propertyEntity.setId(10L);
        propertyEntity.setTitle("Old Castle");
        propertyEntity.setDescription("An old castle on a hill.");
        propertyEntity.setPropertyType(PropertyType.APARTMENT);
        propertyEntity.setArea(500.00);
        propertyEntity.setNumberOfRooms(15);
        propertyEntity.setPricePerDay(new BigDecimal("1000.00"));
        propertyEntity.setAvailability(false);
        propertyEntity.setOwner(ownerEntity);
        propertyEntity.setAddress(addressEntity);
        propertyEntity.setCreationDate(LocalDateTime.now().minusDays(5));
        propertyEntity.setImages(new ArrayList<>());
        propertyEntity.setReviews(new ArrayList<>());
        propertyEntity.setBookings(new ArrayList<>());
    }

    @Nested
    @DisplayName("propertyRequestDtoToProperty Tests")
    class PropertyRequestDtoToPropertyTests {

        @Test
        @DisplayName("Should map PropertyRequestDTO to Property entity correctly")
        void shouldMapDtoToEntityIncludingAddress() {
            Property mappedProperty = propertyMapper.propertyRequestDtoToProperty(propertyRequestDTO);

            assertNotNull(mappedProperty);
            assertEquals(propertyRequestDTO.getTitle(), mappedProperty.getTitle());
            assertEquals(propertyRequestDTO.getDescription(), mappedProperty.getDescription());
            assertEquals(propertyRequestDTO.getPropertyType(), mappedProperty.getPropertyType());
            assertEquals(propertyRequestDTO.getArea(), mappedProperty.getArea());
            assertEquals(propertyRequestDTO.getNumberOfRooms(), mappedProperty.getNumberOfRooms());
            assertEquals(BigDecimal.valueOf(propertyRequestDTO.getPricePerDay()), mappedProperty.getPricePerDay());
            assertEquals(propertyRequestDTO.isAvailability(), mappedProperty.getAvailability());

            assertNull(mappedProperty.getId(), "ID should be ignored");
            assertNull(mappedProperty.getOwner(), "Owner should be ignored");
            assertNotNull(mappedProperty.getCreationDate(), "CreationDate should be initialized by the entity's default value");
            assertTrue(mappedProperty.getCreationDate().isBefore(LocalDateTime.now().plusSeconds(1)) &&
                            mappedProperty.getCreationDate().isAfter(LocalDateTime.now().minusSeconds(5)),
                    "CreationDate should be close to the current time");


            assertNull(mappedProperty.getReviews(), "Reviews should be ignored");
            assertNull(mappedProperty.getImages(), "Images should be ignored");
            assertNull(mappedProperty.getBookings(), "Bookings should be ignored");

            assertNotNull(mappedProperty.getAddress(), "Nested Address entity should be mapped");
            assertEquals(addressRequestDTO.getStreetAddress(), mappedProperty.getAddress().getStreetAddress());
            assertEquals(addressRequestDTO.getCity(), mappedProperty.getAddress().getCity());
            assertEquals(addressRequestDTO.getStateOrProvince(), mappedProperty.getAddress().getStateOrProvince());
            assertEquals(addressRequestDTO.getCountry(), mappedProperty.getAddress().getCountry());
            assertEquals(addressRequestDTO.getPostalCode(), mappedProperty.getAddress().getPostalCode());
            assertNull(mappedProperty.getAddress().getId(), "Address ID should be ignored by AddressMapper when creating from DTO");
        }

        @Test
        @DisplayName("Should handle null PropertyRequestDTO gracefully")
        void shouldHandleNullDto() {
            Property mappedProperty = propertyMapper.propertyRequestDtoToProperty(null);
            assertNull(mappedProperty);
        }

        @Test
        @DisplayName("Should map DTO with null AddressRequestDTO to Property with null Address")
        void shouldMapDtoWithNullNestedAddressDto() {
            propertyRequestDTO.setAddress(null);
            Property mappedProperty = propertyMapper.propertyRequestDtoToProperty(propertyRequestDTO);

            assertNotNull(mappedProperty);
            assertNull(mappedProperty.getAddress());
        }
    }

    @Nested
    @DisplayName("propertyToPropertyResponseDto Tests")
    class PropertyToPropertyResponseDtoTests {

        @Test
        @DisplayName("Should map Property entity to PropertyResponseDTO correctly, including nested DTOs")
        void shouldMapEntityToDtoIncludingNested() {
            PropertyResponseDTO mappedDto = propertyMapper.propertyToPropertyResponseDto(propertyEntity);

            assertNotNull(mappedDto);
            assertEquals(propertyEntity.getId(), mappedDto.getId());
            assertEquals(propertyEntity.getTitle(), mappedDto.getTitle());
            assertEquals(propertyEntity.getDescription(), mappedDto.getDescription());
            assertEquals(propertyEntity.getPropertyType().name(), mappedDto.getPropertyType());
            assertEquals(propertyEntity.getArea(), mappedDto.getArea());
            assertEquals(propertyEntity.getNumberOfRooms(), mappedDto.getNumberOfRooms());
            assertEquals(propertyEntity.getPricePerDay(), mappedDto.getPricePerDay());
            assertEquals(propertyEntity.getAvailability(), mappedDto.getAvailability());
            assertEquals(propertyEntity.getCreationDate(), mappedDto.getCreationDate());

            assertNotNull(mappedDto.getOwner());
            assertEquals(expectedOwnerResponseDTO.getUsername(), mappedDto.getOwner().getUsername());
            assertEquals(expectedOwnerResponseDTO.getId(), mappedDto.getOwner().getId());


            assertNotNull(mappedDto.getAddress());
            assertEquals(expectedAddressResponseDTO.getStreetAddress(), mappedDto.getAddress().getStreetAddress());
            assertEquals(expectedAddressResponseDTO.getCity(), mappedDto.getAddress().getCity());


            assertNotNull(mappedDto.getImages());
            assertTrue(mappedDto.getImages().isEmpty());

            assertNotNull(mappedDto.getReviews());
            assertTrue(mappedDto.getReviews().isEmpty());
        }

        @Test
        @DisplayName("Should handle null Property entity gracefully")
        void shouldHandleNullEntity() {
            PropertyResponseDTO mappedDto = propertyMapper.propertyToPropertyResponseDto(null);
            assertNull(mappedDto);
        }
    }

    @Nested
    @DisplayName("updatePropertyFromDto Tests")
    class UpdatePropertyFromDtoTests {

        @Test
        @DisplayName("Should update existing Property entity from DTO, including nested Address")
        void shouldUpdateEntityFromDtoIncludingAddress() {
            Property targetProperty = new Property();
            targetProperty.setId(20L);
            User originalOwner = new User(); originalOwner.setId(2L);
            targetProperty.setOwner(originalOwner);
            LocalDateTime originalDate = LocalDateTime.now().minusMonths(1);
            targetProperty.setCreationDate(originalDate);
            Address targetAddress = new Address();
            targetAddress.setId(5L);
            targetAddress.setStreetAddress("Old St");
            targetAddress.setCity("Old City");
            targetAddress.setStateOrProvince("Old State");
            targetAddress.setCountry("Old Country");
            targetAddress.setPostalCode("OLD ZIP");
            targetProperty.setAddress(targetAddress);
            targetProperty.setTitle("Initial Title");
            targetProperty.setPricePerDay(new BigDecimal("50.00"));
            propertyMapper.updatePropertyFromDto(propertyRequestDTO, targetProperty);

            assertEquals(propertyRequestDTO.getTitle(), targetProperty.getTitle());
            assertEquals(propertyRequestDTO.getDescription(), targetProperty.getDescription());
            assertEquals(propertyRequestDTO.getPropertyType(), targetProperty.getPropertyType());
            assertEquals(propertyRequestDTO.getArea(), targetProperty.getArea());
            assertEquals(propertyRequestDTO.getNumberOfRooms(), targetProperty.getNumberOfRooms());
            assertEquals(BigDecimal.valueOf(propertyRequestDTO.getPricePerDay()), targetProperty.getPricePerDay());
            assertEquals(propertyRequestDTO.isAvailability(), targetProperty.getAvailability());


            assertNotNull(targetProperty.getAddress());
            assertEquals(addressRequestDTO.getStreetAddress(), targetProperty.getAddress().getStreetAddress());
            assertEquals(addressRequestDTO.getCity(), targetProperty.getAddress().getCity());
            assertEquals(addressRequestDTO.getStateOrProvince(), targetProperty.getAddress().getStateOrProvince());
            assertEquals(addressRequestDTO.getCountry(), targetProperty.getAddress().getCountry());
            assertEquals(addressRequestDTO.getPostalCode(), targetProperty.getAddress().getPostalCode());
            assertEquals(5L, targetProperty.getAddress().getId());


            assertEquals(20L, targetProperty.getId());
            assertEquals(originalOwner, targetProperty.getOwner());
            assertEquals(originalDate, targetProperty.getCreationDate());
        }

        @Test
        @DisplayName("Should ignore null AddressRequestDTO in PropertyRequestDTO during update")
        void shouldIgnoreNullNestedAddressDtoDuringUpdate() {
            Property targetProperty = new Property();
            targetProperty.setTitle("Original Title");
            Address originalAddress = new Address();
            originalAddress.setStreetAddress("Keep This Street");
            originalAddress.setCity("Keep This City");
            targetProperty.setAddress(originalAddress);

            PropertyRequestDTO dtoWithNullAddress = getPropertyRequestDTO();
            propertyMapper.updatePropertyFromDto(dtoWithNullAddress, targetProperty);

            assertEquals("New Title from DTO", targetProperty.getTitle());
            assertEquals("New Desc", targetProperty.getDescription());
            assertNotNull(targetProperty.getAddress(), "Address should NOT become null due to NullValuePropertyMappingStrategy.IGNORE");
            assertEquals("Keep This Street", targetProperty.getAddress().getStreetAddress(), "Address details should remain unchanged");
            assertEquals("Keep This City", targetProperty.getAddress().getCity());
        }

        private static PropertyRequestDTO getPropertyRequestDTO() {
            PropertyRequestDTO dtoWithNullAddress = new PropertyRequestDTO();
            dtoWithNullAddress.setTitle("New Title from DTO");
            dtoWithNullAddress.setAddress(null);
            dtoWithNullAddress.setDescription("New Desc");
            dtoWithNullAddress.setPropertyType(PropertyType.APARTMENT);
            dtoWithNullAddress.setArea(50.0);
            dtoWithNullAddress.setNumberOfRooms(2);
            dtoWithNullAddress.setPricePerDay(75.0);
            dtoWithNullAddress.setAvailability(false);
            return dtoWithNullAddress;
        }

        @Test
        @DisplayName("Should update fields correctly when some DTO fields are null (for non-nested fields)")
        void shouldUpdateNonNullFieldsAndIgnoreNullFieldsInDto() {
            Property targetProperty = getTargetProperty();

            PropertyRequestDTO updateDto = new PropertyRequestDTO();
            updateDto.setTitle("Updated Title");
            updateDto.setDescription(null);
            updateDto.setPropertyType(null);
            updateDto.setArea(null);
            updateDto.setNumberOfRooms(null);
            updateDto.setPricePerDay(120.50);
            updateDto.setAvailability(false);
            updateDto.setAddress(null);


            propertyMapper.updatePropertyFromDto(updateDto, targetProperty);

            assertEquals("Updated Title", targetProperty.getTitle());
            assertEquals("Original Description", targetProperty.getDescription());
            assertEquals(PropertyType.APARTMENT, targetProperty.getPropertyType());
            assertEquals(100.0, targetProperty.getArea());
            assertEquals(3, targetProperty.getNumberOfRooms());
            assertEquals(BigDecimal.valueOf(120.50), targetProperty.getPricePerDay());
            assertFalse(targetProperty.getAvailability());
            assertNotNull(targetProperty.getAddress());
            assertEquals("Unchanged St", targetProperty.getAddress().getStreetAddress());
        }

        private static Property getTargetProperty() {
            Property targetProperty = new Property();
            targetProperty.setTitle("Original Title");
            targetProperty.setDescription("Original Description");
            targetProperty.setPricePerDay(new BigDecimal("100.00"));
            targetProperty.setPropertyType(PropertyType.APARTMENT);
            targetProperty.setArea(100.0);
            targetProperty.setNumberOfRooms(3);
            targetProperty.setAvailability(true);
            Address addrEntity = new Address(1L, "Unchanged St", "Unchanged City", "34231", "Unchanged Country", "Unchanged State");
            targetProperty.setAddress(addrEntity);
            return targetProperty;
        }
    }
}