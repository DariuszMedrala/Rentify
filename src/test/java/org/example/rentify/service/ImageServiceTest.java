package org.example.rentify.service;

import org.example.rentify.dto.request.ImageRequestDTO;
import org.example.rentify.dto.response.ImageResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.entity.Image;
import org.example.rentify.entity.Property;
import org.example.rentify.mapper.ImageMapper;
import org.example.rentify.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageService Unit Tests")
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private PropertyService propertyService;

    @InjectMocks
    private ImageService imageService;

    private Property property;
    private Image image;
    private ImageRequestDTO imageRequestDTO;
    private ImageResponseDTO imageResponseDTO;

    @BeforeEach
    void setUp() {
        property = new Property();
        property.setId(1L);

        image = new Image();
        image.setId(1L);
        image.setImageUrl("example.com/image.jpg");
        image.setProperty(property);
        image.setUploadDate(LocalDateTime.now());

        imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageUrl("example.com/new_image.jpg");

        imageResponseDTO = new ImageResponseDTO();
        imageResponseDTO.setId(1L);
        imageResponseDTO.setImageUrl("example.com/image.jpg");
        imageResponseDTO.setUploadDate(image.getUploadDate());
    }

    @Nested
    @DisplayName("addImageToProperty Tests")
    class AddImageToPropertyTests {

        @Test
        @DisplayName("Should add image to property successfully")
        void addImageToProperty_whenValidInput_shouldReturnSuccessMessage() {

            when(propertyService.getPropertyEntityById(1L)).thenReturn(property);
            when(imageMapper.imageRequestDtoToImage(imageRequestDTO)).thenReturn(image);
            when(imageRepository.save(any(Image.class))).thenReturn(image);

            MessageResponseDTO response = imageService.addImageToProperty(1L, imageRequestDTO);

            assertNotNull(response);
            assertEquals("Image successfully added to property with ID 1", response.getMessage());
            verify(propertyService).getPropertyEntityById(1L);
            verify(imageMapper).imageRequestDtoToImage(imageRequestDTO);
            verify(imageRepository).save(image);
            assertNotNull(image.getUploadDate());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when propertyId is null")
        void addImageToProperty_whenPropertyIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.addImageToProperty(null, imageRequestDTO));
            assertEquals("Property ID and Image Request DTO must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when imageRequestDTO is null")
        void addImageToProperty_whenImageRequestDTOIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.addImageToProperty(1L, null));
            assertEquals("Property ID and Image Request DTO must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when propertyId is negative")
        void addImageToProperty_whenPropertyIdIsNegative_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.addImageToProperty(-1L, imageRequestDTO));
            assertEquals("Property ID and Image Request DTO must not be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found")
        void addImageToProperty_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.addImageToProperty(1L, imageRequestDTO));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("getAllImagesByPropertyId Tests")
    class GetAllImagesByPropertyIdTests {

        @Test
        @DisplayName("Should return list of images when property has images")
        void getAllImagesByPropertyId_whenPropertyHasImages_shouldReturnImageResponseDTOList() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(property);
            when(imageRepository.findByPropertyId(1L)).thenReturn(List.of(image));
            when(imageMapper.imageToImageResponseDto(image)).thenReturn(imageResponseDTO);

            List<ImageResponseDTO> responses = imageService.getAllImagesByPropertyId(1L);

            assertNotNull(responses);
            assertFalse(responses.isEmpty());
            assertEquals(1, responses.size());
            assertEquals(imageResponseDTO, responses.getFirst());
            verify(propertyService).getPropertyEntityById(1L);
            verify(imageRepository).findByPropertyId(1L);
            verify(imageMapper).imageToImageResponseDto(image);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when propertyId is null")
        void getAllImagesByPropertyId_whenPropertyIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.getAllImagesByPropertyId(null));
            assertEquals("Property ID must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when propertyId is zero")
        void getAllImagesByPropertyId_whenPropertyIdIsZero_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.getAllImagesByPropertyId(0L));
            assertEquals("Property ID must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when propertyId is negative")
        void getAllImagesByPropertyId_whenPropertyIdIsNegative_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.getAllImagesByPropertyId(-1L));
            assertEquals("Property ID must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found")
        void getAllImagesByPropertyId_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(null);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.getAllImagesByPropertyId(1L));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found via explicit throw from service")
        void getAllImagesByPropertyId_whenPropertyServiceThrowsNotFound_shouldPropagateResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found from service"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.getAllImagesByPropertyId(1L));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found from service", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no images found for property")
        void getAllImagesByPropertyId_whenNoImagesFound_shouldThrowResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(property);
            when(imageRepository.findByPropertyId(1L)).thenReturn(Collections.emptyList());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.getAllImagesByPropertyId(1L));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No images found for this property", exception.getReason());
        }
    }

    @Nested
    @DisplayName("deleteImageFromProperty Tests")
    class DeleteImageFromPropertyTests {

        @Test
        @DisplayName("Should delete image successfully")
        void deleteImageFromProperty_whenValidIds_shouldReturnSuccessMessage() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(property);
            when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
            doNothing().when(imageRepository).delete(image);

            MessageResponseDTO response = imageService.deleteImageFromProperty(1L, 1L);

            assertNotNull(response);
            assertEquals("Image with ID 1 successfully deleted from property with ID 1", response.getMessage());
            verify(propertyService).getPropertyEntityById(1L);
            verify(imageRepository).findById(1L);
            verify(imageRepository).delete(image);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null propertyId")
        void deleteImageFromProperty_whenPropertyIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.deleteImageFromProperty(null, 1L));
            assertEquals("Property ID and Image ID must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for zero imageId")
        void deleteImageFromProperty_whenImageIdIsZero_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.deleteImageFromProperty(1L, 0L));
            assertEquals("Property ID and Image ID must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for negative propertyId")
        void deleteImageFromProperty_whenPropertyIdIsNegative_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.deleteImageFromProperty(-1L, 1L));
            assertEquals("Property ID and Image ID must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found")
        void deleteImageFromProperty_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(null);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.deleteImageFromProperty(1L, 1L));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property service throws not found")
        void deleteImageFromProperty_whenPropertyServiceThrowsNotFound_shouldPropagateResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found from service"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.deleteImageFromProperty(1L, 1L));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found from service", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when image not found")
        void deleteImageFromProperty_whenImageNotFound_shouldThrowResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(property);
            when(imageRepository.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.deleteImageFromProperty(1L, 1L));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Image not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("updatesImageFromProperty Tests")
    class UpdatesImageFromPropertyTests {

        @Test
        @DisplayName("Should update image successfully")
        void updatesImageFromProperty_whenValidInput_shouldReturnSuccessMessage() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(property);
            when(imageRepository.findById(1L)).thenReturn(Optional.of(image));
            doNothing().when(imageMapper).updateImageFromDto(imageRequestDTO, image);
            when(imageRepository.save(any(Image.class))).thenReturn(image);
            LocalDateTime beforeUpdate = LocalDateTime.now().minusSeconds(1);

            MessageResponseDTO response = imageService.updatesImageFromProperty(1L, 1L, imageRequestDTO);

            assertNotNull(response);
            assertEquals("Image with ID 1 successfully updated for property with ID 1", response.getMessage());
            verify(propertyService).getPropertyEntityById(1L);
            verify(imageRepository).findById(1L);
            verify(imageMapper).updateImageFromDto(imageRequestDTO, image);
            verify(imageRepository).save(image);
            assertNotNull(image.getUploadDate());
            assertTrue(image.getUploadDate().isAfter(beforeUpdate) || image.getUploadDate().isEqual(beforeUpdate));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null propertyId")
        void updatesImageFromProperty_whenPropertyIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.updatesImageFromProperty(null, 1L, imageRequestDTO));
            assertEquals("Property ID, Image ID and Image Request DTO must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null imageRequestDTO")
        void updatesImageFromProperty_whenImageRequestDTOIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.updatesImageFromProperty(1L, 1L, null));
            assertEquals("Property ID, Image ID and Image Request DTO must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for negative imageId")
        void updatesImageFromProperty_whenImageIdIsNegative_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> imageService.updatesImageFromProperty(1L, -1L, imageRequestDTO));
            assertEquals("Property ID, Image ID and Image Request DTO must not be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found")
        void updatesImageFromProperty_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(null);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.updatesImageFromProperty(1L, 1L, imageRequestDTO));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property service throws not found")
        void updatesImageFromProperty_whenPropertyServiceThrowsNotFound_shouldPropagateResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found from service"));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.updatesImageFromProperty(1L, 1L, imageRequestDTO));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found from service", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when image not found")
        void updatesImageFromProperty_whenImageNotFound_shouldThrowResponseStatusException() {
            when(propertyService.getPropertyEntityById(1L)).thenReturn(property);
            when(imageRepository.findById(1L)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> imageService.updatesImageFromProperty(1L, 1L, imageRequestDTO));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Image not found", exception.getReason());
        }
    }
}