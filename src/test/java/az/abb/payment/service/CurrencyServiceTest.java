package az.abb.payment.service;

import az.abb.payment.dto.response.ConvertResponse;
import az.abb.payment.enums.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;
import java.lang.reflect.Field;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private Environment env;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() throws Exception {
        Field feeField = CurrencyService.class.getDeclaredField("feePercentage");
        feeField.setAccessible(true);
        feeField.set(currencyService, BigDecimal.valueOf(2));
    }

    @Test
    void isSameCurrency_ShouldReturnTrue_WhenCurrenciesMatch() {
        assertThat(currencyService.isSameCurrency(Currency.USD, Currency.USD)).isTrue();
    }

    @Test
    void isSameCurrency_ShouldReturnFalse_WhenCurrenciesDiffer() {
        assertThat(currencyService.isSameCurrency(Currency.USD, Currency.EUR)).isFalse();
    }

    @Test
    void convertToAccountCurrency_ShouldReturnConvertResponse_WhenRateIsConfigured() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);
        given(env.getProperty("currency.rates.USD_EUR")).willReturn("0.92");

        // When
        ConvertResponse response = currencyService.convertToAccountCurrency(amount, Currency.USD, Currency.EUR);

        assertThat(response.finalConverted()).isEqualByComparingTo(BigDecimal.valueOf(90.1600));
        assertThat(response.feeAmount()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(response.feePercentage()).isEqualByComparingTo(BigDecimal.valueOf(2));
    }

    @Test
    void convertToAccountCurrency_ShouldThrowRuntimeException_WhenRateNotConfigured() {
        given(env.getProperty("currency.rates.USD_EUR")).willReturn(null);

        assertThatThrownBy(() ->
                currencyService.convertToAccountCurrency(BigDecimal.valueOf(100), Currency.USD, Currency.EUR))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Exchange rate not configured for: currency.rates.USD_EUR");
    }

    @Test
    void resolveAmount_ShouldReturnOriginalAmount_WhenCurrenciesAreTheSame() {
        BigDecimal amount = BigDecimal.valueOf(100);

        BigDecimal result = currencyService.resolveAmount(amount, Currency.USD, Currency.USD);

        assertThat(result).isEqualByComparingTo(amount);
        verifyNoInteractions(env);
    }

    @Test
    void resolveAmount_ShouldReturnConvertedAmount_WhenCurrenciesDiffer() {
        BigDecimal amount = BigDecimal.valueOf(100);
        given(env.getProperty("currency.rates.USD_EUR")).willReturn("0.92");

        BigDecimal result = currencyService.resolveAmount(amount, Currency.USD, Currency.EUR);

        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(90.1600));
    }
}