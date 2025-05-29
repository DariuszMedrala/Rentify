package org.example.rentify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.entity.enums.BookingStatus;
import org.example.rentify.service.BookingService;
import org.example.rentify.service.PropertyService;
import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllerTestConfig.class)
@WebMvcTest(BookingController.class)
@DisplayName("BookingController Integration Tests (Corrected)")
public class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PropertyService propertyService;

    private BookingRequestDTO validBookingRequestDTO;
    private BookingResponseDTO bookingResponseDTO;

    private final Long testPropertyId = 1L;
    private final Long testBookingId = 10L;
    private final String testUsername = "testUser";
    private final String propertyOwnerUsername = "ownerUser";
    private final String adminUsername = "admin";
    private final String otherUsername = "otherUser";

    @BeforeEach
    void setUp() {
        validBookingRequestDTO = new BookingRequestDTO();
        validBookingRequestDTO.setPropertyId(testPropertyId);
        validBookingRequestDTO.setStartDate(LocalDate.now().plusDays(1));
        validBookingRequestDTO.setEndDate(LocalDate.now().plusDays(3));

        bookingResponseDTO = new BookingResponseDTO();
        bookingResponseDTO.setId(testBookingId);
        bookingResponseDTO.setStartDate(LocalDate.now().plusDays(1));
        bookingResponseDTO.setEndDate(LocalDate.now().plusDays(3));
        bookingResponseDTO.setBookingDate(LocalDateTime.now().minusHours(1));
        bookingResponseDTO.setTotalPrice(new BigDecimal("200.00"));
        bookingResponseDTO.setBookingStatus(BookingStatus.PENDING);
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(bookingService, propertyService);
    }

    @Nested
    @DisplayName("POST /api/bookings/create")
    class CreateBookingTests {

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK and success message when authenticated user creates booking with valid DTO")
        void whenAuthenticatedUserCreatesBookingWithValidDTO_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Booking created successfully with ID: " + testBookingId);
            when(bookingService.createBooking(any(BookingRequestDTO.class), eq(testUsername))).thenReturn(successResponse);

            mockMvc.perform(post("/api/bookings/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validBookingRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 400 Bad Request when creating booking with invalid DTO (null startDate)")
        void whenCreateBookingWithNullStartDate_thenReturns400() throws Exception {
            BookingRequestDTO invalidDto = new BookingRequestDTO();
            invalidDto.setPropertyId(testPropertyId);
            invalidDto.setStartDate(null);
            invalidDto.setEndDate(LocalDate.now().plusDays(2));

            MvcResult result = mockMvc.perform(post("/api/bookings/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("Start date cannot be null");
        }

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 400 Bad Request when creating booking with past startDate")
        void whenCreateBookingWithPastStartDate_thenReturns400() throws Exception {
            BookingRequestDTO invalidDto = new BookingRequestDTO();
            invalidDto.setPropertyId(testPropertyId);
            invalidDto.setStartDate(LocalDate.now().minusDays(1));
            invalidDto.setEndDate(LocalDate.now().plusDays(2));

            MvcResult result = mockMvc.perform(post("/api/bookings/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("Start date must be in the present or future");
        }


        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden when anonymous user tries to create booking")
        void whenAnonymousUserCreatesBooking_thenReturns403() throws Exception {
            mockMvc.perform(post("/api/bookings/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validBookingRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/me/all")
    class GetAllBookingsFromLoggedUserTests {

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK and list of bookings for authenticated user")
        void whenAuthenticatedUserRequestsTheirBookings_thenReturnsListOfBookings() throws Exception {
            List<BookingResponseDTO> bookings = List.of(bookingResponseDTO);
            when(bookingService.getAllBookingsFromLoggedUser(testUsername)).thenReturn(bookings);

            mockMvc.perform(get("/api/bookings/me/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(testBookingId))
                    .andExpect(jsonPath("$[0].totalPrice").value(bookingResponseDTO.getTotalPrice().doubleValue()));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden for anonymous user")
        void whenAnonymousUserRequestsBookings_thenReturns403() throws Exception {
            mockMvc.perform(get("/api/bookings/me/all"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/{propertyID}/all")
    class GetAllBookingsForPropertyTests {

        @Test
        @WithAnonymousUser
        @DisplayName("should return 200 OK and list of bookings for a given property ID")
        void whenRequestingBookingsForProperty_thenReturnsListOfBookings() throws Exception {
            List<BookingResponseDTO> bookings = List.of(bookingResponseDTO);
            when(bookingService.getAllBookingsByPropertyId(testPropertyId)).thenReturn(bookings);

            mockMvc.perform(get("/api/bookings/{propertyID}/all", testPropertyId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(testBookingId));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 200 OK and empty list if no bookings for property")
        void whenNoBookingsForProperty_thenReturnsEmptyList() throws Exception {
            when(bookingService.getAllBookingsByPropertyId(testPropertyId)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/bookings/{propertyID}/all", testPropertyId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 404 Not Found if property does not exist when getting bookings")
        void whenPropertyNotFoundForGettingBookings_thenReturns404() throws Exception {
            Long nonExistentPropertyId = 999L;
            String errorMessage = "Property not found with ID: " + nonExistentPropertyId;
            when(bookingService.getAllBookingsByPropertyId(nonExistentPropertyId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(get("/api/bookings/{propertyID}/all", nonExistentPropertyId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("PATCH /api/bookings/{propertyID}/{bookingID}/booking-status")
    class AcceptOrRejectBookingTests {

        @Test
        @WithMockUser(username = propertyOwnerUsername, roles = "USER")
        @DisplayName("should return 200 OK when property owner accepts booking")
        void whenPropertyOwnerAcceptsBooking_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Booking status updated to CONFIRMED");
            when(propertyService.isOwner(eq(testPropertyId), eq(propertyOwnerUsername))).thenReturn(true);
            when(bookingService.acceptOrRejectBooking(eq(testBookingId), eq(testPropertyId), eq(BookingStatus.CONFIRMED)))
                    .thenReturn(successResponse);

            mockMvc.perform(patch("/api/bookings/{propertyID}/{bookingID}/booking-status", testPropertyId, testBookingId)
                            .param("bookingStatus", "CONFIRMED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN rejects booking")
        void whenAdminRejectsBooking_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Booking status updated to REJECTED");
            when(bookingService.acceptOrRejectBooking(eq(testBookingId), eq(testPropertyId), eq(BookingStatus.CANCELLED)))
                    .thenReturn(successResponse);

            mockMvc.perform(patch("/api/bookings/{propertyID}/{bookingID}/booking-status", testPropertyId, testBookingId)
                            .param("bookingStatus", "CANCELLED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = otherUsername, roles = "USER")
        @DisplayName("should return 403 Forbidden when non-owner/non-ADMIN tries to update booking status")
        void whenNonOwnerNonAdminUpdatesStatus_thenReturns403() throws Exception {
            when(propertyService.isOwner(eq(testPropertyId), eq(otherUsername))).thenReturn(false);

            mockMvc.perform(patch("/api/bookings/{propertyID}/{bookingID}/booking-status", testPropertyId, testBookingId)
                            .param("bookingStatus", "CONFIRMED"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden when anonymous user tries to update booking status")
        void whenAnonymousUserUpdatesStatus_thenReturns403() throws Exception {
            mockMvc.perform(patch("/api/bookings/{propertyID}/{bookingID}/booking-status", testPropertyId, testBookingId)
                            .param("bookingStatus", "CONFIRMED"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 404 Not Found if booking does not exist when updating status")
        void whenBookingNotFoundForStatusUpdate_thenReturns404() throws Exception {
            Long nonExistentBookingId = 998L;
            String errorMessage = "Booking not found with ID: " + nonExistentBookingId;
            when(bookingService.acceptOrRejectBooking(eq(nonExistentBookingId), eq(testPropertyId), eq(BookingStatus.CONFIRMED)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(patch("/api/bookings/{propertyID}/{bookingID}/booking-status", testPropertyId, nonExistentBookingId)
                            .param("bookingStatus", "CONFIRMED"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for invalid booking status string")
        void whenInvalidBookingStatusString_thenReturns400() throws Exception {
            mockMvc.perform(patch("/api/bookings/{propertyID}/{bookingID}/booking-status", testPropertyId, testBookingId)
                            .param("bookingStatus", "INVALID_STATUS_VALUE"))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/bookings/{propertyID}/{bookingID}/delete")
    class DeleteBookingTests {

        @Test
        @WithMockUser(username = testUsername, roles = "USER")
        @DisplayName("should return 200 OK when booking owner deletes their booking")
        void whenBookingOwnerDeletesBooking_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Booking deleted successfully");
            when(bookingService.isBookingOwner(eq(testBookingId), eq(testUsername))).thenReturn(true);
            when(bookingService.deleteBooking(eq(testBookingId), eq(testPropertyId), eq(testUsername)))
                    .thenReturn(successResponse);

            mockMvc.perform(delete("/api/bookings/{propertyID}/{bookingID}/delete", testPropertyId, testBookingId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN deletes any booking")
        void whenAdminDeletesBooking_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Booking deleted successfully");
            when(bookingService.deleteBooking(eq(testBookingId), eq(testPropertyId), eq(adminUsername)))
                    .thenReturn(successResponse);

            mockMvc.perform(delete("/api/bookings/{propertyID}/{bookingID}/delete", testPropertyId, testBookingId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = otherUsername, roles = "USER")
        @DisplayName("should return 403 Forbidden when non-owner/non-ADMIN tries to delete booking")
        void whenNonOwnerNonAdminDeletesBooking_thenReturns403() throws Exception {
            when(bookingService.isBookingOwner(eq(testBookingId), eq(otherUsername))).thenReturn(false);

            mockMvc.perform(delete("/api/bookings/{propertyID}/{bookingID}/delete", testPropertyId, testBookingId))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 404 Not Found if booking to delete does not exist")
        void whenBookingToDeleteNotFound_thenReturns404() throws Exception {
            Long nonExistentBookingId = 998L;
            String errorMessage = "Booking not found with ID: " + nonExistentBookingId;
            when(bookingService.deleteBooking(eq(nonExistentBookingId), eq(testPropertyId), eq(adminUsername)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(delete("/api/bookings/{propertyID}/{bookingID}/delete", testPropertyId, nonExistentBookingId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("PUT /api/bookings/{bookingID}/update")
    class UpdateBookingTests {

        @Test
        @WithMockUser(username = testUsername, roles = "USER")
        @DisplayName("should return 200 OK when booking owner updates booking with valid DTO")
        void whenBookingOwnerUpdatesBooking_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Booking updated successfully for ID: " + testBookingId);
            when(bookingService.isBookingOwner(eq(testBookingId), eq(testUsername))).thenReturn(true);
            when(bookingService.updateBooking(any(BookingRequestDTO.class), eq(testBookingId), eq(testUsername)))
                    .thenReturn(successResponse);

            mockMvc.perform(put("/api/bookings/{bookingID}/update", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validBookingRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN updates booking with valid DTO")
        void whenAdminUpdatesBooking_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Booking updated successfully for ID: " + testBookingId);
            when(bookingService.updateBooking(any(BookingRequestDTO.class), eq(testBookingId), eq(adminUsername)))
                    .thenReturn(successResponse);

            mockMvc.perform(put("/api/bookings/{bookingID}/update", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validBookingRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = otherUsername, roles = "USER")
        @DisplayName("should return 403 Forbidden when non-owner/non-ADMIN tries to update booking")
        void whenNonOwnerNonAdminUpdatesBooking_thenReturns403() throws Exception {
            when(bookingService.isBookingOwner(eq(testBookingId), eq(otherUsername))).thenReturn(false);

            mockMvc.perform(put("/api/bookings/{bookingID}/update", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validBookingRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 400 Bad Request when updating booking with invalid DTO (e.g., past endDate)")
        void whenUpdateBookingWithInvalidDTO_thenReturns400() throws Exception {
            BookingRequestDTO invalidDto = new BookingRequestDTO();
            invalidDto.setPropertyId(testPropertyId);
            invalidDto.setStartDate(LocalDate.now().plusDays(1));
            invalidDto.setEndDate(LocalDate.now().minusDays(1));

            when(bookingService.isBookingOwner(eq(testBookingId), eq(testUsername))).thenReturn(true);

            MvcResult result = mockMvc.perform(put("/api/bookings/{bookingID}/update", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andReturn();
            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("End date must be in the present or future");
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 404 Not Found if booking to update does not exist")
        void whenBookingToUpdateNotFound_thenReturns404() throws Exception {
            Long nonExistentBookingId = 998L;
            String errorMessage = "Booking not found with ID: " + nonExistentBookingId;
            when(bookingService.updateBooking(any(BookingRequestDTO.class), eq(nonExistentBookingId), eq(adminUsername)))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(put("/api/bookings/{bookingID}/update", nonExistentBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validBookingRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }
}