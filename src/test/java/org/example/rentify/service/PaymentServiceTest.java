package org.example.rentify.service;

import org.example.rentify.dto.request.PaymentRequestDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.dto.response.PaymentResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Payment;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;
import org.example.rentify.mapper.PaymentMapper;
import org.example.rentify.repository.BookingRepository;
import org.example.rentify.repository.PaymentRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentService Unit Tests")
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private PaymentService paymentService;

    private User user;
    private Booking booking;
    private Payment payment;
    private PaymentRequestDTO paymentRequestDTO;
    private PaymentResponseDTO paymentResponseDTO;

    private final String testUsername = "testUser";
    private final Long bookingId = 1L;
    private final Long paymentId = 1L;
    private final Long propertyId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername(testUsername);

        Property property = new Property();
        property.setId(propertyId);

        booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setProperty(property);
        booking.setTotalPrice(new BigDecimal("200.00"));
        booking.setStartDate(LocalDate.now().plusDays(1));
        booking.setEndDate(LocalDate.now().plusDays(3));

        paymentRequestDTO = new PaymentRequestDTO();
        paymentRequestDTO.setAmount(new BigDecimal("200.00"));
        paymentRequestDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        payment = new Payment();
        payment.setId(paymentId);
        payment.setUser(user);
        payment.setBooking(booking);
        payment.setAmount(new BigDecimal("200.00"));
        payment.setPaymentDate(LocalDateTime.now().minusHours(1));
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);

        paymentResponseDTO = new PaymentResponseDTO();
        paymentResponseDTO.setId(paymentId);
        paymentResponseDTO.setAmount(payment.getAmount());
        paymentResponseDTO.setPaymentDate(payment.getPaymentDate());
    }

    @Nested
    @DisplayName("getAllPayments (Transactional) Tests")
    class GetAllPaymentsTransactionalTests {
        @Test
        @DisplayName("Should return payments for existing user with payments")
        void getAllPayments_whenUserExistsAndHasPayments_shouldReturnListOfPaymentResponseDTO() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUser(user)).thenReturn(List.of(payment));
            when(paymentMapper.paymentToPaymentResponseDto(payment)).thenReturn(paymentResponseDTO);

            List<PaymentResponseDTO> result = paymentService.getAllPayments(testUsername);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(paymentResponseDTO, result.getFirst());
            verify(userRepository).findUserByUsername(testUsername);
            verify(paymentRepository).findByUser(user);
            verify(paymentMapper).paymentToPaymentResponseDto(payment);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found")
        void getAllPayments_whenUserNotFound_shouldThrowNotFoundException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getAllPayments(testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("User not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no payments found for user")
        void getAllPayments_whenNoPaymentsForUser_shouldThrowNotFoundException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUser(user)).thenReturn(Collections.emptyList());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getAllPayments(testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No payments found for user", exception.getReason());
        }
    }


    @Nested
    @DisplayName("makePayment Tests")
    class MakePaymentTests {
        @Test
        @DisplayName("Should create payment successfully for valid booking and matching amount")
        void makePayment_whenValidBookingAndMatchingAmount_shouldSucceed() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBookingId(bookingId)).thenReturn(null);
            when(paymentMapper.paymentRequestDtoToPayment(paymentRequestDTO)).thenReturn(payment);
            when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

            MessageResponseDTO response = paymentService.makePayment(bookingId, paymentRequestDTO);

            assertNotNull(response);
            assertEquals("Payment created successfully for booking ID: " + bookingId, response.getMessage());
            assertNotNull(payment.getPaymentDate());
            assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
            assertEquals(booking, payment.getBooking());
            assertEquals(user, payment.getUser());
            verify(paymentRepository).save(payment);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException for null bookingId")
        void makePayment_whenBookingIdIsNull_shouldThrowBadRequestException() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.makePayment(null, paymentRequestDTO));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Booking ID cannot be null or negative", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when booking not found")
        void makePayment_whenBookingNotFound_shouldThrowNotFoundException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.makePayment(bookingId, paymentRequestDTO));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Booking not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when payment already exists")
        void makePayment_whenPaymentAlreadyExists_shouldThrowBadRequestException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBookingId(bookingId)).thenReturn(new Payment());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.makePayment(bookingId, paymentRequestDTO));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Payment already exists for this booking", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when payment amount does not match")
        void makePayment_whenAmountMismatch_shouldThrowBadRequestException() {
            paymentRequestDTO.setAmount(new BigDecimal("100.00"));
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(paymentRepository.findByBookingId(bookingId)).thenReturn(null);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.makePayment(bookingId, paymentRequestDTO));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Payment amount does not match booking total price", exception.getReason());
        }
    }

    @Nested
    @DisplayName("getPaymentByBookingId Tests")
    class GetPaymentByBookingIdTests {
        @Test
        @DisplayName("Should return payment when found by booking ID")
        void getPaymentByBookingId_whenPaymentExists_shouldReturnPaymentResponseDTO() {
            when(paymentRepository.findByBookingId(bookingId)).thenReturn(payment);
            when(paymentMapper.paymentToPaymentResponseDto(payment)).thenReturn(paymentResponseDTO);

            PaymentResponseDTO result = paymentService.getPaymentByBookingId(bookingId);

            assertNotNull(result);
            assertEquals(paymentResponseDTO, result);
            verify(paymentRepository).findByBookingId(bookingId);
            verify(paymentMapper).paymentToPaymentResponseDto(payment);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException for null bookingId")
        void getPaymentByBookingId_whenBookingIdIsNull_shouldThrowBadRequestException() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getPaymentByBookingId(null));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Booking ID cannot be null or negative", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when payment not found")
        void getPaymentByBookingId_whenPaymentNotFound_shouldThrowNotFoundException() {
            when(paymentRepository.findByBookingId(bookingId)).thenReturn(null);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getPaymentByBookingId(bookingId));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Payment not found for booking ID: " + bookingId, exception.getReason());
        }
    }

    @Nested
    @DisplayName("deletePaymentByBookingId Tests")
    class DeletePaymentByBookingIdTests {
        @Test
        @DisplayName("Should delete payment successfully when found")
        void deletePaymentByBookingId_whenPaymentExists_shouldSucceed() {
            when(paymentRepository.findByBookingId(bookingId)).thenReturn(payment);
            doNothing().when(paymentRepository).delete(payment);

            MessageResponseDTO response = paymentService.deletePaymentByBookingId(bookingId);

            assertNotNull(response);
            assertEquals("Payment deleted successfully for booking ID: " + bookingId, response.getMessage());
            verify(paymentRepository).findByBookingId(bookingId);
            verify(paymentRepository).delete(payment);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException for null bookingId")
        void deletePaymentByBookingId_whenBookingIdIsNull_shouldThrowBadRequestException() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.deletePaymentByBookingId(null));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Booking ID can not be null or negative", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when payment not found")
        void deletePaymentByBookingId_whenPaymentNotFound_shouldThrowNotFoundException() {
            when(paymentRepository.findByBookingId(bookingId)).thenReturn(null);

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.deletePaymentByBookingId(bookingId));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Payment not found for booking ID: " + bookingId, exception.getReason());
        }
    }

    @Nested
    @DisplayName("getAllPaymentsByUser Tests")
    class GetAllPaymentsByUserTests {
        @Test
        @DisplayName("Should return payments for existing user with payments")
        void getAllPaymentsByUser_whenUserExistsAndHasPayments_shouldReturnListOfPaymentResponseDTO() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUser(user)).thenReturn(List.of(payment));
            when(paymentMapper.paymentToPaymentResponseDto(payment)).thenReturn(paymentResponseDTO);

            List<PaymentResponseDTO> result = paymentService.getAllPaymentsByUser(testUsername);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(paymentResponseDTO, result.getFirst());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found")
        void getAllPaymentsByUser_whenUserNotFound_shouldThrowNotFoundException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getAllPaymentsByUser(testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Requested User was not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no payments found for user")
        void getAllPaymentsByUser_whenNoPaymentsForUser_shouldThrowNotFoundException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUser(user)).thenReturn(Collections.emptyList());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getAllPaymentsByUser(testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No payments found for user", exception.getReason());
        }
    }


    @Nested
    @DisplayName("getTotalAmountPaidByUser Tests")
    class GetTotalAmountPaidByUserTests {
        @Test
        @DisplayName("Should return total amount for user with payments")
        void getTotalAmountPaidByUser_whenUserHasPayments_shouldReturnSum() {
            Payment payment2 = new Payment();
            payment2.setAmount(new BigDecimal("150.00"));
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUser(user)).thenReturn(List.of(payment, payment2));

            Double total = paymentService.getTotalAmountPaidByUser(testUsername);

            assertEquals(350.00, total, 0.001);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found")
        void getTotalAmountPaidByUser_whenUserNotFound_shouldThrowNotFoundException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.empty());
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getTotalAmountPaidByUser(testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Requested User was not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user has no payments")
        void getTotalAmountPaidByUser_whenUserHasNoPayments_shouldThrowNotFoundException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(paymentRepository.findByUser(user)).thenReturn(Collections.emptyList());
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getTotalAmountPaidByUser(testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No payments found for user", exception.getReason());
        }
    }

    @Nested
    @DisplayName("getTotalAmountPaidByAllUsers Tests")
    class GetTotalAmountPaidByAllUsersTests {
        @Test
        @DisplayName("Should return total amount for all users")
        void getTotalAmountPaidByAllUsers_whenPaymentsExist_shouldReturnSum() {
            Payment payment1 = new Payment(); payment1.setAmount(new BigDecimal("100.00"));
            Payment payment2 = new Payment(); payment2.setAmount(new BigDecimal("250.50"));
            when(paymentRepository.findAll()).thenReturn(List.of(payment1, payment2));

            BigDecimal total = paymentService.getTotalAmountPaidByAllUsers();

            assertEquals(new BigDecimal("350.50"), total);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no payments exist")
        void getTotalAmountPaidByAllUsers_whenNoPayments_shouldThrowNotFoundException() {
            when(paymentRepository.findAll()).thenReturn(Collections.emptyList());
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getTotalAmountPaidByAllUsers());
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No payments found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("getTotalAmountPaidByAllUsersForProperty Tests")
    class GetTotalAmountPaidByAllUsersForPropertyTests {
        @Test
        @DisplayName("Should return total amount for property")
        void getTotalAmountPaidByAllUsersForProperty_whenBookingsAndPaymentsExist_shouldReturnSum() {
            Booking booking2 = new Booking(); booking2.setId(2L);
            Payment payment2 = new Payment(); payment2.setAmount(new BigDecimal("50.00"));

            when(bookingRepository.findByPropertyId(propertyId)).thenReturn(List.of(booking, booking2));
            when(paymentRepository.findByBookingId(booking.getId())).thenReturn(payment);
            when(paymentRepository.findByBookingId(booking2.getId())).thenReturn(payment2);

            BigDecimal total = paymentService.getTotalAmountPaidByAllUsersForProperty(propertyId);
            assertEquals(new BigDecimal("250.00"), total);
        }

        @Test
        @DisplayName("Should return zero if property bookings have no payments")
        void getTotalAmountPaidByAllUsersForProperty_whenBookingsHaveNoPayments_shouldReturnZero() {
            when(bookingRepository.findByPropertyId(propertyId)).thenReturn(List.of(booking));
            when(paymentRepository.findByBookingId(booking.getId())).thenReturn(null);

            BigDecimal total = paymentService.getTotalAmountPaidByAllUsersForProperty(propertyId);
            assertEquals(BigDecimal.ZERO, total);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException for null propertyId")
        void getTotalAmountPaidByAllUsersForProperty_whenPropertyIdIsNull_shouldThrowBadRequest() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getTotalAmountPaidByAllUsersForProperty(null));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Property ID cannot be null or negative", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no bookings for property")
        void getTotalAmountPaidByAllUsersForProperty_whenNoBookings_shouldThrowNotFound() {
            when(bookingRepository.findByPropertyId(propertyId)).thenReturn(Collections.emptyList());
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.getTotalAmountPaidByAllUsersForProperty(propertyId));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No bookings found for property ID: " + propertyId, exception.getReason());
        }
    }

    @Nested
    @DisplayName("updatePaymentStatus Tests")
    class UpdatePaymentStatusTests {
        @Test
        @DisplayName("Should update payment status successfully")
        void updatePaymentStatus_whenPaymentExists_shouldSucceed() {
            PaymentStatus newStatus = PaymentStatus.COMPLETED;
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(payment)).thenReturn(payment);

            MessageResponseDTO response = paymentService.updatePaymentStatus(paymentId, newStatus);

            assertEquals("Payment status updated successfully for payment ID: " + paymentId, response.getMessage());
            assertEquals(newStatus, payment.getPaymentStatus());
            verify(paymentRepository).save(payment);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException for null paymentId")
        void updatePaymentStatus_whenPaymentIdIsNull_shouldThrowBadRequest() {
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.updatePaymentStatus(null, PaymentStatus.PENDING));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Payment ID cannot be null or negative", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when payment not found")
        void updatePaymentStatus_whenPaymentNotFound_shouldThrowNotFound() {
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());
            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> paymentService.updatePaymentStatus(paymentId, PaymentStatus.COMPLETED));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Payment not found for ID: " + paymentId, exception.getReason());
        }
    }

    @Nested
    @DisplayName("updatePaymentMethod Tests")
    class UpdatePaymentMethodTests {
        @Test
        @DisplayName("Should update payment method successfully")
        void updatePaymentMethod_whenPaymentExists_shouldSucceed() {
            PaymentMethod newMethod = PaymentMethod.BANK_TRANSFER;
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            when(paymentRepository.save(payment)).thenReturn(payment);

            MessageResponseDTO response = paymentService.updatePaymentMethod(paymentId, newMethod);

            assertEquals("Payment method updated successfully for payment ID: " + paymentId, response.getMessage());
            assertEquals(newMethod, payment.getPaymentMethod());
            verify(paymentRepository).save(payment);
        }
    }

    @Nested
    @DisplayName("updatePayment Tests")
    class UpdatePaymentTests {
        @Test
        @DisplayName("Should update payment successfully")
        void updatePayment_whenPaymentExists_shouldSucceed() {
            PaymentRequestDTO updateDto = new PaymentRequestDTO();
            updateDto.setAmount(new BigDecimal("250.00"));
            updateDto.setPaymentMethod(PaymentMethod.PAYPAL);

            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            doNothing().when(paymentMapper).updatePaymentFromDto(updateDto, payment);
            when(paymentRepository.save(payment)).thenReturn(payment);

            MessageResponseDTO response = paymentService.updatePayment(paymentId, updateDto);

            assertEquals("Payment updated successfully for payment ID: " + paymentId, response.getMessage());
            assertNotNull(payment.getPaymentDate());
            assertEquals(PaymentStatus.PENDING, payment.getPaymentStatus());
            verify(paymentMapper).updatePaymentFromDto(updateDto, payment);
            verify(paymentRepository).save(payment);
        }
    }

    @Nested
    @DisplayName("isPaymentOwner Tests")
    class IsPaymentOwnerTests {
        @Test
        @DisplayName("Should return true if user is payment owner")
        void isPaymentOwner_whenUserIsOwner_shouldReturnTrue() {
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

            boolean result = paymentService.isPaymentOwner(paymentId, testUsername);
            assertTrue(result);
        }

        @Test
        @DisplayName("Should return false if user is not payment owner")
        void isPaymentOwner_whenUserIsNotOwner_shouldReturnFalse() {
            when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
            boolean result = paymentService.isPaymentOwner(paymentId, "anotherUser");
            assertFalse(result);
        }
    }
}