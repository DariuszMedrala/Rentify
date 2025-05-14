package org.example.rentify.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.request.ImageRequestDTO;
import org.example.rentify.dto.response.ImageResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties/{propertyId}/image")
@Tag(name = "Image Management", description = "Endpoints for managing property images")
@SecurityRequirement(name = "bearerAuth")
public class ImageController {

    private final ImageService imageService;

    @Autowired
    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    /**
     * Adds an image to a property.
     *
     * @param propertyId the ID of the property
     * @param imageRequestDTO the DTO containing image data
     * @return a MessageResponseDTO indicating the result of the operation
     */
    @Operation(summary = "Add an image to a property",
            description = "Adds an image to the specified property. Requires authentication.")
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#propertyId, principal.username))")
    public MessageResponseDTO addImageToProperty(@Parameter (description = "Property ID", in = ParameterIn.PATH)
                                                     @PathVariable Long propertyId,
                                                 @Valid @RequestBody ImageRequestDTO imageRequestDTO){
        return imageService.addImageToProperty(propertyId, imageRequestDTO);
    }

    /**
     * Retrieves all images associated with a property.
     *
     * @param propertyId the ID of the property
     * @return a ResponseEntity containing the list of images
     */
    @Operation(summary = "Display all images for a property",
            description = "Retrieves all images associated with the specified property.")
    @GetMapping("/all")
    public ResponseEntity<List<ImageResponseDTO>> getAllImagesForProperty(@Parameter(
            description = "Property ID", in = ParameterIn.PATH) @PathVariable Long propertyId) {
        return ResponseEntity.ok(imageService.getAllImagesByPropertyId(propertyId));
    }

    /**
     * Deletes an image associated with a property.
     *
     * @param propertyId the ID of the property
     * @param imageId the ID of the image to be deleted
     * @return a MessageResponseDTO indicating the result of the operation
     */
    @DeleteMapping("/delete/{imageId}")
    @Operation(summary = "Delete an image from a property",
            description = "Deletes the specified image from the property. Requires authentication.")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#propertyId, principal.username))")
    public MessageResponseDTO deleteImageFromProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long propertyId,
                                                     @Parameter(description = "Image ID", in = ParameterIn.PATH) @PathVariable Long imageId) {

        return imageService.deleteImageFromProperty(propertyId, imageId);
    }

    /**
     * * Updates an image associated with a property.
     *
     * @param propertyId the ID of the property
     * @param imageId the ID of the image to be updated
     * @param imageRequestDTO the DTO containing updated image data
     * @return a MessageResponseDTO indicating the result of the operation
     */
    @PutMapping("/update/{imageId}")
    @Operation(summary = "Update an image from a property",
               description = "Updates the specified image from the property. Requires authentication.")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#propertyId, principal.username))")
    public MessageResponseDTO updatesImageFromProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long propertyId,
                                                      @Parameter(description = "Image ID", in = ParameterIn.PATH) @PathVariable Long imageId,
                                                      @Parameter(description = "Image Request DT0") @Valid @RequestBody ImageRequestDTO imageRequestDTO){

        return imageService.updatesImageFromProperty(propertyId, imageId, imageRequestDTO);
    }
}
