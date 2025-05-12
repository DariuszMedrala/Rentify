package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.example.rentify.dto.request.ReviewRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.ReviewResponseDTO;
import org.example.rentify.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * ReviewController is a REST controller that handles review-related operations.
 * It provides endpoints for managing reviews for bookings.
 */
@RestController
@RequestMapping("/api/bookings/reviews")
@Tag(name = "Review Management", description = "Endpoints for managing reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Retrieves reviews for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @return a ResponseEntity containing the reviews for the specified booking
     */
    @Operation(summary = "Get review by booking ID", description = "Retrieves review for a specific booking.")
    @GetMapping("/{bookingId}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @bookingService.isBookingOwner(#bookingId, principal.username))")
    public ResponseEntity<ReviewResponseDTO> getReviewsFromBookingId(@Parameter(description = "Booking ID", in = ParameterIn.PATH)
                                                                         @PathVariable Long bookingId) {
        return ResponseEntity.ok(reviewService.getReviewsByBookingId(bookingId));
    }

    /**
     * Retrieves all reviews made by the authenticated user.
     *
     * @param authentication the authentication object containing user details
     * @return a ResponseEntity containing a list of reviews made by the user
     */
    @Operation(summary = "Get all reviews", description = "Retrieves all reviews made by the authenticated user.")
    @GetMapping("/me/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviews(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        return ResponseEntity.ok(reviewService.getAllReviewsByUser(username));
    }

    /**
     * Retrieves all reviews for a specific property.
     *
     * @param propertyId the ID of the property
     * @return a ResponseEntity containing a list of reviews for the specified property
     */
    @Operation(summary = "Get all reviews by property ID", description = "Retrieves all reviews for a specific property.")
    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<ReviewResponseDTO>> getAllReviewsByPropertyId(@Parameter(description = "Property ID", in = ParameterIn.PATH)
                                                                                     @PathVariable Long propertyId) {
        return ResponseEntity.ok(reviewService.getAllReviewsByPropertyId(propertyId));
    }

    /**
     * Creates a new review for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @param reviewRequestDTO the DTO containing review data
     * @return a MessageResponseDTO containing the message if created successfully
     */
    @Operation(summary = "Create review for booking", description = "Creates a new review for a specific booking.")
    @PostMapping("/{bookingId}/create")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @bookingService.isBookingOwner(#bookingId, principal.username))")
    public MessageResponseDTO createReview(@Parameter(description = "Booking ID", in = ParameterIn.PATH)
                                                              @PathVariable Long bookingId,
                                                          @Parameter(description = "Review Request DTO")
                                                          @Valid @RequestBody ReviewRequestDTO reviewRequestDTO){
        return reviewService.createReview(bookingId, reviewRequestDTO);
    }

   /**
     * Deletes a review by its ID.
     *
     * @param reviewId the ID of the review to be deleted
     * @return a MessageResponseDTO containing the message if deleted successfully
     */
    @Operation(summary = "Delete review by ID", description = "Deletes a review by its ID.")
    @DeleteMapping("/{reviewId}/delete")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @reviewService.isReviewOwner(#reviewId, principal.username))")
    public MessageResponseDTO deleteReview(@Parameter(description = "Review ID", in = ParameterIn.PATH) @PathVariable Long reviewId) {
        return reviewService.deleteReview(reviewId);
    }

    /**
     * Updates a review by its ID.
     *
     * @param reviewId the ID of the review to be updated
     * @param reviewRequestDTO the DTO containing updated review data
     * @return a MessageResponseDTO containing the message if updated successfully
     */
    @Operation(summary = "Update review by ID", description = "Updates a review by its ID.")
    @PutMapping("/{reviewId}/update")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @reviewService.isReviewOwner(#reviewId, principal.username))")
    public MessageResponseDTO updateReview(@Parameter(description = "Review ID", in = ParameterIn.PATH) @PathVariable Long reviewId,
                                          @Parameter(description = "Review Request DTO")
                                          @Valid @RequestBody ReviewRequestDTO reviewRequestDTO){
       return reviewService.updateReview(reviewId, reviewRequestDTO);
    }

    /**
     * Updates the comment of a review by its ID.
     *
     * @param reviewId the ID of the review to be updated
     * @param comment the new comment for the review
     * @return a MessageResponseDTO containing the message if updated successfully
     */
    @Operation(summary = "Update review comment by ID", description = "Updates a review comment by its ID.")
    @PatchMapping("/{reviewId}/update/comment")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @reviewService.isReviewOwner(#reviewId, principal.username))")
    public MessageResponseDTO updateReviewComment (@Parameter(description = "Review ID", in = ParameterIn.PATH) @PathVariable Long reviewId,
                                                                     @Max(value = 2000, message = "Comment must be below 2000 words")
                                                                     @Parameter(description = "Comment", in = ParameterIn.PATH)
                                                                     @RequestBody String comment){
        return reviewService.updateReviewComment(reviewId, comment);
    }

    /**
     * Updates the rating of a review by its ID.
     *
     * @param reviewId the ID of the review to be updated
     * @param rating the new rating for the review
     * @return a MessageResponseDTO containing the message if updated successfully
     */
    @Operation(summary = "Update review rating by ID", description = "Updates a review rating by its ID.")
    @PatchMapping("/{reviewId}/update/rating/{rating}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @reviewService.isReviewOwner(#reviewId, principal.username))")
    public MessageResponseDTO updateReviewRating(@Parameter(description = "Review ID", in = ParameterIn.PATH)
                                                                    @PathVariable Long reviewId,
                                                 @Parameter(description = "Rating", in = ParameterIn.PATH)
                                                                 @Min(value = 1, message = "Rating must be at least 1.")
                                                                 @Max(value = 5, message = "Rating must be at most 5.")
                                                                 @PathVariable Integer rating){
        return reviewService.updateReviewRating(reviewId, rating);
    }
}
