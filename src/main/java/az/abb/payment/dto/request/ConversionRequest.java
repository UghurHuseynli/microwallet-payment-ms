package az.abb.payment.dto.request;

import az.abb.payment.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversionRequest {
    private BigDecimal amount;
    private Currency fromCurrency;
    private Currency toCurrency;
}
