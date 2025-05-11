package org.example.rentify.mapper;

import org.example.rentify.dto.request.ReviewRequestDTO;
import org.example.rentify.dto.response.ReviewResponseDTO;
import org.example.rentify.entity.Review;
import org.mapstruct.*;

/**
 * ReviewMapper interface for mapping between Review entity and DTOs.
 * Uses MapStruct for automatic implementation generation.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {UserMapper.class, PropertyMapper.class, BookingMapper.class})
public interface ReviewMapper {

    /**
     * Converts a ReviewRequestDTO to a Review entity.
     *
     * @param reviewRequestDTO the ReviewRequestDTO to convert
     * @return the converted Review entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "reviewDate", ignore = true)
    Review reviewRequestDtoToReview(ReviewRequestDTO reviewRequestDTO);

    /**
     * Maps a Review entity to a ReviewResponseDTO.
     *
     * @param review Review entity to convert
     * @return the converted ReviewResponseDTO
     */
    @Mapping(target = "propertyId", source = "property.id")
    @Mapping(target = "bookingId", source = "booking.id")
    ReviewResponseDTO reviewToReviewResponseDto(Review review);

    /**
     * Updates an existing Review entity with data from a ReviewRequestDTO.
     *
     * @param reviewRequestDTO the DTO containing update data
     * @param review Review entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "reviewDate", ignore = true)
    void updateReviewFromDto(ReviewRequestDTO reviewRequestDTO, @MappingTarget Review review);
}