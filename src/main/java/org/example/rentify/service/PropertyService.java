package org.example.rentify.service;

import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.PropertyResponseDTO;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.PropertyType;
import org.example.rentify.mapper.PropertyMapper;
import org.example.rentify.repository.PropertyRepository;
import org.example.rentify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * PropertyService class for managing properties in the system.
 * This class provides methods to interact with the PropertyRepository.
 * It throws specific exception for error conditions, to be handled by the global exception handler.
 */
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final UserRepository userRepository;

    @Autowired
    public PropertyService(PropertyRepository propertyRepository, PropertyMapper propertyMapper, UserRepository userRepository) {
        this.propertyRepository = propertyRepository;
        this.propertyMapper = propertyMapper;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves all properties in a paginated format as DTOs.
     *
     * @param pageable Pagination information.
     * @return A page of PropertyResponseDTOs.
     * @throws ResponseStatusException If the propertiesPage is empty
     */
    @Transactional(readOnly = true)
    public Page<PropertyResponseDTO> findAllProperties(Pageable pageable) {

        Page<Property> propertiesPage = propertyRepository.findAll(pageable);
        if (propertiesPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No properties found with the specified criteria.");
        }
        return propertiesPage.map(propertyMapper::propertyToPropertyResponseDto);
    }

    /**
     * Retrieves all properties with availability status
     *
     * @param availability The availability of the property.
     * @return A page of PropertyResponseDTOs.
     * @throws ResponseStatusException  If the propertiesPage is empty
     * @throws IllegalArgumentException If the availability is null.
     */
    @Transactional(readOnly = true)
    public Page<PropertyResponseDTO> findAllPropertiesByAvailability(Boolean availability, Pageable pageable) {
        Page<Property> propertiesPage = propertyRepository.findByAvailability(availability, pageable);
        if (propertiesPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No properties found with the specified criteria.");
        }
        if (availability == null) {
            throw new IllegalArgumentException("Availability cannot be null.");
        }
        return propertiesPage.map(propertyMapper::propertyToPropertyResponseDto);
    }

    /**
     * Retrieves all properties with specific criteria in a paginated format as DTOs.
     *
     * @param addressCountry The country of the property's address.
     * @param addressCity    The city of the property's address.
     * @param availability   The availability of the property.
     * @param pageable       Pagination information.
     * @return A page of PropertyResponseDTOs.
     * @throws ResponseStatusException  If the propertiesPage is empty
     */
    @Transactional(readOnly = true)
    public Page<PropertyResponseDTO> findAllPropertiesByAddressCountryAndCityAndAvailability(String addressCountry, String addressCity, Boolean availability, Pageable pageable) {

        Page<Property> propertiesPage = propertyRepository.findAllByAddress_CountryAndAddress_CityAndAvailability(addressCountry, addressCity, availability, pageable);
        if (propertiesPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No properties found with the specified criteria.");
        }
        return propertiesPage.map(propertyMapper::propertyToPropertyResponseDto);
    }

    /**
     * Retrieves all properties with specific criteria in a paginated format as DTOs.
     *
     * @param propertyType The type of the property.
     * @param availability The availability of the property.
     * @param pageable     Pagination information.
     * @return A page of PropertyResponseDTOs.
     * @throws ResponseStatusException  If the propertiesPage is empty
     */
    @Transactional(readOnly = true)
    public Page<PropertyResponseDTO> findAllPropertiesByPropertyTypeAndAvailability(PropertyType propertyType, Boolean availability, Pageable pageable) {

        Page<Property> propertiesPage = propertyRepository.findByPropertyTypeAndAvailability(propertyType, availability, pageable);
        if (propertiesPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No properties found with the specified criteria.");
        }
        return propertiesPage.map(propertyMapper::propertyToPropertyResponseDto);
    }

    /**
     * Creates a new property.
     *
     * @param propertyRequestDTO The PropertyRequestDTO containing the property details.
     * @param authenticatedUsername The username of the authenticated user creating the property.
     * @return A MessageResponseDTO indicating success.
     * @throws IllegalArgumentException If the PropertyRequestDTO is null or if the authenticatedUsername is blank.
     */
    @Transactional
    public MessageResponseDTO createProperty(PropertyRequestDTO propertyRequestDTO, String authenticatedUsername) {

        User owner = userRepository.findUserByUsername(authenticatedUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Authenticated user data inconsistent."));

        Property property = propertyMapper.propertyRequestDtoToProperty(propertyRequestDTO);
        property.setOwner(owner);
        property.setCreationDate(LocalDateTime.now());
        property.setAvailability(propertyRequestDTO.isAvailability());

        Property savedProperty = propertyRepository.save(property);
        return new MessageResponseDTO("Property created successfully with ID: " + savedProperty.getId());
    }

    /**
     * Deletes a property by its ID.
     *
     * @param id The ID of the property to delete.
     * @return A MessageResponseDTO indicating success.
     * @throws IllegalArgumentException If the ID is null or not positive.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional
    public MessageResponseDTO deletePropertyById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID for deletion must be a positive number.");
        }
        propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id));

        propertyRepository.deletePropertyById(id);
        return new MessageResponseDTO("Property deleted successfully with ID: " + id);
    }

    /**
     * Updates a property by its ID.
     *
     * @param id The ID of the property to update.
     * @param propertyRequestDTO The PropertyRequestDTO containing the updated property details.
     * @return A MessageResponseDTO indicating success.
     * @throws IllegalArgumentException If the ID is null or not positive, or if the PropertyRequestDTO is null.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional
    public MessageResponseDTO updateProperty(Long id, PropertyRequestDTO propertyRequestDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID for update must be a positive number.");
        }
        if (propertyRequestDTO == null) {
            throw new IllegalArgumentException("Property request DTO cannot be null for update.");
        }

        Property propertyToUpdate = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id));
        propertyMapper.updatePropertyFromDto(propertyRequestDTO, propertyToUpdate);
        propertyRepository.save(propertyToUpdate);
        return new MessageResponseDTO("Property updated successfully with ID: " + id);
    }

    /**
     * Retrieves a property by its ID.
     *
     * @param id The ID of the property to retrieve.
     * @return The PropertyResponseDTO of the found property.
     * @throws IllegalArgumentException If the ID is null or not positive.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional(readOnly = true)
    public PropertyResponseDTO findPropertyById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id));
        return propertyMapper.propertyToPropertyResponseDto(property);
    }


    /**
     * Checks if the authenticated user is the owner of the property with the given ID.
     *
     * @param id The ID of the property.
     * @param username The username of the authenticated user.
     * @return true if the user is the owner, false otherwise.
     * @throws IllegalArgumentException If the ID is null or not positive, or if the username is blank.
     * @throws ResponseStatusException  If the user or property is not found.
     */
    public boolean isOwner(Long id, String username) {
        if (id == null || id <= 0 || !StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Property ID and username must be a positive number and not blank.");
        }
        if (userRepository.findUserByUsername(username).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with given username: " + username);
        }
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id));
        return property.getOwner().getUsername().equals(username);
    }

    /**
     * Retrieves a property entity by its ID.
     *
     * @param id The ID of the property to retrieve.
     * @return The Property entity.
     * @throws IllegalArgumentException If the ID is null or not positive.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional(readOnly = true)
    public Property getPropertyEntityById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        return propertyRepository.findPropertyById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id));
    }

    /**
     * Updates the price per day of a property by its ID.
     *
     * @param id The ID of the property to update.
     * @param pricePerDay The new price per day.
     * @return A MessageResponseDTO indicating success.
     * @throws IllegalArgumentException If the ID is null or not positive, or if the pricePerDay is null or not positive.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional
    public MessageResponseDTO updatePropertyPricePerDay(Long id, BigDecimal pricePerDay) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price per day must be a positive number.");
        }
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id));

        property.setPricePerDay(pricePerDay);
        propertyRepository.save(property);
        return new MessageResponseDTO("Property price per day updated successfully with ID: " + id);
    }

    /**
     * Updates the availability of a property by its ID.
     *
     * @param id The ID of the property to update.
     * @param availability The new availability status.
     * @return A MessageResponseDTO indicating success.
     * @throws IllegalArgumentException If the ID is null or not positive, or if the availability is null.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional
    public MessageResponseDTO updatePropertyAvailability(Long id, Boolean availability) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        if (availability == null) {
            throw new IllegalArgumentException("Availability cannot be null.");
        }
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id));
        property.setAvailability(availability);
        propertyRepository.save(property);
        return new MessageResponseDTO("Property availability updated successfully with ID: " + id);
    }

    /**
     * Updates the description of a property by its ID.
     *
     * @param id The ID of the property to update.
     * @param description The new description.
     * @return A MessageResponseDTO indicating success.
     * @throws IllegalArgumentException If the ID is null or not positive, or if the description is blank.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional
    public MessageResponseDTO updatePropertyDescription(Long id, String description) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("Description cannot be blank.");
        }
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id));
        property.setDescription(description);
        propertyRepository.save(property);
        return new MessageResponseDTO("Property description updated successfully with ID: " + id);
    }
}
