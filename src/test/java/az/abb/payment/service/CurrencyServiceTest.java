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
        // Inject @Value field manually since Mockito doesn't handle @Value
        Field feeField = CurrencyService.class.getDeclaredField("feePercentage");
        feeField.setAccessible(true);
        feeField.set(currencyService, BigDecimal.valueOf(2));
    }

    // ─── isSameCurrency ──────────────────────────────────────────────────────

    @Test
    void isSameCurrency_ShouldReturnTrue_WhenCurrenciesMatch() {
        assertThat(currencyService.isSameCurrency(Currency.USD, Currency.USD)).isTrue();
    }

    @Test
    void isSameCurrency_ShouldReturnFalse_WhenCurrenciesDiffer() {
        assertThat(currencyService.isSameCurrency(Currency.USD, Currency.EUR)).isFalse();
    }

    // ─── convertToAccountCurrency ─────────────────────────────────────────────

    @Test
    void convertToAccountCurrency_ShouldReturnConvertResponse_WhenRateIsConfigured() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);
        given(env.getProperty("currency.rates.USD_EUR")).willReturn("0.92");

        // When
        ConvertResponse response = currencyService.convertToAccountCurrency(amount, Currency.USD, Currency.EUR);

        // Then
        // rate        = 0.92
        // convertedRaw = 100 * 0.92 = 92
        // feeAmount    = 100 * 2 / 100 = 2.0000
        // finalConverted = 92 - (2.0000 * 0.92) = 92 - 1.8400 = 90.1600
        assertThat(response.finalConverted()).isEqualByComparingTo(BigDecimal.valueOf(90.1600));
        assertThat(response.feeAmount()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(response.feePercentage()).isEqualByComparingTo(BigDecimal.valueOf(2));
    }

    @Test
    void convertToAccountCurrency_ShouldThrowRuntimeException_WhenRateNotConfigured() {
        // Given
        given(env.getProperty("currency.rates.USD_EUR")).willReturn(null);

        // When / Then
        assertThatThrownBy(() ->
                currencyService.convertToAccountCurrency(BigDecimal.valueOf(100), Currency.USD, Currency.EUR))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Exchange rate not configured for: currency.rates.USD_EUR");
    }

    // ─── resolveAmount ────────────────────────────────────────────────────────

    @Test
    void resolveAmount_ShouldReturnOriginalAmount_WhenCurrenciesAreTheSame() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);

        // When
        BigDecimal result = currencyService.resolveAmount(amount, Currency.USD, Currency.USD);

        // Then
        assertThat(result).isEqualByComparingTo(amount);
        verifyNoInteractions(env); // rate lookup should never happen
    }

    @Test
    void resolveAmount_ShouldReturnConvertedAmount_WhenCurrenciesDiffer() {
        // Given
        BigDecimal amount = BigDecimal.valueOf(100);
        given(env.getProperty("currency.rates.USD_EUR")).willReturn("0.92");

        // When
        BigDecimal result = currencyService.resolveAmount(amount, Currency.USD, Currency.EUR);

        // Then
        assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(90.1600));
    }
}