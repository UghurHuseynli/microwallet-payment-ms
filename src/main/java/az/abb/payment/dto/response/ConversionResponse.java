package az.abb.payment.dto.response;

import az.abb.payment.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversionResponse {
    private Long accountId;
    private BigDecimal originalAmount;
    private Currency fromCurrency;
    private BigDecimal convertedAmount;
    private Currency toCurrency;
    private BigDecimal feeAmount;
    private BigDecimal feePercentage;
    private BigDecimal netAmount;
}
