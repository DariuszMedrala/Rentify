package org.example.rentify.entity;

import org.example.rentify.entity.enums.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Property Entity Unit Tests")
class PropertyTest {

    private User owner;
    private Address address;

    @BeforeEach
    void setUp() {
        owner = User.builder().id(1L).username("propertyOwner").build();
        address = Address.builder().id(1L).streetAddress("123 Property Lane").city("PropVille").build();
    }

    @Test
    @DisplayName("Should create Property with no-args constructor and set fields using setters")
    void testNoArgsConstructorAndSetters() {
        Property property = new Property();
        assertNull(property.getId());
        assertTrue(property.getAvailability());
        assertNotNull(property.getCreationDate());

        LocalDateTime creationTime = LocalDateTime.of(2025, 1, 1, 10, 0);
        property.setId(1L);
        property.setOwner(owner);
        property.setTitle("Lovely Cottage");
        property.setDescription("A cozy cottage in the countryside.");
        property.setPropertyType(PropertyType.HOUSE);
        property.setArea(120.75);
        property.setNumberOfRooms(3);
        property.setPricePerDay(new BigDecimal("150.00"));
        property.setAvailability(false);
        property.setCreationDate(creationTime);
        property.setAddress(address);
        property.setImages(new ArrayList<>());
        property.setBookings(new ArrayList<>());
        property.setReviews(new ArrayList<>());

        assertEquals(1L, property.getId());
        assertEquals(owner, property.getOwner());
        assertEquals("Lovely Cottage", property.getTitle());
        assertEquals("A cozy cottage in the countryside.", property.getDescription());
        assertEquals(PropertyType.HOUSE, property.getPropertyType());
        assertEquals(120.75, property.getArea());
        assertEquals(3, property.getNumberOfRooms());
        assertEquals(new BigDecimal("150.00"), property.getPricePerDay());
        assertFalse(property.getAvailability());
        assertEquals(creationTime, property.getCreationDate());
        assertEquals(address, property.getAddress());
        assertNotNull(property.getImages());
        assertNotNull(property.getBookings());
        assertNotNull(property.getReviews());
    }

    @Test
    @DisplayName("Should create Property with all-args constructor")
    void testAllArgsConstructor() {
        LocalDateTime creationTime = LocalDateTime.of(2024, 12, 1, 0, 0);
        List<Image> images = Collections.singletonList(Image.builder().id(1L).build());
        List<Booking> bookings = Collections.singletonList(Booking.builder().id(1L).build());
        List<Review> reviews = Collections.singletonList(Review.builder().id(1L).build());

        Property property = new Property(
                2L, owner, "Grand Villa", "A luxurious villa with a pool.",
                PropertyType.VILLA, 350.50, 7, new BigDecimal("500.00"),
                true, creationTime, address, images, bookings, reviews
        );

        assertEquals(2L, property.getId());
        assertEquals(owner, property.getOwner());
        assertEquals("Grand Villa", property.getTitle());
        assertEquals("A luxurious villa with a pool.", property.getDescription());
        assertEquals(PropertyType.VILLA, property.getPropertyType());
        assertEquals(350.50, property.getArea());
        assertEquals(7, property.getNumberOfRooms());
        assertEquals(new BigDecimal("500.00"), property.getPricePerDay());
        assertTrue(property.getAvailability());
        assertEquals(creationTime, property.getCreationDate());
        assertEquals(address, property.getAddress());
        assertEquals(images, property.getImages());
        assertEquals(bookings, property.getBookings());
        assertEquals(reviews, property.getReviews());
    }

    @Test
    @DisplayName("Should create Property using builder with default values")
    void testBuilderWithDefaults() {
        Property property = Property.builder()
                .id(3L)
                .owner(owner)
                .title("Modern Apartment")
                .description("City center apartment with great views.")
                .propertyType(PropertyType.APARTMENT)
                .area(85.0)
                .numberOfRooms(2)
                .pricePerDay(new BigDecimal("220.00"))
                .address(address)
                .images(new ArrayList<>())
                .bookings(new ArrayList<>())
                .reviews(new ArrayList<>())
                .build();

        assertEquals(3L, property.getId());
        assertEquals(owner, property.getOwner());
        assertEquals("Modern Apartment", property.getTitle());
        assertEquals(PropertyType.APARTMENT, property.getPropertyType());
        assertTrue(property.getAvailability(), "Availability should default to true via @Builder.Default");
        assertNotNull(property.getCreationDate(), "CreationDate should default to current time via @Builder.Default");
        assertTrue(property.getCreationDate().isBefore(LocalDateTime.now().plusSeconds(1)) &&
                        property.getCreationDate().isAfter(LocalDateTime.now().minusSeconds(5)),
                "Default CreationDate should be close to current time");
    }

    @Test
    @DisplayName("Should create Property using builder overriding default values")
    void testBuilderOverridingDefaults() {
        LocalDateTime specificCreationTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        Property property = Property.builder()
                .id(4L)
                .owner(owner)
                .title("Rustic Cabin")
                .propertyType(PropertyType.HOUSE)
                .area(60.0)
                .pricePerDay(new BigDecimal("90.00"))
                .availability(false)
                .creationDate(specificCreationTime)
                .address(address)
                .build();

        assertEquals(4L, property.getId());
        assertFalse(property.getAvailability(), "Availability should be overridden by builder");
        assertEquals(specificCreationTime, property.getCreationDate(), "CreationDate should be overridden by builder");
    }


