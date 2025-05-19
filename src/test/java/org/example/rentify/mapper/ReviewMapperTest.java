package org.example.rentify.mapper;

import org.example.rentify.dto.request.ReviewRequestDTO;
import org.example.rentify.dto.response.ReviewResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.Review;
import org.example.rentify.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReviewMapper Unit Tests")
class ReviewMapperTest {

    private ReviewMapper reviewMapper;

    private ReviewRequestDTO reviewRequestDTO;
    private Review reviewEntity;

    @BeforeEach
    void setUp() {
        reviewMapper = Mappers.getMapper(ReviewMapper.class);

        User user = new User();
        user.setId(1L);

        Property property = new Property();
        property.setId(2L);

        Booking booking = new Booking();
        booking.setId(3L);

        reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setRating(5);
        reviewRequestDTO.setComment("Excellent experience!");

        reviewEntity = new Review();
        reviewEntity.setId(10L);
        reviewEntity.setUser(user);
        reviewEntity.setProperty(property);
        reviewEntity.setBooking(booking);
        reviewEntity.setRating(4);
        reviewEntity.setComment("Very good.");
        reviewEntity.setReviewDate(LocalDateTime.now().minusDays(1));
    }

    @Nested
    @DisplayName("reviewRequestDtoToReview Tests")
    class ReviewRequestDtoToReviewTests {

        @Test
        @DisplayName("Should map ReviewRequestDTO to Review entity correctly")
        void shouldMapDtoToEntity() {
            Review mappedReview = reviewMapper.reviewRequestDtoToReview(reviewRequestDTO);

            assertNotNull(mappedReview);
            assertEquals(reviewRequestDTO.getRating(), mappedReview.getRating());
            assertEquals(reviewRequestDTO.getComment(), mappedReview.getComment());

            assertNull(mappedReview.getId(), "ID should be ignored");
            assertNull(mappedReview.getUser(), "User should be ignored");
            assertNull(mappedReview.getProperty(), "Property should be ignored");
            assertNull(mappedReview.getBooking(), "Booking should be ignored");
            assertNull(mappedReview.getReviewDate(), "ReviewDate should be ignored");
        }

        @Test
        @DisplayName("Should handle null ReviewRequestDTO gracefully")
        void shouldHandleNullDto() {
            Review mappedReview = reviewMapper.reviewRequestDtoToReview(null);
            assertNull(mappedReview, "Mapping a null DTO should result in a null entity");
        }

        @Test
        @DisplayName("Should map DTO with null comment to entity with null comment")
        void shouldMapDtoWithNullComment() {
            ReviewRequestDTO dtoWithNullComment = new ReviewRequestDTO();
            dtoWithNullComment.setRating(3);

            Review mappedReview = reviewMapper.reviewRequestDtoToReview(dtoWithNullComment);

            assertNotNull(mappedReview);
            assertEquals(3, mappedReview.getRating());
            assertNull(mappedReview.getComment());
        }
    }

    @Nested
    @DisplayName("reviewToReviewResponseDto Tests")
    class ReviewToReviewResponseDtoTests {

        @Test
        @DisplayName("Should map Review entity to ReviewResponseDTO correctly")
        void shouldMapEntityToDto() {
            ReviewResponseDTO mappedDto = reviewMapper.reviewToReviewResponseDto(reviewEntity);

            assertNotNull(mappedDto);
            assertEquals(reviewEntity.getId(), mappedDto.getId());
            assertEquals(reviewEntity.getProperty().getId(), mappedDto.getPropertyId());
            assertEquals(reviewEntity.getBooking().getId(), mappedDto.getBookingId());
            assertEquals(reviewEntity.getRating(), mappedDto.getRating());
            assertEquals(reviewEntity.getComment(), mappedDto.getComment());
            assertEquals(reviewEntity.getReviewDate(), mappedDto.getReviewDate());
        }

