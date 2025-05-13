package org.example.rentify.service;

import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.BookingStatus;
import org.example.rentify.entity.enums.PaymentStatus;
import org.example.rentify.mapper.BookingMapper;
import org.example.rentify.repository.BookingRepository;
import org.example.rentify.repository.PropertyRepository;
import org.example.rentify.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/*
 * BookingService class for managing bookings in the Rentify application.
 * This service provides methods to create and manage bookings for properties.
 */
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final BookingMapper bookingMapper;

    @Autowired
    public BookingService(BookingRepository bookingRepository, PropertyRepository propertyRepository, UserRepository userRepository, BookingMapper bookingMapper) {
        this.bookingRepository = bookingRepository;
        this.propertyRepository = propertyRepository;
        this.userRepository = userRepository;
        this.bookingMapper = bookingMapper;
    }

    /**
     * Creates a new booking for a property.
     *
     * @param bookingRequestDTO the booking request data transfer object
     * @param username the username of the user making the booking
     * @throws ResponseStatusException if the property is not found, or if it is not available for booking,
     *                                 or if there are date conflicts with existing bookings
     * @return a message response data transfer object indicating success
     */
    @Transactional
    public MessageResponseDTO createBooking(BookingRequestDTO bookingRequestDTO, String username) {
        Property property = propertyRepository.findPropertyById(bookingRequestDTO.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));

        if (!Boolean.TRUE.equals(property.getAvailability())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property is not available for booking");
        }
        List<Booking> overlappingBookings = bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                bookingRequestDTO.getPropertyId(),
                bookingRequestDTO.getEndDate(),
                bookingRequestDTO.getStartDate());

        if (!overlappingBookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Property is already booked for the selected dates");
        }

        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Booking booking = bookingMapper.bookingRequestDtoToBooking(bookingRequestDTO);
        booking.setUser(user);
        booking.setProperty(property);

        long days = ChronoUnit.DAYS.between(bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate()) + 1;
        if (days <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        booking.setTotalPrice(property.getPricePerDay().multiply(BigDecimal.valueOf(days)));
        booking.setBookingDate(LocalDateTime.now());
        booking.setBookingStatus(BookingStatus.PENDING);
        bookingMapper.bookingToBookingResponseDto(bookingRepository.save(booking));
        return new MessageResponseDTO("Booking created successfully with ID: " + booking.getId());
    }

    /**
     * Retrieves all bookings for the logged-in user.
     *
     * @param username the username of the logged-in user
     * @throws ResponseStatusException if the user is not found or if no bookings are found
     * @return a list of booking response data transfer objects
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookingsFromLoggedUser(String username) {
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        List<Booking> bookings = bookingRepository.findByUserId(user.getId());
        if (bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No bookings found for this user");
        }
        return bookings.stream()
                .map(bookingMapper::bookingToBookingResponseDto)
                .toList();
    }

    /**
     * Retrieves all bookings for a property.
     *
     * @param propertyID the ID of the property
     * @throws ResponseStatusException if no bookings are found for the property
     * @throws IllegalArgumentException if the property ID is null or negative
     * @return a list of booking response data transfer objects
     */
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getAllBookingsByPropertyId(Long propertyID) {
        if (propertyID == null || propertyID <= 0) {
            throw new IllegalArgumentException("Property ID cannot be null or negative");
        }
        List<BookingResponseDTO> bookings = bookingRepository.findByPropertyId(propertyID)
                .stream()
                .map(bookingMapper::bookingToBookingResponseDto)
                .toList();
        if (bookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No bookings found for this property");
        }
        return bookings;
    }

   /**
     * Accepts or rejects a booking request for a property.
     *
     * @param bookingID the ID of the booking
     * @param propertyID the ID of the property
     * @param bookingStatus the status to set for the booking
     * @throws ResponseStatusException if the booking is not found or if the payment is completed and status is cancelled
     * @throws IllegalArgumentException if any of the parameters are null or negative
     * @return a message response data transfer object indicating success
     */
    @Transactional
    public MessageResponseDTO acceptOrRejectBooking(Long bookingID, Long propertyID, BookingStatus bookingStatus) {
        if(bookingID == null || bookingID <= 0 ||  propertyID == null || propertyID <= 0 || bookingStatus == null) {
            throw new IllegalArgumentException("Booking ID, Property ID, and Booking Status cannot be null or negative");
        }
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (bookingStatus.equals(BookingStatus.CANCELLED)) {
            if (booking.getPayment().getPaymentStatus().equals(PaymentStatus.COMPLETED)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot cancel a completed booking");
            }
            bookingRepository.delete(booking);
            return new MessageResponseDTO("Booking cancelled successfully");
        }
        booking.setBookingStatus(bookingStatus);
        return new MessageResponseDTO("Booking status updated successfully to: " + bookingStatus + " for booking ID: " + bookingID);
    }

    /**
     * Checks if the logged-in user is the owner of a booking.
     *
     * @param bookingId the ID of the booking
     * @param username the username of the logged-in user
     * @throws ResponseStatusException if the booking is not found
     * @return true if the user is the owner of the booking, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean isBookingOwner(Long bookingId, String username) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        return Objects.equals(booking.getUser().getUsername(), username);
    }

    /**
     * Deletes a booking for a property.
     *
     * @param bookingID the ID of the booking
     * @param propertyID the ID of the property
     * @param username the username of the user making the request
     * @throws ResponseStatusException if the booking is not found
     * @throws IllegalArgumentException if any of the parameters are null or negative
     * @return a message response data transfer object indicating success
     */
    @Transactional
    public MessageResponseDTO deleteBooking(Long bookingID, Long propertyID, String username) {
        if (bookingID == null || bookingID <= 0 || propertyID == null || propertyID <= 0 || username == null) {
            throw new IllegalArgumentException("Booking ID, Property ID, and Username cannot be null or negative");
        }
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        bookingRepository.delete(booking);
        return new MessageResponseDTO("Booking ID " + bookingID + " deleted successfully");
    }

    /**
     * Updates a booking for a property.
     *
     * @param bookingRequestDTO the booking request data transfer object
     * @param bookingID the ID of the booking
     * @param username the username of the user making the request
     * @throws ResponseStatusException if the booking is not found or if there are date conflicts with existing bookings
     * @throws IllegalArgumentException if any of the parameters are null or negative
     * @return a message response data transfer object indicating success
     */
    @Transactional
    public MessageResponseDTO updateBooking(BookingRequestDTO bookingRequestDTO, Long bookingID, String username) {
        if (bookingRequestDTO == null || bookingID == null || bookingID <= 0) {
            throw new IllegalArgumentException("Booking Request DTO or Booking ID cannot be null or negative");
        }
        Booking booking = bookingRepository.findById(bookingID)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        Property property = propertyRepository.findPropertyById(bookingRequestDTO.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        if (!Objects.equals(property.getId(), booking.getProperty().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property ID does not match the booking's property");
        }
        List<Booking> overlappingBookings = bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                bookingRequestDTO.getPropertyId(),
                bookingRequestDTO.getEndDate(),
                bookingRequestDTO.getStartDate()
        );
        if (!overlappingBookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Property is already booked for the selected dates");
        }
        bookingMapper.updateBookingFromDto(bookingRequestDTO, booking);
        booking.setBookingDate(LocalDateTime.now());

        long days = ChronoUnit.DAYS.between(bookingRequestDTO.getStartDate(), bookingRequestDTO.getEndDate()) + 1;
        if (days <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        booking.setTotalPrice(booking.getProperty().getPricePerDay().multiply(BigDecimal.valueOf(days)));
        booking.setBookingStatus(BookingStatus.PENDING);
        bookingRepository.save(booking);

        return new MessageResponseDTO("Booking updated successfully with ID: " + booking.getId());
    }
}