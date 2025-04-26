package org.example.rentify.mapper;

import org.example.rentify.dto.request.PaymentRequestDTO;
import org.example.rentify.dto.response.PaymentResponseDTO;
import org.example.rentify.entity.Payment;
import org.mapstruct.*;


/**
 * PaymentMapper interface for mapping between Payment entity and DTOs.
 * Uses MapStruct for automatic implementation generation.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {BookingMapper.class})
public interface PaymentMapper {

    /**
     * Converts a PaymentRequestDTO to a Payment entity.
     *
     * @param paymentRequestDTO the PaymentRequestDTO to convert
     * @return the converted Payment entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "paymentDate", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "paymentStatus", expression = "java(org.example.rentify.entity.enums.PaymentStatus.PENDING)")
    Payment paymentRequestDtoToPayment(PaymentRequestDTO paymentRequestDTO);

    /**
     * Converts a Payment entity to a PaymentResponseDTO.
     *
     * @param payment the Payment entity to convert
     * @return the converted PaymentResponseDTO
     */
    PaymentResponseDTO paymentToPaymentResponseDto(Payment payment);

    /**
     * Updates an existing Payment entity with data from a PaymentRequestDTO.
     *
     * @param paymentRequestDTO the DTO containing update data
     * @param payment the Payment entity to update
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "booking", ignore = true)
    @Mapping(target = "paymentDate", ignore = true)
    @Mapping(target = "paymentStatus", ignore = true)
    void updatePaymentFromDto(PaymentRequestDTO paymentRequestDTO, @MappingTarget Payment payment);
}