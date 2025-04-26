package org.example.rentify.mapper;

import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.entity.Booking;
import org.mapstruct.*;

/*
 * BookingMapper interface for mapping between Booking entity and Booking DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {PropertyMapper.class, UserMapper.class, PaymentMapper.class})
public interface BookingMapper {

    /**
     * Maps a BookingRequestDTO to a Booking entity.
     *
     * @param bookingRequestDTO the request DTO
     * @return the converted Booking entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "bookingDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "bookingStatus", expression = "java(org.example.rentify.entity.enums.BookingStatus.PENDING)")
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Booking bookingRequestDtoToBooking(BookingRequestDTO bookingRequestDTO);

    /**
     * Maps a Booking entity to a BookingResponseDTO.
     *
     * @param booking the Booking entity
     * @return the converted BookingResponseDTO
     */
    BookingResponseDTO bookingToBookingResponseDto(Booking booking);

    /**
     * Updates an existing Booking entity with data from a BookingRequestDTO.
     *
     * @param bookingRequestDTO the DTO containing update data
     * @param booking the Booking entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "bookingDate", ignore = true)
    @Mapping(target = "bookingStatus", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    void updateBookingFromDto(BookingRequestDTO bookingRequestDTO, @MappingTarget Booking booking);
}