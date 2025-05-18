package org.example.rentify.service;

import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.PropertyResponseDTO;
import org.example.rentify.entity.Address;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.PropertyType;
import org.example.rentify.mapper.PropertyMapper;
import org.example.rentify.repository.PropertyRepository;
import org.example.rentify.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PropertyService Unit Tests")
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private PropertyMapper propertyMapper;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PropertyService propertyService;

    private User user;
    private Property property;
    private PropertyRequestDTO propertyRequestDTO;
    private PropertyResponseDTO propertyResponseDTO;
    private Pageable pageable;
    private final String testUsername = "testOwner";
    private final Long propertyId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername(testUsername);

        Address address = new Address();
        address.setCountry("Poland");
        address.setCity("Krakow");

        property = new Property();
        property.setId(propertyId);
        property.setOwner(user);
        property.setTitle("Test Property");
        property.setPricePerDay(new BigDecimal("150.00"));
        property.setAvailability(true);
        property.setCreationDate(LocalDateTime.now());
        property.setPropertyType(PropertyType.APARTMENT);
        property.setAddress(address);
        property.setDescription("A nice property for testing.");


        propertyRequestDTO = new PropertyRequestDTO();
        propertyRequestDTO.setTitle("New Test Property");
        propertyRequestDTO.setDescription("Updated description");
        propertyRequestDTO.setAvailability(false);

        propertyResponseDTO = new PropertyResponseDTO();
        propertyResponseDTO.setId(propertyId);
        propertyResponseDTO.setTitle("Test Property");
        propertyResponseDTO.setPricePerDay(new BigDecimal("150.00"));
        propertyResponseDTO.setAvailability(true);

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("findAllProperties Tests")
    class FindAllPropertiesTests {
        @Test
        @DisplayName("Should return page of properties when properties exist")
        void findAllProperties_whenPropertiesExist_shouldReturnPageOfPropertyResponseDTO() {
            Page<Property> propertiesPage = new PageImpl<>(List.of(property), pageable, 1);
            when(propertyRepository.findAll(pageable)).thenReturn(propertiesPage);
            when(propertyMapper.propertyToPropertyResponseDto(property)).thenReturn(propertyResponseDTO);

            Page<PropertyResponseDTO> result = propertyService.findAllProperties(pageable);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.getTotalElements());
            assertEquals(propertyResponseDTO, result.getContent().getFirst());
            verify(propertyRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no properties found")
        void findAllProperties_whenNoPropertiesFound_shouldThrowResponseStatusException() {
            Page<Property> emptyPage = Page.empty(pageable);
            when(propertyRepository.findAll(pageable)).thenReturn(emptyPage);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> propertyService.findAllProperties(pageable));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No properties found with the specified criteria.", exception.getReason());
        }
    }

    @Nested
    @DisplayName("findAllPropertiesByAvailability Tests")
    class FindAllPropertiesByAvailabilityTests {
        @Test
        @DisplayName("Should return page of properties for given availability")
        void findAllPropertiesByAvailability_whenPropertiesExist_shouldReturnPage() {
            Boolean availability = true;
            Page<Property> propertiesPage = new PageImpl<>(List.of(property), pageable, 1);
            when(propertyRepository.findByAvailability(availability, pageable)).thenReturn(propertiesPage);
            when(propertyMapper.propertyToPropertyResponseDto(property)).thenReturn(propertyResponseDTO);

            Page<PropertyResponseDTO> result = propertyService.findAllPropertiesByAvailability(availability, pageable);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when availability is null AFTER repo call")
        void findAllPropertiesByAvailability_whenAvailabilityIsNull_shouldThrowIllegalArgumentException() {
            Page<Property> propertiesPage = new PageImpl<>(List.of(property), pageable, 1);
            when(propertyRepository.findByAvailability(null, pageable)).thenReturn(propertiesPage);


            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> propertyService.findAllPropertiesByAvailability(null, pageable));
            assertEquals("Availability cannot be null.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no properties found for availability")
        void findAllPropertiesByAvailability_whenNoPropertiesFound_shouldThrowResponseStatusException() {
            Boolean availability = true;
            Page<Property> emptyPage = Page.empty(pageable);
            when(propertyRepository.findByAvailability(availability, pageable)).thenReturn(emptyPage);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> propertyService.findAllPropertiesByAvailability(availability, pageable));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("findAllPropertiesByAddressCountryAndCityAndAvailability Tests")
    class FindAllPropertiesByAddressCountryAndCityAndAvailabilityTests {
        @Test
        @DisplayName("Should return page of properties for given criteria")
        void findAllPropertiesByAddressCountryAndCityAndAvailability_whenPropertiesExist_shouldReturnPage() {
            String country = "Poland";
            String city = "Krakow";
            Boolean availability = true;
            Page<Property> propertiesPage = new PageImpl<>(List.of(property), pageable, 1);
            when(propertyRepository.findAllByAddress_CountryAndAddress_CityAndAvailability(country, city, availability, pageable))
                    .thenReturn(propertiesPage);
            when(propertyMapper.propertyToPropertyResponseDto(property)).thenReturn(propertyResponseDTO);

            Page<PropertyResponseDTO> result = propertyService.findAllPropertiesByAddressCountryAndCityAndAvailability(country, city, availability, pageable);

            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no properties found for criteria")
        void findAllPropertiesByAddressCountryAndCityAndAvailability_whenNoPropertiesFound_shouldThrow() {
            String country = "Poland";
            String city = "Krakow";
            Boolean availability = true;
            Page<Property> emptyPage = Page.empty(pageable);
            when(propertyRepository.findAllByAddress_CountryAndAddress_CityAndAvailability(country, city, availability, pageable))
                    .thenReturn(emptyPage);

            assertThrows(ResponseStatusException.class,
                    () -> propertyService.findAllPropertiesByAddressCountryAndCityAndAvailability(country, city, availability, pageable));
        }
    }

    @Nested
    @DisplayName("findAllPropertiesByPropertyTypeAndAvailability Tests")
    class FindAllPropertiesByPropertyTypeAndAvailabilityTests {
        @Test
        @DisplayName("Should return page of properties for given type and availability")
        void findAllPropertiesByPropertyTypeAndAvailability_whenPropertiesExist_shouldReturnPage() {
            PropertyType type = PropertyType.APARTMENT;
            Boolean availability = true;
            Page<Property> propertiesPage = new PageImpl<>(List.of(property), pageable, 1);
            when(propertyRepository.findByPropertyTypeAndAvailability(type, availability, pageable)).thenReturn(propertiesPage);
            when(propertyMapper.propertyToPropertyResponseDto(property)).thenReturn(propertyResponseDTO);

            Page<PropertyResponseDTO> result = propertyService.findAllPropertiesByPropertyTypeAndAvailability(type, availability, pageable);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no properties for type and availability")
        void findAllPropertiesByPropertyTypeAndAvailability_whenNoPropertiesFound_shouldThrow() {
            PropertyType type = PropertyType.APARTMENT;
            Boolean availability = true;
            Page<Property> emptyPage = Page.empty(pageable);
            when(propertyRepository.findByPropertyTypeAndAvailability(type, availability, pageable)).thenReturn(emptyPage);

            assertThrows(ResponseStatusException.class,
                    () -> propertyService.findAllPropertiesByPropertyTypeAndAvailability(type, availability, pageable));
        }
    }


    @Nested
    @DisplayName("createProperty Tests")
    class CreatePropertyTests {
        @Test
        @DisplayName("Should create property successfully")
        void createProperty_whenValidInput_shouldSucceed() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));

            Property newProperty = new Property();
            newProperty.setTitle(propertyRequestDTO.getTitle());

            when(propertyMapper.propertyRequestDtoToProperty(propertyRequestDTO)).thenReturn(newProperty);
            when(propertyRepository.save(any(Property.class))).thenAnswer(invocation -> {
                Property saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });

            MessageResponseDTO response = propertyService.createProperty(propertyRequestDTO, testUsername);

            assertNotNull(response);
            assertTrue(response.getMessage().startsWith("Property created successfully with ID:"));
            assertEquals(user, newProperty.getOwner());
            assertNotNull(newProperty.getCreationDate());
            assertEquals(propertyRequestDTO.isAvailability(), newProperty.getAvailability());
            verify(propertyRepository).save(newProperty);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when authenticated user not found")
        void createProperty_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> propertyService.createProperty(propertyRequestDTO, testUsername));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
            assertEquals("Authenticated user data inconsistent.", exception.getReason());
        }

        @Test
        @DisplayName("Should handle null PropertyRequestDTO leading to NPE if not checked earlier")
        void createProperty_whenDtoIsNull_shouldThrowNPEFromMapperOrGetter() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(propertyMapper.propertyRequestDtoToProperty(null)).thenThrow(NullPointerException.class);

            assertThrows(NullPointerException.class,
                    () -> propertyService.createProperty(null, testUsername));
        }
    }

    @Nested
    @DisplayName("deletePropertyById Tests")
    class DeletePropertyByIdTests {
        @Test
        @DisplayName("Should delete property successfully when property exists")
        void deletePropertyById_whenPropertyExists_shouldSucceed() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            doNothing().when(propertyRepository).deletePropertyById(propertyId);

            MessageResponseDTO response = propertyService.deletePropertyById(propertyId);

            assertNotNull(response);
            assertEquals("Property deleted successfully with ID: " + propertyId, response.getMessage());
            verify(propertyRepository).findById(propertyId);
            verify(propertyRepository).deletePropertyById(propertyId);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null ID")
        void deletePropertyById_whenIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> propertyService.deletePropertyById(null));
            assertEquals("Property ID for deletion must be a positive number.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found")
        void deletePropertyById_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> propertyService.deletePropertyById(propertyId));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found with ID: " + propertyId, exception.getReason());
        }
    }

    @Nested
    @DisplayName("updateProperty Tests")
    class UpdatePropertyTests {
        @Test
        @DisplayName("Should update property successfully")
        void updateProperty_whenValidInput_shouldSucceed() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            doNothing().when(propertyMapper).updatePropertyFromDto(propertyRequestDTO, property);
            when(propertyRepository.save(property)).thenReturn(property);

            MessageResponseDTO response = propertyService.updateProperty(propertyId, propertyRequestDTO);

            assertNotNull(response);
            assertEquals("Property updated successfully with ID: " + propertyId, response.getMessage());
            verify(propertyMapper).updatePropertyFromDto(propertyRequestDTO, property);
            verify(propertyRepository).save(property);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null ID")
        void updateProperty_whenIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> propertyService.updateProperty(null, propertyRequestDTO));
            assertEquals("Property ID for update must be a positive number.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null DTO")
        void updateProperty_whenDtoIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> propertyService.updateProperty(propertyId, null));
            assertEquals("Property request DTO cannot be null for update.", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found")
        void updateProperty_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> propertyService.updateProperty(propertyId, propertyRequestDTO));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        }
    }

    @Nested
    @DisplayName("findPropertyById (DTO) Tests")
    class FindPropertyByIdDTOTests {
        @Test
        @DisplayName("Should return property DTO when property exists")
        void findPropertyById_whenPropertyExists_shouldReturnDTO() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            when(propertyMapper.propertyToPropertyResponseDto(property)).thenReturn(propertyResponseDTO);

            PropertyResponseDTO result = propertyService.findPropertyById(propertyId);
            assertNotNull(result);
            assertEquals(propertyResponseDTO, result);
        }
    }

    @Nested
    @DisplayName("getPropertyEntityById Tests")
    class GetPropertyEntityByIdTests {
        @Test
        @DisplayName("Should return property entity when exists")
        void getPropertyEntityById_whenPropertyExists_shouldReturnEntity() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            Property result = propertyService.getPropertyEntityById(propertyId);
            assertNotNull(result);
            assertEquals(property, result);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property entity not found")
        void getPropertyEntityById_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.empty());
            assertThrows(ResponseStatusException.class, () -> propertyService.getPropertyEntityById(propertyId));
        }
    }


    @Nested
    @DisplayName("isOwner Tests")
    class IsOwnerTests {
        @Test
        @DisplayName("Should return true if user is owner")
        void isOwner_whenUserIsOwner_shouldReturnTrue() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));

            boolean result = propertyService.isOwner(propertyId, testUsername);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false if user is not owner")
        void isOwner_whenUserIsNotOwner_shouldReturnFalse() {
            User anotherUser = new User(); anotherUser.setUsername("another");
            property.setOwner(anotherUser);
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));


            boolean result = propertyService.isOwner(propertyId, testUsername);
            assertFalse(result);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for blank username")
        void isOwner_whenUsernameIsBlank_shouldThrowIllegalArgumentException() {
            assertThrows(IllegalArgumentException.class, () -> propertyService.isOwner(propertyId, " "));
        }

        @Test
        @DisplayName("Should throw ResponseStatusException if user not found")
        void isOwner_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.empty());
            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> propertyService.isOwner(propertyId, testUsername));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertEquals("User not found with given username: " + testUsername, ex.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException if property not found")
        void isOwner_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                    () -> propertyService.isOwner(propertyId, testUsername));
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
            assertEquals("Property not found with ID: " + propertyId, ex.getReason());
        }
    }

    @Nested
    @DisplayName("updatePropertyPricePerDay Tests")
    class UpdatePropertyPricePerDayTests {
        BigDecimal newPrice = new BigDecimal("250.00");
        @Test
        @DisplayName("Should update price successfully")
        void updatePropertyPricePerDay_whenValid_shouldSucceed() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.save(property)).thenReturn(property);

            MessageResponseDTO response = propertyService.updatePropertyPricePerDay(propertyId, newPrice);

            assertEquals("Property price per day updated successfully with ID: " + propertyId, response.getMessage());
            assertEquals(newPrice, property.getPricePerDay());
            verify(propertyRepository).save(property);
        }
    }

    @Nested
    @DisplayName("updatePropertyAvailability Tests")
    class UpdatePropertyAvailabilityTests {
        @Test
        @DisplayName("Should update availability successfully")
        void updatePropertyAvailability_whenValid_shouldSucceed() {
            Boolean newAvailability = false;
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.save(property)).thenReturn(property);

            MessageResponseDTO response = propertyService.updatePropertyAvailability(propertyId, newAvailability);

            assertEquals("Property availability updated successfully with ID: " + propertyId, response.getMessage());
            assertEquals(newAvailability, property.getAvailability());
            verify(propertyRepository).save(property);
        }
    }

    @Nested
    @DisplayName("updatePropertyDescription Tests")
    class UpdatePropertyDescriptionTests {
        String newDescription = "Updated test description.";
        @Test
        @DisplayName("Should update description successfully")
        void updatePropertyDescription_whenValid_shouldSucceed() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            when(propertyRepository.save(property)).thenReturn(property);

            MessageResponseDTO response = propertyService.updatePropertyDescription(propertyId, newDescription);

            assertEquals("Property description updated successfully with ID: " + propertyId, response.getMessage());
            assertEquals(newDescription, property.getDescription());
            verify(propertyRepository).save(property);
        }
    }
}