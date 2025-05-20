package org.example.rentify.entity;

import org.example.rentify.entity.enums.PaymentMethod;
import org.example.rentify.entity.enums.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payment Entity Unit Tests")
class PaymentTest {

    private User user;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testUserForPayment").build();
        booking = Booking.builder().id(1L).build();
    }

    @Test
    @DisplayName("Should create Payment with no-args constructor and set fields using setters")
    void testNoArgsConstructorAndSetters() {
        Payment payment = new Payment();
        assertNull(payment.getId());

        LocalDateTime paymentTime = LocalDateTime.of(2025, 5, 27, 12, 0, 0);
        BigDecimal amount = new BigDecimal("199.99");

        payment.setId(1L);
        payment.setUser(user);
        payment.setBooking(booking);
        payment.setPaymentDate(paymentTime);
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setPaymentStatus(PaymentStatus.COMPLETED);
        payment.setTransactionId("txn_abc123");

        assertEquals(1L, payment.getId());
        assertEquals(user, payment.getUser());
        assertEquals(booking, payment.getBooking());
        assertEquals(paymentTime, payment.getPaymentDate());
        assertEquals(amount, payment.getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD, payment.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, payment.getPaymentStatus());
        assertEquals("txn_abc123", payment.getTransactionId());
    }

    @Test
    @DisplayName("Should create Payment with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime paymentTime = LocalDateTime.of(2025, 5, 26, 18, 30, 0);
        BigDecimal amount = new BigDecimal("99.50");
        PaymentMethod method = PaymentMethod.PAYPAL;
        PaymentStatus status = PaymentStatus.PENDING;
        String transactionId = "txn_def456";

        Payment payment = new Payment(2L, user, booking, paymentTime, amount, method, status, transactionId);

        assertEquals(2L, payment.getId());
        assertEquals(user, payment.getUser());
        assertEquals(booking, payment.getBooking());
        assertEquals(paymentTime, payment.getPaymentDate());
        assertEquals(amount, payment.getAmount());
        assertEquals(method, payment.getPaymentMethod());
        assertEquals(status, payment.getPaymentStatus());
        assertEquals(transactionId, payment.getTransactionId());
    }

    @Test
    @DisplayName("Should create Payment using builder")
    void testBuilder() {
        LocalDateTime paymentTime = LocalDateTime.now();
        BigDecimal amount = new BigDecimal("300.00");
        PaymentMethod method = PaymentMethod.BANK_TRANSFER;
        PaymentStatus status = PaymentStatus.FAILED;
        String transactionId = "txn_ghi789";

        Payment payment = Payment.builder()
                .id(3L)
                .user(user)
                .booking(booking)
                .paymentDate(paymentTime)
                .amount(amount)
                .paymentMethod(method)
                .paymentStatus(status)
                .transactionId(transactionId)
                .build();

        assertEquals(3L, payment.getId());
        assertEquals(user, payment.getUser());
        assertEquals(booking, payment.getBooking());
        assertEquals(paymentTime, payment.getPaymentDate());
        assertEquals(amount, payment.getAmount());
        assertEquals(method, payment.getPaymentMethod());
        assertEquals(status, payment.getPaymentStatus());
        assertEquals(transactionId, payment.getTransactionId());
    }

    @Test
    @DisplayName("Equals and HashCode should be consistent based on defined fields")
    void testEqualsAndHashCode_SameLogicalObjects() {
        LocalDateTime commonPaymentDate = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        BigDecimal commonAmount = new BigDecimal("150.00");
        String commonTxnId = "common_txn";

        Payment payment1 = Payment.builder()
                .id(1L)
                .paymentDate(commonPaymentDate)
                .amount(commonAmount)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionId(commonTxnId)
                .user(user)
                .booking(booking)
                .build();

        Payment payment2 = Payment.builder()
                .id(1L)
                .paymentDate(commonPaymentDate)
                .amount(commonAmount)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionId(commonTxnId)
                .user(User.builder().id(99L).build())
                .booking(Booking.builder().id(99L).build())
                .build();

        assertEquals(payment1, payment2, "Payments with same id, date, amount, method, status, and txnId should be equal.");
        assertEquals(payment1.hashCode(), payment2.hashCode(), "HashCodes should be the same for equal objects based on defined fields.");
    }

    @Test
    @DisplayName("Equals should return false for different objects based on defined fields")
    void testEquals_DifferentObjects() {
        LocalDateTime commonPaymentDate = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
        BigDecimal commonAmount = new BigDecimal("150.00");
        String commonTxnId = "common_txn";

        Payment payment1 = Payment.builder()
                .id(1L)
                .paymentDate(commonPaymentDate)
                .amount(commonAmount)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionId(commonTxnId)
                .user(user).booking(booking)
                .build();

        Payment payment2_differentId = Payment.builder().id(2L).paymentDate(commonPaymentDate).amount(commonAmount).paymentMethod(PaymentMethod.CREDIT_CARD).paymentStatus(PaymentStatus.COMPLETED).transactionId(commonTxnId).user(user).booking(booking).build();
        Payment payment3_differentDate = Payment.builder().id(1L).paymentDate(LocalDateTime.now()).amount(commonAmount).paymentMethod(PaymentMethod.CREDIT_CARD).paymentStatus(PaymentStatus.COMPLETED).transactionId(commonTxnId).user(user).booking(booking).build();
        Payment payment4_differentAmount = Payment.builder().id(1L).paymentDate(commonPaymentDate).amount(new BigDecimal("1.00")).paymentMethod(PaymentMethod.CREDIT_CARD).paymentStatus(PaymentStatus.COMPLETED).transactionId(commonTxnId).user(user).booking(booking).build();
        Payment payment5_differentMethod = Payment.builder().id(1L).paymentDate(commonPaymentDate).amount(commonAmount).paymentMethod(PaymentMethod.PAYPAL).paymentStatus(PaymentStatus.COMPLETED).transactionId(commonTxnId).user(user).booking(booking).build();
        Payment payment6_differentStatus = Payment.builder().id(1L).paymentDate(commonPaymentDate).amount(commonAmount).paymentMethod(PaymentMethod.CREDIT_CARD).paymentStatus(PaymentStatus.PENDING).transactionId(commonTxnId).user(user).booking(booking).build();
        Payment payment7_differentTxnId = Payment.builder().id(1L).paymentDate(commonPaymentDate).amount(commonAmount).paymentMethod(PaymentMethod.CREDIT_CARD).paymentStatus(PaymentStatus.COMPLETED).transactionId("different_txn").user(user).booking(booking).build();

        assertNotEquals(payment1, payment2_differentId);
        assertNotEquals(payment1, payment3_differentDate);
        assertNotEquals(payment1, payment4_differentAmount);
        assertNotEquals(payment1, payment5_differentMethod);
        assertNotEquals(payment1, payment6_differentStatus);
        assertNotEquals(payment1, payment7_differentTxnId);
        assertNotEquals(new Object(), payment1);
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void testEquals_SameInstance() {
        Payment payment1 = Payment.builder().id(1L).build();
        assertEquals(payment1, payment1);
    }

    @Test
    @DisplayName("HashCode consistency based on defined fields")
    void testHashCode_Consistency() {
        Payment payment = Payment.builder()
                .id(1L)
                .paymentDate(LocalDateTime.of(2025, 1, 1, 10, 0))
                .amount(new BigDecimal("100.00"))
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .paymentStatus(PaymentStatus.COMPLETED)
                .transactionId("txn1")
                .user(user)
                .booking(booking)
                .build();
        int initialHashCode = payment.hashCode();

        payment.setUser(User.builder().id(2L).build());
        assertEquals(initialHashCode, payment.hashCode(), "HashCode should not change if user (not in hashCode) changes.");

        payment.setAmount(new BigDecimal("101.00"));
        assertNotEquals(initialHashCode, payment.hashCode(), "HashCode should change if amount (in hashCode) changes.");
    }

    @Test
    @DisplayName("Test with null transactionId for equals and hashCode")
    void testNullTransactionIdInEqualsAndHashCode() {
        LocalDateTime commonDate = LocalDateTime.of(2025, 1, 1, 0,0);
        BigDecimal commonAmount = new BigDecimal("50.00");

        Payment payment1 = Payment.builder().id(1L).paymentDate(commonDate).amount(commonAmount).paymentMethod(PaymentMethod.PAYPAL).paymentStatus(PaymentStatus.PENDING).transactionId(null).build();
        Payment payment2 = Payment.builder().id(1L).paymentDate(commonDate).amount(commonAmount).paymentMethod(PaymentMethod.PAYPAL).paymentStatus(PaymentStatus.PENDING).transactionId(null).build();
        Payment payment3 = Payment.builder().id(1L).paymentDate(commonDate).amount(commonAmount).paymentMethod(PaymentMethod.PAYPAL).paymentStatus(PaymentStatus.PENDING).transactionId("has_id").build();

        assertEquals(payment1, payment2);
        assertEquals(payment1.hashCode(), payment2.hashCode());
        assertNotEquals(payment1, payment3);
    }
}