package org.example.rentify.service;

import org.example.rentify.dto.request.ImageRequestDTO;

import org.example.rentify.dto.response.ImageResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.entity.Image;
import org.example.rentify.entity.Property;
import org.example.rentify.mapper.ImageMapper;
import org.example.rentify.repository.ImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * ImageService class for managing images associated with properties.
 * This service provides methods to add images to properties and handle image-related operations.
 */
@Service
public class ImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

    private final ImageRepository imageRepository;
    private final ImageMapper imageMapper;
    private final PropertyService propertyService;

    public ImageService(ImageRepository imageRepository, ImageMapper imageMapper, PropertyService propertyService) {
        this.imageRepository = imageRepository;
        this.imageMapper = imageMapper;
        this.propertyService = propertyService;
    }

    /**
     * Adds an image to a property.
     *
     * @param propertyId the ID of the property to which the image will be added
     * @param imageRequestDTO the DTO containing image data
     * @throws IllegalArgumentException if propertyId is null or negative, or if imageRequestDTO is null
     * @return a MessageResponseDTO indicating the result of the operation
     */
    @Transactional
    public MessageResponseDTO addImageToProperty(Long propertyId, ImageRequestDTO imageRequestDTO) {
        if (propertyId == null || propertyId < 0 || imageRequestDTO == null) {
            throw new IllegalArgumentException("Property ID and Image Request DTO must not be null");
        }
        Property managedProperty = propertyService.getPropertyEntityById(propertyId);
        Image image = imageMapper.imageRequestDtoToImage(imageRequestDTO);
        image.setProperty(managedProperty);
        image.setUploadDate(LocalDateTime.now());
        imageRepository.save(image);
        return new MessageResponseDTO("Image successfully added to property with ID " + propertyId);
    }

    /**
     * Retrieves all images associated with a property.
     *
     * @param propertyId the ID of the property
     * @throws IllegalArgumentException if propertyId is null or negative
     * @throws ResponseStatusException if no images are found for the property or the property does not exist
     * @return a list of ImageResponseDTOs representing the images
     */
    @Transactional(readOnly = true)
    public List<ImageResponseDTO> getAllImagesByPropertyId(Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            throw new IllegalArgumentException("Property ID must not be null or negative");
        }
        if (propertyService.getPropertyEntityById(propertyId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
        List<Image> images = imageRepository.findByPropertyId(propertyId);
        if (images.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No images found for this property");
        }
        return images.stream()
                .map(imageMapper::imageToImageResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Deletes an image associated with a property.
     *
     * @param propertyId the ID of the property
     * @param imageId the ID of the image to be deleted
     * @throws IllegalArgumentException if propertyId or imageId is null or negative
     * @throws ResponseStatusException if the property or image does not exist
     * @return a MessageResponseDTO indicating the result of the operation
     */
    @Transactional
    public MessageResponseDTO deleteImageFromProperty(Long propertyId, Long imageId) {
        if (propertyId == null || propertyId <= 0 || imageId == null || imageId <= 0) {
            throw new IllegalArgumentException("Property ID and Image ID must not be null or negative");
        }
        if (propertyService.getPropertyEntityById(propertyId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
        imageRepository.delete(image);
        return new MessageResponseDTO("Image with ID " + imageId + " successfully deleted from property with ID " + propertyId);
    }

   /** * Updates an image associated with a property.
     *
     * @param propertyId the ID of the property
     * @param imageId the ID of the image to be updated
     * @param imageRequestDTO the DTO containing updated image data
     * @throws IllegalArgumentException if propertyId, imageId, or imageRequestDTO is null or negative
     * @throws ResponseStatusException if the property or image does not exist
     * @return a MessageResponseDTO indicating the result of the operation
     */
    @Transactional
    public MessageResponseDTO updatesImageFromProperty(Long propertyId, Long imageId, ImageRequestDTO imageRequestDTO) {
        if (propertyId == null || propertyId <= 0 || imageId == null || imageId <= 0 || imageRequestDTO == null) {
            throw new IllegalArgumentException("Property ID, Image ID and Image Request DTO must not be null or negative");
        }
        if (propertyService.getPropertyEntityById(propertyId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
        imageMapper.updateImageFromDto(imageRequestDTO, image);
        image.setUploadDate(LocalDateTime.now());
        imageRepository.save(image);
        return new MessageResponseDTO("Image with ID " + imageId + " successfully updated for property with ID " + propertyId);
    }
}
