package org.example.rentify.mapper;

import org.example.rentify.dto.request.ImageRequestDTO;
import org.example.rentify.dto.response.ImageResponseDTO;
import org.example.rentify.entity.Image;
import org.mapstruct.*;

/**
 * ImageMapper interface for mapping between Image entity and DTOs.
 * Uses MapStruct for automatic implementation generation.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {PropertyMapper.class}
)
public interface ImageMapper {

    /**
     * Converts an ImageRequestDTO to an Image entity.
     *
     * @param imageRequestDTO the ImageRequestDTO to convert
     * @return the converted Image entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "uploadDate", expression = "java(java.time.LocalDateTime.now())")
    Image imageRequestDtoToImage(ImageRequestDTO imageRequestDTO);

    /**
     * Converts an Image entity to an ImageResponseDTO.
     *
     * @param image the Image entity to convert
     * @return the converted ImageResponseDTO
     */
    ImageResponseDTO imageToImageResponseDto(Image image);

    /**
     * Updates an existing Image entity with data from an ImageRequestDTO.
     *
     * @param imageRequestDTO the DTO containing update data
     * @param image the Image entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "uploadDate", ignore = true)
    void updateImageFromDto(ImageRequestDTO imageRequestDTO, @MappingTarget Image image);
}