package az.abb.payment.dto.response;

import az.abb.payment.enums.Currency;
import az.abb.payment.enums.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private BigDecimal balance;
    private Long paymentId;
    private BigDecimal payedAmount;
    private Currency currency;
    private PaymentStatus status;
    private String eventId;
    private LocalDateTime payedDate;
}
