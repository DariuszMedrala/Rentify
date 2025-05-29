package org.example.rentify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentify.dto.request.AddressRequestDTO;
import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.*;
import org.example.rentify.entity.enums.PropertyType;
import org.example.rentify.service.PropertyService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsStringIgnoringCase;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllerTestConfig.class)
@WebMvcTest(PropertyController.class)
@DisplayName("PropertyController Integration Tests")
public class PropertyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PropertyService propertyService;

    private PropertyRequestDTO validPropertyRequestDTO;
    private PropertyResponseDTO propertyResponseDTO;
    private AddressRequestDTO validAddressRequestDTO;


    private final Long testPropertyId = 1L;
    private final String testUsername = "testUser";
    private final String propertyOwnerUsername = "ownerUser";
    private final String adminUsername = "admin";
    private final String otherUsername = "otherUser";

    @BeforeEach
    void setUp() {
        validAddressRequestDTO = new AddressRequestDTO();
        validAddressRequestDTO.setStreetAddress("123 Main St");
        validAddressRequestDTO.setCity("Anytown");
        validAddressRequestDTO.setStateOrProvince("AnyState");
        validAddressRequestDTO.setCountry("CountryLand");
        validAddressRequestDTO.setPostalCode("12345");

        validPropertyRequestDTO = new PropertyRequestDTO();
        validPropertyRequestDTO.setTitle("Beautiful Beach House");
        validPropertyRequestDTO.setDescription("A beautiful beach house with stunning ocean views.");
        validPropertyRequestDTO.setPropertyType(PropertyType.HOUSE);
        validPropertyRequestDTO.setArea(200.50);
        validPropertyRequestDTO.setNumberOfRooms(5);
        validPropertyRequestDTO.setPricePerDay(250.75);
        validPropertyRequestDTO.setAvailability(true);
        validPropertyRequestDTO.setAddress(validAddressRequestDTO);

        UserResponseDTO ownerResponseDTO = new UserResponseDTO();
        ownerResponseDTO.setId(100L);
        ownerResponseDTO.setUsername(propertyOwnerUsername);

        AddressResponseDTO addressResponseDTO = getAddressResponseDTO();


        propertyResponseDTO = new PropertyResponseDTO();
        propertyResponseDTO.setId(testPropertyId);
        propertyResponseDTO.setOwner(ownerResponseDTO);
        propertyResponseDTO.setTitle(validPropertyRequestDTO.getTitle());
        propertyResponseDTO.setDescription(validPropertyRequestDTO.getDescription());
        propertyResponseDTO.setPropertyType(validPropertyRequestDTO.getPropertyType().name());
        propertyResponseDTO.setArea(validPropertyRequestDTO.getArea());
        propertyResponseDTO.setNumberOfRooms(validPropertyRequestDTO.getNumberOfRooms());
        propertyResponseDTO.setPricePerDay(BigDecimal.valueOf(validPropertyRequestDTO.getPricePerDay()));
        propertyResponseDTO.setAvailability(validPropertyRequestDTO.isAvailability());
        propertyResponseDTO.setCreationDate(LocalDateTime.now().minusDays(1));
        propertyResponseDTO.setAddress(addressResponseDTO);
        propertyResponseDTO.setImages(Collections.emptyList());
        propertyResponseDTO.setReviews(Collections.emptyList());
    }

    private AddressResponseDTO getAddressResponseDTO() {
        AddressResponseDTO addressResponseDTO = new AddressResponseDTO();
        addressResponseDTO.setId(1L);
        addressResponseDTO.setStreetAddress(validAddressRequestDTO.getStreetAddress());
        addressResponseDTO.setCity(validAddressRequestDTO.getCity());
        addressResponseDTO.setStateOrProvince(validAddressRequestDTO.getStateOrProvince());
        addressResponseDTO.setCountry(validAddressRequestDTO.getCountry());
        addressResponseDTO.setPostalCode(validAddressRequestDTO.getPostalCode());
        return addressResponseDTO;
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(propertyService);
    }

    @Nested
    @DisplayName("GET /api/properties/all")
    class FindAllPropertiesTests {
        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK and a page of properties")
        void whenFindAllProperties_thenReturnsPageOfProperties() throws Exception {
            Page<PropertyResponseDTO> propertyPage = new PageImpl<>(List.of(propertyResponseDTO), PageRequest.of(0, 10), 1);
            when(propertyService.findAllProperties(any(Pageable.class))).thenReturn(propertyPage);

            mockMvc.perform(get("/api/properties/all")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.content[0].id").value(testPropertyId))
                    .andExpect(jsonPath("$.totalPages").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/properties/{id}")
    class FindPropertyByIdTests {
        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK and property DTO when property exists")
        void whenPropertyExists_thenReturnsPropertyDTO() throws Exception {
            when(propertyService.findPropertyById(testPropertyId)).thenReturn(propertyResponseDTO);

            mockMvc.perform(get("/api/properties/{id}", testPropertyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testPropertyId))
                    .andExpect(jsonPath("$.title").value(propertyResponseDTO.getTitle()));
        }

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 404 Not Found when property does not exist")
        void whenPropertyDoesNotExist_thenReturns404() throws Exception {
            Long nonExistentId = 999L;
            String errorMessage = "Property not found with ID: " + nonExistentId;
            when(propertyService.findPropertyById(nonExistentId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(get("/api/properties/{id}", nonExistentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("GET /api/properties/availability/{availability}")
    class FindAllPropertiesByAvailabilityTests {
        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK and page of available properties")
        void whenFindByAvailable_thenReturnsProperties() throws Exception {
            Page<PropertyResponseDTO> propertyPage = new PageImpl<>(List.of(propertyResponseDTO), PageRequest.of(0, 5), 1);
            when(propertyService.findAllPropertiesByAvailability(eq(true), any(Pageable.class))).thenReturn(propertyPage);

            mockMvc.perform(get("/api/properties/availability/{availability}", true)
                            .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].availability").value(true));
        }
    }

    @Nested
    @DisplayName("GET /api/properties/{city}/{country}/{availability}")
    class GetAllPropertiesByCityCountryAvailabilityTests {
        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK for valid criteria")
        void whenValidCriteria_thenReturnsPage() throws Exception {
            Page<PropertyResponseDTO> propertyPage = new PageImpl<>(List.of(propertyResponseDTO));
            when(propertyService.findAllPropertiesByAddressCountryAndCityAndAvailability(eq("Anytown"), eq("CountryLand"), eq(true), any(Pageable.class)))
                    .thenReturn(propertyPage);

            mockMvc.perform(get("/api/properties/{city}/{country}/{availability}", "Anytown", "CountryLand", true))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(testPropertyId));
        }

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 400 Bad Request if country name is too long")
        void whenCountryNameTooLong_thenReturns400() throws Exception {
            String longCityName = "a".repeat(101);
            String validCountry = "CountryLand";

            mockMvc.perform(get("/api/properties/{city}/{country}/{availability}", longCityName, validCountry, true))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", allOf(
                            containsStringIgnoringCase("city: City name cannot be longer than 100 characters"),
                            not(containsStringIgnoringCase("country:"))
                    )));
        }
    }

    @Nested
    @DisplayName("GET /api/properties/{propertyType}/{availability}")
    class GetAllPropertiesByPropertyTypeAvailabilityTests {
        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK for valid property type")
        void whenValidPropertyType_thenReturnsPage() throws Exception {
            Page<PropertyResponseDTO> propertyPage = new PageImpl<>(List.of(propertyResponseDTO));
            when(propertyService.findAllPropertiesByPropertyTypeAndAvailability(eq(PropertyType.HOUSE), eq(true), any(Pageable.class)))
                    .thenReturn(propertyPage);

            mockMvc.perform(get("/api/properties/{propertyType}/{availability}", "HOUSE", true))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].propertyType").value("HOUSE"));
        }

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 400 Bad Request for invalid property type string")
        void whenInvalidPropertyTypeString_thenReturns400() throws Exception {
            mockMvc.perform(get("/api/properties/{propertyType}/{availability}", "INVALID_TYPE", true))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("Validation Error: Parameter 'propertyType' has an invalid value: 'INVALID_TYPE'. Expected type: 'PropertyType'.")));
        }
    }


    @Nested
    @DisplayName("POST /api/properties/create")
    class CreatePropertyTests {
        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK when authenticated user creates property with valid DTO")
        void whenAuthenticatedUserCreatesProperty_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property created successfully with ID: " + testPropertyId);
            when(propertyService.createProperty(any(PropertyRequestDTO.class), eq(testUsername))).thenReturn(successResponse);

            mockMvc.perform(post("/api/properties/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPropertyRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 400 Bad Request for invalid PropertyRequestDTO (e.g., blank title)")
        void whenCreatePropertyWithInvalidDto_thenReturns400() throws Exception {
            PropertyRequestDTO invalidDto = new PropertyRequestDTO();
            invalidDto.setAddress(validAddressRequestDTO);
            invalidDto.setPropertyType(PropertyType.APARTMENT);
            invalidDto.setArea(50.0);
            invalidDto.setPricePerDay(50.0);


            mockMvc.perform(post("/api/properties/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("Validation Error: title: Property title cannot be blank")));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden for anonymous user")
        void whenAnonymousUserCreatesProperty_thenReturns403() throws Exception {
            mockMvc.perform(post("/api/properties/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPropertyRequestDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/properties/delete/{id}")
    class DeletePropertyTests {
        @Test
        @WithMockUser(username = propertyOwnerUsername)
        @DisplayName("should return 200 OK when property owner deletes their property")
        void whenOwnerDeletesProperty_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property deleted successfully.");
            when(propertyService.isOwner(eq(testPropertyId), eq(propertyOwnerUsername))).thenReturn(true);
            when(propertyService.deletePropertyById(testPropertyId)).thenReturn(successResponse);

            mockMvc.perform(delete("/api/properties/delete/{id}", testPropertyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN deletes property")
        void whenAdminDeletesProperty_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property deleted successfully.");
            when(propertyService.deletePropertyById(testPropertyId)).thenReturn(successResponse);

            mockMvc.perform(delete("/api/properties/delete/{id}", testPropertyId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = otherUsername)
        @DisplayName("should return 403 Forbidden when non-owner/non-ADMIN tries to delete")
        void whenNonOwnerDeletesProperty_thenReturns403() throws Exception {
            when(propertyService.isOwner(eq(testPropertyId), eq(otherUsername))).thenReturn(false);
            mockMvc.perform(delete("/api/properties/delete/{id}", testPropertyId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/properties/update/{id}")
    class UpdatePropertyTests {
        @Test
        @WithMockUser(username = propertyOwnerUsername)
        @DisplayName("should return 200 OK when property owner updates property")
        void whenOwnerUpdatesProperty_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property updated successfully.");
            when(propertyService.isOwner(eq(testPropertyId), eq(propertyOwnerUsername))).thenReturn(true);
            when(propertyService.updateProperty(eq(testPropertyId), any(PropertyRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/properties/update/{id}", testPropertyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPropertyRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN updates property")
        void whenAdminUpdatesProperty_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property updated successfully.");
            when(propertyService.updateProperty(eq(testPropertyId), any(PropertyRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/properties/update/{id}", testPropertyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPropertyRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = propertyOwnerUsername)
        @DisplayName("should return 400 Bad Request when owner updates with invalid DTO")
        void whenOwnerUpdatesWithInvalidDto_thenReturns400() throws Exception {
            when(propertyService.isOwner(eq(testPropertyId), eq(propertyOwnerUsername))).thenReturn(true);

            PropertyRequestDTO invalidDto = getPropertyRequestDTO();

            mockMvc.perform(put("/api/properties/update/{id}", testPropertyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsStringIgnoringCase("Validation Error: title: Property title cannot be blank")));
        }
    }

    private PropertyRequestDTO getPropertyRequestDTO() {
        PropertyRequestDTO invalidDto = new PropertyRequestDTO();
        invalidDto.setTitle("");
        invalidDto.setDescription("A valid description for the test.");
        invalidDto.setPropertyType(PropertyType.APARTMENT);
        invalidDto.setArea(100.0);
        invalidDto.setNumberOfRooms(2);
        invalidDto.setPricePerDay(50.0);
        invalidDto.setAddress(validAddressRequestDTO);
        return invalidDto;
    }

    @Nested
    @DisplayName("PATCH /api/properties/{id}/price_per_day/{pricePerDay}")
    class UpdatePropertyPricePerDayTests {
        @Test
        @WithMockUser(username = propertyOwnerUsername)
        @DisplayName("should return 200 OK when owner updates price")
        void whenOwnerUpdatesPrice_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property price updated.");
            BigDecimal newPrice = new BigDecimal("300.00");
            when(propertyService.isOwner(eq(testPropertyId), eq(propertyOwnerUsername))).thenReturn(true);
            when(propertyService.updatePropertyPricePerDay(testPropertyId, newPrice)).thenReturn(successResponse);

            mockMvc.perform(patch("/api/properties/{id}/price_per_day/{pricePerDay}", testPropertyId, newPrice.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when admin updates price")
        void whenAdminUpdatesPrice_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property price updated.");
            BigDecimal newPrice = new BigDecimal("350.00");
            when(propertyService.updatePropertyPricePerDay(testPropertyId, newPrice)).thenReturn(successResponse);

            mockMvc.perform(patch("/api/properties/{id}/price_per_day/{pricePerDay}", testPropertyId, newPrice.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
    }

    @Nested
    @DisplayName("PATCH /api/properties/{id}/availability/{availability}")
    class UpdatePropertyAvailabilityTests {
        @Test
        @WithMockUser(username = propertyOwnerUsername)
        @DisplayName("should return 200 OK when owner updates availability")
        void whenOwnerUpdatesAvailability_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property availability updated.");
            when(propertyService.isOwner(eq(testPropertyId), eq(propertyOwnerUsername))).thenReturn(true);
            when(propertyService.updatePropertyAvailability(testPropertyId, false)).thenReturn(successResponse);

            mockMvc.perform(patch("/api/properties/{id}/availability/{availability}", testPropertyId, false))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
    }

    @Nested
    @DisplayName("PATCH /api/properties/{id}/description/{description}")
    class UpdatePropertyDescriptionTests {
        @Test
        @WithMockUser(username = propertyOwnerUsername)
        @DisplayName("should return 200 OK when owner updates description")
        void whenOwnerUpdatesDescription_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Property description updated.");
            String newDescription = "Updated awesome description";
            when(propertyService.isOwner(eq(testPropertyId), eq(propertyOwnerUsername))).thenReturn(true);
            when(propertyService.updatePropertyDescription(testPropertyId, newDescription)).thenReturn(successResponse);

            mockMvc.perform(patch("/api/properties/{id}/description/{description}", testPropertyId, newDescription))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
    }
}