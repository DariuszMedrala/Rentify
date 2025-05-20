package org.example.rentify.entity;

import org.example.rentify.entity.enums.BookingStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Booking Entity Unit Tests")
class BookingTest {

    private Property property;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testUser").build();
        property = Property.builder().id(1L).title("Test Property").build();
    }

    @Test
    @DisplayName("Should create Booking with no-args constructor and set fields using setters")
    void testNoArgsConstructorAndSetters() {
        Booking booking = new Booking();
        assertNull(booking.getId());

        booking.setId(1L);
        booking.setProperty(property);
        booking.setUser(user);
        booking.setStartDate(LocalDate.of(2025, 10, 1));
        booking.setEndDate(LocalDate.of(2025, 10, 10));
        booking.setTotalPrice(new BigDecimal("1000.00"));
        booking.setBookingDate(LocalDateTime.of(2025, 9, 1, 10, 0));
        booking.setBookingStatus(BookingStatus.PENDING);
        Payment payment = new Payment();
        booking.setPayment(payment);
        Review review = new Review();
        booking.setReview(review);

        assertEquals(1L, booking.getId());
        assertEquals(property, booking.getProperty());
        assertEquals(user, booking.getUser());
        assertEquals(LocalDate.of(2025, 10, 1), booking.getStartDate());
        assertEquals(LocalDate.of(2025, 10, 10), booking.getEndDate());
        assertEquals(new BigDecimal("1000.00"), booking.getTotalPrice());
        assertEquals(LocalDateTime.of(2025, 9, 1, 10, 0), booking.getBookingDate());
        assertEquals(BookingStatus.PENDING, booking.getBookingStatus());
        assertEquals(payment, booking.getPayment());
        assertEquals(review, booking.getReview());
    }

    @Test
    @DisplayName("Should create Booking with all-args constructor")
    void testAllArgsConstructor() {
        LocalDate startDate = LocalDate.of(2025, 11, 1);
        LocalDate endDate = LocalDate.of(2025, 11, 5);
        BigDecimal totalPrice = new BigDecimal("500.00");
        LocalDateTime bookingDate = LocalDateTime.of(2025, 10, 15, 14, 30);
        BookingStatus status = BookingStatus.CONFIRMED;
        Payment payment = new Payment();
        Review review = new Review();

        Booking booking = new Booking(2L, property, user, startDate, endDate, totalPrice, bookingDate, status, payment, review);

        assertEquals(2L, booking.getId());
        assertEquals(property, booking.getProperty());
        assertEquals(user, booking.getUser());
        assertEquals(startDate, booking.getStartDate());
        assertEquals(endDate, booking.getEndDate());
        assertEquals(totalPrice, booking.getTotalPrice());
        assertEquals(bookingDate, booking.getBookingDate());
        assertEquals(status, booking.getBookingStatus());
        assertEquals(payment, booking.getPayment());
        assertEquals(review, booking.getReview());
    }

    @Test
    @DisplayName("Should create Booking using builder")
    void testBuilder() {
        LocalDate startDate = LocalDate.of(2026, 1, 10);
        LocalDate endDate = LocalDate.of(2026, 1, 15);
        BigDecimal totalPrice = new BigDecimal("750.00");
        LocalDateTime bookingDate = LocalDateTime.now();
        BookingStatus status = BookingStatus.COMPLETED;
        Payment payment = Payment.builder().id(2L).build();
        Review review = Review.builder().id(2L).build();


        Booking booking = Booking.builder()
                .id(3L)
                .property(property)
                .user(user)
                .startDate(startDate)
                .endDate(endDate)
                .totalPrice(totalPrice)
                .bookingDate(bookingDate)
                .bookingStatus(status)
                .payment(payment)
                .review(review)
                .build();

        assertEquals(3L, booking.getId());
        assertEquals(property, booking.getProperty());
        assertEquals(user, booking.getUser());
        assertEquals(startDate, booking.getStartDate());
        assertEquals(endDate, booking.getEndDate());
        assertEquals(totalPrice, booking.getTotalPrice());
        assertEquals(bookingDate, booking.getBookingDate());
        assertEquals(status, booking.getBookingStatus());
        assertEquals(payment, booking.getPayment());
        assertEquals(review, booking.getReview());
    }

    @Test
    @DisplayName("Equals and HashCode should be consistent based on defined fields")
    void testEqualsAndHashCode_SameLogicalObjects() {
        LocalDateTime commonBookingDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        LocalDate commonStartDate = LocalDate.of(2025, 2, 1);
        LocalDate commonEndDate = LocalDate.of(2025, 2, 10);
        BigDecimal commonPrice = new BigDecimal("200.00");

        Booking booking1 = Booking.builder()
                .id(1L)
                .startDate(commonStartDate)
                .endDate(commonEndDate)
                .totalPrice(commonPrice)
                .bookingDate(commonBookingDate)
                .bookingStatus(BookingStatus.PENDING)
                .property(property)
                .user(user)
                .build();

        Booking booking2 = Booking.builder()
                .id(1L)
                .startDate(commonStartDate)
                .endDate(commonEndDate)
                .totalPrice(commonPrice)
                .bookingDate(commonBookingDate)
                .bookingStatus(BookingStatus.PENDING)
                .property(Property.builder().id(99L).build())
                .user(User.builder().id(99L).build())
                .build();

        assertEquals(booking1, booking2, "Bookings with same ID, dates, price, bookingDate, and status should be equal.");
        assertEquals(booking1.hashCode(), booking2.hashCode(), "HashCodes should be the same for equal objects based on defined fields.");
    }

    @Test
    @DisplayName("Equals should return false for different objects based on defined fields")
    void testEquals_DifferentObjects() {
        LocalDateTime commonBookingDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        LocalDate commonStartDate = LocalDate.of(2025, 2, 1);
        LocalDate commonEndDate = LocalDate.of(2025, 2, 10);
        BigDecimal commonPrice = new BigDecimal("200.00");

        Booking booking1 = Booking.builder()
                .id(1L)
                .startDate(commonStartDate)
                .endDate(commonEndDate)
                .totalPrice(commonPrice)
                .bookingDate(commonBookingDate)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        Booking booking2_differentId = Booking.builder()
                .id(2L) // Inne ID
                .startDate(commonStartDate)
                .endDate(commonEndDate)
                .totalPrice(commonPrice)
                .bookingDate(commonBookingDate)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        Booking booking3_differentStartDate = Booking.builder()
                .id(1L)
                .startDate(LocalDate.of(2025, 3, 1))
                .endDate(commonEndDate)
                .totalPrice(commonPrice)
                .bookingDate(commonBookingDate)
                .bookingStatus(BookingStatus.PENDING)
                .build();

        Booking booking4_differentStatus = Booking.builder()
                .id(1L)
                .startDate(commonStartDate)
                .endDate(commonEndDate)
                .totalPrice(commonPrice)
                .bookingDate(commonBookingDate)
                .bookingStatus(BookingStatus.CONFIRMED)
                .build();


        assertNotEquals(booking1, booking2_differentId, "Bookings with different IDs should not be equal.");
        assertNotEquals(booking1, booking3_differentStartDate, "Bookings with different start dates should not be equal.");
        assertNotEquals(booking1, booking4_differentStatus, "Bookings with different statuses should not be equal.");
        assertNotEquals(null, booking1, "Booking should not be equal to null.");
        assertNotEquals(new Object(), booking1, "Booking should not be equal to an object of a different type.");
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void testEquals_SameInstance() {
        Booking booking1 = Booking.builder()
                .id(1L)
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 2, 10))
                .totalPrice(new BigDecimal("200.00"))
                .bookingDate(LocalDateTime.of(2025, 1, 1, 12, 0, 0))
                .bookingStatus(BookingStatus.PENDING)
                .build();
        assertEquals(booking1, booking1);
    }


    @Test
    @DisplayName("HashCode consistency based on defined fields")
    void testHashCode_Consistency() {
        Booking booking = Booking.builder()
                .id(1L)
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 2, 10))
                .totalPrice(new BigDecimal("200.00"))
                .bookingDate(LocalDateTime.of(2025, 1, 1, 12, 0, 0))
                .bookingStatus(BookingStatus.PENDING)
                .build();
        int initialHashCode = booking.hashCode();

        booking.setProperty(Property.builder().id(5L).title("Another Property").build());
        assertEquals(initialHashCode, booking.hashCode(), "HashCode should not change if a field not in hashCode definition changes.");

        booking.setStartDate(LocalDate.of(2025, 3, 1));
        assertNotEquals(initialHashCode, booking.hashCode(), "HashCode should change if a field included in hashCode calculation changes.");
    }
}