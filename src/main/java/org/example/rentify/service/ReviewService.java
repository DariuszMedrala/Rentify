package org.example.rentify.service;

import org.example.rentify.dto.request.ReviewRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.ReviewResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Review;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.BookingStatus;
import org.example.rentify.mapper.ReviewMapper;
import org.example.rentify.repository.BookingRepository;
import org.example.rentify.repository.PropertyRepository;
import org.example.rentify.repository.ReviewRepository;
import org.example.rentify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ReviewService is a service class that handles review-related operations.
 * It provides methods to create, update, delete, and retrieve reviews for bookings.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final ReviewMapper reviewMapper;
    private final UserRepository userRepository;
    private final PropertyRepository propertyRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, BookingRepository bookingRepository, ReviewMapper reviewMapper, UserRepository userRepository, PropertyRepository propertyRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.reviewMapper = reviewMapper;
        this.userRepository = userRepository;
        this.propertyRepository = propertyRepository;
    }

    /**
     * Retrieves reviews for a specific booking by its ID.
     *
     * @param bookingId the ID of the booking
     * @throws ResponseStatusException if the booking is not found, or if the reviews are not found
     * @throws IllegalArgumentException if the booking ID or username is null or negative
     * @return a ReviewResponseDTO containing the reviews for the specified booking
     */
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewsByBookingId(Long bookingId) {
        if (bookingId == null || bookingId <= 0) {
            throw new IllegalArgumentException("Booking ID and Username cannot be null or negative");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (booking.getReview() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No reviews found for this booking");
        }
        Review Review = reviewRepository.findByBookingId(bookingId);
        return reviewMapper.reviewToReviewResponseDto(Review);
    }

    /**
     * Retrieves all reviews made by a specific user.
     *
     * @param username the username of the user
     * @throws ResponseStatusException if the user is not found, or if no reviews are found
     * @return a list of ReviewResponseDTOs containing all reviews made by the specified user
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviewsByUser(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Review> reviews = reviewRepository.findByUserId(user.getId());
        if (reviews.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No reviews found for this user");
        }
        return reviews.stream()
                .map(reviewMapper::reviewToReviewResponseDto)
                .toList();
    }

    /**
     * Retrieves all reviews for a specific property by its ID.
     *
     * @param propertyId the ID of the property
     * @throws ResponseStatusException if no reviews are found for the property, or if the property is not found
     * @throws IllegalArgumentException if the property ID is null or negative
     * @return a list of ReviewResponseDTOs containing all reviews for the specified property
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviewsByPropertyId(Long propertyId) {
        if (propertyId == null || propertyId <= 0) {
            throw new IllegalArgumentException("Property ID cannot be null or negative");
        }
        if (propertyRepository.findById(propertyId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found");
        }
        List<Review> reviews = reviewRepository.findByPropertyId(propertyId);
        if (reviews.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No reviews found for this property");
        }
        return reviews.stream()
                .map(reviewMapper::reviewToReviewResponseDto)
                .toList();
    }

    /**
     * Creates a new review for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @param reviewRequestDTO the DTO containing review data
     * @throws ResponseStatusException if the booking is not found, or if the review already exists
     * @throws IllegalArgumentException if the booking ID or review request DTO is null or negative
     * @return a MessageResponseDTO with a message if the creation was successful
     */
    @Transactional
    public MessageResponseDTO createReview(Long bookingId, ReviewRequestDTO reviewRequestDTO) {
        if (bookingId == null || bookingId <= 0 || reviewRequestDTO == null) {
            throw new IllegalArgumentException("Booking ID, Review Response DTO cannot be null or negative");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getReview() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review already exists for this booking");
        }
        if (booking.getBookingStatus() != BookingStatus.COMPLETED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking must be completed to create a review");
        }
        Review review = reviewMapper.reviewRequestDtoToReview(reviewRequestDTO);
        review.setBooking(booking);
        review.setUser(booking.getUser());
        review.setProperty(booking.getProperty());
        review.setReviewDate(LocalDateTime.now());
        reviewRepository.save(review);
        return new MessageResponseDTO("Review created successfully for booking with ID " + bookingId + "!");
    }

    /**
     * Deletes a review by its ID.
     *
     * @param reviewId the ID of the review to delete
     * @throws ResponseStatusException if the review is not found
     * @throws IllegalArgumentException if the review ID is null or negative
     * @return a MessageResponseDTO with a message if the deletion was successful
     */
    @Transactional
    public MessageResponseDTO deleteReview(Long reviewId) {
        if (reviewId == null || reviewId <= 0) {
            throw new IllegalArgumentException("Review ID and cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (review.getBooking() != null) {
            review.getBooking().setReview(null);
        }
        reviewRepository.deleteReviewById(reviewId);
        return new MessageResponseDTO("Review with ID " + reviewId + " deleted successfully!");
    }

    /**
     * Updates an existing review.
     *
     * @param reviewId the ID of the review to update
     * @param reviewRequestDTO the DTO containing updated review data
     * @throws ResponseStatusException if the review is not found
     * @throws IllegalArgumentException if the review ID or review request DTO is null or negative
     * @return a MessageResponseDTO with a message if the update was successful
     */
    @Transactional
    public MessageResponseDTO updateReview(Long reviewId, ReviewRequestDTO reviewRequestDTO) {
        if (reviewId == null || reviewId <= 0 || reviewRequestDTO == null) {
            throw new IllegalArgumentException("Review ID, Review Request DTO cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        review.setReviewDate(LocalDateTime.now());
        reviewMapper.updateReviewFromDto(reviewRequestDTO, review);
        reviewMapper.reviewToReviewResponseDto(reviewRepository.save(review));
        return (new MessageResponseDTO("Review with review ID " + reviewId + " updated successfully!"));
    }

    /**
     * Updates the comment of an existing review.
     *
     * @param reviewId the ID of the review to update
     * @param comment the new comment for the review
     * @throws ResponseStatusException if the review is not found
     * @throws IllegalArgumentException if the review ID or comment is null or negative
     * @return a MessageResponseDTO with a message if the update was successful
     */
    @Transactional
    public MessageResponseDTO updateReviewComment(Long reviewId, String comment) {
        if (reviewId == null || reviewId <= 0 || comment == null) {
            throw new IllegalArgumentException("Review ID or comment cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        review.setReviewDate(LocalDateTime.now());
        review.setComment(comment);
        reviewMapper.reviewToReviewResponseDto(reviewRepository.save(review));
        return (new MessageResponseDTO("Review description with review ID " + reviewId + " updated to " + comment + " successfully!"));
    }

   /**
     * Updates the rating of an existing review.
     *
     * @param reviewId the ID of the review to update
     * @param rating the new rating for the review
     * @throws ResponseStatusException if the review is not found
     * @throws IllegalArgumentException if the review ID or rating is null or negative
     * @return a MessageResponseDTO with a message if the update was successful
     */
    @Transactional
    public MessageResponseDTO updateReviewRating(Long reviewId, Integer rating) {
        if (reviewId == null || reviewId <= 0 || rating == null || rating <= 0) {
            throw new IllegalArgumentException("Review ID or rating cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        review.setReviewDate(LocalDateTime.now());
        review.setRating(rating);
        reviewMapper.reviewToReviewResponseDto(reviewRepository.save(review));
        return (new MessageResponseDTO("Review rating with review ID " + reviewId + " updated to " + rating + " successfully!"));
    }

    /**
     * Checks if the user is the owner of the review.
     * @param reviewId the ID of the review
     * @param username the username of the user
     * @return true if the user is the owner of the review, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isReviewOwner(Long reviewId, String username) {
        if (reviewId == null || reviewId <= 0 || username == null) {
            throw new IllegalArgumentException("Review ID and Username cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        return review.getUser().getUsername().equals(username);
    }
}
