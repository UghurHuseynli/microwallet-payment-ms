package az.abb.payment.service;

import az.abb.payment.dto.request.ConversionRequest;
import az.abb.payment.dto.response.ConversionResponse;
import az.abb.payment.dto.response.ConvertResponse;
import az.abb.payment.entity.Account;
import az.abb.payment.exception.AccountNotFoundException;
import az.abb.payment.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionService {

    private final AccountRepository accountRepository;
    private final CurrencyService  currencyService;

    @Transactional
    public ConversionResponse convertAmount(Long userId, ConversionRequest request) {
         Account account = accountRepository.findByUserId(userId)
                 .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        ConvertResponse data = currencyService.convertToAccountCurrency(request.getAmount(), request.getFromCurrency(), request.getToCurrency());

        BigDecimal netAmount = data.finalConverted().subtract(data.feeAmount());

        return ConversionResponse.builder()
                .accountId(account.getId())
                .originalAmount(request.getAmount())
                .fromCurrency(request.getFromCurrency())
                .convertedAmount(data.finalConverted())
                .toCurrency(request.getToCurrency())
                .feeAmount(data.feeAmount())
                .feePercentage(data.feePercentage())
                .netAmount(netAmount)
                .build();
    }
}
