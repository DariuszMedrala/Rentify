package org.example.rentify.mapper;

import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.BookingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BookingMapper Unit Tests")
class BookingMapperTest {

    private BookingMapper bookingMapper;

    private BookingRequestDTO bookingRequestDTO;
    private Booking bookingEntity;

    @BeforeEach
    void setUp() {
        bookingMapper = Mappers.getMapper(BookingMapper.class);
        User userEntity = new User();
        userEntity.setId(1L);
        userEntity.setUsername("testUser");
        Property propertyEntity = new Property();
        propertyEntity.setId(1L);

        bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.setPropertyId(1L);
        bookingRequestDTO.setStartDate(LocalDate.of(2025, 10, 1));
        bookingRequestDTO.setEndDate(LocalDate.of(2025, 10, 10));

        bookingEntity = new Booking();
        bookingEntity.setId(10L);
        bookingEntity.setUser(userEntity);
        bookingEntity.setProperty(propertyEntity);
        bookingEntity.setStartDate(LocalDate.of(2025, 9, 1));
        bookingEntity.setEndDate(LocalDate.of(2025, 9, 5));
        bookingEntity.setTotalPrice(new BigDecimal("500.00"));
        bookingEntity.setBookingDate(LocalDateTime.now().minusDays(2));
        bookingEntity.setBookingStatus(BookingStatus.CONFIRMED);
    }

    @Nested
    @DisplayName("bookingRequestDtoToBooking Tests")
    class BookingRequestDtoToBookingTests {

        @Test
        @DisplayName("Should map BookingRequestDTO to Booking entity correctly")
        void shouldMapDtoToEntity() {
            Booking mappedBooking = bookingMapper.bookingRequestDtoToBooking(bookingRequestDTO);

            assertNotNull(mappedBooking);
            assertEquals(bookingRequestDTO.getStartDate(), mappedBooking.getStartDate());
            assertEquals(bookingRequestDTO.getEndDate(), mappedBooking.getEndDate());

            assertNull(mappedBooking.getId(), "ID should be ignored");
            assertNull(mappedBooking.getUser(), "User should be ignored");
            assertNull(mappedBooking.getProperty(), "Property should be ignored");
            assertNull(mappedBooking.getBookingDate(), "BookingDate should be ignored");
            assertNull(mappedBooking.getBookingStatus(), "BookingStatus should be ignored");
            assertNull(mappedBooking.getTotalPrice(), "TotalPrice should be ignored");
            assertNull(mappedBooking.getPayment(), "Payment should be ignored");
            assertNull(mappedBooking.getReview(), "Review should be ignored");
        }

        @Test
        @DisplayName("Should handle null BookingRequestDTO gracefully")
        void shouldHandleNullDto() {
            Booking mappedBooking = bookingMapper.bookingRequestDtoToBooking(null);
            assertNull(mappedBooking, "Mapping a null DTO should result in a null entity");
        }

        @Test
        @DisplayName("Should map DTO with null date fields to entity with null date fields")
        void shouldMapDtoWithNullDateFields() {
            BookingRequestDTO dtoWithNulls = new BookingRequestDTO();
            dtoWithNulls.setPropertyId(5L);
            Booking mappedBooking = bookingMapper.bookingRequestDtoToBooking(dtoWithNulls);

            assertNotNull(mappedBooking);
            assertNull(mappedBooking.getStartDate());
            assertNull(mappedBooking.getEndDate());
        }
    }

    @Nested
    @DisplayName("bookingToBookingResponseDto Tests")
    class BookingToBookingResponseDtoTests {

        @Test
        @DisplayName("Should map Booking entity to BookingResponseDTO correctly")
        void shouldMapEntityToDto() {
            BookingResponseDTO mappedDto = bookingMapper.bookingToBookingResponseDto(bookingEntity);

            assertNotNull(mappedDto);
            assertEquals(bookingEntity.getId(), mappedDto.getId());
            assertEquals(bookingEntity.getStartDate(), mappedDto.getStartDate());
            assertEquals(bookingEntity.getEndDate(), mappedDto.getEndDate());
            assertEquals(bookingEntity.getTotalPrice(), mappedDto.getTotalPrice());
            assertEquals(bookingEntity.getBookingDate(), mappedDto.getBookingDate());
            assertEquals(bookingEntity.getBookingStatus(), mappedDto.getBookingStatus());
        }

