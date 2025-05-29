package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.PropertyResponseDTO;
import org.example.rentify.entity.enums.PropertyType;
import org.example.rentify.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
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
@Validated
public class PropertyController {

    private final PropertyService propertyService;

    @Autowired
    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    /**
     * Retrieves all properties in a paginated format.
     *
     * @param pageable the pagination information
     * @return a paginated list of properties
     */
    @Operation(summary = "Get all properties", description = "Retrieve a paginated list of all properties")
    @GetMapping("/all")
    public ResponseEntity<Page<PropertyResponseDTO>> findAllProperties(@Parameter(
            name = "pageable",
            description = "Pageable object containing pagination information",
            example = "{\"page\": 0, \"size\": 10, \"sort\": \"title,asc\"}") Pageable pageable) {

        return ResponseEntity.ok(propertyService.findAllProperties(pageable));
    }

    /**
     * Retrieves a property by its ID.
     *
     * @param id the ID of the property to retrieve.
     * @return the property's details.
     */
    @Operation(summary = "Get property with given ID", description = "Retrieve a property DTO from given ID")
    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponseDTO> findPropertyById(@Parameter(
            description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id) {

        return ResponseEntity.ok(propertyService.findPropertyById(id));
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
    public ResponseEntity<Page<PropertyResponseDTO>> findAllPropertiesByAvailability(
            @Parameter(description = "Availability status", in = ParameterIn.PATH) @PathVariable boolean availability,
            @Parameter(name = "pageable",
                    description = "Pageable object containing pagination information",
                    example = "{\"page\": 0, \"size\": 10, \"sort\": \"title,asc\"}") Pageable pageable) {

        return ResponseEntity.ok(propertyService.findAllPropertiesByAvailability(availability, pageable));
    }

    /**
     * Retrieves all properties with a given city and country and availability status.
     *
     * @param city        the name of the city
     * @param country     the name of the country
     * @param availability the availability status
     * @param pageable    the pagination information.
     * @return a paginated list of properties with the given city, country, and availability status.
     */
    @Operation(summary = "Get all properties with country, city and availability status", description = "Retrieve a paginated list of all properties with given criteria")
    @GetMapping("/{city}/{country}/{availability}")
    public ResponseEntity<Page<PropertyResponseDTO>> getAllPropertiesByCityCountryAvailability(
            @Parameter(description = "Name of the city", in = ParameterIn.PATH)
            @Size(max = 100, message = "City name cannot be longer than 100 characters")
            @Valid @PathVariable String city,
            @Parameter(description = "Name of the country", in = ParameterIn.PATH)
            @Size(max = 100, message = "Country name cannot be longer than) 100 characters")
            @Valid @PathVariable String country,
            @Parameter(description = "Availability status", in = ParameterIn.PATH) @PathVariable boolean availability,
            @Parameter(name = "pageable",
                    description = "Pageable object containing pagination information",
                    example = "{\"page\": 0, \"size\": 10, \"sort\": \"title,asc\"}") Pageable pageable) {

        return ResponseEntity.ok(propertyService.findAllPropertiesByAddressCountryAndCityAndAvailability(city, country, availability, pageable));
    }

    /**
     * Retrieves all properties with a given property type and availability status.
     *
     * @param propertyType the type of the property
     * @param availability the availability status
     * @param pageable     the pagination information.
     * @return a paginated list of properties with the given property type and availability status.
     */
    @Operation(summary = "Get all properties with property type and availability status", description = "Retrieve a paginated list of all properties with given criteria")
    @GetMapping("/{propertyType}/{availability}")
    public ResponseEntity<Page<PropertyResponseDTO>> getAllPropertiesByPropertyTypeAvailability(
            @Parameter(description = "Property type", in = ParameterIn.PATH) @Valid @PathVariable PropertyType propertyType,
            @Parameter(description = "Availability status", in = ParameterIn.PATH) @PathVariable boolean availability,
            @Parameter(name = "pageable",
                    description = "Pageable object containing pagination information",
                    example = "{\"page\": 0, \"size\": 10, \"sort\": \"title,asc\"}") Pageable pageable) {

        return ResponseEntity.ok(propertyService.findAllPropertiesByPropertyTypeAndAvailability(propertyType, availability, pageable));
    }

    /**
     * Creates a new property.
     *
     * @param propertyRequestDTO the property requests DTO containing property details.
     * @param authentication the authentication object containing the currently authenticated user.
     * @return a success message or an error message if creation fails.
     */
    @Operation(summary = "Create a new property", description = "Allows authenticated users to list a new property. The owner will be the currently authenticated user.")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public MessageResponseDTO createProperty(@Parameter(description = "Property request DTO containing property details")
                                            @Valid @RequestBody PropertyRequestDTO propertyRequestDTO,
                                            Authentication authentication) {

        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        return propertyService.createProperty(propertyRequestDTO, username);
    }

    /**
     * Deletes a property by its ID if logged-in user is the owner of the property or an admin.
     *
     * @param id the ID of the property to delete.
     * @return a success message or an error message if deletion fails.
     */
    @Operation(summary = "Delete a property", description = "Allows authenticated users to delete a property. Requires ADMIN role or for the user to be deleting their own data.")
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public MessageResponseDTO deleteProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id) {

       return propertyService.deletePropertyById(id);
    }

    /**
     * Updates a property by its ID if logged-in user is the owner of the property or an admin.
     *
     * @param id the ID of the property to update
     * @param propertyRequestDTO the property request DTO containing updated property details
     * @return a success message or an error message if an update fails
     */
    @Operation(summary = "Update a property", description = "Allows authenticated users to update a property. Requires ADMIN role or for the user to be updating their own data")
    @PutMapping("update/{id}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public MessageResponseDTO updateProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                            @Parameter(description = "Property request DTO containing property details") @Valid @RequestBody PropertyRequestDTO propertyRequestDTO) {

       return propertyService.updateProperty(id, propertyRequestDTO);
    }

    /**
     * Updates the price per day of a property by its ID if logged-in user is the owner of the property or an admin.
     * @param id the ID of the property to update
     * @param pricePerDay the new price per day
     * @return a success message or an error message if an update fails
     */
    @Operation(summary = "Update a property price per day", description = "Allows authenticated owner to update a property price per day.")
    @PatchMapping("{id}/price_per_day/{pricePerDay}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public MessageResponseDTO updatePropertyPricePerDay(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                                       @Parameter(description = "New price per day", in = ParameterIn.PATH) @PathVariable BigDecimal pricePerDay){

        return propertyService.updatePropertyPricePerDay(id, pricePerDay);
    }

    /**
     * Updates the availability status of a property by its ID if logged-in user is the owner of the property or an admin.
     * @param id the ID of the property to update
     * @param availability the new availability status
     * @return a success message or an error message if an update fails
     */
    @Operation(summary = "Update a property availability", description = "Allows authenticated owner to update a property availability.")
    @PatchMapping("{id}/availability/{availability}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public MessageResponseDTO updatePropertyAvailability(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                                         @Parameter(description = "New availability status", in = ParameterIn.PATH) @PathVariable Boolean availability) {

        return propertyService.updatePropertyAvailability(id, availability);

    }

    /**
     * Updates the description of a property by its ID if logged-in user is the owner of the property or an admin.
     * @param id the ID of the property to update
     * @param description the new description
     * @return a success message or an error message if an update fails
     */
    @Operation(summary = "Update a property description", description = "Allows authenticated owner to update a property description.")
    @PatchMapping("{id}/description/{description}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#id, principal.username))")
    public MessageResponseDTO updatePropertyDescription(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long id,
                                                         @Parameter(description = "New description", in = ParameterIn.PATH) @PathVariable String description) {

        return propertyService.updatePropertyDescription(id, description);
    }
}
