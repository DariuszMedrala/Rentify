package org.example.rentify.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Role Entity Unit Tests")
class RoleTest {

    @Test
    @DisplayName("Should create Role with no-args constructor and set fields using setters")
    void testNoArgsConstructorAndSetters() {
        Role role = new Role();
        assertNull(role.getId());
        assertNull(role.getName());
        assertNull(role.getDescription());
        assertNull(role.getUsers());

        role.setId(1L);
        role.setName("ROLE_ADMIN");
        role.setDescription("Administrator role with full permissions");
        Set<User> users = new HashSet<>();
        role.setUsers(users);

        assertEquals(1L, role.getId());
        assertEquals("ROLE_ADMIN", role.getName());
        assertEquals("Administrator role with full permissions", role.getDescription());
        assertEquals(users, role.getUsers());
    }

    @Test
    @DisplayName("Should create Role with all-args constructor")
    void testAllArgsConstructor() {
        Set<User> usersSet = new HashSet<>();
        User user1 = User.builder().id(1L).username("user1").build();
        usersSet.add(user1);

        Role role = new Role(2L, "ROLE_MODERATOR", "Moderator role with specific permissions", usersSet);

        assertEquals(2L, role.getId());
        assertEquals("ROLE_MODERATOR", role.getName());
        assertEquals("Moderator role with specific permissions", role.getDescription());
        assertEquals(usersSet, role.getUsers());
        assertTrue(role.getUsers().contains(user1));
    }

    @Test
    @DisplayName("Should create Role using builder")
    void testBuilder() {
        Set<User> usersForBuilder = new HashSet<>();
        User user2 = User.builder().id(2L).username("user2").build();
        usersForBuilder.add(user2);

        Role role = Role.builder()
                .id(3L)
                .name("ROLE_SUPPORT")
                .description("Support team role")
                .users(usersForBuilder)
                .build();

        assertEquals(3L, role.getId());
        assertEquals("ROLE_SUPPORT", role.getName());
        assertEquals("Support team role", role.getDescription());
        assertEquals(usersForBuilder, role.getUsers());
        assertTrue(role.getUsers().contains(user2));
    }

    @Test
    @DisplayName("Equals and HashCode should be consistent based on defined fields")
    void testEqualsAndHashCode_SameLogicalObjects() {
        Role role1 = Role.builder()
                .id(1L)
                .name("ROLE_TEST")
                .description("A test role")
                .users(Set.of(User.builder().id(1L).build()))
                .build();

        Role role2 = Role.builder()
                .id(1L)
                .name("ROLE_TEST")
                .description("A test role")
                .users(Set.of(User.builder().id(2L).build()))
                .build();

        assertEquals(role1, role2, "Roles with same id, name, and description should be equal.");
        assertEquals(role1.hashCode(), role2.hashCode(), "HashCodes should be the same for equal objects based on defined fields.");
    }

    @Test
    @DisplayName("Equals should return false for different objects based on defined fields")
    void testEquals_DifferentObjects() {
        Role role1 = Role.builder().id(1L).name("ROLE_ONE").description("Desc One").build();
        Role role2_differentId = Role.builder().id(2L).name("ROLE_ONE").description("Desc One").build();
        Role role3_differentName = Role.builder().id(1L).name("ROLE_TWO").description("Desc One").build();
        Role role4_differentDesc = Role.builder().id(1L).name("ROLE_ONE").description("Desc Two").build();

        assertNotEquals(role1, role2_differentId);
        assertNotEquals(role1, role3_differentName);
        assertNotEquals(role1, role4_differentDesc);
        assertNotEquals(null, role1);
        assertNotEquals(new Object(), role1);
    }

    @Test
    @DisplayName("Equals should be true for same instance")
    void testEquals_SameInstance() {
        Role role1 = Role.builder().id(1L).name("ROLE_INSTANCE").build();
        assertEquals(role1, role1);
    }

    @Test
    @DisplayName("HashCode consistency based on defined fields")
    void testHashCode_Consistency() {
        Role role = Role.builder()
                .id(1L)
                .name("CONSISTENT_ROLE")
                .description("Consistent Description")
                .users(new HashSet<>())
                .build();
        int initialHashCode = role.hashCode();

        role.setUsers(Set.of(User.builder().id(10L).build()));
        assertEquals(initialHashCode, role.hashCode(), "HashCode should not change if users (not in hashCode) changes.");

        role.setName("CHANGED_CONSISTENT_ROLE");
        assertNotEquals(initialHashCode, role.hashCode(), "HashCode should change if name (in hashCode) changes.");
    }

    @Test
    @DisplayName("Test with null description for equals and hashCode")
    void testNullDescriptionInEqualsAndHashCode() {
        Role role1 = Role.builder().id(1L).name("ROLE_NULL_DESC").description(null).build();
        Role role2 = Role.builder().id(1L).name("ROLE_NULL_DESC").description(null).build();
        Role role3 = Role.builder().id(1L).name("ROLE_NULL_DESC").description("Not Null").build();

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1, role3);
    }

    @Test
    @DisplayName("Test with null name for equals and hashCode (though name is nullable=false in DB)")
    void testNullNameInEqualsAndHashCode() {
        Role role1 = Role.builder().id(1L).name(null).description("Desc").build();
        Role role2 = Role.builder().id(1L).name(null).description("Desc").build();
        Role role3 = Role.builder().id(1L).name("Not Null").description("Desc").build();

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());
        assertNotEquals(role1, role3);
    }
}