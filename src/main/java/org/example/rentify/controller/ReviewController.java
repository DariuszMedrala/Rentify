package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.rentify.dto.request.ReviewRequestDTO;
import org.example.rentify.dto.response.ReviewResponseDTO;
import org.example.rentify.service.ReviewService;
import org.springframework.http.HttpStatus;
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
     * @param authentication the authentication object containing user details
     * @return a ResponseEntity containing the reviews for the specified booking
     */
    @Operation(summary = "Get review by booking ID", description = "Retrieves review for a specific booking.")
    @GetMapping("/{bookingId}")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @bookingService.isBookingOwner(#bookingId, principal.username))")
    public ResponseEntity<ReviewResponseDTO> getReviewsFromBookingId(@Parameter(description = "Booking ID", in = ParameterIn.PATH)
                                                                         @PathVariable Long bookingId,
                                                                     Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        ReviewResponseDTO reviews = reviewService.getReviewsByBookingId(bookingId, username);
        return ResponseEntity.ok(reviews);
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
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        List<ReviewResponseDTO> reviews = reviewService.getAllReviewsByUser(username);
        return ResponseEntity.ok(reviews);
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
        List<ReviewResponseDTO> reviews = reviewService.getAllReviewsByPropertyId(propertyId);
        return ResponseEntity.ok(reviews);
    }

    /**
     * Creates a new review for a specific booking.
     *
     * @param bookingId the ID of the booking
     * @param reviewRequestDTO the DTO containing review data to be created
     * @param authentication the authentication object containing user details
     * @return a ResponseEntity containing the created review
     */
    @Operation(summary = "Create review for booking", description = "Creates a new review for a specific booking.")
    @PostMapping("/{bookingId}/create")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @bookingService.isBookingOwner(#bookingId, principal.username))")
    public ResponseEntity<ReviewResponseDTO> createReview(@Parameter(description = "Booking ID", in = ParameterIn.PATH)
                                                               @PathVariable Long bookingId,
                                                          @Parameter(description = "Review Request DTO") @RequestBody ReviewRequestDTO reviewRequestDTO,
                                                          Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        ReviewResponseDTO review = reviewService.createReview(bookingId, reviewRequestDTO, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /**
     * Deletes a review by its ID.
     *
     * @param reviewId the ID of the review to be deleted
     * @param authentication the authentication object containing user details
     * @return a ResponseEntity indicating the result of the deletion operation
     */
    @Operation(summary = "Delete review by ID", description = "Deletes a review by its ID.")
    @DeleteMapping("/{reviewId}/delete")
    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@Parameter(description = "Review ID", in = ParameterIn.PATH) @PathVariable Long reviewId,
                                          Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        reviewService.deleteReview(reviewId, username);
        return ResponseEntity.status(HttpStatus.OK).body("Review deleted successfully");
    }

    /**
     * Updates a review by its ID.
     *
     * @param reviewId the ID of the review to be updated
     * @param reviewRequestDTO the DTO containing updated review data
     * @param authentication the authentication object containing user details
     * @return a ResponseEntity containing the updated review
     */
    @Operation(summary = "Update review by ID", description = "Updates a review by its ID.")
    @PutMapping("/{reviewId}/update")
    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDTO> updateReview(@Parameter(description = "Review ID", in = ParameterIn.PATH) @PathVariable Long reviewId,
                                                          @Parameter(description = "Review Request DTO") @RequestBody ReviewRequestDTO reviewRequestDTO,
                                                          Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        ReviewResponseDTO updatedReview = reviewService.updateReview(reviewId, reviewRequestDTO, username);
        return ResponseEntity.ok(updatedReview);
    }
    /**
     * Updates the description of a review by its ID.
     *
     * @param reviewId the ID of the review to be updated
     * @param description the new description for the review
     * @param authentication the authentication object containing user details
     * @return a ResponseEntity containing the updated review
     */
    @Operation(summary = "Update review description by ID", description = "Updates a review description by its ID.")
    @PatchMapping("/{reviewId}/update/description")
    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDTO> updateReviewDescription(@Parameter(description = "Review ID", in = ParameterIn.PATH) @PathVariable Long reviewId,
                                                                     @Parameter(description = "Description") @RequestBody String description,
                                                                      Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        ReviewResponseDTO updatedReview = reviewService.updateReviewDescription(reviewId, description, username);
        return ResponseEntity.ok(updatedReview);
    }

    /**
     * Updates the rating of a review by its ID.
     *
     * @param reviewId the ID of the review to be updated
     * @param rating the new rating for the review
     * @param authentication the authentication object containing user details
     * @return a ResponseEntity containing the updated review
     */
    @Operation(summary = "Update review rating by ID", description = "Updates a review rating by its ID.")
    @PatchMapping("/{reviewId}/update/rating/{rating}")
    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    public ResponseEntity<ReviewResponseDTO> updateReviewRating(@Parameter(description = "Review ID", in = ParameterIn.PATH) @PathVariable Long reviewId,
                                                                 @Parameter(description = "Rating", in = ParameterIn.PATH)  @PathVariable Integer rating,
                                                                 Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        ReviewResponseDTO updatedReview = reviewService.updateReviewRating(reviewId, rating, username);
        return ResponseEntity.ok(updatedReview);
    }

}
