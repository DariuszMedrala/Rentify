package org.example.rentify.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Address Entity Unit Tests")
class AddressTest {

    @Test
    @DisplayName("Should create Address with no-args constructor and set fields using setters")
    void testNoArgsConstructorAndSetters() {
        Address address = new Address();
        assertNull(address.getId());
        assertNull(address.getStreetAddress());

        address.setId(1L);
        address.setStreetAddress("123 Main St");
        address.setCity("Springfield");
        address.setPostalCode("62704");
        address.setCountry("USA");
        address.setStateOrProvince("Illinois");

        assertEquals(1L, address.getId());
        assertEquals("123 Main St", address.getStreetAddress());
        assertEquals("Springfield", address.getCity());
        assertEquals("62704", address.getPostalCode());
        assertEquals("USA", address.getCountry());
        assertEquals("Illinois", address.getStateOrProvince());
    }

    @Test
    @DisplayName("Should create Address with all-args constructor")
    void testAllArgsConstructor() {
        Address address = new Address(1L, "456 Oak Ave", "Metropolis", "10001", "USA", "New York");

        assertEquals(1L, address.getId());
        assertEquals("456 Oak Ave", address.getStreetAddress());
        assertEquals("Metropolis", address.getCity());
        assertEquals("10001", address.getPostalCode());
        assertEquals("USA", address.getCountry());
        assertEquals("New York", address.getStateOrProvince());
    }

    @Test
    @DisplayName("Should create Address using builder")
    void testBuilder() {
        Address address = Address.builder()
                .id(2L)
                .streetAddress("789 Pine Ln")
                .city("Gotham")
                .postalCode("07001")
                .country("USA")
                .stateOrProvince("New Jersey")
                .build();

        assertEquals(2L, address.getId());
        assertEquals("789 Pine Ln", address.getStreetAddress());
        assertEquals("Gotham", address.getCity());
        assertEquals("07001", address.getPostalCode());
        assertEquals("USA", address.getCountry());
        assertEquals("New Jersey", address.getStateOrProvince());
    }

    @Test
    @DisplayName("Equals and HashCode should be consistent for same logical objects")
    void testEqualsAndHashCode_SameObjects() {
        Address address1 = new Address(1L, "123 Main St", "Springfield", "62704", "USA", "Illinois");
        Address address2 = new Address(1L, "123 Main St", "Springfield", "62704", "USA", "Illinois");

        assertEquals(address1, address2, "Two addresses with the same field values (excluding relationships) should be equal.");
        assertEquals(address1.hashCode(), address2.hashCode(), "HashCodes should be the same for equal objects.");
    }

    @Test
    @DisplayName("Equals should return false for different objects")
    void testEquals_DifferentObjects() {
        Address address1 = new Address(1L, "123 Main St", "Springfield", "62704", "USA", "Illinois");
        Address address2 = new Address(2L, "456 Oak Ave", "Metropolis", "10001", "USA", "New York");
        Address address3 = new Address(1L, " DIFFERENT ST", "Springfield", "62704", "USA", "Illinois");


        assertNotEquals(address1, address2, "Addresses with different IDs and fields should not be equal.");
        assertNotEquals(address1, address3, "Addresses with same ID but different streetAddress should not be equal.");
        assertNotEquals(null, address1, "Address should not be equal to null.");
        assertNotEquals(new Object(), address1, "Address should not be equal to an object of a different type.");
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void testEquals_SameInstance() {
        Address address1 = new Address(1L, "123 Main St", "Springfield", "62704", "USA", "Illinois");
        assertEquals(address1, address1);
    }

    @Test
    @DisplayName("HashCode should be consistent")
    void testHashCode_Consistency() {
        Address address = new Address(1L, "123 Main St", "Springfield", "62704", "USA", "Illinois");
        int initialHashCode = address.hashCode();
        address.setStreetAddress("Different Street");
        int newHashCode = address.hashCode();
        assertNotEquals(initialHashCode, newHashCode, "HashCode should change if a field included in hashCode calculation changes.");
        address.setStreetAddress("123 Main St");
        assertEquals(initialHashCode, address.hashCode(), "HashCode should be consistent if fields are reverted.");
    }
}