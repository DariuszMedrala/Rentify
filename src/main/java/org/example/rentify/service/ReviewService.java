package org.example.rentify.service;

import org.example.rentify.dto.request.ReviewRequestDTO;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

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
     * @param username  the username of the user making the request
     * @throws ResponseStatusException if the booking is not found, or if the reviews are not found
     * @throws IllegalArgumentException if the booking ID or username is null or negative
     * @return a ReviewResponseDTO containing the reviews for the specified booking
     */
    @Transactional(readOnly = true)
    public ReviewResponseDTO getReviewsByBookingId(Long bookingId, String username) {
        if (bookingId == null || bookingId <= 0 || username == null) {
            throw new IllegalArgumentException("Booking ID and Username cannot be null or negative");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (booking.getReview() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No reviews found for this booking");
        }
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to access this booking's reviews");
        }
        Review Review = reviewRepository.findByBookingId(bookingId);
        return reviewMapper.reviewToReviewResponseDto(Review);
    }

    /**
     * Retrieves all reviews made by a specific user.
     *
     * @param username the username of the user
     * @throws ResponseStatusException if the user is not found, or if no reviews are found
     * @throws IllegalArgumentException if the username is null
     * @return a list of ReviewResponseDTOs containing all reviews made by the specified user
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> getAllReviewsByUser(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
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
     * @throws ResponseStatusException if no reviews are found for the property
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
     * @param bookingId        the ID of the booking
     * @param reviewRequestDTO the review request data transfer object
     * @param username         the username of the user making the request
     * @throws ResponseStatusException if the booking is not found, or if a review already exists for this booking
     * @throws IllegalArgumentException if the booking ID, review request DTO, or username is null or negative
     * @return a ReviewResponseDTO containing the created review
     */
    @Transactional
    public ReviewResponseDTO createReview(Long bookingId, ReviewRequestDTO reviewRequestDTO, String username) {
        if (bookingId == null || bookingId <= 0 || reviewRequestDTO == null || username == null) {
            throw new IllegalArgumentException("Booking ID, Review Response DTO and Username cannot be null or negative");
        }
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (!booking.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to create a review for this booking");
        }
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
        return reviewMapper.reviewToReviewResponseDto(reviewRepository.save(review));
    }

    /**
     * Deletes a review by its ID.
     *
     * @param reviewId the ID of the review to delete
     * @param username the username of the user making the request
     * @throws ResponseStatusException if the review is not found, or if the user does not have permission to delete it
     * @throws IllegalArgumentException if the review ID or username is null or negative
     */
    @Transactional
    public void deleteReview(Long reviewId, String username) {
        if (reviewId == null || reviewId <= 0 || username == null) {
            throw new IllegalArgumentException("Review ID and Username cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to delete this review");
        }
        if (review.getBooking() != null) {
            review.getBooking().setReview(null);
        }
        reviewRepository.deleteReviewById(reviewId);
    }

    /**
     * Updates an existing review.
     *
     * @param reviewId        the ID of the review to update
     * @param reviewRequestDTO the review request data transfer object
     * @param username         the username of the user making the request
     * @throws ResponseStatusException if the review is not found, or if the user does not have permission to update it
     * @throws IllegalArgumentException if the review ID, review request DTO, or username is null or negative
     * @return a ReviewResponseDTO containing the updated review
     */
    @Transactional
    public ReviewResponseDTO updateReview(Long reviewId, ReviewRequestDTO reviewRequestDTO, String username) {
        if (reviewId == null || reviewId <= 0 || reviewRequestDTO == null || username == null) {
            throw new IllegalArgumentException("Review ID, Review Request DTO and Username cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to update this review");
        }
        review.setReviewDate(LocalDateTime.now());
        reviewMapper.updateReviewFromDto(reviewRequestDTO, review);
        return reviewMapper.reviewToReviewResponseDto(reviewRepository.save(review));
    }

    /**
     * Updates the description of an existing review.
     *
     * @param reviewId   the ID of the review to update
     * @param description the new description for the review
     * @param username    the username of the user making the request
     * @throws ResponseStatusException if the review is not found, or if the user does not have permission to update it
     * @throws IllegalArgumentException if the review ID, description, or username is null or negative
     * @return a ReviewResponseDTO containing the updated review
     */
    public ReviewResponseDTO updateReviewDescription(Long reviewId, String description, String username) {
        if (reviewId == null || reviewId <= 0 || description == null || username == null) {
            throw new IllegalArgumentException("Review ID, Description and Username cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to update this review");
        }
        review.setReviewDate(LocalDateTime.now());
        review.setComment(description);
        return reviewMapper.reviewToReviewResponseDto(reviewRepository.save(review));
    }

    /**
     * Updates the rating of an existing review.
     *
     * @param reviewId the ID of the review to update
     * @param rating   the new rating for the review
     * @param username the username of the user making the request
     * @throws ResponseStatusException if the review is not found, or if the user does not have permission to update it
     * @throws IllegalArgumentException if the review ID, rating, or username is null or negative
     * @return a ReviewResponseDTO containing the updated review
     */
    public ReviewResponseDTO updateReviewRating(Long reviewId, Integer rating, String username) {
        if (reviewId == null || reviewId <= 0 || rating == null || rating <= 0 || username == null) {
            throw new IllegalArgumentException("Review ID, Rating and Username cannot be null or negative");
        }
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));
        if (!review.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to update this review");
        }
        review.setReviewDate(LocalDateTime.now());
        review.setRating(rating);
        return reviewMapper.reviewToReviewResponseDto(reviewRepository.save(review));
    }
}
