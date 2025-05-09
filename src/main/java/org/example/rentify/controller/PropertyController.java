package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.PropertyResponseDTO;
import org.example.rentify.entity.enums.PropertyType;
import org.example.rentify.service.PropertyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


/*
 * PropertyController is a REST controller for managing properties in the system.
 * It provides endpoints for creating, updating, deleting, and retrieving properties.
 */
@RestController
@RequestMapping("/api/properties")
@Tag(name = "Properties Management", description = "Endpoints for managing properties")
@SecurityRequirement(name = "bearerAuth")
public class PropertyController {

    private static final Logger logger = LoggerFactory.getLogger(PropertyController.class);

    private final PropertyService propertyService;

    @Autowired
    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    /**
     * Retrieves all properties in a paginated format.
     *
     * @param pageable the pagination information.
     * @return a paginated list of properties.
     */
    @Operation(summary = "Get all properties", description = "Retrieve a paginated list of all properties")
    @GetMapping("/all")
    public ResponseEntity<Page<PropertyResponseDTO>> getAllProperties(@Parameter(
            name = "pageable",
            description = "Pageable object containing pagination information",
            example = "{\"page\": 0, \"size\": 10, \"sort\": \"title,asc\"}") Pageable pageable) {
        Page<PropertyResponseDTO> propertyResponseDTOPage = propertyService.findAllProperties(pageable);
        return ResponseEntity.ok(propertyResponseDTOPage);
    }

    /**
     * Retrieves a property by its ID.
     *
     * @param id the ID of the property to retrieve.
     * @return the property's details.
     */
    @Operation(summary = "Get property with given ID", description = "Retrieve a property DTO from given ID")
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> getPropertyById(@Parameter(
            description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id) {

        PropertyResponseDTO property = propertyService.findPropertyById(id);
        return ResponseEntity.ok(property);
    }

    /**
     * Retrieves all properties with a given availability status.
     *
     * @param availability the availability status
     * @param pageable     the pagination information.
     * @return a paginated list of properties with the given availability status.
     */
    @Operation(summary = "Get all properties with availability status", description = "Retrieve a paginated list of all properties with given availability status")
    @GetMapping("/availability/{availability}")
    public ResponseEntity<Page<PropertyResponseDTO>> getAllPropertiesByAvailability(
            @Parameter(description = "Availability status", in = ParameterIn.PATH) @PathVariable boolean availability,
            @Parameter(name = "pageable",
                    description = "Pageable object containing pagination information",
                    example = "{\"page\": 0, \"size\": 10, \"sort\": \"title,asc\"}") Pageable pageable) {

        Page<PropertyResponseDTO> propertyResponseDTOPage = propertyService.findAllPropertiesByAvailability(availability, pageable);
        return ResponseEntity.ok(propertyResponseDTOPage);
    }

    /**
     * Retrieves all properties with a given country name, city name and availability status.
     *
     * @param country      the country name
     * @param city         the city name
     * @param availability the availability status
     * @param pageable     the pagination information.
     * @return a paginated list of properties with the given country name, city name and availability status.
     */
    @Operation(summary = "Get all properties with country, city and availability status", description = "Retrieve a paginated list of all properties with given criteria")
    @GetMapping("/{city}/{country}/{availability}")
    public ResponseEntity<Page<PropertyResponseDTO>> getAllPropertiesByCityCountryAvailability(
            @Parameter(description = "Name of the city", in = ParameterIn.PATH) @PathVariable String city,
            @Parameter(description = "Name of the country", in = ParameterIn.PATH) @PathVariable String country,
            @Parameter(description = "Availability status", in = ParameterIn.PATH) @PathVariable boolean availability,
            @Parameter(name = "pageable",
                    description = "Pageable object containing pagination information",
                    example = "{\"page\": 0, \"size\": 10, \"sort\": \"title,asc\"}") Pageable pageable) {

        Page<PropertyResponseDTO> propertyResponseDTOPage = propertyService.findAllPropertiesByAddressCountryAndCityAndAvailability(city, country, availability, pageable);
        return ResponseEntity.ok(propertyResponseDTOPage);
    }

    /**
     * Retrieves all properties with a given property type and availability status.
     *
     * @param propertyType the property type
     * @param availability the availability status
     * @param pageable     the pagination information.
     * @return a paginated list of properties with the given property type and availability status.
     */
    @Operation(summary = "Get all properties with property type and availability status", description = "Retrieve a paginated list of all properties with given criteria")
    @GetMapping("/{propertyType}/{availability}")
    public ResponseEntity<Page<PropertyResponseDTO>> getAllPropertiesByPropertyTypeAvailability(
            @Parameter(description = "Property type", in = ParameterIn.PATH) @PathVariable PropertyType propertyType,
            @Parameter(description = "Availability status", in = ParameterIn.PATH) @PathVariable boolean availability,
            @Parameter(name = "pageable",
                    description = "Pageable object containing pagination information",
                    example = "{\"page\": 0, \"size\": 10, \"sort\": \"title,asc\"}") Pageable pageable) {

        Page<PropertyResponseDTO> propertyResponseDTOPage = propertyService.findAllPropertiesByPropertyTypeAndAvailability(propertyType, availability, pageable);
        return ResponseEntity.ok(propertyResponseDTOPage);
    }

    /**
     * Creates a new property and assigns it to the currently authenticated user.
     *
     * @param propertyRequestDTO property request DTO containing property details.
     * @param authentication     the authentication object containing the currently authenticated user.
     * @return a success message or an error message if creation fails.
     */
    @Operation(summary = "Create a new property", description = "Allows authenticated users to list a new property. The owner will be the currently authenticated user.")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createProperty(@Parameter(description = "Property request DTO containing property details")
                                            @Valid @RequestBody PropertyRequestDTO propertyRequestDTO,
                                            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            logger.error("Unexpected principal type : {}", principal.getClass().getName());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
        PropertyResponseDTO createdProperty = propertyService.createProperty(propertyRequestDTO, username);
        return ResponseEntity.ok(new MessageResponseDTO("Property created successfully with ID " + createdProperty.getId() + "."));
    }

    /**
     * Deletes a property by its ID if logged-in user is the owner of the property or an admin.
     *
     * @param id             ID of the property to delete.
     * @param authentication Authentication object containing the currently authenticated user.
     * @return A success message or an error message if deletion fails.
     */
    @Operation(summary = "Delete a property", description = "Allows authenticated users to delete a property. Requires ADMIN role or for the user to be deleting their own data.")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public ResponseEntity<?> deleteProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                            Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        propertyService.deletePropertyById(id);
        logger.info("Property with ID {} deleted successfully by user '{}'", id, authentication.getName());
        return ResponseEntity.ok(new MessageResponseDTO("Property with ID " + id + " deleted successfully."));
    }

