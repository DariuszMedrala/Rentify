package org.example.rentify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.rentify.dto.request.PaymentRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.PaymentResponseDTO;
import org.example.rentify.entity.enums.BookingStatus;
import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;
import org.example.rentify.service.BookingService;
import org.example.rentify.service.PaymentService;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(ControllerTestConfig.class)
@WebMvcTest(PaymentController.class)
@DisplayName("PaymentController Integration Tests")
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BookingService bookingService;

    private PaymentRequestDTO validPaymentRequestDTO;
    private PaymentResponseDTO paymentResponseDTO;


    private final Long testBookingId = 1L;
    private final Long testPaymentId = 100L;
    private final String testUsername = "testUser";
    private final String ownerUsername = "ownerUser";
    private final String adminUsername = "admin";


    @BeforeEach
    void setUp() {
        validPaymentRequestDTO = new PaymentRequestDTO();
        validPaymentRequestDTO.setAmount(new BigDecimal("100.01"));
        validPaymentRequestDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        validPaymentRequestDTO.setTransactionId("txn_valid123");

        BookingResponseDTO mockBookingResponseDTO = new BookingResponseDTO();
        mockBookingResponseDTO.setId(testBookingId);
        mockBookingResponseDTO.setTotalPrice(BigDecimal.valueOf(100.01));
        mockBookingResponseDTO.setBookingDate(LocalDateTime.now());
        mockBookingResponseDTO.setBookingStatus(BookingStatus.CONFIRMED);

        paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setId(testPaymentId);
        paymentResponseDTO.setBooking(mockBookingResponseDTO);
        paymentResponseDTO.setAmount(new BigDecimal("100.00"));
        paymentResponseDTO.setPaymentDate(LocalDateTime.now().minusHours(1));
        paymentResponseDTO.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentResponseDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        paymentResponseDTO.setTransactionId("txn_valid123");
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(paymentService, bookingService);
    }

    @Nested
    @DisplayName("GET /api/bookings/payments/me/all")
    class GetAllPaymentsForAuthenticatedUserTests {
        @Test
        @WithMockUser(username = testUsername)
        @DisplayName("should return 200 OK and list of payments for authenticated user")
        void whenAuthenticatedUser_thenReturnsTheirPayments() throws Exception {
            List<PaymentResponseDTO> payments = List.of(paymentResponseDTO);
            when(paymentService.getAllPayments(testUsername)).thenReturn(payments);

            mockMvc.perform(get("/api/bookings/payments/me/all"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$[0].id").value(testPaymentId));
        }

        @Test
        @WithAnonymousUser
        @DisplayName("should return 403 Forbidden for anonymous user")
        void whenAnonymousUser_thenReturns403() throws Exception {
            mockMvc.perform(get("/api/bookings/payments/me/all"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("POST /api/bookings/payments/{bookingId}/pay")
    class MakePaymentTests {

        @Test
        @WithMockUser(username = ownerUsername, roles = "USER")
        @DisplayName("should return 200 OK when booking owner makes payment with valid DTO")
        void whenBookingOwnerMakesPayment_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Payment created successfully for booking ID: " + testBookingId);
            when(bookingService.isBookingOwner(eq(testBookingId), eq(ownerUsername))).thenReturn(true);
            when(paymentService.makePayment(eq(testBookingId), any(PaymentRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(post("/api/bookings/payments/{bookingId}/pay", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPaymentRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN makes payment with valid DTO")
        void whenAdminMakesPayment_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Payment created successfully for booking ID: " + testBookingId);
            when(paymentService.makePayment(eq(testBookingId), any(PaymentRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(post("/api/bookings/payments/{bookingId}/pay", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPaymentRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = "otherUser", roles = "USER")
        @DisplayName("should return 403 Forbidden when non-owner/non-ADMIN makes payment (with valid DTO)")
        void whenNonOwnerMakesPayment_thenReturns403() throws Exception {
            when(bookingService.isBookingOwner(eq(testBookingId), eq("otherUser"))).thenReturn(false);

            mockMvc.perform(post("/api/bookings/payments/{bookingId}/pay", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPaymentRequestDTO)))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for invalid PaymentRequestDTO (null amount and method)")
        void whenMakePaymentWithInvalidDto_thenReturns400() throws Exception {
            PaymentRequestDTO invalidDto = new PaymentRequestDTO();
            invalidDto.setAmount(null);
            invalidDto.setPaymentMethod(null);

            MvcResult result = mockMvc.perform(post("/api/bookings/payments/{bookingId}/pay", testBookingId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            assertThat(responseBody).contains("Validation Error:");
            assertThat(responseBody).contains("paymentMethod: Payment method cannot be null");
            assertThat(responseBody).contains("amount: Amount cannot be null");
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/payments/{bookingId}")
    class GetPaymentByBookingIdTests {
        @Test
        @WithMockUser(username = ownerUsername, roles = "USER")
        @DisplayName("should return 200 OK and payment when booking owner requests")
        void whenBookingOwnerRequests_thenReturnsPayment() throws Exception {
            when(bookingService.isBookingOwner(eq(testBookingId), eq(ownerUsername))).thenReturn(true);
            when(paymentService.getPaymentByBookingId(testBookingId)).thenReturn(paymentResponseDTO);

            mockMvc.perform(get("/api/bookings/payments/{bookingId}", testBookingId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testPaymentId));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK and payment when ADMIN requests")
        void whenAdminRequests_thenReturnsPayment() throws Exception {
            when(paymentService.getPaymentByBookingId(testBookingId)).thenReturn(paymentResponseDTO);

            mockMvc.perform(get("/api/bookings/payments/{bookingId}", testBookingId))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testPaymentId));
        }

        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 404 Not Found when payment for booking ID does not exist")
        void whenPaymentForBookingNotFound_thenReturns404() throws Exception {
            Long nonExistentBookingId = 998L;
            String errorMessage = "Payment not found for booking ID: " + nonExistentBookingId;
            when(paymentService.getPaymentByBookingId(nonExistentBookingId))
                    .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, errorMessage));

            mockMvc.perform(get("/api/bookings/payments/{bookingId}", nonExistentBookingId))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("DELETE /api/bookings/payments/{bookingId}/delete")
    class DeletePaymentByBookingIdTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN deletes payment")
        void whenAdminDeletesPayment_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Payment deleted successfully for booking ID: " + testBookingId);
            when(paymentService.deletePaymentByBookingId(testBookingId)).thenReturn(successResponse);

            mockMvc.perform(delete("/api/bookings/payments/{bookingId}/delete", testBookingId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/payments/user/{username}")
    class GetAllPaymentsByUserTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK and payments for ADMIN")
        void whenAdminRequestsPaymentsForUser_thenSucceeds() throws Exception {
            List<PaymentResponseDTO> payments = List.of(paymentResponseDTO);
            when(paymentService.getAllPaymentsByUser(testUsername)).thenReturn(payments);

            mockMvc.perform(get("/api/bookings/payments/user/{username}", testUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(testPaymentId));
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/payments/user/{username}/total")
    class GetTotalAmountPaidByUserTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK and total amount for ADMIN")
        void whenAdminRequestsTotalForUser_thenSucceeds() throws Exception {
            Double expectedAmount = 100.00;
            when(paymentService.getTotalAmountPaidByUser(testUsername)).thenReturn(expectedAmount);

            mockMvc.perform(get("/api/bookings/payments/user/{username}/total", testUsername))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(content().string(String.valueOf(expectedAmount)));
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/payments/total")
    class GetTotalAmountPaidByAllUsersTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK and total amount for ADMIN")
        void whenAdminRequestsTotalForAll_thenSucceeds() throws Exception {
            BigDecimal expectedTotal = new BigDecimal("500.00");
            when(paymentService.getTotalAmountPaidByAllUsers()).thenReturn(expectedTotal);

            MvcResult result = mockMvc.perform(get("/api/bookings/payments/total"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            String content = result.getResponse().getContentAsString();
            assertThat(new BigDecimal(content)).isEqualByComparingTo(expectedTotal);
        }
    }

    @Nested
    @DisplayName("GET /api/bookings/payments/property/{propertyId}/total")
    class GetTotalAmountPaidByAllUsersForPropertyTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK and total amount for property for ADMIN")
        void whenAdminRequestsTotalForProperty_thenSucceeds() throws Exception {
            BigDecimal expectedTotal = new BigDecimal("250.00");
            Long testPropertyId = 200L;
            when(paymentService.getTotalAmountPaidByAllUsersForProperty(testPropertyId)).thenReturn(expectedTotal);

            MvcResult result = mockMvc.perform(get("/api/bookings/payments/property/{propertyId}/total", testPropertyId))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andReturn();
            String content = result.getResponse().getContentAsString();
            assertThat(new BigDecimal(content)).isEqualByComparingTo(expectedTotal);
        }
    }

    @Nested
    @DisplayName("PATCH /{paymentId}/{paymentStatus}/update-status")
    class UpdatePaymentStatusTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN updates payment status")
        void whenAdminUpdatesStatus_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Payment status updated successfully for payment ID: " + testPaymentId);
            when(paymentService.updatePaymentStatus(eq(testPaymentId), eq(PaymentStatus.COMPLETED))).thenReturn(successResponse);

            mockMvc.perform(patch("/api/bookings/payments/{paymentId}/{paymentStatus}/update-status", testPaymentId, "COMPLETED"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
    }

    @Nested
    @DisplayName("PATCH /{paymentId}/{paymentMethod}/update-method")
    class UpdatePaymentMethodTests {
        @Test
        @WithMockUser(username = adminUsername, roles = "ADMIN")
        @DisplayName("should return 200 OK when ADMIN updates payment method")
        void whenAdminUpdatesMethod_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Payment method updated successfully for payment ID: " + testPaymentId);
            when(paymentService.updatePaymentMethod(eq(testPaymentId), eq(PaymentMethod.BANK_TRANSFER))).thenReturn(successResponse);

            mockMvc.perform(patch("/api/bookings/payments/{paymentId}/{paymentMethod}/update-method", testPaymentId, "BANK_TRANSFER"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
    }

    @Nested
    @DisplayName("PUT /{paymentId}/update")
    class UpdatePaymentTests {
        @Test
        @WithMockUser(username = ownerUsername, roles="USER")
        @DisplayName("should return 200 OK when payment owner updates payment")
        void whenPaymentOwnerUpdatesPayment_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Payment updated successfully for payment ID: " + testPaymentId);
            when(paymentService.isPaymentOwner(eq(testPaymentId), eq(ownerUsername))).thenReturn(true);
            when(paymentService.updatePayment(eq(testPaymentId), any(PaymentRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/bookings/payments/{paymentId}/update", testPaymentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPaymentRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }

        @Test
        @WithMockUser(username = adminUsername, roles="ADMIN")
        @DisplayName("should return 200 OK when ADMIN updates payment")
        void whenAdminUpdatesPayment_thenSucceeds() throws Exception {
            MessageResponseDTO successResponse = new MessageResponseDTO("Payment updated successfully for payment ID: " + testPaymentId);
            when(paymentService.updatePayment(eq(testPaymentId), any(PaymentRequestDTO.class))).thenReturn(successResponse);

            mockMvc.perform(put("/api/bookings/payments/{paymentId}/update", testPaymentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validPaymentRequestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value(successResponse.getMessage()));
        }
    }
}