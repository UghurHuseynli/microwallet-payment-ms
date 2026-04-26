package az.abb.payment.dto.response;

import java.math.BigDecimal;

public record ConvertResponse (
        BigDecimal finalConverted,
        BigDecimal feeAmount,
        BigDecimal feePercentage
) {}
