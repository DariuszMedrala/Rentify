package org.example.rentify.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("User Entity Unit Tests")
class UserTest {

    private Address address;
    private Role roleUser;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        address = Address.builder().id(1L).streetAddress("1 Test St").city("Testville").build();
        roleUser = Role.builder().id(1L).name("USER").build();
        roleAdmin = Role.builder().id(2L).name("ADMIN").build();
    }

    @Test
    @DisplayName("Should create User with no-args constructor and set fields using setters")
    void testNoArgsConstructorAndSetters() {
        User user = new User();
        assertNull(user.getId());
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());

        LocalDate regDate = LocalDate.of(2025, 1, 1);
        user.setId(1L);
        user.setUsername("john_doe");
        user.setPassword("secretPassword");
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPhoneNumber("123456789");
        user.setRegistrationDate(regDate);
        user.setAccountNonExpired(false);
        user.setAccountNonLocked(false);
        user.setCredentialsNonExpired(false);
        user.setEnabled(false);
        user.setAddress(address);
        user.setProperties(new ArrayList<>());
        user.setBookings(new ArrayList<>());
        user.setReviews(new ArrayList<>());
        user.setPayments(new ArrayList<>());
        user.setRoles(Set.of(roleUser));

        assertEquals(1L, user.getId());
        assertEquals("john_doe", user.getUsername());
        assertEquals("secretPassword", user.getPassword());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("123456789", user.getPhoneNumber());
        assertEquals(regDate, user.getRegistrationDate());
        assertFalse(user.isAccountNonExpired());
        assertFalse(user.isAccountNonLocked());
        assertFalse(user.isCredentialsNonExpired());
        assertFalse(user.isEnabled());
        assertEquals(address, user.getAddress());
        assertNotNull(user.getProperties());
        assertNotNull(user.getBookings());
        assertNotNull(user.getReviews());
        assertNotNull(user.getPayments());
        assertTrue(user.getRoles().contains(roleUser));
    }

    @Test
    @DisplayName("Should create User with all-args constructor")
    void testAllArgsConstructor() {
        LocalDate regDate = LocalDate.of(2024, 10, 15);
        Set<Role> roles = new HashSet<>(Set.of(roleAdmin));
        User user = new User(2L, "jane_doe", "pa$$wOrd", "jane@example.com",
                "Jane", "Doe", "987654321", regDate,
                false, false, false, false,
                address, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), roles);

        assertEquals(2L, user.getId());
        assertEquals("jane_doe", user.getUsername());
        assertEquals("pa$$wOrd", user.getPassword());
        assertEquals("jane@example.com", user.getEmail());
        assertEquals("Jane", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("987654321", user.getPhoneNumber());
        assertEquals(regDate, user.getRegistrationDate());
        assertFalse(user.isAccountNonExpired());
        assertFalse(user.isAccountNonLocked());
        assertFalse(user.isCredentialsNonExpired());
        assertFalse(user.isEnabled());
        assertEquals(address, user.getAddress());
        assertTrue(user.getRoles().contains(roleAdmin));
    }

    @Test
    @DisplayName("Should create User using builder with default security flags and roles")
    void testBuilderWithDefaults() {
        User user = User.builder()
                .id(3L)
                .username("builder_user")
                .password("builder_pass")
                .email("builder@example.com")
                .firstName("Builder")
                .lastName("Test")
                .phoneNumber("555123456")
                .registrationDate(LocalDate.now())
                .address(address)
                .build();

        assertEquals(3L, user.getId());
        assertEquals("builder_user", user.getUsername());
        assertTrue(user.isAccountNonExpired(), "AccountNonExpired should default to true");
        assertTrue(user.isAccountNonLocked(), "AccountNonLocked should default to true");
        assertTrue(user.isCredentialsNonExpired(), "CredentialsNonExpired should default to true");
        assertTrue(user.isEnabled(), "Enabled should default to true");
        assertNotNull(user.getRoles(), "Roles should default to an empty set");
        assertTrue(user.getRoles().isEmpty(), "Roles should default to an empty set");
        assertNull(user.getProperties());
        assertNull(user.getBookings());
        assertNull(user.getReviews());
        assertNull(user.getPayments());

    }

    @Test
    @DisplayName("Should create User using builder overriding default security flags and setting roles")
    void testBuilderOverridingDefaults() {
        User user = User.builder()
                .id(4L)
                .username("custom_user")
                .password("custom_pass")
                .email("custom@example.com")
                .accountNonExpired(false)
                .accountNonLocked(false)
                .credentialsNonExpired(false)
                .enabled(false)
                .roles(new HashSet<>(Set.of(roleUser, roleAdmin)))
                .build();

        assertEquals(4L, user.getId());
        assertFalse(user.isAccountNonExpired());
        assertFalse(user.isAccountNonLocked());
        assertFalse(user.isCredentialsNonExpired());
        assertFalse(user.isEnabled());
        assertEquals(2, user.getRoles().size());
        assertTrue(user.getRoles().contains(roleUser));
        assertTrue(user.getRoles().contains(roleAdmin));
    }


    @Test
    @DisplayName("getAuthorities should return correct authorities based on roles")
    void testGetAuthorities() {
        User userWithRoles = User.builder()
                .roles(new HashSet<>(Set.of(roleUser, roleAdmin)))
                .build();

        Collection<? extends GrantedAuthority> authorities = userWithRoles.getAuthorities();
        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_USER")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    @Test
    @DisplayName("getAuthorities should return empty set if roles is null")
    void testGetAuthorities_NullRoles() {
        User userWithNullRoles = User.builder().roles(null).build();
        Collection<? extends GrantedAuthority> authorities = userWithNullRoles.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    @DisplayName("getAuthorities should return empty set if roles is empty")
    void testGetAuthorities_EmptyRoles() {
        User userWithEmptyRoles = User.builder().roles(new HashSet<>()).build();
        Collection<? extends GrantedAuthority> authorities = userWithEmptyRoles.getAuthorities();
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    @DisplayName("Equals and HashCode should be consistent based on defined fields")
    void testEqualsAndHashCode_SameLogicalObjects() {
        LocalDate commonRegDate = LocalDate.of(2025, 1, 1);
        User user1 = User.builder()
                .id(1L).username("test").password("pass").email("e@e.com")
                .firstName("F").lastName("L").phoneNumber("123").registrationDate(commonRegDate)
                .accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true).enabled(true)
                .address(address).roles(Set.of(roleUser))
                .build();

        User user2 = User.builder()
                .id(1L).username("test").password("pass").email("e@e.com")
                .firstName("F").lastName("L").phoneNumber("123").registrationDate(commonRegDate)
                .accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true).enabled(true)
                .address(Address.builder().id(99L).build())
                .roles(Set.of(roleAdmin))
                .build();

        assertEquals(user1, user2, "Users with same key fields should be equal.");
        assertEquals(user1.hashCode(), user2.hashCode(), "Hashcodes should be the same for equal objects based on defined fields.");
    }

    @Test
    @DisplayName("Equals should return false for different objects based on defined fields")
    void testEquals_DifferentObjects() {
        LocalDate commonRegDate = LocalDate.of(2025, 1, 1);
        User user1 = User.builder()
                .id(1L).username("user1").password("p1").email("e1@e.com")
                .firstName("F1").lastName("L1").phoneNumber("111").registrationDate(commonRegDate)
                .accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true).enabled(true)
                .build();

        User user2_differentId = User.builder().id(2L).username("user1").password("p1").email("e1@e.com").firstName("F1").lastName("L1").phoneNumber("111").registrationDate(commonRegDate).accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true).enabled(true).build();
        User user3_differentUsername = User.builder().id(1L).username("user2").password("p1").email("e1@e.com").firstName("F1").lastName("L1").phoneNumber("111").registrationDate(commonRegDate).accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true).enabled(true).build();
        User user4_differentEnabled = User.builder().id(1L).username("user1").password("p1").email("e1@e.com").firstName("F1").lastName("L1").phoneNumber("111").registrationDate(commonRegDate).accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true).enabled(false).build();


        assertNotEquals(user1, user2_differentId);
        assertNotEquals(user1, user3_differentUsername);
        assertNotEquals(user1, user4_differentEnabled);
        assertNotEquals(null, user1);
        assertNotEquals(new Object(), user1);
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void testEquals_SameInstance() {
        User user1 = User.builder().id(1L).username("instance_user").build();
        assertEquals(user1, user1);
    }

    @Test
    @DisplayName("HashCode consistency based on defined fields")
    void testHashCode_Consistency() {
        User user = User.builder()
                .id(1L).username("consistent_user").password("pass").email("cons@e.com")
                .firstName("Cons").lastName("Istent").phoneNumber("777").registrationDate(LocalDate.now())
                .accountNonExpired(true).accountNonLocked(true).credentialsNonExpired(true).enabled(true)
                .address(address)
                .build();
        int initialHashCode = user.hashCode();

        user.setAddress(Address.builder().id(2L).city("NewCity").build());
        assertEquals(initialHashCode, user.hashCode(), "HashCode should not change if address (not in hashCode) changes.");

        user.setUsername("changed_consistent_user");
        assertNotEquals(initialHashCode, user.hashCode(), "HashCode should change if username (in hashCode) changes.");
    }
}