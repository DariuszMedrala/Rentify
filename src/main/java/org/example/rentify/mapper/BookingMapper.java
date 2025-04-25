package org.example.rentify.mapper;

import org.example.rentify.dto.request.BookingRequestDTO;
import org.example.rentify.dto.response.BookingResponseDTO;
import org.example.rentify.entity.Booking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/*
 * BookingMapper interface for mapping between Booking entity and Booking DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring",
        uses = {PropertyMapper.class, UserMapper.class, PaymentMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BookingMapper {

    /**
     * Maps a Booking entity to a BookingResponseDTO.
     *
     * @param booking the Booking entity to map
     * @return the mapped BookingResponseDTO
     */
    @Mapping(target = "property", source = "property")
    @Mapping(target = "user", source = "user")
    @Mapping(target = "payment", source = "payment")
    BookingResponseDTO toDto(Booking booking);

    /**
     * Maps a list of Booking entities to a list of BookingResponseDTOs.
     *
     * @param bookings the list of Booking entities to map
     * @return the list of mapped BookingResponseDTOs
     */
    List<BookingResponseDTO> toDtoList(List<Booking> bookings);

    /**
     * Maps a BookingRequestDTO to a Booking entity.
     *
     * @param bookingRequestDTO the BookingRequestDTO to map
     * @return the mapped Booking entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "bookingDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "bookingStatus", ignore = true)
    @Mapping(target = "property", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(target = "reviews", ignore = true)
    Booking toEntity(BookingRequestDTO bookingRequestDTO);
}