        @Test
        @DisplayName("Should handle null Booking entity gracefully")
        void shouldHandleNullEntity() {
            BookingResponseDTO mappedDto = bookingMapper.bookingToBookingResponseDto(null);
            assertNull(mappedDto, "Mapping a null entity should result in a null DTO");
        }

        @Test
        @DisplayName("Should map entity with null fields to DTO with null fields")
        void shouldMapEntityWithNullFields() {
            Booking entityWithNulls = new Booking();
            entityWithNulls.setId(20L);

            BookingResponseDTO mappedDto = bookingMapper.bookingToBookingResponseDto(entityWithNulls);

            assertNotNull(mappedDto);
            assertEquals(20L, mappedDto.getId());
            assertNull(mappedDto.getStartDate());
            assertNull(mappedDto.getEndDate());
            assertNull(mappedDto.getTotalPrice());
            assertNull(mappedDto.getBookingDate());
            assertNull(mappedDto.getBookingStatus());
        }
    }

    @Nested
    @DisplayName("updateBookingFromDto Tests")
    class UpdateBookingFromDtoTests {

        @Test
        @DisplayName("Should update existing Booking entity from DTO with non-null DTO fields")
        void shouldUpdateEntityFromDto_NonNullFields() {
            Booking targetBooking = new Booking();
            targetBooking.setId(30L);
            targetBooking.setUser(new User());
            targetBooking.setProperty(new Property());
            targetBooking.setBookingDate(LocalDateTime.now().minusDays(5));
            targetBooking.setBookingStatus(BookingStatus.CANCELLED);
            targetBooking.setTotalPrice(new BigDecimal("99.00"));
            targetBooking.setStartDate(LocalDate.of(2024, 1, 1));
            targetBooking.setEndDate(LocalDate.of(2024, 1, 5));


            bookingMapper.updateBookingFromDto(bookingRequestDTO, targetBooking);

            assertEquals(bookingRequestDTO.getStartDate(), targetBooking.getStartDate());
            assertEquals(bookingRequestDTO.getEndDate(), targetBooking.getEndDate());

            assertEquals(30L, targetBooking.getId(), "ID should not be changed");
            assertNotNull(targetBooking.getUser(), "User should not be changed");
            assertNotNull(targetBooking.getProperty(), "Property should not be changed");
            assertNotNull(targetBooking.getBookingDate(), "BookingDate should not be changed");
            assertEquals(BookingStatus.CANCELLED, targetBooking.getBookingStatus(), "BookingStatus should not be changed");
            assertEquals(new BigDecimal("99.00"), targetBooking.getTotalPrice(), "TotalPrice should not be changed");
            assertNull(targetBooking.getPayment(), "Payment should remain ignored/null");
            assertNull(targetBooking.getReview(), "Review should remain ignored/null");
        }

        @Test
        @DisplayName("Should ignore null fields from DTO during update")
        void shouldIgnoreNullFieldsFromDtoDuringUpdate() {
            Booking targetBooking = new Booking();
            LocalDate originalStartDate = LocalDate.of(2023, 1, 1);
            LocalDate originalEndDate = LocalDate.of(2023, 1, 10);
            targetBooking.setStartDate(originalStartDate);
            targetBooking.setEndDate(originalEndDate);

            BookingRequestDTO updateDtoWithNulls = new BookingRequestDTO();

            bookingMapper.updateBookingFromDto(updateDtoWithNulls, targetBooking);

            assertEquals(originalStartDate, targetBooking.getStartDate(), "StartDate should not be updated for null DTO field");
            assertEquals(originalEndDate, targetBooking.getEndDate(), "EndDate should not be updated for null DTO field");
        }
    }
}