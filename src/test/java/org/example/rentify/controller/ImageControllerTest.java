package org.example.rentify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentify.dto.request.ImageRequestDTO;
import org.example.rentify.dto.response.ImageResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.entity.Property;
import org.example.rentify.service.ImageService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllerTestConfig.class)
@WebMvcTest(ImageController.class)
@DisplayName("ImageController Integration Tests")
public class ImageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ImageService imageService;

    @Autowired
    private PropertyService propertyService;

    private ImageRequestDTO imageRequestDTO;
    private ImageResponseDTO imageResponseDTO;
    private final Long testPropertyId = 1L;
    private final Long testImageId = 10L;
    private final String ownerUsername = "ownerUser";
    private final String otherUsername = "otherUser";
    private final String adminUsername = "adminUser";


    @BeforeEach
    void setUp() {
        imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageUrl("http://example.com/image.jpg");
        imageRequestDTO.setDescription("A beautiful landscape");

        imageResponseDTO = new ImageResponseDTO();
        imageResponseDTO.setId(testImageId);
        imageResponseDTO.setImageUrl("example.com/image.jpg");
        imageResponseDTO.setDescription("A beautiful landscape");
        imageResponseDTO.setUploadDate(LocalDateTime.now());

        Property mockProperty = new Property();
        mockProperty.setId(testPropertyId);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(imageService, propertyService);
    }

    @Nested
    @DisplayName("POST /api/properties/{propertyId}/image/add")
    class AddImageToPropertyTests {

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should allow ADMIN to add image and return 200 OK")
        void whenAddImageAsAdmin_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Image successfully added to property with ID " + testPropertyId);
            when(imageService.addImageToProperty(eq(testPropertyId), any(ImageRequestDTO.class)))
                    .thenReturn(successResponse);

            mockMvc.perform(post("/api/properties/{propertyId}/image/add", testPropertyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(imageRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = ownerUsername, roles = "USER")
        @DisplayName("should allow property OWNER to add image and return 200 OK")
        void whenAddImageAsOwner_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Image successfully added to property with ID " + testPropertyId);
            when(propertyService.isOwner(eq(testPropertyId), eq(ownerUsername))).thenReturn(true);
            when(imageService.addImageToProperty(eq(testPropertyId), any(ImageRequestDTO.class)))
                    .thenReturn(successResponse);

            mockMvc.perform(post("/api/properties/{propertyId}/image/add", testPropertyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(imageRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = otherUsername, roles = "USER")
        @DisplayName("should return 403 Forbidden when non-owner/non-ADMIN tries to add image")
        void whenAddImageAsNonOwner_thenReturns403() throws Exception {
            when(propertyService.isOwner(eq(testPropertyId), eq(otherUsername))).thenReturn(false);

            mockMvc.perform(post("/api/properties/{propertyId}/image/add", testPropertyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(imageRequestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden when anonymous user tries to add image")
        void whenAddImageAsAnonymous_thenReturns403() throws Exception {
            mockMvc.perform(post("/api/properties/{propertyId}/image/add", testPropertyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(imageRequestDTO)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for invalid ImageRequestDTO")
        void whenAddImageWithInvalidDto_thenReturns400() throws Exception {
            ImageRequestDTO invalidDto = new ImageRequestDTO();
            invalidDto.setImageUrl(null);
            invalidDto.setDescription("Test Description");

            mockMvc.perform(post("/api/properties/{propertyId}/image/add", testPropertyId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value("Validation Error: imageUrl: Image URL cannot be blank"));
        }
    }

    @Nested
    @DisplayName("GET /api/properties/{propertyId}/image/all")
    class GetAllImagesForPropertyTests {
        @Test
        @DisplayName("should return 200 OK and list of images for public access")
        void whenGetAllImages_thenSucceeds() throws Exception {
            when(imageService.getAllImagesByPropertyId(testPropertyId)).thenReturn(List.of(imageResponseDTO));

            mockMvc.perform(get("/api/properties/{propertyId}/image/all", testPropertyId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(imageResponseDTO.getId()))
                    .andExpect(jsonPath("$[0].imageUrl").value(imageResponseDTO.getImageUrl()));
        }

        @Test
        @DisplayName("should return 404 Not Found if property does not exist (service throws)")
        void whenGetAllImagesForNonExistentProperty_thenReturns404() throws Exception {
            String errorMessage = "Property not found";
            Long nonExistentPropertyId = 999L;
            when(imageService.getAllImagesByPropertyId(nonExistentPropertyId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(get("/api/properties/{propertyId}/image/all", nonExistentPropertyId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @DisplayName("should return 404 Not Found if no images exist for property (service throws)")
        void whenGetAllImagesAndNoneExist_thenReturns404() throws Exception {
            String errorMessage = "No images found for this property";
            when(imageService.getAllImagesByPropertyId(testPropertyId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));


            mockMvc.perform(get("/api/properties/{propertyId}/image/all", testPropertyId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("DELETE /api/properties/{propertyId}/image/delete/{imageId}")
    class DeleteImageFromPropertyTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should allow ADMIN to delete image and return 200 OK")
        void whenDeleteImageAsAdmin_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Image with ID " + testImageId + " successfully deleted from property with ID " + testPropertyId);
            when(imageService.deleteImageFromProperty(testPropertyId, testImageId)).thenReturn(successResponse);

            mockMvc.perform(delete("/api/properties/{propertyId}/image/delete/{imageId}", testPropertyId, testImageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = ownerUsername, roles = "USER")
        @DisplayName("should allow property OWNER to delete image and return 200 OK")
        void whenDeleteImageAsOwner_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Image with ID " + testImageId + " successfully deleted from property with ID " + testPropertyId);
            when(propertyService.isOwner(eq(testPropertyId), eq(ownerUsername))).thenReturn(true);
            when(imageService.deleteImageFromProperty(testPropertyId, testImageId)).thenReturn(successResponse);

            mockMvc.perform(delete("/api/properties/{propertyId}/image/delete/{imageId}", testPropertyId, testImageId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = otherUsername, roles = "USER")
        @DisplayName("should return 403 Forbidden for non-owner/non-ADMIN")
        void whenDeleteImageAsNonOwner_thenReturns403() throws Exception {
            when(propertyService.isOwner(eq(testPropertyId), eq(otherUsername))).thenReturn(false);


            mockMvc.perform(delete("/api/properties/{propertyId}/image/delete/{imageId}", testPropertyId, testImageId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("PUT /api/properties/{propertyId}/image/update/{imageId}")
    class UpdatesImageFromPropertyTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should allow ADMIN to update image and return 200 OK")
        void whenUpdateImageAsAdmin_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Image with ID " + testImageId + " successfully updated for property with ID " + testPropertyId);
            when(imageService.updatesImageFromProperty(eq(testPropertyId), eq(testImageId), any(ImageRequestDTO.class)))
                    .thenReturn(successResponse);

            mockMvc.perform(put("/api/properties/{propertyId}/image/update/{imageId}", testPropertyId, testImageId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(imageRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = ownerUsername, roles = "USER")
        @DisplayName("should allow property OWNER to update image and return 200 OK")
        void whenUpdateImageAsOwner_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Image with ID " + testImageId + " successfully updated for property with ID " + testPropertyId);
            when(propertyService.isOwner(eq(testPropertyId), eq(ownerUsername))).thenReturn(true);
            when(imageService.updatesImageFromProperty(eq(testPropertyId), eq(testImageId), any(ImageRequestDTO.class)))
                    .thenReturn(successResponse);

            mockMvc.perform(put("/api/properties/{propertyId}/image/update/{imageId}", testPropertyId, testImageId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(imageRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
    }
}