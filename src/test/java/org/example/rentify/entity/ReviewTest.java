package org.example.rentify.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Review Entity Unit Tests")
class ReviewTest {

    private User user;
    private Property property;
    private Booking booking;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("testReviewer").build();
        property = Property.builder().id(1L).title("Reviewed Property").build();
        booking = Booking.builder().id(1L).build();
    }

    @Test
    @DisplayName("Should create Review with no-args constructor and set fields using setters")
    void testNoArgsConstructorAndSetters() {
        Review review = new Review();
        assertNull(review.getId());

        LocalDateTime reviewTime = LocalDateTime.of(2025, 5, 27, 14, 0, 0);

        review.setId(1L);
        review.setUser(user);
        review.setProperty(property);
        review.setBooking(booking);
        review.setRating(5);
        review.setComment("Excellent experience!");
        review.setReviewDate(reviewTime);

        assertEquals(1L, review.getId());
        assertEquals(user, review.getUser());
        assertEquals(property, review.getProperty());
        assertEquals(booking, review.getBooking());
        assertEquals(5, review.getRating());
        assertEquals("Excellent experience!", review.getComment());
        assertEquals(reviewTime, review.getReviewDate());
    }

    @Test
    @DisplayName("Should create Review with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime reviewTime = LocalDateTime.of(2025, 5, 26, 10, 30, 0);
        Review review = new Review(2L, user, property, booking, 4, "Very good stay.", reviewTime);

        assertEquals(2L, review.getId());
        assertEquals(user, review.getUser());
        assertEquals(property, review.getProperty());
        assertEquals(booking, review.getBooking());
        assertEquals(4, review.getRating());
        assertEquals("Very good stay.", review.getComment());
        assertEquals(reviewTime, review.getReviewDate());
    }

    @Test
    @DisplayName("Should create Review using builder")
    void testBuilder() {
        LocalDateTime reviewTime = LocalDateTime.now();
        Review review = Review.builder()
                .id(3L)
                .user(user)
                .property(property)
                .booking(booking)
                .rating(3)
                .comment("It was okay.")
                .reviewDate(reviewTime)
                .build();

        assertEquals(3L, review.getId());
        assertEquals(user, review.getUser());
        assertEquals(property, review.getProperty());
        assertEquals(booking, review.getBooking());
        assertEquals(3, review.getRating());
        assertEquals("It was okay.", review.getComment());
        assertEquals(reviewTime, review.getReviewDate());
    }

    @Test
    @DisplayName("Equals and HashCode should be consistent based on defined fields")
    void testEqualsAndHashCode_SameLogicalObjects() {
        LocalDateTime commonReviewDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

        Review review1 = Review.builder()
                .id(1L)
                .rating(5)
                .comment("Great!")
                .reviewDate(commonReviewDate)
                .user(user)
                .property(property)
                .booking(booking)
                .build();

        Review review2 = Review.builder()
                .id(1L)
                .rating(5)
                .comment("Great!")
                .reviewDate(commonReviewDate)
                .user(User.builder().id(99L).build())
                .property(Property.builder().id(99L).build())
                .booking(Booking.builder().id(99L).build())
                .build();

        assertEquals(review1, review2, "Reviews with same id, rating, comment, and reviewDate should be equal.");
        assertEquals(review1.hashCode(), review2.hashCode(), "HashCodes should be the same for equal objects based on defined fields.");
    }

    @Test
    @DisplayName("Equals should return false for different objects based on defined fields")
    void testEquals_DifferentObjects() {
        LocalDateTime commonReviewDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

        Review review1 = Review.builder().id(1L).rating(5).comment("Base").reviewDate(commonReviewDate).user(user).property(property).booking(booking).build();
        Review review2_differentId = Review.builder().id(2L).rating(5).comment("Base").reviewDate(commonReviewDate).user(user).property(property).booking(booking).build();
        Review review3_differentRating = Review.builder().id(1L).rating(4).comment("Base").reviewDate(commonReviewDate).user(user).property(property).booking(booking).build();
        Review review4_differentComment = Review.builder().id(1L).rating(5).comment("Different").reviewDate(commonReviewDate).user(user).property(property).booking(booking).build();
        Review review5_differentDate = Review.builder().id(1L).rating(5).comment("Base").reviewDate(LocalDateTime.now()).user(user).property(property).booking(booking).build();


        assertNotEquals(review1, review2_differentId);
        assertNotEquals(review1, review3_differentRating);
        assertNotEquals(review1, review4_differentComment);
        assertNotEquals(review1, review5_differentDate);
        assertNotEquals(null, review1);
        assertNotEquals(new Object(), review1);
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void testEquals_SameInstance() {
        Review review1 = Review.builder().id(1L).rating(5).build();
        assertEquals(review1, review1);
    }

    @Test
    @DisplayName("HashCode consistency based on defined fields")
    void testHashCode_Consistency() {
        Review review = Review.builder()
                .id(1L)
                .rating(5)
                .comment("Consistent")
                .reviewDate(LocalDateTime.of(2025,1,1,0,0))
                .user(user)
                .property(property)
                .booking(booking)
                .build();
        int initialHashCode = review.hashCode();

        review.setUser(User.builder().id(2L).build());
        assertEquals(initialHashCode, review.hashCode(), "HashCode should not change if user (not in hashCode) changes.");

        review.setComment("Changed Consistent");
        assertNotEquals(initialHashCode, review.hashCode(), "HashCode should change if comment (in hashCode) changes.");
    }

    @Test
    @DisplayName("Test with null comment for equals and hashCode")
    void testNullCommentInEqualsAndHashCode() {
        LocalDateTime commonDate = LocalDateTime.of(2025, 1, 1, 0,0);
        Review review1 = Review.builder().id(1L).rating(4).comment(null).reviewDate(commonDate).build();
        Review review2 = Review.builder().id(1L).rating(4).comment(null).reviewDate(commonDate).build();
        Review review3 = Review.builder().id(1L).rating(4).comment("Not Null").reviewDate(commonDate).build();

        assertEquals(review1, review2);
        assertEquals(review1.hashCode(), review2.hashCode());
        assertNotEquals(review1, review3);
    }
}