package org.example.rentify.mapper;

import org.example.rentify.dto.request.PaymentRequestDTO;
import org.example.rentify.dto.response.PaymentResponseDTO;
import org.example.rentify.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/*
 * PaymentMapper interface for mapping between Payment entity and DTOs.
 * This interface uses MapStruct to generate the implementation at compile time.
 */
@Mapper(componentModel = "spring",
        uses = {BookingMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    /**
     * Maps a Payment entity to a PaymentResponseDTO.
     *
     * @param payment the Payment entity to map
     * @return the mapped PaymentResponseDTO
     */
    @Mapping(target = "booking", source = "booking")
    PaymentResponseDTO toDto(Payment payment);

    /**
     * Maps a list of Payment entities to a list of PaymentResponseDTOs.
     *
     * @param payments the list of Payment entities to map
     * @return the list of mapped PaymentResponseDTOs
     */
    List<PaymentResponseDTO> toDtoList(List<Payment> payments);

    /**
     * Maps a PaymentRequestDTO to a Payment entity.
     *
     * @param paymentRequestDTO the PaymentRequestDTO to map
     * @return the mapped Payment entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "paymentDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "paymentStatus", ignore = true)
    @Mapping(target = "booking", ignore = true)
    Payment toEntity(PaymentRequestDTO paymentRequestDTO);
}