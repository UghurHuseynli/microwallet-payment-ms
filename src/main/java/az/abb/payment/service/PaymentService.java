package az.abb.payment.service;

import az.abb.payment.dto.event.PaymentHistoryEvent;
import az.abb.payment.dto.request.PaymentRequest;
import az.abb.payment.dto.response.PaymentResponse;
import az.abb.payment.entity.Account;
import az.abb.payment.entity.Payment;
import az.abb.payment.enums.Currency;
import az.abb.payment.enums.PaymentStatus;
import az.abb.payment.exception.AccountNotFoundException;
import az.abb.payment.exception.InsufficientFundsException;
import az.abb.payment.mapper.PaymentMapper;
import az.abb.payment.repository.AccountRepository;
import az.abb.payment.repository.PaymentRepository;
import az.abb.payment.strategy.Payable;
import az.abb.payment.strategy.PaymentStrategyResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final CurrencyService currencyService;
    private final KafkaPublisherService kafkaPublisherService;
    private final PaymentMapper paymentMapper;
    private final Environment env;

    private final PaymentStrategyResolver strategyResolver;

    @Value("${kafka.topics.payment-result}")
    private String paymentResultTopic;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request, Long userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() ->  new AccountNotFoundException("No account found for user ID: " + userId));

        BigDecimal finalAmount = currencyService.resolveAmount(request.amount(), request.currencyType(), account.getCurrency());
        if (account.getBalance().compareTo(finalAmount) < 0){
            throw new InsufficientFundsException("Insufficient balance");
        }
        BigDecimal balance = account.getBalance().subtract(finalAmount);
        Currency accountCurrency = account.getCurrency();
        account.setBalance(balance);
        accountRepository.save(account);

        Payment payment = Payment.builder()
                .currency(accountCurrency)
                .amount(request.amount())
                .accountId(account.getId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentType(request.paymentType())
                .build();
        paymentRepository.save(payment);
        Payable strategy = strategyResolver.resolve(request.paymentType());
        strategy.doPayment(account, payment, finalAmount);

        PaymentHistoryEvent event = paymentMapper.toResponse(payment);

        String eventId = kafkaPublisherService.publish(event, paymentResultTopic);

        return new PaymentResponse(balance, payment.getId(), finalAmount, request.currencyType(), PaymentStatus.IN_PROGRESS, eventId, payment.getCreatedAt());
    }
}
