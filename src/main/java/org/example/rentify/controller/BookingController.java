package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * BookingController is a REST controller that handles booking-related operations.
 * It provides endpoints for creating and managing bookings for properties.
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Management", description = "Endpoints for managing bookings")
public class BookingController {

    private final BookingService bookingService;

    @Autowired
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * Creates a new booking for a property.
     *
     * @param bookingRequestDTO the booking request data transfer object
     * @param authentication the authentication object
     * @return the created booking response data transfer object
     */
    @Operation(summary = "Create a new booking", description = "Creates a new booking if property is available and there are no date conflicts.")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDTO> createBooking(@Valid @RequestBody BookingRequestDTO bookingRequestDTO, Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        BookingResponseDTO response = bookingService.createBooking(bookingRequestDTO, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all bookings for the authenticated user.
     *
     * @param authentication the authentication object
     * @return a list of booking response data transfer objects
     */
    @Operation(summary = "Get all bookings", description = "Retrieves all bookings for the authenticated user.")
    @GetMapping("/me/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        List<BookingResponseDTO> bookings = bookingService.getAllBookingsFromLoggedUser(username);
        return ResponseEntity.ok(bookings);
    }
}
