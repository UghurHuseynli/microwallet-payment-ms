package az.abb.payment.service;

import az.abb.payment.dto.request.ConversionRequest;
import az.abb.payment.dto.response.ConversionResponse;
import az.abb.payment.dto.response.ConvertResponse;
import az.abb.payment.entity.Account;
import az.abb.payment.enums.Currency;
import az.abb.payment.exception.AccountNotFoundException;
import az.abb.payment.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConversionServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private ConversionService conversionService;

    private ConversionRequest request;

    @BeforeEach
    void setUp() {
        request = ConversionRequest.builder()
                .amount(BigDecimal.valueOf(100))
                .fromCurrency(Currency.USD)
                .toCurrency(Currency.EUR)
                .build();
    }

    @Test
    void convertAmount_ShouldReturnConversionResponse_WhenEverythingIsValid() {
        // Given
        Long userId = 1L;

        Account account = Account.builder()
                .id(10L)
                .userId(userId)
                .build();

        ConvertResponse convertResponse = new ConvertResponse(
                BigDecimal.valueOf(92),   // finalConverted
                BigDecimal.valueOf(2),    // feeAmount
                BigDecimal.valueOf(2.0)   // feePercentage
        );

        given(accountRepository.findByUserId(userId)).willReturn(Optional.of(account));
        given(currencyService.convertToAccountCurrency(
                request.getAmount(),
                request.getFromCurrency(),
                request.getToCurrency()
        )).willReturn(convertResponse);

        // When
        ConversionResponse response = conversionService.convertAmount(userId, request);

        // Then
        assertThat(response.getAccountId()).isEqualTo(10L);
        assertThat(response.getOriginalAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(response.getFromCurrency()).isEqualTo(Currency.USD);
        assertThat(response.getConvertedAmount()).isEqualByComparingTo(BigDecimal.valueOf(92));
        assertThat(response.getToCurrency()).isEqualTo(Currency.EUR);
        assertThat(response.getFeeAmount()).isEqualByComparingTo(BigDecimal.valueOf(2));
        assertThat(response.getFeePercentage()).isEqualByComparingTo(BigDecimal.valueOf(2.0));
        assertThat(response.getNetAmount()).isEqualByComparingTo(BigDecimal.valueOf(90)); // 92 - 2

        verify(accountRepository).findByUserId(userId);
        verify(currencyService).convertToAccountCurrency(
                request.getAmount(),
                request.getFromCurrency(),
                request.getToCurrency()
        );
    }

    @Test
    void convertAmount_ShouldThrowAccountNotFoundException_WhenAccountNotFound() {
        // Given
        Long userId = 99L;
        given(accountRepository.findByUserId(userId)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> conversionService.convertAmount(userId, request))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account not found");

        verify(currencyService, never()).convertToAccountCurrency(any(), any(), any());
    }

    @Test
    void convertAmount_ShouldPropagateException_WhenCurrencyServiceFails() {
        // Given
        Long userId = 1L;

        Account account = Account.builder()
                .id(10L)
                .userId(userId)
                .build();

        given(accountRepository.findByUserId(userId)).willReturn(Optional.of(account));
        given(currencyService.convertToAccountCurrency(any(), any(), any()))
                .willThrow(new RuntimeException("Currency service unavailable"));

        // When / Then
        assertThatThrownBy(() -> conversionService.convertAmount(userId, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Currency service unavailable");
    }
}