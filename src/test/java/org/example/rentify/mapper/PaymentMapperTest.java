package org.example.rentify.mapper;

import org.example.rentify.dto.request.PaymentRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.dto.response.PaymentResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Payment;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        PaymentMapperImpl.class,
        BookingMapperImpl.class
})
@DisplayName("PaymentMapper Integration Tests (Spring Context)")
class PaymentMapperTest {

    @Autowired
    private PaymentMapper paymentMapper;

    private PaymentRequestDTO paymentRequestDTO;
    private Payment paymentEntity;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");

        Booking bookingEntity = new Booking();
        bookingEntity.setId(1L);
        bookingEntity.setUser(user);
        BookingResponseDTO expectedBookingResponseDTO = new BookingResponseDTO();
        expectedBookingResponseDTO.setId(bookingEntity.getId());


        paymentRequestDTO = new PaymentRequestDTO();
        paymentRequestDTO.setAmount(new BigDecimal("150.75"));
        paymentRequestDTO.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        paymentRequestDTO.setTransactionId("txn_123abc");

        paymentEntity = new Payment();
        paymentEntity.setId(10L);
        paymentEntity.setUser(user);
        paymentEntity.setBooking(bookingEntity);
        paymentEntity.setPaymentDate(LocalDateTime.now().minusHours(2));
        paymentEntity.setAmount(new BigDecimal("200.50"));
        paymentEntity.setPaymentMethod(PaymentMethod.PAYPAL);
        paymentEntity.setPaymentStatus(PaymentStatus.COMPLETED);
        paymentEntity.setTransactionId("txn_xyz789");
    }

    @Nested
    @DisplayName("paymentRequestDtoToPayment Tests")
    class PaymentRequestDtoToPaymentTests {

        @Test
        @DisplayName("Should map PaymentRequestDTO to Payment entity correctly")
        void shouldMapDtoToEntity() {
            Payment mappedPayment = paymentMapper.paymentRequestDtoToPayment(paymentRequestDTO);

            assertNotNull(mappedPayment);
            assertEquals(paymentRequestDTO.getAmount(), mappedPayment.getAmount());
            assertEquals(paymentRequestDTO.getPaymentMethod(), mappedPayment.getPaymentMethod());
            assertEquals(paymentRequestDTO.getTransactionId(), mappedPayment.getTransactionId());

            assertNull(mappedPayment.getId(), "ID should be ignored");
            assertNull(mappedPayment.getBooking(), "Booking should be ignored");
            assertNull(mappedPayment.getPaymentDate(), "PaymentDate should be ignored");
            assertNull(mappedPayment.getPaymentStatus(), "PaymentStatus should be ignored");
            assertNull(mappedPayment.getUser(), "User should be ignored");
        }

        @Test
        @DisplayName("Should handle null PaymentRequestDTO gracefully")
        void shouldHandleNullDto() {
            Payment mappedPayment = paymentMapper.paymentRequestDtoToPayment(null);
            assertNull(mappedPayment, "Mapping a null DTO should result in a null entity");
        }

        @Test
        @DisplayName("Should map DTO with null fields to entity with null fields")
        void shouldMapDtoWithNullFields() {
            PaymentRequestDTO dtoWithNulls = new PaymentRequestDTO();

            Payment mappedPayment = paymentMapper.paymentRequestDtoToPayment(dtoWithNulls);

            assertNotNull(mappedPayment);
            assertNull(mappedPayment.getAmount());
            assertNull(mappedPayment.getPaymentMethod());
            assertNull(mappedPayment.getTransactionId());
        }
    }

    @Nested
    @DisplayName("paymentToPaymentResponseDto Tests")
    class PaymentToPaymentResponseDtoTests {

        @Test
        @DisplayName("Should map Payment entity to PaymentResponseDTO correctly including nested Booking")
        void shouldMapEntityToDto() {
            PaymentResponseDTO mappedDto = paymentMapper.paymentToPaymentResponseDto(paymentEntity);

            assertNotNull(mappedDto);
            assertEquals(paymentEntity.getId(), mappedDto.getId());
            assertEquals(paymentEntity.getPaymentDate(), mappedDto.getPaymentDate());
            assertEquals(paymentEntity.getAmount(), mappedDto.getAmount());
            assertEquals(paymentEntity.getPaymentMethod(), mappedDto.getPaymentMethod());
            assertEquals(paymentEntity.getPaymentStatus(), mappedDto.getPaymentStatus());
            assertEquals(paymentEntity.getTransactionId(), mappedDto.getTransactionId());

            assertNotNull(mappedDto.getBooking(), "Nested BookingResponseDTO should be mapped");
            assertEquals(paymentEntity.getBooking().getId(), mappedDto.getBooking().getId());
        }

        @Test
        @DisplayName("Should handle null Payment entity gracefully")
        void shouldHandleNullEntity() {
            PaymentResponseDTO mappedDto = paymentMapper.paymentToPaymentResponseDto(null);
            assertNull(mappedDto, "Mapping a null entity should result in a null DTO");
        }

        @Test
        @DisplayName("Should map entity with null fields to DTO with null fields")
        void shouldMapEntityWithNullFields() {
            Payment entityWithNulls = new Payment();
            entityWithNulls.setId(5L);

            PaymentResponseDTO mappedDto = paymentMapper.paymentToPaymentResponseDto(entityWithNulls);

            assertNotNull(mappedDto);
            assertEquals(5L, mappedDto.getId());
            assertNull(mappedDto.getBooking());
            assertNull(mappedDto.getPaymentDate());
            assertNull(mappedDto.getAmount());
            assertNull(mappedDto.getPaymentMethod());
            assertNull(mappedDto.getPaymentStatus());
            assertNull(mappedDto.getTransactionId());
        }

        @Test
        @DisplayName("Should map entity with null Booking to DTO with null BookingResponseDTO")
        void shouldMapEntityWithNullBooking() {
            paymentEntity.setBooking(null);
            PaymentResponseDTO mappedDto = paymentMapper.paymentToPaymentResponseDto(paymentEntity);

            assertNotNull(mappedDto);
            assertNull(mappedDto.getBooking(), "BookingResponseDTO should be null if entity's booking is null");
        }
    }

    @Nested
    @DisplayName("updatePaymentFromDto Tests")
    class UpdatePaymentFromDtoTests {

        @Test
        @DisplayName("Should update existing Payment entity from DTO with non-null DTO fields")
        void shouldUpdateEntityFromDto_NonNullFields() {
            Payment targetPayment = new Payment();
            targetPayment.setId(20L);
            targetPayment.setAmount(new BigDecimal("50.00"));
            targetPayment.setPaymentMethod(PaymentMethod.BANK_TRANSFER);
            targetPayment.setTransactionId("old_txn");
            User originalUser = new User(); originalUser.setId(2L);
            targetPayment.setUser(originalUser);
            Booking originalBooking = new Booking(); originalBooking.setId(2L);
            targetPayment.setBooking(originalBooking);
            LocalDateTime originalDate = LocalDateTime.now().minusDays(1);
            targetPayment.setPaymentDate(originalDate);
            targetPayment.setPaymentStatus(PaymentStatus.FAILED);

            paymentMapper.updatePaymentFromDto(paymentRequestDTO, targetPayment);

            assertEquals(paymentRequestDTO.getAmount(), targetPayment.getAmount());
            assertEquals(paymentRequestDTO.getPaymentMethod(), targetPayment.getPaymentMethod());
            assertEquals(paymentRequestDTO.getTransactionId(), targetPayment.getTransactionId());

            assertEquals(20L, targetPayment.getId(), "ID should not be changed");
            assertEquals(originalUser, targetPayment.getUser(), "User should not be changed");
            assertEquals(originalBooking, targetPayment.getBooking(), "Booking should not be changed");
            assertEquals(originalDate, targetPayment.getPaymentDate(), "PaymentDate should not be changed");
            assertEquals(PaymentStatus.FAILED, targetPayment.getPaymentStatus(), "PaymentStatus should not be changed");
        }

        @Test
        @DisplayName("Should ignore null fields from DTO during update")
        void shouldIgnoreNullFieldsFromDtoDuringUpdate() {
            Payment targetPayment = new Payment();
            targetPayment.setAmount(new BigDecimal("100.00"));
            targetPayment.setPaymentMethod(PaymentMethod.PAYPAL);
            targetPayment.setTransactionId("original_txn_id");

            PaymentRequestDTO updateDtoWithNulls = new PaymentRequestDTO();
            updateDtoWithNulls.setAmount(null);
            updateDtoWithNulls.setPaymentMethod(PaymentMethod.PAYPAL);
            updateDtoWithNulls.setTransactionId(null);

            paymentMapper.updatePaymentFromDto(updateDtoWithNulls, targetPayment);

            assertEquals(new BigDecimal("100.00"), targetPayment.getAmount(), "Amount should not change for null DTO field");
            assertEquals(PaymentMethod.PAYPAL, targetPayment.getPaymentMethod(), "PaymentMethod should be updated");
            assertEquals("original_txn_id", targetPayment.getTransactionId(), "TransactionId should not change for null DTO field");
        }
    }
}