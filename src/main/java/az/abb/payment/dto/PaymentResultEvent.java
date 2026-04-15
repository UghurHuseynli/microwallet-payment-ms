package az.abb.payment.dto;

import az.abb.payment.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResultEvent implements KafkaEvent {
    private String eventId;
    private String eventType;
    private Long accountId;
    private BigDecimal amount;
    private Currency currency;
    private BigDecimal convertedAmount;
    private Currency targetCurrency;
    private BigDecimal feeAmount;
    private String status;
    private String message;
    private LocalDateTime processedAt;
}
