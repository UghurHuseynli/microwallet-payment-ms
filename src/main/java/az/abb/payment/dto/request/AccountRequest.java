package az.abb.payment.dto.request;

import az.abb.payment.enums.Currency;

import java.math.BigDecimal;

public record AccountRequest (
        BigDecimal balance,
        Currency currency
) {}
