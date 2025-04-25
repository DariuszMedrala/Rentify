package org.example.rentify.mapper;

import org.example.rentify.dto.request.ImageRequestDTO;
import org.example.rentify.dto.response.ImageResponseDTO;
import org.example.rentify.entity.Image;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/*
* ImageMapper interface for converting between Image entities and DTOs.
* This interface uses MapStruct to generate the implementation code.
 */
@Mapper(componentModel = "spring",
        uses = {PropertyMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImageMapper {

    /**
     * Converts an Image entity to an ImageResponseDTO.
     *
     * @param image the Image entity to convert
     * @return the converted ImageResponseDTO
     */
    @Mapping(target = "property", source = "property")
    ImageResponseDTO toDto(Image image);

    /**
     * Converts a list of Image entities to a list of ImageResponseDTOs.
     *
     * @param images the list of Image entities to convert
     * @return the list of converted ImageResponseDTOs
     */
    List<ImageResponseDTO> toDtoList(List<Image> images);

    /**
     * Converts an ImageRequestDTO to an Image entity.
     *
     * @param imageRequestDTO the ImageRequestDTO to convert
     * @return the converted Image entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "uploadDate", expression = "java(java.time.LocalDateTime.now())")
    Image toEntity(ImageRequestDTO imageRequestDTO);
}