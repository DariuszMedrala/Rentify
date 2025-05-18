package org.example.rentify.service;

import org.example.rentify.dto.request.ReviewRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.ReviewResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.Review;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.BookingStatus;
import org.example.rentify.mapper.ReviewMapper;
import org.example.rentify.repository.BookingRepository;
import org.example.rentify.repository.PropertyRepository;
import org.example.rentify.repository.ReviewRepository;
import org.example.rentify.repository.UserRepository;
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
@DisplayName("ReviewService Unit Tests")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ReviewMapper reviewMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PropertyRepository propertyRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Property property;
    private Booking booking;
    private Review review;
    private ReviewRequestDTO reviewRequestDTO;
    private ReviewResponseDTO reviewResponseDTO;
    private Long bookingId;
    private Long reviewId;
    private Long propertyId;
    private String username;

    @BeforeEach
    void setUp() {
        bookingId = 1L;
        reviewId = 1L;
        propertyId = 1L;
        username = "testUser";

        user = new User();
        user.setId(1L);
        user.setUsername(username);

        property = new Property();
        property.setId(propertyId);

        booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setProperty(property);
        booking.setBookingStatus(BookingStatus.COMPLETED);

        review = new Review();
        review.setId(reviewId);
        review.setBooking(booking);
        review.setUser(user);
        review.setProperty(property);
        review.setComment("Great place!");
        review.setRating(5);
        review.setReviewDate(LocalDateTime.now().minusDays(1));

        reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setComment("Excellent stay!");
        reviewRequestDTO.setRating(5);

        reviewResponseDTO = new ReviewResponseDTO();
        reviewResponseDTO.setId(reviewId);
        reviewResponseDTO.setComment(review.getComment());
        reviewResponseDTO.setRating(review.getRating());
        reviewResponseDTO.setReviewDate(review.getReviewDate());
        reviewResponseDTO.setPropertyId(property.getId());
        reviewResponseDTO.setBookingId(booking.getId());
    }

    @Nested
    @DisplayName("getReviewByBookingId Tests")
    class GetReviewByBookingIdTests {

        @Test
        @DisplayName("Should return review when booking and review exist")
        void getReviewByBookingId_whenBookingAndReviewExist_shouldReturnReviewResponseDTO() {
            booking.setReview(review);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(reviewRepository.findByBookingId(bookingId)).thenReturn(review);
            when(reviewMapper.reviewToReviewResponseDto(review)).thenReturn(reviewResponseDTO);

            ReviewResponseDTO result = reviewService.getReviewByBookingId(bookingId);

            assertNotNull(result);
            assertEquals(reviewResponseDTO.getId(), result.getId());
            verify(bookingRepository).findById(bookingId);
            verify(reviewRepository).findByBookingId(bookingId);
            verify(reviewMapper).reviewToReviewResponseDto(review);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null bookingId")
        void getReviewByBookingId_whenBookingIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.getReviewByBookingId(null));
            assertEquals("Booking ID and Username cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for non-positive bookingId")
        void getReviewByBookingId_whenBookingIdIsNonPositive_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.getReviewByBookingId(0L));
            assertEquals("Booking ID and Username cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when booking not found")
        void getReviewByBookingId_whenBookingNotFound_shouldThrowResponseStatusException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.getReviewByBookingId(bookingId));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Booking not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no review found for booking")
        void getReviewByBookingId_whenNoReviewForBooking_shouldThrowResponseStatusException() {
            booking.setReview(null);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.getReviewByBookingId(bookingId));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No reviews found for this booking", exception.getReason());
        }
    }

    @Nested
    @DisplayName("getAllReviewsByUser Tests")
    class GetAllReviewsByUserTests {
        @Test
        @DisplayName("Should return list of reviews for existing user with reviews")
        void getAllReviewsByUser_whenUserExistsAndHasReviews_shouldReturnReviewResponseDTOList() {
            when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
            when(reviewRepository.findByUserId(user.getId())).thenReturn(List.of(review));
            when(reviewMapper.reviewToReviewResponseDto(review)).thenReturn(reviewResponseDTO);

            List<ReviewResponseDTO> results = reviewService.getAllReviewsByUser(username);

            assertNotNull(results);
            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            assertEquals(reviewResponseDTO, results.getFirst());
            verify(userRepository).findUserByUsername(username);
            verify(reviewRepository).findByUserId(user.getId());
            verify(reviewMapper).reviewToReviewResponseDto(review);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found")
        void getAllReviewsByUser_whenUserNotFound_shouldThrowResponseStatusException() {
            when(userRepository.findUserByUsername(username)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.getAllReviewsByUser(username));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("User not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user has no reviews")
        void getAllReviewsByUser_whenUserHasNoReviews_shouldThrowResponseStatusException() {
            when(userRepository.findUserByUsername(username)).thenReturn(Optional.of(user));
            when(reviewRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.getAllReviewsByUser(username));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No reviews found for this user", exception.getReason());
        }
    }

    @Nested
    @DisplayName("getAllReviewsByPropertyId Tests")
    class GetAllReviewsByPropertyIdTests {
        @Test
        @DisplayName("Should return list of reviews for existing property with reviews")
        void getAllReviewsByPropertyId_whenPropertyExistsAndHasReviews_shouldReturnReviewResponseDTOList() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(reviewRepository.findByPropertyId(propertyId)).thenReturn(List.of(review));
            when(reviewMapper.reviewToReviewResponseDto(review)).thenReturn(reviewResponseDTO);

            List<ReviewResponseDTO> results = reviewService.getAllReviewsByPropertyId(propertyId);

            assertNotNull(results);
            assertFalse(results.isEmpty());
            assertEquals(1, results.size());
            assertEquals(reviewResponseDTO, results.getFirst());
            verify(propertyRepository).findById(propertyId);
            verify(reviewRepository).findByPropertyId(propertyId);
            verify(reviewMapper).reviewToReviewResponseDto(review);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null propertyId")
        void getAllReviewsByPropertyId_whenPropertyIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.getAllReviewsByPropertyId(null));
            assertEquals("Property ID cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for non-positive propertyId")
        void getAllReviewsByPropertyId_whenPropertyIdIsNonPositive_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.getAllReviewsByPropertyId(0L));
            assertEquals("Property ID cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found")
        void getAllReviewsByPropertyId_whenPropertyNotFound_shouldThrowResponseStatusException() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.getAllReviewsByPropertyId(propertyId));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property has no reviews")
        void getAllReviewsByPropertyId_whenPropertyHasNoReviews_shouldThrowResponseStatusException() {
            when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
            when(reviewRepository.findByPropertyId(propertyId)).thenReturn(Collections.emptyList());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.getAllReviewsByPropertyId(propertyId));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No reviews found for this property", exception.getReason());
        }
    }

    @Nested
    @DisplayName("createReview Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review successfully for completed booking without existing review")
        void createReview_whenValidInputAndBookingCompletedAndNoExistingReview_shouldReturnSuccessMessage() {
            booking.setReview(null);
            booking.setBookingStatus(BookingStatus.COMPLETED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(reviewMapper.reviewRequestDtoToReview(reviewRequestDTO)).thenReturn(review);
            when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));


            MessageResponseDTO response = reviewService.createReview(bookingId, reviewRequestDTO);

            assertNotNull(response);
            assertEquals("Review created successfully for booking with ID " + bookingId + "!", response.getMessage());
            verify(bookingRepository).findById(bookingId);
            verify(reviewMapper).reviewRequestDtoToReview(reviewRequestDTO);
            verify(reviewRepository).save(review);
            assertNotNull(review.getReviewDate());
            assertEquals(booking, review.getBooking());
            assertEquals(user, review.getUser());
            assertEquals(property, review.getProperty());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null bookingId")
        void createReview_whenBookingIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.createReview(null, reviewRequestDTO));
            assertEquals("Booking ID, Review Response DTO cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null reviewRequestDTO")
        void createReview_whenReviewRequestDTOIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.createReview(bookingId, null));
            assertEquals("Booking ID, Review Response DTO cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when booking not found")
        void createReview_whenBookingNotFound_shouldThrowResponseStatusException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.createReview(bookingId, reviewRequestDTO));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Booking not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when review already exists for booking")
        void createReview_whenReviewAlreadyExists_shouldThrowResponseStatusException() {
            booking.setReview(new Review());
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.createReview(bookingId, reviewRequestDTO));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Review already exists for this booking", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when booking is not completed")
        void createReview_whenBookingNotCompleted_shouldThrowResponseStatusException() {
            booking.setReview(null);
            booking.setBookingStatus(BookingStatus.CONFIRMED);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.createReview(bookingId, reviewRequestDTO));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Booking must be completed to create a review", exception.getReason());
        }
    }

    @Nested
    @DisplayName("deleteReview Tests")
    class DeleteReviewTests {
        @Test
        @DisplayName("Should delete review successfully")
        void deleteReview_whenReviewExists_shouldReturnSuccessMessage() {
            booking.setReview(review);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            doNothing().when(reviewRepository).deleteReviewById(reviewId);

            MessageResponseDTO response = reviewService.deleteReview(reviewId);

            assertNotNull(response);
            assertEquals("Review with ID " + reviewId + " deleted successfully!", response.getMessage());
            assertNull(review.getBooking().getReview());
            verify(reviewRepository).findById(reviewId);
            verify(reviewRepository).deleteReviewById(reviewId);
        }

        @Test
        @DisplayName("Should delete review successfully when review has no booking")
        void deleteReview_whenReviewExistsWithoutBooking_shouldReturnSuccessMessage() {
            review.setBooking(null);
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            doNothing().when(reviewRepository).deleteReviewById(reviewId);

            MessageResponseDTO response = reviewService.deleteReview(reviewId);

            assertNotNull(response);
            assertEquals("Review with ID " + reviewId + " deleted successfully!", response.getMessage());
            verify(reviewRepository).findById(reviewId);
            verify(reviewRepository).deleteReviewById(reviewId);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null reviewId")
        void deleteReview_whenReviewIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.deleteReview(null));
            assertEquals("Review ID and cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when review not found")
        void deleteReview_whenReviewNotFound_shouldThrowResponseStatusException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.deleteReview(reviewId));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Review not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("updateReview Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update review successfully")
        void updateReview_whenValidInputAndReviewExists_shouldReturnSuccessMessage() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            doNothing().when(reviewMapper).updateReviewFromDto(reviewRequestDTO, review);
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.reviewToReviewResponseDto(review)).thenReturn(reviewResponseDTO);


            LocalDateTime beforeUpdate = review.getReviewDate();
            MessageResponseDTO response = reviewService.updateReview(reviewId, reviewRequestDTO);

            assertNotNull(response);
            assertEquals("Review with review ID " + reviewId + " updated successfully!", response.getMessage());
            assertTrue(review.getReviewDate().isAfter(beforeUpdate) || review.getReviewDate().isEqual(beforeUpdate));
            verify(reviewRepository).findById(reviewId);
            verify(reviewMapper).updateReviewFromDto(reviewRequestDTO, review);
            verify(reviewRepository).save(review);
            verify(reviewMapper).reviewToReviewResponseDto(review);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null reviewId")
        void updateReview_whenReviewIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.updateReview(null, reviewRequestDTO));
            assertEquals("Review ID, Review Request DTO cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null reviewRequestDTO")
        void updateReview_whenReviewRequestDTOIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.updateReview(reviewId, null));
            assertEquals("Review ID, Review Request DTO cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when review not found")
        void updateReview_whenReviewNotFound_shouldThrowResponseStatusException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.updateReview(reviewId, reviewRequestDTO));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Review not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("updateReviewComment Tests")
    class UpdateReviewCommentTests {
        String newComment = "Updated comment";

        @Test
        @DisplayName("Should update review comment successfully")
        void updateReviewComment_whenValidInput_shouldReturnSuccessMessage() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.reviewToReviewResponseDto(review)).thenReturn(reviewResponseDTO);

            LocalDateTime beforeUpdate = review.getReviewDate();
            MessageResponseDTO response = reviewService.updateReviewComment(reviewId, newComment);

            assertNotNull(response);
            assertEquals("Review description with review ID " + reviewId + " updated to " + newComment + " successfully!", response.getMessage());
            assertEquals(newComment, review.getComment());
            assertTrue(review.getReviewDate().isAfter(beforeUpdate) || review.getReviewDate().isEqual(beforeUpdate));
            verify(reviewRepository).findById(reviewId);
            verify(reviewRepository).save(review);
            verify(reviewMapper).reviewToReviewResponseDto(review);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null reviewId")
        void updateReviewComment_whenReviewIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.updateReviewComment(null, newComment));
            assertEquals("Review ID or comment cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null comment")
        void updateReviewComment_whenCommentIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.updateReviewComment(reviewId, null));
            assertEquals("Review ID or comment cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when review not found")
        void updateReviewComment_whenReviewNotFound_shouldThrowResponseStatusException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.updateReviewComment(reviewId, newComment));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Review not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("updateReviewRating Tests")
    class UpdateReviewRatingTests {
        Integer newRating = 4;

        @Test
        @DisplayName("Should update review rating successfully")
        void updateReviewRating_whenValidInput_shouldReturnSuccessMessage() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));
            when(reviewRepository.save(any(Review.class))).thenReturn(review);
            when(reviewMapper.reviewToReviewResponseDto(review)).thenReturn(reviewResponseDTO);

            LocalDateTime beforeUpdate = review.getReviewDate();
            MessageResponseDTO response = reviewService.updateReviewRating(reviewId, newRating);

            assertNotNull(response);
            assertEquals("Review rating with review ID " + reviewId + " updated to " + newRating + " successfully!", response.getMessage());
            assertEquals(newRating, review.getRating());
            assertTrue(review.getReviewDate().isAfter(beforeUpdate) || review.getReviewDate().isEqual(beforeUpdate));
            verify(reviewRepository).findById(reviewId);
            verify(reviewRepository).save(review);
            verify(reviewMapper).reviewToReviewResponseDto(review);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null reviewId")
        void updateReviewRating_whenReviewIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.updateReviewRating(null, newRating));
            assertEquals("Review ID or rating cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null rating")
        void updateReviewRating_whenRatingIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.updateReviewRating(reviewId, null));
            assertEquals("Review ID or rating cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for non-positive rating")
        void updateReviewRating_whenRatingIsNonPositive_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.updateReviewRating(reviewId, 0));
            assertEquals("Review ID or rating cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when review not found")
        void updateReviewRating_whenReviewNotFound_shouldThrowResponseStatusException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.updateReviewRating(reviewId, newRating));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Review not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("isReviewOwner Tests")
    class IsReviewOwnerTests {

        @Test
        @DisplayName("Should return true when user is owner")
        void isReviewOwner_whenUserIsOwner_shouldReturnTrue() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            boolean result = reviewService.isReviewOwner(reviewId, username);

            assertTrue(result);
            verify(reviewRepository).findById(reviewId);
        }

        @Test
        @DisplayName("Should return false when user is not owner")
        void isReviewOwner_whenUserIsNotOwner_shouldReturnFalse() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

            boolean result = reviewService.isReviewOwner(reviewId, "anotherUser");

            assertFalse(result);
            verify(reviewRepository).findById(reviewId);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null reviewId")
        void isReviewOwner_whenReviewIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.isReviewOwner(null, username));
            assertEquals("Review ID and Username cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null username")
        void isReviewOwner_whenUsernameIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> reviewService.isReviewOwner(reviewId, null));
            assertEquals("Review ID and Username cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when review not found")
        void isReviewOwner_whenReviewNotFound_shouldThrowResponseStatusException() {
            when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> reviewService.isReviewOwner(reviewId, username));

            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Review not found", exception.getReason());
        }
    }
}