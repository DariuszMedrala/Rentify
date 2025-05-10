package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.entity.enums.BookingStatus;
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
     * @param authentication    the authentication object
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

    /**
     * Retrieves all bookings for a specific property.
     *
     * @param propertyID the ID of the property
     * @return a list of booking response data transfer objects
     */
    @Operation(summary = "Get all bookings for given property ID", description = "Retrieves all bookings for a given property ID.")
    @GetMapping("/{propertyID}/all")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookingsForProperty(@Parameter(description = "Property ID") @PathVariable Long propertyID) {
        List<BookingResponseDTO> bookings = bookingService.getAllBookingsByPropertyId(propertyID);
        return ResponseEntity.ok(bookings);
    }

    /**
     * Accepts or rejects a booking request for a property.
     *
     * @param propertyID   the ID of the property
     * @param bookingID    the ID of the booking
     * @param bookingStatus the new status of the booking
     * @param authentication the authentication object
     * @return the updated booking response data transfer object
     */
    @Operation(summary = "Allows the owner of a property to accept or reject a booking request",
            description = "Allows the owner of a property to accept or reject a booking request.")
    @PatchMapping("/{propertyID}/{bookingID}/accept")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#propertyID, principal.username))")
    public ResponseEntity<BookingResponseDTO> acceptBooking(@Parameter(description = "Property ID") @PathVariable Long propertyID,
                                                            @Parameter(description = "Booking ID") @PathVariable Long bookingID,
                                                            @Parameter(description = "Booking status") @RequestParam BookingStatus bookingStatus,
                                                            Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String username = userDetails.getUsername();
        BookingResponseDTO bookingResponseDTO = bookingService.acceptOrRejectBooking(bookingID, propertyID, bookingStatus, username);
        return ResponseEntity.ok(bookingResponseDTO);
    }
    /**
     * Deletes a booking for a property.
     *
     * @param bookingID the ID of the booking
     * @param authentication the authentication object
     * @return a response entity indicating the result of the operation
     */
    @Operation(summary = "Delete a booking",
            description = "Deletes a booking for a property if the user is the owner of the property or an admin or the user who made the booking.")
    @DeleteMapping("{propertyID}/{bookingID}/delete")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') " +
            "or @propertyService.isOwner(#propertyID, principal.username)" +
            "or @bookingService.isBookingOwner(#bookingID, principal.username))")
    public ResponseEntity<?> deleteBooking(@Parameter(description = "Property ID") @PathVariable Long propertyID,
                                           @Parameter(description = "Booking ID") @PathVariable Long bookingID,
                                           Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        bookingService.deleteBooking(bookingID, propertyID, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.OK).body("Booking deleted successfully");
    }

    /**
     * Updates a booking for a property.
     *
     * @param bookingID the ID of the booking
     * @param bookingRequestDTO the updated booking request data transfer object
     * @param authentication the authentication object
     * @return the updated booking response data transfer object
     */
    @Operation(summary = "Update a booking",
            description = "Updates a booking for a property if an admin or the user who made the booking.")
    @PutMapping("/{bookingID}/update")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') " +
            "or @bookingService.isBookingOwner(#bookingID, principal.username))")
    public ResponseEntity<BookingResponseDTO> updateBooking(@Parameter(description = "Booking ID") @PathVariable Long bookingID,
                                                            @Parameter(description = "Booking request DTO") @Valid @RequestBody BookingRequestDTO bookingRequestDTO,
                                                            Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        BookingResponseDTO bookingResponseDTO = bookingService.updateBooking(bookingRequestDTO, bookingID, userDetails.getUsername());
        return ResponseEntity.ok(bookingResponseDTO);
    }
}