    @Test
    @DisplayName("Equals and HashCode should be consistent based on defined fields")
    void testEqualsAndHashCode_SameLogicalObjects() {
        LocalDateTime commonCreationDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        Property property1 = Property.builder()
                .id(1L).title("Test Prop").description("Desc").propertyType(PropertyType.HOUSE)
                .area(100.0).numberOfRooms(3).pricePerDay(new BigDecimal("100"))
                .availability(true).creationDate(commonCreationDate)
                .owner(owner).address(address)
                .build();

        Property property2 = Property.builder()
                .id(1L).title("Test Prop").description("Desc").propertyType(PropertyType.HOUSE)
                .area(100.0).numberOfRooms(3).pricePerDay(new BigDecimal("100"))
                .availability(true).creationDate(commonCreationDate)
                .owner(User.builder().id(99L).build())
                .address(Address.builder().id(99L).build())
                .build();

        assertEquals(property1, property2, "Properties with same key fields (id, title, desc, type, area, rooms, price, avail, creationDate) should be equal.");
        assertEquals(property1.hashCode(), property2.hashCode(), "HashCodes should be the same for equal objects based on defined fields.");
    }

    @Test
    @DisplayName("Equals should return false for different objects based on defined fields")
    void testEquals_DifferentObjects() {
        LocalDateTime commonCreationDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        BigDecimal commonPrice = new BigDecimal("100");
        Double commonArea = 100.0;
        Integer commonRooms = 3;
        Boolean commonAvailability = true;

        Property property1 = Property.builder()
                .id(1L)
                .title("Base Title")
                .description("Base Description")
                .propertyType(PropertyType.HOUSE) // Typ 1
                .area(commonArea)
                .numberOfRooms(commonRooms)
                .pricePerDay(commonPrice)
                .availability(commonAvailability)
                .creationDate(commonCreationDate)
                .build();

        Property property_differentId = Property.builder()
                .id(2L)
                .title("Base Title")
                .description("Base Description")
                .propertyType(PropertyType.HOUSE)
                .area(commonArea)
                .numberOfRooms(commonRooms)
                .pricePerDay(commonPrice)
                .availability(commonAvailability)
                .creationDate(commonCreationDate)
                .build();

        Property property_differentTitle = Property.builder()
                .id(1L)
                .title("DIFFERENT Title")
                .description("Base Description")
                .propertyType(PropertyType.HOUSE)
                .area(commonArea)
                .numberOfRooms(commonRooms)
                .pricePerDay(commonPrice)
                .availability(commonAvailability)
                .creationDate(commonCreationDate)
                .build();

        Property property_differentCreationDate = Property.builder()
                .id(1L)
                .title("Base Title")
                .description("Base Description")
                .propertyType(PropertyType.HOUSE)
                .area(commonArea)
                .numberOfRooms(commonRooms)
                .pricePerDay(commonPrice)
                .availability(commonAvailability)
                .creationDate(commonCreationDate.plusDays(1))
                .build();

        assertNotEquals(property1, property_differentId, "Should be false due to different ID");
        assertNotEquals(property1, property_differentTitle, "Should be false due to different Title");
        assertNotEquals(property1, property_differentCreationDate, "Should be false due to different CreationDate");
        assertNotEquals(null, property1, "Should be false when comparing with null");
        assertNotEquals(new Object(), property1, "Should be false when comparing with different type");
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void testEquals_SameInstance() {
        Property property1 = Property.builder().id(1L).title("Prop").build();
        assertEquals(property1, property1);
    }

    @Test
    @DisplayName("HashCode consistency based on defined fields")
    void testHashCode_Consistency() {
        Property property = Property.builder()
                .id(1L).title("Consistent Prop").description("Desc").propertyType(PropertyType.APARTMENT)
                .area(100.0).numberOfRooms(3).pricePerDay(new BigDecimal("100"))
                .availability(true).creationDate(LocalDateTime.of(2025,1,1,0,0))
                .owner(owner)
                .build();
        int initialHashCode = property.hashCode();

        property.setOwner(User.builder().id(2L).build());
        assertEquals(initialHashCode, property.hashCode(), "HashCode should not change if owner (not in hashCode) changes.");
        property.setTitle("Changed Consistent Prop");
        assertNotEquals(initialHashCode, property.hashCode(), "HashCode should change if title (in hashCode) changes.");
    }

    @Test
    @DisplayName("Test with null description and numberOfRooms for equals and hashCode")
    void testNullFieldsInEqualsAndHashCode() {
        LocalDateTime commonCreationDate = LocalDateTime.of(2025, 1, 1, 12, 0, 0);
        PropertyType commonType = PropertyType.PENTHOUSE;
        Double commonArea = 50.0;
        BigDecimal commonPrice = new BigDecimal("50");
        Boolean commonAvailability = true;


        Property property1 = Property.builder().id(1L).title("Test").description(null).propertyType(commonType).area(commonArea).numberOfRooms(null).pricePerDay(commonPrice).availability(commonAvailability).creationDate(commonCreationDate).build();
        Property property2 = Property.builder().id(1L).title("Test").description(null).propertyType(commonType).area(commonArea).numberOfRooms(null).pricePerDay(commonPrice).availability(commonAvailability).creationDate(commonCreationDate).build();
        Property property3 = Property.builder().id(1L).title("Test").description("Has Desc").propertyType(commonType).area(commonArea).numberOfRooms(null).pricePerDay(commonPrice).availability(commonAvailability).creationDate(commonCreationDate).build();
        Property property4 = Property.builder().id(1L).title("Test").description(null).propertyType(commonType).area(commonArea).numberOfRooms(2).pricePerDay(commonPrice).availability(commonAvailability).creationDate(commonCreationDate).build();

        assertEquals(property1, property2);
        assertEquals(property1.hashCode(), property2.hashCode());
        assertNotEquals(property1, property3);
        assertNotEquals(property1, property4);
    }
}