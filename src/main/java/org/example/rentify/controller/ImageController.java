package org.example.rentify.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.rentify.dto.request.ImageRequestDTO;
import org.example.rentify.dto.response.ImageResponseDTO;
import org.example.rentify.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
     * @param authentication the authentication object
     * @return a ResponseEntity indicating the result of the operation
     */
    @Operation(summary = "Add an image to a property",
            description = "Adds an image to the specified property. Requires authentication.")
    @PostMapping("/add")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#propertyId, principal.username))")
    public ResponseEntity<?> addImageToProperty(@PathVariable Long propertyId, @RequestBody ImageRequestDTO imageRequestDTO, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        imageService.addImageToProperty(propertyId, imageRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Image added successfully");
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
            description = "Property ID", in = ParameterIn.PATH)  @PathVariable Long propertyId) {
        return ResponseEntity.ok(imageService.getAllImagesByPropertyId(propertyId));
    }

    /**
     * Deletes an image associated with a property.
     *
     * @param propertyId the ID of the property
     * @param imageId the ID of the image to be deleted
     * @return a ResponseEntity indicating the result of the operation
     */
    @DeleteMapping("/delete/{imageId}")
    @Operation(summary = "Delete an image from a property",
            description = "Deletes the specified image from the property. Requires authentication.")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#propertyId, principal.username))")
    public ResponseEntity<?> deleteImageFromProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long propertyId,
                                                     @Parameter(description = "Image ID", in = ParameterIn.PATH) @PathVariable Long imageId,
                                                     @Parameter(description = "Authentication") Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        imageService.deleteImageFromProperty(propertyId, imageId);
        return ResponseEntity.status(HttpStatus.OK).body("Image deleted successfully");
    }

    /**
     * Updates an image associated with a property.
     *
     * @param propertyId the ID of the property
     * @param imageId the ID of the image to be updated
     * @param imageRequestDTO the DTO containing updated image data
     * @return a ResponseEntity indicating the result of the operation
     */
    @PutMapping("/update/{imageId}")
    @Operation(summary = "Update an image from a property",
               description = "Updates the specified image from the property. Requires authentication.")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#propertyId, principal.username))")
    public ResponseEntity<?> updatesImageFromProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long propertyId,
                                                      @Parameter(description = "Image ID", in = ParameterIn.PATH) @PathVariable Long imageId,
                                                      @Parameter(description = "Image Request DT0") @RequestBody ImageRequestDTO imageRequestDTO,
                                                      @Parameter(description = "Authentication") Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        imageService.updatesImageFromProperty(propertyId, imageId, imageRequestDTO);
        return ResponseEntity.status(HttpStatus.OK).body("Image updated successfully");
    }
}
