package org.example.rentify.mapper;

import org.example.rentify.dto.request.ReviewRequestDTO;
import org.example.rentify.dto.response.ReviewResponseDTO;
import org.example.rentify.entity.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/*
* ReviewMapper interface for converting between Review entities and DTOs.
* This interface uses MapStruct to generate the implementation code.
 */
@Mapper(componentModel = "spring",
        uses = {UserMapper.class, PropertyMapper.class, BookingMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReviewMapper {

    /**
     * Converts a Review entity to a ReviewResponseDTO.
     *
     * @param review Review entity to convert
     * @return the converted ReviewResponseDTO
     */
    @Mapping(target = "user", source = "user")
    @Mapping(target = "property", source = "property")
    @Mapping(target = "booking", source = "booking")
    ReviewResponseDTO toDto(Review review);

    /**
     * Converts a list of Review entities to a list of ReviewResponseDTOs.
     *
     * @param reviews the list of Review entities to convert
     * @return the converted list of ReviewResponseDTOs
     */
    List<ReviewResponseDTO> toDtoList(List<Review> reviews);

    /**
     * Converts a ReviewRequestDTO to a Review entity.
     *
     * @param reviewRequestDTO the ReviewRequestDTO to convert
     * @return the converted Review entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "reviewDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "booking", ignore = true)
    Review toEntity(ReviewRequestDTO reviewRequestDTO);
}
