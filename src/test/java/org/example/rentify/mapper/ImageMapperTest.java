package org.example.rentify.mapper;

import org.example.rentify.dto.request.ImageRequestDTO;
import org.example.rentify.dto.response.ImageResponseDTO;
import org.example.rentify.entity.Image;
import org.example.rentify.entity.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ImageMapper Unit Tests")
class ImageMapperTest {

    private ImageMapper imageMapper;

    private ImageRequestDTO imageRequestDTO;
    private Image imageEntity;

    @BeforeEach
    void setUp() {
        imageMapper = Mappers.getMapper(ImageMapper.class);

        Property property = new Property();
        property.setId(1L);

        imageRequestDTO = new ImageRequestDTO();
        imageRequestDTO.setImageUrl("example.com/new_image.jpg");
        imageRequestDTO.setDescription("New beautiful view");

        imageEntity = new Image();
        imageEntity.setId(1L);
        imageEntity.setImageUrl("example.com/image.jpg");
        imageEntity.setDescription("Beautiful view");
        imageEntity.setUploadDate(LocalDateTime.now().minusDays(1));
        imageEntity.setProperty(property);
    }

    @Nested
    @DisplayName("imageRequestDtoToImage Tests")
    class ImageRequestDtoToImageTests {

        @Test
        @DisplayName("Should map ImageRequestDTO to Image entity correctly")
        void shouldMapDtoToEntity() {
            Image mappedImage = imageMapper.imageRequestDtoToImage(imageRequestDTO);

            assertNotNull(mappedImage);
            assertEquals(imageRequestDTO.getImageUrl(), mappedImage.getImageUrl());
            assertEquals(imageRequestDTO.getDescription(), mappedImage.getDescription());

            assertNull(mappedImage.getId(), "ID should be ignored");
            assertNull(mappedImage.getProperty(), "Property should be ignored");
            assertNull(mappedImage.getUploadDate(), "UploadDate should be ignored");
        }

        @Test
        @DisplayName("Should handle null ImageRequestDTO gracefully")
        void shouldHandleNullDto() {
            Image mappedImage = imageMapper.imageRequestDtoToImage(null);
            assertNull(mappedImage, "Mapping a null DTO should result in a null entity");
        }

        @Test
        @DisplayName("Should map DTO with null fields to entity with null fields")
        void shouldMapDtoWithNullFields() {
            ImageRequestDTO dtoWithNulls = new ImageRequestDTO();

            Image mappedImage = imageMapper.imageRequestDtoToImage(dtoWithNulls);

            assertNotNull(mappedImage);
            assertNull(mappedImage.getImageUrl());
            assertNull(mappedImage.getDescription());
        }
    }

    @Nested
    @DisplayName("imageToImageResponseDto Tests")
    class ImageToImageResponseDtoTests {

        @Test
        @DisplayName("Should map Image entity to ImageResponseDTO correctly")
        void shouldMapEntityToDto() {
            ImageResponseDTO mappedDto = imageMapper.imageToImageResponseDto(imageEntity);

            assertNotNull(mappedDto);
            assertEquals(imageEntity.getId(), mappedDto.getId());
            assertEquals(imageEntity.getImageUrl(), mappedDto.getImageUrl());
            assertEquals(imageEntity.getDescription(), mappedDto.getDescription());
            assertEquals(imageEntity.getUploadDate(), mappedDto.getUploadDate());
        }

        @Test
        @DisplayName("Should handle null Image entity gracefully")
        void shouldHandleNullEntity() {
            ImageResponseDTO mappedDto = imageMapper.imageToImageResponseDto(null);
            assertNull(mappedDto, "Mapping a null entity should result in a null DTO");
        }

        @Test
        @DisplayName("Should map entity with null fields to DTO with null fields")
        void shouldMapEntityWithNullFields() {
            Image entityWithNulls = new Image();
            entityWithNulls.setId(5L);

            ImageResponseDTO mappedDto = imageMapper.imageToImageResponseDto(entityWithNulls);

            assertNotNull(mappedDto);
            assertEquals(5L, mappedDto.getId());
            assertNull(mappedDto.getImageUrl());
            assertNull(mappedDto.getDescription());
            assertNull(mappedDto.getUploadDate());
        }
    }

    @Nested
    @DisplayName("updateImageFromDto Tests")
    class UpdateImageFromDtoTests {

        @Test
        @DisplayName("Should update existing Image entity from DTO with non-null DTO fields")
        void shouldUpdateEntityFromDto_NonNullFields() {
            Image targetImage = new Image();
            targetImage.setId(10L);
            targetImage.setImageUrl("example.com/original.jpg");
            targetImage.setDescription("Original description");
            LocalDateTime originalDate = LocalDateTime.now().minusDays(2);
            targetImage.setUploadDate(originalDate);
            Property originalProperty = new Property(); originalProperty.setId(2L);
            targetImage.setProperty(originalProperty);

            imageMapper.updateImageFromDto(imageRequestDTO, targetImage);

            assertEquals(imageRequestDTO.getImageUrl(), targetImage.getImageUrl());
            assertEquals(imageRequestDTO.getDescription(), targetImage.getDescription());

            assertEquals(10L, targetImage.getId(), "ID should not be changed");
            assertEquals(originalProperty, targetImage.getProperty(), "Property should not be changed");
            assertEquals(originalDate, targetImage.getUploadDate(), "UploadDate should not be changed");
        }

        @Test
        @DisplayName("Should ignore null fields from DTO during update due to NullValuePropertyMappingStrategy.IGNORE")
        void shouldIgnoreNullFieldsFromDtoDuringUpdate() {
            Image targetImage = new Image();
            targetImage.setImageUrl("example.com/original_url.jpg");
            targetImage.setDescription("Original Description");

            ImageRequestDTO updateDtoWithNulls = new ImageRequestDTO();
            updateDtoWithNulls.setImageUrl(null);
            updateDtoWithNulls.setDescription("New Description Only");

            imageMapper.updateImageFromDto(updateDtoWithNulls, targetImage);

            assertEquals("example.com/original_url.jpg", targetImage.getImageUrl(), "ImageUrl should not be updated for null DTO field");
            assertEquals("New Description Only", targetImage.getDescription(), "Description should be updated");
        }
    }
}