        @Test
        @DisplayName("Should handle null Review entity gracefully")
        void shouldHandleNullEntity() {
            ReviewResponseDTO mappedDto = reviewMapper.reviewToReviewResponseDto(null);
            assertNull(mappedDto, "Mapping a null entity should result in a null DTO");
        }

        @Test
        @DisplayName("Should map entity with null comment to DTO with null comment")
        void shouldMapEntityWithNullComment() {
            reviewEntity.setComment(null);
            ReviewResponseDTO mappedDto = reviewMapper.reviewToReviewResponseDto(reviewEntity);

            assertNotNull(mappedDto);
            assertNull(mappedDto.getComment());
        }

        @Test
        @DisplayName("Should map entity with null property to DTO with null propertyId")
        void shouldMapEntityWithNullProperty() {
            reviewEntity.setProperty(null);
            ReviewResponseDTO mappedDto = reviewMapper.reviewToReviewResponseDto(reviewEntity);
            assertNotNull(mappedDto);
            assertNull(mappedDto.getPropertyId(), "PropertyId should be null if property is null, due to NPE guard in MapStruct");
        }


        @Test
        @DisplayName("Should map entity with null booking to DTO with null bookingId")
        void shouldMapEntityWithNullBooking() {
            reviewEntity.setBooking(null);
            ReviewResponseDTO mappedDto = reviewMapper.reviewToReviewResponseDto(reviewEntity);
            assertNotNull(mappedDto);
            assertNull(mappedDto.getBookingId(), "BookingId should be null if booking is null, due to NPE guard in MapStruct");
        }
    }

    @Nested
    @DisplayName("updateReviewFromDto Tests")
    class UpdateReviewFromDtoTests {

        @Test
        @DisplayName("Should update existing Review entity from DTO with non-null DTO fields")
        void shouldUpdateEntityFromDto_NonNullFields() {
            Review targetReview = new Review();
            targetReview.setId(20L);
            targetReview.setRating(1);
            targetReview.setComment("Old comment");
            User originalUser = new User(); originalUser.setId(5L);
            targetReview.setUser(originalUser);
            Property originalProperty = new Property(); originalProperty.setId(6L);
            targetReview.setProperty(originalProperty);
            Booking originalBooking = new Booking(); originalBooking.setId(7L);
            targetReview.setBooking(originalBooking);
            LocalDateTime originalDate = LocalDateTime.now().minusHours(5);
            targetReview.setReviewDate(originalDate);


            reviewMapper.updateReviewFromDto(reviewRequestDTO, targetReview);

            assertEquals(reviewRequestDTO.getRating(), targetReview.getRating());
            assertEquals(reviewRequestDTO.getComment(), targetReview.getComment());

            assertEquals(20L, targetReview.getId(), "ID should not be changed");
            assertEquals(originalUser, targetReview.getUser(), "User should not be changed");
            assertEquals(originalProperty, targetReview.getProperty(), "Property should not be changed");
            assertEquals(originalBooking, targetReview.getBooking(), "Booking should not be changed");
            assertEquals(originalDate, targetReview.getReviewDate(), "ReviewDate should not be changed");
        }

        @Test
        @DisplayName("Should ignore null comment from DTO during update")
        void shouldIgnoreNullCommentFromDtoDuringUpdate() {
            Review targetReview = new Review();
            targetReview.setRating(2);
            targetReview.setComment("ORIGINAL_COMMENT");

            ReviewRequestDTO updateDtoWithNullComment = new ReviewRequestDTO();
            updateDtoWithNullComment.setRating(4);
            updateDtoWithNullComment.setComment(null);


            reviewMapper.updateReviewFromDto(updateDtoWithNullComment, targetReview);

            assertEquals(4, targetReview.getRating(), "Rating should be updated");
            assertEquals("ORIGINAL_COMMENT", targetReview.getComment(), "Comment should not be updated due to null in DTO");
        }
    }
}