package az.abb.payment.dto.request;

import az.abb.payment.enums.Currency;
import az.abb.payment.enums.PaymentType;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        Currency currencyType,
        PaymentType paymentType,
        String description
) {
}
