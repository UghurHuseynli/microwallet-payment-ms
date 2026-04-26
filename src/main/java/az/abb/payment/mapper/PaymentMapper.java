package az.abb.payment.mapper;

import az.abb.payment.dto.event.PaymentHistoryEvent;
import az.abb.payment.entity.Payment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    @Mapping(source = "id", target = "paymentId")
    @Mapping(source = "createdAt", target = "paymentDate")
    PaymentHistoryEvent toResponse(Payment payment);
}
