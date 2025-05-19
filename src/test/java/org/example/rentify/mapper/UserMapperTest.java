package org.example.rentify.mapper;

import org.example.rentify.dto.registration.UserRegistrationDTO;
import org.example.rentify.dto.request.AddressRequestDTO;
import org.example.rentify.dto.request.UserRequestDTO;
import org.example.rentify.dto.response.RoleResponseDTO;
import org.example.rentify.dto.response.UserResponseDTO;
import org.example.rentify.entity.Address;
import org.example.rentify.entity.Role;
import org.example.rentify.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {
        UserMapperImpl.class,
        AddressMapperImpl.class,
        RoleMapperImpl.class
})
@DisplayName("UserMapper Integration Tests (Spring Context)")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    private UserRegistrationDTO userRegistrationDTO;
    private UserRequestDTO userRequestDTO;
    private User userEntity;
    private Address addressEntity;
    private Role roleEntity;

    @BeforeEach
    void setUp() {
        AddressRequestDTO addressRequestDTO = new AddressRequestDTO("123 Main St", "Test City", "Test State", "Test Country", "12345");

        userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setUsername("newUser");
        userRegistrationDTO.setPassword("password123");
        userRegistrationDTO.setEmail("newuser@example.com");
        userRegistrationDTO.setFirstName("New");
        userRegistrationDTO.setLastName("User");
        userRegistrationDTO.setPhoneNumber("123456789");
        userRegistrationDTO.setAddress(addressRequestDTO);


        userRequestDTO = new UserRequestDTO();
        userRequestDTO.setUsername("updatedUser");
        userRequestDTO.setEmail("updated@example.com");
        userRequestDTO.setFirstName("Updated");
        userRequestDTO.setLastName("Name");
        userRequestDTO.setPhoneNumber("987654321");
        userRequestDTO.setAddress(new AddressRequestDTO("456 Updated St", "UpdCity", "UpdState", "UpdCountry", "U6789"));

        roleEntity = new Role();
        roleEntity.setId(1L);
        roleEntity.setName("USER");
        roleEntity.setDescription("Standard user role");

        addressEntity = new Address();
        addressEntity.setId(1L);
        addressEntity.setStreetAddress("789 Old St");
        addressEntity.setCity("OldCity");
        addressEntity.setStateOrProvince("OldState");
        addressEntity.setCountry("OldCountry");
        addressEntity.setPostalCode("O0000");

        userEntity = new User();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        userEntity.setPassword("encodedPassword");
        userEntity.setEmail("test@example.com");
        userEntity.setFirstName("Test");
        userEntity.setLastName("User");
        userEntity.setPhoneNumber("111222333");
        userEntity.setRegistrationDate(LocalDate.now().minusMonths(1));
        userEntity.setAddress(addressEntity);
        userEntity.setRoles(new HashSet<>(Collections.singletonList(roleEntity)));
        userEntity.setEnabled(true);
        userEntity.setAccountNonExpired(true);
        userEntity.setAccountNonLocked(true);
        userEntity.setCredentialsNonExpired(true);
    }

    @Nested
    @DisplayName("userRegistrationDtoToUser Tests")
    class UserRegistrationDtoToUserTests {

        @Test
        @DisplayName("Should map UserRegistrationDTO to User entity correctly")
        void shouldMapDtoToEntity() {
            User mappedUser = userMapper.userRegistrationDtoToUser(userRegistrationDTO);

            assertNotNull(mappedUser);
            assertEquals(userRegistrationDTO.getUsername(), mappedUser.getUsername());
            assertEquals(userRegistrationDTO.getEmail(), mappedUser.getEmail());
            assertEquals(userRegistrationDTO.getFirstName(), mappedUser.getFirstName());
            assertEquals(userRegistrationDTO.getLastName(), mappedUser.getLastName());
            assertEquals(userRegistrationDTO.getPhoneNumber(), mappedUser.getPhoneNumber());

            assertNotNull(mappedUser.getAddress(), "Address should be mapped from nested DTO");
            assertEquals(userRegistrationDTO.getAddress().getStreetAddress(), mappedUser.getAddress().getStreetAddress());
            assertNull(mappedUser.getId(), "ID should be ignored");
            assertNull(mappedUser.getPassword(), "Password from UserRegistrationDTO is not directly mapped by UserMapper; typically handled by service");
            assertNull(mappedUser.getProperties(), "Properties should be ignored");
            assertNull(mappedUser.getBookings(), "Bookings should be ignored");
            assertNull(mappedUser.getReviews(), "Reviews should be ignored");
            assertNull(mappedUser.getRegistrationDate(), "RegistrationDate should be ignored");
            assertNull(mappedUser.getPayments(), "Payments should be ignored");

            assertTrue(mappedUser.isEnabled(), "Enabled should be set to true by constant mapping");
            assertTrue(mappedUser.isAccountNonExpired(), "AccountNonExpired should be set to true");
            assertTrue(mappedUser.isAccountNonLocked(), "AccountNonLocked should be set to true");
            assertTrue(mappedUser.isCredentialsNonExpired(), "CredentialsNonExpired should be set to true");
        }

        @Test
        @DisplayName("Should handle null UserRegistrationDTO gracefully")
        void shouldHandleNullDto() {
            User mappedUser = userMapper.userRegistrationDtoToUser(null);
            assertNull(mappedUser);
        }
    }

    @Nested
    @DisplayName("userRequestDtoToUser Tests")
    class UserRequestDtoToUserTests {
        @Test
        @DisplayName("Should map UserRequestDTO to User entity correctly")
        void shouldMapDtoToEntity() {
            User mappedUser = userMapper.userRequestDtoToUser(userRequestDTO);

            assertNotNull(mappedUser);
            assertEquals(userRequestDTO.getUsername(), mappedUser.getUsername());
            assertEquals(userRequestDTO.getEmail(), mappedUser.getEmail());
            assertEquals(userRequestDTO.getFirstName(), mappedUser.getFirstName());
            assertEquals(userRequestDTO.getLastName(), mappedUser.getLastName());
            assertEquals(userRequestDTO.getPhoneNumber(), mappedUser.getPhoneNumber());

            assertNotNull(mappedUser.getAddress());
            assertEquals(userRequestDTO.getAddress().getStreetAddress(), mappedUser.getAddress().getStreetAddress());

            assertNull(mappedUser.getId());
            assertNull(mappedUser.getPassword());
        }

        @Test
        @DisplayName("Should handle null UserRequestDTO gracefully")
        void shouldHandleNullDto() {
            User mappedUser = userMapper.userRequestDtoToUser(null);
            assertNull(mappedUser);
        }
    }


    @Nested
    @DisplayName("updateUserFromDto Tests")
    class UpdateUserFromDtoTests {

        @Test
        @DisplayName("Should update existing User entity from UserRequestDTO")
        void shouldUpdateEntityFromDto() {
            User targetUser = new User();
            targetUser.setId(5L);
            targetUser.setUsername("originalUser");
            targetUser.setEmail("original@example.com");
            targetUser.setPassword("originalEncodedPassword");
            Address originalAddress = new Address();
            originalAddress.setId(2L);
            originalAddress.setStreetAddress("1 Old Street");
            targetUser.setAddress(originalAddress);


            userMapper.updateUserFromDto(userRequestDTO, targetUser);

            assertEquals(userRequestDTO.getUsername(), targetUser.getUsername());
            assertEquals(userRequestDTO.getEmail(), targetUser.getEmail());
            assertEquals(userRequestDTO.getFirstName(), targetUser.getFirstName());
            assertEquals(userRequestDTO.getLastName(), targetUser.getLastName());
            assertEquals(userRequestDTO.getPhoneNumber(), targetUser.getPhoneNumber());

            assertNotNull(targetUser.getAddress());
            assertEquals(userRequestDTO.getAddress().getStreetAddress(), targetUser.getAddress().getStreetAddress());
            assertEquals(2L, targetUser.getAddress().getId(), "ID of existing Address should be preserved if AddressMapper updates in place");


            assertEquals(5L, targetUser.getId(), "ID should not be changed");
            assertEquals("originalEncodedPassword", targetUser.getPassword(), "Password should not be changed by this DTO mapping");
        }

        @Test
        @DisplayName("Should ignore null fields from UserRequestDTO during update")
        void shouldIgnoreNullFieldsFromDto() {
            User targetUser = new User();
            targetUser.setUsername("originalUser");
            targetUser.setEmail("original@example.com");
            targetUser.setFirstName("OriginalFirst");

            UserRequestDTO updateDtoWithNulls = new UserRequestDTO();
            updateDtoWithNulls.setUsername("newUsername");
            updateDtoWithNulls.setEmail(null);
            updateDtoWithNulls.setFirstName(null);
            userMapper.updateUserFromDto(updateDtoWithNulls, targetUser);

            assertEquals("newUsername", targetUser.getUsername());
            assertEquals("original@example.com", targetUser.getEmail(), "Email should not be updated if DTO field is null");
            assertEquals("OriginalFirst", targetUser.getFirstName(), "FirstName should not be updated if DTO field is null");
        }

        @Test
        @DisplayName("Should ignore null AddressRequestDTO in UserRequestDTO during update")
        void shouldIgnoreNullNestedAddressDtoDuringUpdate() {
            User targetUser = new User();
            targetUser.setUsername("userWithAddress");
            Address originalAddress = new Address();
            originalAddress.setStreetAddress("Keep This Street");
            targetUser.setAddress(originalAddress);

            UserRequestDTO dtoWithNullAddress = new UserRequestDTO();
            dtoWithNullAddress.setUsername("usernameChanged");
            dtoWithNullAddress.setAddress(null);

            userMapper.updateUserFromDto(dtoWithNullAddress, targetUser);

            assertEquals("usernameChanged", targetUser.getUsername());
            assertNotNull(targetUser.getAddress(), "Address should NOT become null due to IGNORE strategy");
            assertEquals("Keep This Street", targetUser.getAddress().getStreetAddress(), "Address should remain unchanged");
        }
    }

    @Nested
    @DisplayName("userToUserResponseDto Tests")
    class UserToUserResponseDtoTests {

        @Test
        @DisplayName("Should map User entity to UserResponseDTO correctly")
        void shouldMapEntityToDto() {
            UserResponseDTO mappedDto = userMapper.userToUserResponseDto(userEntity);

            assertNotNull(mappedDto);
            assertEquals(userEntity.getId(), mappedDto.getId());
            assertEquals(userEntity.getUsername(), mappedDto.getUsername());
            assertEquals(userEntity.getEmail(), mappedDto.getEmail());
            assertEquals(userEntity.getFirstName(), mappedDto.getFirstName());
            assertEquals(userEntity.getLastName(), mappedDto.getLastName());
            assertEquals(userEntity.getPhoneNumber(), mappedDto.getPhoneNumber());
            assertEquals(userEntity.getRegistrationDate(), mappedDto.getRegistrationDate());

            assertNotNull(mappedDto.getAddress(), "AddressResponseDTO should be mapped");
            assertEquals(addressEntity.getStreetAddress(), mappedDto.getAddress().getStreetAddress());

            assertNotNull(mappedDto.getRoles(), "Set of RoleResponseDTO should be mapped");
            assertEquals(1, mappedDto.getRoles().size());
            RoleResponseDTO mappedRoleDto = mappedDto.getRoles().iterator().next();
            assertEquals(roleEntity.getName(), mappedRoleDto.getName());
        }

        @Test
        @DisplayName("Should handle null User entity gracefully")
        void shouldHandleNullEntity() {
            UserResponseDTO mappedDto = userMapper.userToUserResponseDto(null);
            assertNull(mappedDto);
        }

        @Test
        @DisplayName("Should map entity with null Address and Roles to DTO")
        void shouldMapEntityWithNullAddressAndRoles() {
            userEntity.setAddress(null);
            userEntity.setRoles(null);

            UserResponseDTO mappedDto = userMapper.userToUserResponseDto(userEntity);

            assertNotNull(mappedDto);
            assertNull(mappedDto.getAddress());
            assertNull(mappedDto.getRoles());

            userEntity.setRoles(Collections.emptySet());
            mappedDto = userMapper.userToUserResponseDto(userEntity);
            assertNotNull(mappedDto.getRoles());
            assertTrue(mappedDto.getRoles().isEmpty());
        }
    }
}