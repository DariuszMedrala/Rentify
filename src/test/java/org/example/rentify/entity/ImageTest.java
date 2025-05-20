package org.example.rentify.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Image Entity Unit Tests")
class ImageTest {

    private Property property;

    @BeforeEach
    void setUp() {
        property = Property.builder().id(1L).title("Test Property for Image").build();
    }

    @Test
    @DisplayName("Should create Image with no-args constructor and set fields using setters")
    void testNoArgsConstructorAndSetters() {
        Image image = new Image();
        assertNull(image.getId());
        assertNull(image.getImageUrl());

        LocalDateTime uploadTime = LocalDateTime.of(2025, 5, 27, 10, 0, 0);

        image.setId(1L);
        image.setProperty(property);
        image.setImageUrl("example.com/image.png");
        image.setDescription("A beautiful test image");
        image.setUploadDate(uploadTime);

        assertEquals(1L, image.getId());
        assertEquals(property, image.getProperty());
        assertEquals("example.com/image.png", image.getImageUrl());
        assertEquals("A beautiful test image", image.getDescription());
        assertEquals(uploadTime, image.getUploadDate());
    }

    @Test
    @DisplayName("Should create Image with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime uploadTime = LocalDateTime.of(2025, 5, 26, 15, 30, 0);
        Image image = new Image(2L, property, "example.com/another.jpg", "Another test image", uploadTime);

        assertEquals(2L, image.getId());
        assertEquals(property, image.getProperty());
        assertEquals("example.com/another.jpg", image.getImageUrl());
        assertEquals("Another test image", image.getDescription());
        assertEquals(uploadTime, image.getUploadDate());
    }

    @Test
    @DisplayName("Equals and HashCode should be consistent based on defined fields")
    void testEqualsAndHashCode_SameLogicalObjects() {
        LocalDateTime commonUploadDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

        Image image1 = new Image(1L, null, "example.com/img1.jpg", "Desc1", commonUploadDate);
        Image image2 = new Image(1L, Property.builder().id(99L).build(), "example.com/img1.jpg", "Desc1", commonUploadDate);
        assertEquals(image1, image2, "Images with same id, imageUrl, description, and uploadDate should be equal.");
        assertEquals(image1.hashCode(), image2.hashCode(), "HashCodes should be the same for equal objects based on defined fields.");
    }

    @Test
    @DisplayName("Equals should return false for different objects based on defined fields")
    void testEquals_DifferentObjects() {
        LocalDateTime commonUploadDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        Property someProperty = Property.builder().id(1L).build();

        Image image1 = new Image(1L, someProperty, "example.com/img.jpg", "Desc", commonUploadDate);
        Image image2_differentId = new Image(2L, someProperty, "example.com/img.jpg", "Desc", commonUploadDate);
        Image image3_differentUrl = new Image(1L, someProperty, "example.com/img_DIFFERENT.jpg", "Desc", commonUploadDate);
        Image image4_differentDesc = new Image(1L, someProperty, "http:/example.com/img.jpg", "DIFFERENT Desc", commonUploadDate);
        Image image5_differentDate = new Image(1L, someProperty, "example.com/img.jpg", "Desc", LocalDateTime.now());

        assertNotEquals(image1, image2_differentId, "Images with different IDs should not be equal.");
        assertNotEquals(image1, image3_differentUrl, "Images with different imageUrls should not be equal.");
        assertNotEquals(image1, image4_differentDesc, "Images with different descriptions should not be equal.");
        assertNotEquals(image1, image5_differentDate, "Images with different upload dates should not be equal.");
        assertNotEquals(null, image1, "Image should not be equal to null.");
        assertNotEquals(new Object(), image1, "Image should not be equal to an object of a different type.");
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void testEquals_SameInstance() {
        Image image1 = new Image(1L, property, "example.com/img.jpg", "Desc", LocalDateTime.now());
        assertEquals(image1, image1);
    }

    @Test
    @DisplayName("HashCode consistency based on defined fields")
    void testHashCode_Consistency() {
        Image image = new Image(1L, property, "example.com/img.jpg", "Desc", LocalDateTime.of(2025, 1, 1, 0,0));
        int initialHashCode = image.hashCode();

        image.setProperty(Property.builder().id(5L).title("Another Property For Image").build());
        assertEquals(initialHashCode, image.hashCode(), "HashCode should not change if a field not in hashCode definition (like property) changes.");

        image.setImageUrl("example.com/img_changed.jpg");
        assertNotEquals(initialHashCode, image.hashCode(), "HashCode should change if a field included in hashCode calculation changes.");
    }

    @Test
    @DisplayName("Test with null description for equals and hashCode")
    void testNullDescriptionInEqualsAndHashCode() {
        LocalDateTime commonUploadDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        Image image1 = new Image(1L, property, "url", null, commonUploadDate);
        Image image2 = new Image(1L, property, "url", null, commonUploadDate);
        Image image3 = new Image(1L, property, "url", "desc", commonUploadDate);

        assertEquals(image1, image2);
        assertEquals(image1.hashCode(), image2.hashCode());
        assertNotEquals(image1, image3);
    }
}