    /**
     * Updates a property by its ID if logged-in user is the owner of the property or an admin.
     *
     * @param id             ID of the property to update.
     * @param authentication Authentication object containing the currently authenticated user.
     * @param propertyRequestDTO Property request DTO containing property details.
     * @return A success message or an error message if deletion fails.
     */
    @Operation(summary = "Update a property", description = "Allows authenticated users to update a property. Requires ADMIN role or for the user to be updating their own data")
    @PutMapping("update/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public ResponseEntity<?> updateProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                            Authentication authentication,
                                            @Parameter(description = "Property request DTO containing property details") @Valid @RequestBody PropertyRequestDTO propertyRequestDTO) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        propertyService.updateProperty(id, propertyRequestDTO);
        logger.info("Property with ID {} updated successfully by owner '{}'", id, authentication.getName());
        return ResponseEntity.ok(new MessageResponseDTO("Property with ID " + id + " updated successfully."));
    }

    /**
     * Updates price per day of a property by its ID if logged-in user is the owner of the property or an admin.
     * @param id the ID of the property to update
     * @param pricePerDay the new price per day
     * @param authentication the authentication object containing the currently authenticated user
     * @return a success message or an error message if an update fails
     */
    @Operation(summary = "Update a property price per day", description = "Allows authenticated owner to update a property price per day.")
    @PatchMapping("{id}/price_per_day/{pricePerDay}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public ResponseEntity<?> updatePropertyPricePerDay(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                                       @Parameter(description = "New price per day", in = ParameterIn.PATH) @PathVariable BigDecimal pricePerDay,
                                                       Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        propertyService.updatePropertyPricePerDay(id, pricePerDay);
        logger.info("Property with ID number {} updated successfully by owner '{}'", id, authentication.getName());
        return ResponseEntity.ok(new MessageResponseDTO("Property with ID " + id + " updated successfully."));
    }

    /**
     * Updates availability status of a property by its ID if logged-in user is the owner of the property or an admin.
     * @param id the ID of the property to update
     * @param availability the new availability status
     * @param authentication the authentication object containing the currently authenticated user
     * @return a success message or an error message if an update fails
     */
    @Operation(summary = "Update a property availability", description = "Allows authenticated owner to update a property availability.")
    @PatchMapping("{id}/availability/{availability}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public ResponseEntity<?> updatePropertyAvailability(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                                         @Parameter(description = "New availability status", in = ParameterIn.PATH) @PathVariable Boolean availability,
                                                         Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        propertyService.updatePropertyAvailability(id, availability);
        logger.info("Property with given ID number {} updated successfully by owner '{}'", id, authentication.getName());
        return ResponseEntity.ok(new MessageResponseDTO("Property with ID " + id + " updated successfully."));
    }

    /**
     * Updates description of a property by its ID if logged-in user is the owner of the property or an admin.
     * @param id the ID of the property to update
     * @param description the new description
     * @param authentication the authentication object containing the currently authenticated user
     * @return a success message or an error message if an update fails
     */
    @Operation(summary = "Update a property description", description = "Allows authenticated owner to update a property description.")
    @PatchMapping("{id}/description/{description}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public ResponseEntity<?> updatePropertyDescription(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                                         @Parameter(description = "New description", in = ParameterIn.PATH) @PathVariable String description,
                                                         Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        propertyService.updatePropertyDescription(id, description);
        logger.info("Property with given ID number {} updated with success by owner '{}'", id, authentication.getName());
        return ResponseEntity.ok(new MessageResponseDTO("Property with ID " + id + " updated successfully."));
    }
}
