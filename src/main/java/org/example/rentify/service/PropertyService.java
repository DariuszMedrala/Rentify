package org.example.rentify.service;

import org.example.rentify.dto.request.PropertyRequestDTO;
import org.example.rentify.dto.response.PropertyResponseDTO;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.PropertyType;
import org.example.rentify.mapper.PropertyMapper;
import org.example.rentify.repository.PropertyRepository;
import org.example.rentify.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PropertyService.class);

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
        logger.debug("Fetching all properties, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
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
        logger.debug("Fetching all properties with availability, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
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
     * @param addressCountry The country of the property address.
     * @param addressCity    The city of the property address.
     * @param availability   The availability of the property.
     * @param pageable       Pagination information.
     * @return A page of PropertyResponseDTOs.
     * @throws ResponseStatusException  If the propertiesPage is empty
     * @throws IllegalArgumentException If the addressCountry or addressCity or availability is null.
     */
    @Transactional(readOnly = true)
    public Page<PropertyResponseDTO> findAllPropertiesByAddressCountryAndCityAndAvailability(String addressCountry, String addressCity, Boolean availability, Pageable pageable) {
        logger.debug("Fetching all properties with specific criteria, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Property> propertiesPage = propertyRepository.findAllByAddress_CountryAndAddress_CityAndAvailability(addressCountry, addressCity, availability, pageable);
        if (propertiesPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No properties found with the specified criteria.");
        }
        if (addressCountry == null || addressCity == null || availability == null) {
            throw new IllegalArgumentException("Address country, address city and availability cannot be null");
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
     * @throws IllegalArgumentException If the propertyType or availability is null.
     */
    @Transactional(readOnly = true)
    public Page<PropertyResponseDTO> findAllPropertiesByPropertyTypeAndAvailability(PropertyType propertyType, Boolean availability, Pageable pageable) {
        logger.debug("Fetching all properties with property type and availability, page: {}, size: {}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Property> propertiesPage = propertyRepository.findByPropertyTypeAndAvailability(propertyType, availability, pageable);
        if (propertiesPage.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No properties found with the specified criteria.");
        }
        if (propertyType == null || availability == null) {
            throw new IllegalArgumentException("Property type and availability cannot be null");
        }
        return propertiesPage.map(propertyMapper::propertyToPropertyResponseDto);
    }

    /**
     * Creates a Property and assigns it to the authenticated user.
     *
     * @param propertyRequestDTO    The PropertyRequestDTO containing the property details.
     * @param authenticatedUsername The username of the authenticated user.
     * @return The PropertyResponseDTO of the created property.
     * @throws IllegalArgumentException If the PropertyRequestDTO is null, the title is blank, or the address is null.
     * @throws ResponseStatusException  If the authenticated user is not found in the database. This should not happen.
     */
    @Transactional
    public PropertyResponseDTO createProperty(PropertyRequestDTO propertyRequestDTO, String authenticatedUsername) {
        if (propertyRequestDTO == null || !StringUtils.hasText(propertyRequestDTO.getTitle()) || propertyRequestDTO.getAddress() == null) {
            throw new IllegalArgumentException("Property request DTO or title or address cannot be null for creation.");
        }
        logger.info("Attempting to create property: {} by authenticated user: {}", propertyRequestDTO.getTitle(), authenticatedUsername);

        User owner = userRepository.findUserByUsername(authenticatedUsername)
                .orElseThrow(() -> {
                    logger.error("Authenticated user '{}' not found in database. This should not happen.", authenticatedUsername);
                    return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Authenticated user data inconsistent.");
                });

        Property property = propertyMapper.propertyRequestDtoToProperty(propertyRequestDTO);
        property.setOwner(owner);
        property.setCreationDate(LocalDateTime.now());
        property.setAvailability(propertyRequestDTO.isAvailability());

        Property savedProperty = propertyRepository.save(property);
        logger.info("Property created successfully with ID: {} for owner: {}", savedProperty.getId(), owner.getUsername());
        return propertyMapper.propertyToPropertyResponseDto(savedProperty);
    }

    /**
     * Deletes a property by its ID.
     *
     * @param id The ID of the property to delete.
     * @throws IllegalArgumentException If the ID is null or not positive.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional
    public void deletePropertyById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID for deletion must be a positive number.");
        }
        logger.info("Attempting to delete property with ID: {}", id);
        propertyRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Property not found with ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id);
                });
        propertyRepository.deletePropertyById(id);
        logger.info("Property deleted successfully with ID: {}", id);
    }

    /**
     * Updates a property by its ID.
     *
     * @param id                 The ID of the property to update.
     * @param propertyRequestDTO The PropertyRequestDTO containing the property details.
     * @throws IllegalArgumentException If the PropertyRequestDTO is null, or the ID is not positive or null
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional
    public void updateProperty(Long id, PropertyRequestDTO propertyRequestDTO) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID for update must be a positive number.");
        }
        if (propertyRequestDTO == null) {
            throw new IllegalArgumentException("Property request DTO cannot be null for update.");
        }
        logger.info("Attempting to update property with ID: {}", id);

        Property propertyToUpdate = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> {
                    logger.warn("Property not found with given ID number: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id);
                });
        propertyMapper.updatePropertyFromDto(propertyRequestDTO, propertyToUpdate);
        Property savedProperty = propertyRepository.save(propertyToUpdate);
        logger.info("Property with ID {} updated successfully.", savedProperty.getId());
    }

    /**
     * Finds a property by its ID.
     *
     * @param id The ID of the property to find.
     * @return The PropertyResponseDTO of the found property.
     * @throws IllegalArgumentException If the ID is null or not positive.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database.
     */
    @Transactional(readOnly = true)
    public PropertyResponseDTO findPropertyById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        logger.debug("Attempting to find property with ID: {}", id);
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> {
                    logger.warn("Property not found with provided ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id);
                });
        logger.info("Property found successfully with ID: {}", id);
        return propertyMapper.propertyToPropertyResponseDto(property);
    }


    /**
     * Checks if a property with a given ID has 'owner' with a given username.
     *
     * @param id       The ID of the property to check.
     * @param username The username of the owner to check.
     * @throws IllegalArgumentException If the ID or username is null or not positive.
     * @throws ResponseStatusException  If the property with the given ID is not found in the database, or the owner is not found in the database.
     * @return True if the property has the given owner, false otherwise.
     */
    public boolean isOwner(Long id, String username) {
        if (id == null || id <= 0 || !StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Property ID and username must be a positive number and not blank.");
        }
        if (userRepository.findUserByUsername(username).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with given username: " + username);
        }
        logger.debug("Checking if property with ID: {} has owner with username: {}", id, username);
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Property not found with given ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id);
                });
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
        logger.debug("Attempting to find property with ID number: {}", id);
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> {
                    logger.warn("Property not found with provided ID number: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id);
                });
        logger.info("Property found successfully with ID number: {}", id);
        return property;
    }

    /**
     * Updates price per day of a property by its ID.
     * @param id The ID of the property to update.
     * @param pricePerDay The new price per day.
     * @throws IllegalArgumentException If the ID is null or not positive, or if the price per day is null or not positive.
     */
    @Transactional
    public void updatePropertyPricePerDay(Long id, BigDecimal pricePerDay) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price per day must be a positive number.");
        }
        logger.debug("Attempting to update property with ID: {} and price per day: {}", id, pricePerDay);
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> {
                    logger.warn("Property can't be found with given ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id);
                });
        property.setPricePerDay(pricePerDay);
        propertyRepository.save(property);
        logger.info("Property with ID {} updated successfully with new price per day: {}", id, pricePerDay);
    }

    /**
     * Updates availability of a property by its ID.
     * @param id The ID of the property to update.
     * @param availability The new availability status.
     * @throws IllegalArgumentException If the ID is null or not positive, or if the availability is null.
     */
    @Transactional
    public void updatePropertyAvailability(Long id, Boolean availability) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        if (availability == null) {
            throw new IllegalArgumentException("Availability cannot be null.");
        }
        logger.debug("Attempting to update property with ID: {} and availability: {}", id, availability);
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> {
                    logger.warn("Property cant be found with given ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id);
                });
        property.setAvailability(availability);
        propertyRepository.save(property);
        logger.info("Property with ID {} updated successfully with new availability: {}", id, availability);
    }

    /**
     * Updates description of a property by its ID.
     * @param id The ID of the property to update.
     * @param description The new description.
     * @throws IllegalArgumentException If the ID is null or not positive, or if the description is blank.
     */
    @Transactional
    public void updatePropertyDescription(Long id, String description) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Property ID must be a positive number.");
        }
        if (!StringUtils.hasText(description)) {
            throw new IllegalArgumentException("Description cannot be blank.");
        }
        logger.debug("Attempting to update property with ID: {} and description: {}", id, description);
        Property property = propertyRepository.findPropertyById(id)
                .orElseThrow(() -> {
                    logger.warn("Property cannot be found with given ID: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found with ID: " + id);
                });
        property.setDescription(description);
        propertyRepository.save(property);
        logger.info("Property with ID {} updated successfully with new description: {}", id, description);
    }
}
