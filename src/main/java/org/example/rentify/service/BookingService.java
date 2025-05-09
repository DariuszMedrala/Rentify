package org.example.rentify.service;

import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.entity.Booking;
import org.example.rentify.entity.Property;
import org.example.rentify.entity.User;
import org.example.rentify.entity.enums.BookingStatus;
import org.example.rentify.mapper.BookingMapper;
import org.example.rentify.repository.BookingRepository;
import org.example.rentify.repository.PropertyRepository;
import org.example.rentify.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/*
 * BookingService class for managing bookings in the Rentify application.
 * This service provides methods to create and manage bookings for properties.
 */
@Service
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

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
     * @return the created booking response data transfer object
     * @throws ResponseStatusException if the property is not found, not available, or if there are date conflicts
     */
    @Transactional
    public BookingResponseDTO createBooking(BookingRequestDTO bookingRequestDTO, String username) {
        Property property = propertyRepository.findPropertyById(bookingRequestDTO.getPropertyId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
        if (!Boolean.TRUE.equals(property.getAvailability())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Property is not available for booking");
        }
        List<Booking> overlappingBookings = bookingRepository.findByPropertyIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                bookingRequestDTO.getPropertyId(),
                bookingRequestDTO.getEndDate(),
                bookingRequestDTO.getStartDate()
        );
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
        Booking saved = bookingRepository.save(booking);
        logger.info("Booking created for property {} by user {}", property.getId(), user.getId());
        return bookingMapper.bookingToBookingResponseDto(saved);
    }

    /**
     * Retrieves all bookings for a user.
     *
     * @param username the username of the user
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
}