package org.example.rentify.service;

import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.Payment;
import org.example.rentify.entity.enums.BookingStatus;
import org.example.rentify.entity.enums.PaymentStatus;
import org.example.rentify.mapper.BookingMapper;
import org.example.rentify.repository.BookingRepository;
import org.example.rentify.repository.PropertyRepository;
import org.example.rentify.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService Unit Tests")
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private PropertyRepository propertyRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingMapper bookingMapper;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Property property;
    private Booking booking;
    private BookingRequestDTO bookingRequestDTO;
    private BookingResponseDTO bookingResponseDTO;
    private Payment payment;

    private final String testUsername = "testUser";
    private final Long propertyId = 1L;
    private final Long bookingId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername(testUsername);

        property = new Property();
        property.setId(propertyId);
        property.setPricePerDay(new BigDecimal("100.00"));
        property.setAvailability(true);

        bookingRequestDTO = new BookingRequestDTO();
        bookingRequestDTO.setPropertyId(propertyId);
        bookingRequestDTO.setStartDate(LocalDate.now().plusDays(1));
        bookingRequestDTO.setEndDate(LocalDate.now().plusDays(3));

        booking = new Booking();
        booking.setId(bookingId);
        booking.setUser(user);
        booking.setProperty(property);
        booking.setStartDate(bookingRequestDTO.getStartDate());
        booking.setEndDate(bookingRequestDTO.getEndDate());
        booking.setTotalPrice(new BigDecimal("300.00"));
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingStatus(BookingStatus.PENDING);

        bookingResponseDTO = new BookingResponseDTO();
        bookingResponseDTO.setId(bookingId);
        bookingResponseDTO.setStartDate(booking.getStartDate());
        bookingResponseDTO.setEndDate(booking.getEndDate());
        bookingResponseDTO.setTotalPrice(booking.getTotalPrice());

        payment = new Payment();
        payment.setId(1L);
        booking.setPayment(payment);
    }

    @Nested
    @DisplayName("createBooking Tests")
    class CreateBookingTests {

        @Test
        @DisplayName("Should create booking successfully when property available and no overlaps")
        void createBooking_whenPropertyAvailableAndNoOverlap_shouldSucceed() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            when(bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    propertyId, bookingRequestDTO.getEndDate(), bookingRequestDTO.getStartDate()))
                    .thenReturn(Collections.emptyList());
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(bookingMapper.bookingRequestDtoToBooking(bookingRequestDTO)).thenReturn(booking);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
                Booking savedBooking = invocation.getArgument(0);
                savedBooking.setId(2L);
                return savedBooking;
            });
            when(bookingMapper.bookingToBookingResponseDto(any(Booking.class))).thenReturn(new BookingResponseDTO());


            MessageResponseDTO response = bookingService.createBooking(bookingRequestDTO, testUsername);

            assertNotNull(response);
            assertTrue(response.getMessage().startsWith("Booking created successfully with ID:"));
            verify(propertyRepository).findPropertyById(propertyId);
            verify(bookingRepository).findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(anyLong(), any(LocalDate.class), any(LocalDate.class));
            verify(userRepository).findUserByUsername(testUsername);
            verify(bookingMapper).bookingRequestDtoToBooking(bookingRequestDTO);
            verify(bookingRepository).save(any(Booking.class));
            verify(bookingMapper).bookingToBookingResponseDto(any(Booking.class));
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not found")
        void createBooking_whenPropertyNotFound_shouldThrowNotFoundException() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.createBooking(bookingRequestDTO, testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when property not available")
        void createBooking_whenPropertyNotAvailable_shouldThrowBadRequestException() {
            property.setAvailability(false);
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.createBooking(bookingRequestDTO, testUsername));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Property is not available for booking", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when dates overlap")
        void createBooking_whenDatesOverlap_shouldThrowConflictException() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            when(bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    propertyId, bookingRequestDTO.getEndDate(), bookingRequestDTO.getStartDate()))
                    .thenReturn(List.of(new Booking()));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.createBooking(bookingRequestDTO, testUsername));
            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("Property is already booked for the selected dates", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found")
        void createBooking_whenUserNotFound_shouldThrowNotFoundException() {
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            when(bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(anyLong(), any(), any())).thenReturn(Collections.emptyList());
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.createBooking(bookingRequestDTO, testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("User not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when end date is not after start date")
        void createBooking_whenEndDateNotAfterStartDate_shouldThrowBadRequestException() {
            bookingRequestDTO.setEndDate(bookingRequestDTO.getStartDate().minusDays(1));
            when(propertyRepository.findPropertyById(propertyId)).thenReturn(Optional.of(property));
            when(bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(anyLong(), any(), any())).thenReturn(Collections.emptyList());
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(bookingMapper.bookingRequestDtoToBooking(bookingRequestDTO)).thenReturn(booking);


            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.createBooking(bookingRequestDTO, testUsername));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("End date must be after start date", exception.getReason());
        }
    }

    @Nested
    @DisplayName("getAllBookingsFromLoggedUser Tests")
    class GetAllBookingsFromLoggedUserTests {
        @Test
        @DisplayName("Should return bookings for logged user")
        void getAllBookingsFromLoggedUser_whenUserHasBookings_shouldReturnListOfBookingResponseDTO() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(bookingRepository.findByUserId(user.getId())).thenReturn(List.of(booking));
            when(bookingMapper.bookingToBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDTO);

            List<BookingResponseDTO> result = bookingService.getAllBookingsFromLoggedUser(testUsername);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(bookingResponseDTO, result.getFirst());
            verify(userRepository).findUserByUsername(testUsername);
            verify(bookingRepository).findByUserId(user.getId());
            verify(bookingMapper).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when user not found")
        void getAllBookingsFromLoggedUser_whenUserNotFound_shouldThrowNotFoundException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.getAllBookingsFromLoggedUser(testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("User not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no bookings found for user")
        void getAllBookingsFromLoggedUser_whenNoBookingsFound_shouldThrowNotFoundException() {
            when(userRepository.findUserByUsername(testUsername)).thenReturn(Optional.of(user));
            when(bookingRepository.findByUserId(user.getId())).thenReturn(Collections.emptyList());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.getAllBookingsFromLoggedUser(testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No bookings found for this user", exception.getReason());
        }
    }

    @Nested
    @DisplayName("getAllBookingsByPropertyId Tests")
    class GetAllBookingsByPropertyIdTests {
        @Test
        @DisplayName("Should return bookings for property ID")
        void getAllBookingsByPropertyId_whenPropertyHasBookings_shouldReturnListOfBookingResponseDTO() {
            when(bookingRepository.findByPropertyId(propertyId)).thenReturn(List.of(booking));
            when(bookingMapper.bookingToBookingResponseDto(any(Booking.class))).thenReturn(bookingResponseDTO);

            List<BookingResponseDTO> result = bookingService.getAllBookingsByPropertyId(propertyId);

            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            assertEquals(bookingResponseDTO, result.getFirst());
            verify(bookingRepository).findByPropertyId(propertyId);
            verify(bookingMapper).bookingToBookingResponseDto(booking);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when property ID is null")
        void getAllBookingsByPropertyId_whenPropertyIdIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingService.getAllBookingsByPropertyId(null));
            assertEquals("Property ID cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when property ID is not positive")
        void getAllBookingsByPropertyId_whenPropertyIdNotPositive_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingService.getAllBookingsByPropertyId(0L));
            assertEquals("Property ID cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when no bookings found for property")
        void getAllBookingsByPropertyId_whenNoBookingsFound_shouldThrowNotFoundException() {
            when(bookingRepository.findByPropertyId(propertyId)).thenReturn(Collections.emptyList());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.getAllBookingsByPropertyId(propertyId));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("No bookings found for this property", exception.getReason());
        }
    }

    @Nested
    @DisplayName("acceptOrRejectBooking Tests")
    class AcceptOrRejectBookingTests {

        @Test
        @DisplayName("Should update booking status when not cancelling")
        void acceptOrRejectBooking_whenNotCancelling_shouldUpdateStatus() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            MessageResponseDTO response = bookingService.acceptOrRejectBooking(bookingId, propertyId, BookingStatus.CONFIRMED);

            assertEquals("Booking status updated successfully to: CONFIRMED for booking ID: " + bookingId, response.getMessage());
            assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
            verify(bookingRepository).save(booking);
        }

        @Test
        @DisplayName("Should delete booking when cancelling and payment not completed")
        void acceptOrRejectBooking_whenCancellingAndPaymentNotCompleted_shouldDeleteBooking() {
            payment.setPaymentStatus(PaymentStatus.PENDING);
            booking.setPayment(payment);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            doNothing().when(bookingRepository).delete(booking);


            MessageResponseDTO response = bookingService.acceptOrRejectBooking(bookingId, propertyId, BookingStatus.CANCELLED);

            assertEquals("Booking cancelled successfully", response.getMessage());
            verify(bookingRepository).delete(booking);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null bookingID")
        void acceptOrRejectBooking_whenBookingIdNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingService.acceptOrRejectBooking(null, propertyId, BookingStatus.CONFIRMED));
            assertEquals("Booking ID, Property ID, and Booking Status cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when booking not found")
        void acceptOrRejectBooking_whenBookingNotFound_shouldThrowNotFoundException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.acceptOrRejectBooking(bookingId, propertyId, BookingStatus.CONFIRMED));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Booking not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when cancelling a completed booking")
        void acceptOrRejectBooking_whenCancellingCompletedBooking_shouldThrowBadRequestException() {
            payment.setPaymentStatus(PaymentStatus.COMPLETED);
            booking.setPayment(payment);
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.acceptOrRejectBooking(bookingId, propertyId, BookingStatus.CANCELLED));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Cannot cancel a completed booking", exception.getReason());
        }
    }

    @Nested
    @DisplayName("isBookingOwner Tests")
    class IsBookingOwnerTests {
        @Test
        @DisplayName("Should return true if user is booking owner")
        void isBookingOwner_whenUserIsOwner_shouldReturnTrue() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            boolean result = bookingService.isBookingOwner(bookingId, testUsername);

            assertTrue(result);
            verify(bookingRepository).findById(bookingId);
        }

        @Test
        @DisplayName("Should return false if user is not booking owner")
        void isBookingOwner_whenUserIsNotOwner_shouldReturnFalse() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

            boolean result = bookingService.isBookingOwner(bookingId, "anotherUser");

            assertFalse(result);
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when booking not found")
        void isBookingOwner_whenBookingNotFound_shouldThrowNotFoundException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.isBookingOwner(bookingId, testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Booking not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("deleteBooking Tests")
    class DeleteBookingTests {
        @Test
        @DisplayName("Should delete booking successfully")
        void deleteBooking_whenBookingExists_shouldSucceed() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            doNothing().when(bookingRepository).delete(booking);

            MessageResponseDTO response = bookingService.deleteBooking(bookingId, propertyId, testUsername);

            assertEquals("Booking ID " + bookingId + " deleted successfully", response.getMessage());
            verify(bookingRepository).findById(bookingId);
            verify(bookingRepository).delete(booking);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException for null bookingID")
        void deleteBooking_whenBookingIdNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingService.deleteBooking(null, propertyId, testUsername));
            assertEquals("Booking ID, Property ID, and Username cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException when booking not found")
        void deleteBooking_whenBookingNotFound_shouldThrowNotFoundException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.deleteBooking(bookingId, propertyId, testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Booking not found", exception.getReason());
        }
    }

    @Nested
    @DisplayName("updateBooking Tests")
    class UpdateBookingTests {
        BookingRequestDTO updateDto;

        @BeforeEach
        void updateSetup() {
            updateDto = new BookingRequestDTO();
            updateDto.setPropertyId(propertyId);
            updateDto.setStartDate(LocalDate.now().plusDays(5));
            updateDto.setEndDate(LocalDate.now().plusDays(7));
        }

        @Test
        @DisplayName("Should update booking successfully")
        void updateBooking_whenValidAndNoConflict_shouldSucceed() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(propertyRepository.findPropertyById(updateDto.getPropertyId())).thenReturn(Optional.of(property));
            booking.setProperty(property);
            when(bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    updateDto.getPropertyId(), updateDto.getEndDate(), updateDto.getStartDate()))
                    .thenReturn(Collections.emptyList());
            doNothing().when(bookingMapper).updateBookingFromDto(eq(updateDto), any(Booking.class));
            when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

            MessageResponseDTO response = bookingService.updateBooking(updateDto, bookingId, testUsername);

            assertEquals("Booking updated successfully with ID: " + bookingId, response.getMessage());
            verify(bookingRepository).findById(bookingId);
            verify(propertyRepository).findPropertyById(updateDto.getPropertyId());
            verify(bookingRepository).findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(anyLong(), any(LocalDate.class), any(LocalDate.class));
            verify(bookingMapper).updateBookingFromDto(eq(updateDto), any(Booking.class));
            verify(bookingRepository).save(booking);
            assertEquals(BookingStatus.PENDING, booking.getBookingStatus());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException if DTO is null")
        void updateBooking_whenDtoIsNull_shouldThrowIllegalArgumentException() {
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> bookingService.updateBooking(null, bookingId, testUsername));
            assertEquals("Booking Request DTO or Booking ID cannot be null or negative", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException if booking not found")
        void updateBooking_whenBookingNotFound_shouldThrowNotFoundException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.updateBooking(updateDto, bookingId, testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Booking not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException if property in DTO not found")
        void updateBooking_whenPropertyInDtoNotFound_shouldThrowNotFoundException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(propertyRepository.findPropertyById(updateDto.getPropertyId())).thenReturn(Optional.empty());

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.updateBooking(updateDto, bookingId, testUsername));
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            assertEquals("Property not found", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException if property ID in DTO does not match booking's property")
        void updateBooking_whenPropertyIdMismatch_shouldThrowBadRequestException() {
            Property differentProperty = new Property();
            differentProperty.setId(99L);
            booking.setProperty(differentProperty);

            updateDto.setPropertyId(propertyId);

            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            Property propertyFromDto = new Property();
            propertyFromDto.setId(updateDto.getPropertyId());
            when(propertyRepository.findPropertyById(updateDto.getPropertyId())).thenReturn(Optional.of(propertyFromDto));


            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.updateBooking(updateDto, bookingId, testUsername));
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("Property ID does not match the booking's property", exception.getReason());
        }

        @Test
        @DisplayName("Should throw ResponseStatusException on date conflict during update")
        void updateBooking_whenDateConflict_shouldThrowConflictException() {
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(propertyRepository.findPropertyById(updateDto.getPropertyId())).thenReturn(Optional.of(property));
            booking.setProperty(property);
            Booking overlappingBooking = new Booking();
            overlappingBooking.setId(bookingId + 1);
            when(bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    updateDto.getPropertyId(), updateDto.getEndDate(), updateDto.getStartDate()))
                    .thenReturn(List.of(overlappingBooking));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.updateBooking(updateDto, bookingId, testUsername));

            assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
            assertEquals("Property is already booked for the selected dates", exception.getReason());
        }


        @Test
        @DisplayName("Should throw ResponseStatusException if end date not after start date during update")
        void updateBooking_whenEndDateNotAfterStartDate_shouldThrowBadRequestException() {
            updateDto.setEndDate(updateDto.getStartDate().minusDays(1));
            when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
            when(propertyRepository.findPropertyById(updateDto.getPropertyId())).thenReturn(Optional.of(property));
            booking.setProperty(property);
            when(bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(anyLong(), any(), any()))
                    .thenReturn(Collections.emptyList());
            doNothing().when(bookingMapper).updateBookingFromDto(eq(updateDto), any(Booking.class));

            ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                    () -> bookingService.updateBooking(updateDto, bookingId, testUsername));

            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            assertEquals("End date must be after start date", exception.getReason());
        }
    }
}