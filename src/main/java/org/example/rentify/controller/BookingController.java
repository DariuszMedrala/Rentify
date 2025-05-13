package org.example.rentify.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.dto.response.MessageResponseDTO;
import org.example.rentify.entity.enums.BookingStatus;
import org.example.rentify.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
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
     * @return a message response indicating the result of the operation
     */
    @Operation(summary = "Create a new booking", description = "Creates a new booking if property is available and there are no date conflicts.")
    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public MessageResponseDTO createBooking(@Parameter(description = "Booking Request DTO")
                                                @Valid @RequestBody BookingRequestDTO bookingRequestDTO,
                                            Authentication authentication) {
        return bookingService.createBooking(bookingRequestDTO, ((UserDetails) authentication.getPrincipal()).getUsername());
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
    public ResponseEntity<List<BookingResponseDTO>> getAllBookingsFromLoggedUser(Authentication authentication) {
        return ResponseEntity.ok(bookingService.getAllBookingsFromLoggedUser(((UserDetails) authentication.getPrincipal()).getUsername()));
    }

    /**
     * Retrieves all bookings for a specific property.
     *
     * @param propertyID the ID of the property
     * @return a list of booking response data transfer objects
     */
    @Operation(summary = "Get all bookings for given property ID", description = "Retrieves all bookings for a given property ID.")
    @GetMapping("/{propertyID}/all")
    public ResponseEntity<List<BookingResponseDTO>> getAllBookingsForProperty(@Parameter(description = "Property ID", in = ParameterIn.PATH)
                                                                                  @PathVariable Long propertyID) {
        return ResponseEntity.ok(bookingService.getAllBookingsByPropertyId(propertyID));
    }

    /**
     * Accepts or rejects a booking request for a property.
     *
     * @param propertyID the ID of the property
     * @param bookingID the ID of the booking
     * @param bookingStatus the status to set for the booking
     * @return a message response indicating the result of the operation
     */
    @Operation(summary = "Allows the owner of a property to accept or reject a booking request",
            description = "Allows the owner of a property to accept or reject a booking request.")
    @PatchMapping("/{propertyID}/{bookingID}/booking-status")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') or @propertyService.isOwner(#propertyID, principal.username))")
    public MessageResponseDTO acceptBooking(@Parameter(description = "Property ID", in = ParameterIn.PATH)
                                                                @PathVariable Long propertyID,
                                                            @Parameter(description = "Booking ID", in = ParameterIn.PATH) @PathVariable Long bookingID,
                                                            @Parameter(description = "Booking status") @RequestParam BookingStatus bookingStatus){
        return bookingService.acceptOrRejectBooking(bookingID, propertyID, bookingStatus);
    }

    /**
     * Deletes a booking for a property.
     *
     * @param propertyID the ID of the property
     * @param bookingID the ID of the booking
     * @param authentication the authentication object
     * @return a message response indicating the result of the operation
     */
    @Operation(summary = "Delete a booking for a property",
            description = "Deletes a booking for a property if an admin or the user who made the booking.")
    @DeleteMapping("{propertyID}/{bookingID}/delete")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') " +
            "or @bookingService.isBookingOwner(#bookingID, principal.username))")
    public MessageResponseDTO deleteBooking(@Parameter(description = "Property ID", in = ParameterIn.PATH) @PathVariable Long propertyID,
                                           @Parameter(description = "Booking ID", in = ParameterIn.PATH) @PathVariable Long bookingID,
                                           Authentication authentication) {
        return bookingService.deleteBooking(bookingID, propertyID, ((UserDetails) authentication.getPrincipal()).getUsername());

    }

    /**
     * Updates a booking for a property.
     *
     * @param bookingID the ID of the booking
     * @param bookingRequestDTO the booking request data transfer object
     * @param authentication the authentication object
     * @return a message response indicating the result of the operation
     */
    @Operation(summary = "Update a booking",
            description = "Updates a booking for a property if an admin or the user who made the booking.")
    @PutMapping("/{bookingID}/update")
    @PreAuthorize("isAuthenticated() and (hasRole('ADMIN') " +
            "or @bookingService.isBookingOwner(#bookingID, principal.username))")
    public MessageResponseDTO updateBooking(@Parameter(description = "Booking ID", in = ParameterIn.PATH)
                                                @PathVariable Long bookingID, @Parameter(description = "Booking request DTO")
                                                @Valid @RequestBody BookingRequestDTO bookingRequestDTO, Authentication authentication) {
    return bookingService.updateBooking(bookingRequestDTO, bookingID, ((UserDetails) authentication.getPrincipal()).getUsername());
    }
}


