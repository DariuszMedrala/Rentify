package org.example.rentify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentify.dto.request.ReviewRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.ReviewResponseDTO;
import org.example.rentify.service.BookingService;
import org.example.rentify.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;


import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllerTestConfig.class)
@WebMvcTest(ReviewController.class)
@DisplayName("ReviewController Integration Tests")
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private BookingService bookingService;

    private ReviewRequestDTO reviewRequestDTO;
    private ReviewResponseDTO reviewResponseDTO;
    private final Long testBookingId = 1L;
    private final Long testPropertyId = 2L;
    private final Long testReviewId = 3L;
    private final String testUsername = "testUser";
    private final String ownerUsername = "ownerUser";
    private final String adminUsername = "adminUser";

    @BeforeEach
    void setUp() {
        Mockito.reset(reviewService, bookingService);

        reviewRequestDTO = new ReviewRequestDTO();
        reviewRequestDTO.setRating(5);
        reviewRequestDTO.setComment("Great experience!");

        reviewResponseDTO = new ReviewResponseDTO();
        reviewResponseDTO.setId(testReviewId);
        reviewResponseDTO.setBookingId(testBookingId);
        reviewResponseDTO.setPropertyId(testPropertyId);
        reviewResponseDTO.setRating(5);
        reviewResponseDTO.setComment("Great experience!");
        reviewResponseDTO.setReviewDate(LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/bookings/reviews/{bookingId}")
    class GetReviewByBookingIdTests {

        @Test
        @WithMockUser(username = ownerUsername, roles = "USER")
        @DisplayName("should return 200 OK and review when owner requests")
        void whenOwnerRequestsReview_thenReturnsReview() throws Exception {
            when(bookingService.isBookingOwner(eq(testBookingId), eq(ownerUsername))).thenReturn(true);
            when(reviewService.getReviewByBookingId(testBookingId)).thenReturn(reviewResponseDTO);

            mockMvc.perform(get("/api/bookings/reviews/{bookingId}", testBookingId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(testReviewId))
                    .andExpect(jsonPath("$.comment").value("Great experience!"));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK and review when ADMIN requests")
        void whenAdminRequestsReview_thenReturnsReview() throws Exception {
            when(reviewService.getReviewByBookingId(testBookingId)).thenReturn(reviewResponseDTO);

            mockMvc.perform(get("/api/bookings/reviews/{bookingId}", testBookingId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testReviewId));
        }

        @Test
        @WithMockUser(username = "otherUser", roles = "USER")
        @DisplayName("should return 403 Forbidden when non-owner/non-ADMIN requests review")
        void whenNonOwnerRequestsReview_thenReturns403() throws Exception {
            when(bookingService.isBookingOwner(eq(testBookingId), eq("otherUser"))).thenReturn(false);

            mockMvc.perform(get("/api/bookings/reviews/{bookingId}", testBookingId))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden when anonymous user requests review")
        void whenAnonymousRequestsReview_thenReturns403() throws Exception {
            mockMvc.perform(get("/api/bookings/reviews/{bookingId}", testBookingId))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/reviews/me/all")
    class GetAllReviewsByUserTests {
        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK and user's reviews when authenticated")
        void whenAuthenticatedUserRequestsOwnReviews_thenReturnsReviews() throws Exception {
            List<ReviewResponseDTO> reviews = List.of(reviewResponseDTO);
            when(reviewService.getAllReviewsByUser(testUsername)).thenReturn(reviews);

            mockMvc.perform(get("/api/bookings/reviews/me/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(testReviewId));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden when anonymous user requests /me/all")
        void whenAnonymousRequestsMeAll_thenReturns403() throws Exception {
            mockMvc.perform(get("/api/bookings/reviews/me/all"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/reviews/property/{propertyId}")
    class GetAllReviewsByPropertyIdTests {
        @Test
        @DisplayName("should return 200 OK and list of reviews for public access")
        void whenRequestingReviewsForProperty_thenReturnsReviews() throws Exception {
            when(reviewService.getAllReviewsByPropertyId(testPropertyId)).thenReturn(List.of(reviewResponseDTO));

            mockMvc.perform(get("/api/bookings/reviews/property/{propertyId}", testPropertyId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(testReviewId));
        }

        @Test
        @DisplayName("should return 404 Not Found if property does not exist")
        void whenPropertyNotFound_thenReturns404() throws Exception {
            when(reviewService.getAllReviewsByPropertyId(testPropertyId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

            mockMvc.perform(get("/api/bookings/reviews/property/{propertyId}", testPropertyId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Property not found"));
        }
    }

    @Nested
    @DisplayName("POST /api/bookings/reviews/{bookingId}/create")
    class CreateReviewTests {
        @Test
        @WithMockUser(username = ownerUsername, roles="USER")
        @DisplayName("should return 200 OK when owner creates review")
        void whenOwnerCreatesReview_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Review created successfully for booking with ID " + testBookingId + "!");
            when(bookingService.isBookingOwner(eq(testBookingId), eq(ownerUsername))).thenReturn(true);
            when(reviewService.createReview(eq(testBookingId), any(ReviewRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(post("/api/bookings/reviews/{bookingId}/create", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles="ADMIN")
        @DisplayName("should return 200 OK when ADMIN creates review")
        void whenAdminCreatesReview_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Review created successfully for booking with ID " + testBookingId + "!");
            when(reviewService.createReview(eq(testBookingId), any(ReviewRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(post("/api/bookings/reviews/{bookingId}/create", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(reviewRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = ownerUsername, roles = "USER")
        @DisplayName("should return 400 Bad Request for invalid DTO when creating review")
        void whenCreateReviewWithInvalidDto_thenReturns400() throws Exception {
            ReviewRequestDTO invalidDto = new ReviewRequestDTO();
            invalidDto.setComment("Comment");

            when(bookingService.isBookingOwner(eq(testBookingId), eq(ownerUsername))).thenReturn(true);

            mockMvc.perform(post("/api/bookings/reviews/{bookingId}/create", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Validation Error: rating: Rating must be at least 1"));
        }
    }
}