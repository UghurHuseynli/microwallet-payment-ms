package az.abb.payment.service;

import az.abb.payment.dto.response.ConvertResponse;
import az.abb.payment.enums.Currency;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
class CurrencyService {

    private final Environment env;

    @Value("${payment.fee.percentage}")
    private BigDecimal feePercentage;

    public boolean isSameCurrency(Currency requestCurrency, Currency accountCurrency) {
        return requestCurrency.equals(accountCurrency);
    }

    public ConvertResponse convertToAccountCurrency(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {

//        Fetch exchange fee from config
        String rateKey = "currency.rates." + fromCurrency + "_" + toCurrency;
        String rateStr = env.getProperty(rateKey);
        if (rateStr == null) {
            throw new RuntimeException("Exchange rate not configured for: " + rateKey);
        }

        BigDecimal rate = new BigDecimal(rateStr);
        BigDecimal convertedRaw = amount.multiply(rate);

//         Calculate fee
        BigDecimal feeAmount = amount
                .multiply(feePercentage)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal finalConverted = convertedRaw.subtract(feeAmount.multiply(rate))
                .setScale(4, RoundingMode.HALF_UP);

        return new ConvertResponse(finalConverted, feeAmount, feePercentage);
    }

    public BigDecimal resolveAmount(BigDecimal amount, Currency fromCurrency, Currency toCurrency) {
        if (isSameCurrency(fromCurrency, toCurrency)) {
            return amount;
        }
        return convertToAccountCurrency(amount, fromCurrency, toCurrency).finalConverted();
    }

}
