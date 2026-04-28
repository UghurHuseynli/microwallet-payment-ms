package az.abb.payment.service;

import az.abb.payment.dto.event.PaymentHistoryEvent;
import az.abb.payment.dto.request.PaymentRequest;
import az.abb.payment.dto.response.PaymentResponse;
import az.abb.payment.entity.Account;
import az.abb.payment.entity.Payment;
import az.abb.payment.enums.Currency;
import az.abb.payment.enums.PaymentStatus;
import az.abb.payment.enums.PaymentType;
import az.abb.payment.exception.AccountNotFoundException;
import az.abb.payment.exception.InsufficientFundsException;
import az.abb.payment.mapper.PaymentMapper;
import az.abb.payment.repository.AccountRepository;
import az.abb.payment.repository.PaymentRepository;
import az.abb.payment.strategy.Payable;
import az.abb.payment.strategy.PaymentStrategyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CurrencyService currencyService;
    @Mock
    private KafkaPublisherService kafkaPublisherService;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private PaymentHistoryEvent paymentHistoryEvent;
    @Mock
    private Environment env;
    @Mock
    private PaymentStrategyResolver strategyResolver;
    @Mock
    private Payable paymentStrategy;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() throws Exception {
        Field topicField = PaymentService.class.getDeclaredField("paymentResultTopic");
        topicField.setAccessible(true);
        topicField.set(paymentService, "payment-result-topic");
    }

    // ─── processPayment ───────────────────────────────────────────────────────

    @Test
    void processPayment_ShouldReturnPaymentResponse_WhenPaymentIsSuccessful() {
        // Given
        Long userId = 1L;
        BigDecimal requestAmount  = BigDecimal.valueOf(100);
        BigDecimal finalAmount    = BigDecimal.valueOf(92);
        BigDecimal initialBalance = BigDecimal.valueOf(500);
        BigDecimal expectedBalance = initialBalance.subtract(finalAmount);

        PaymentRequest request = new PaymentRequest(requestAmount, Currency.USD, PaymentType.TRANSFER, "Test payment");

        Account account = Account.builder()
                .id(10L)
                .userId(userId)
                .balance(initialBalance)
                .currency(Currency.EUR)
                .build();

        Payment savedPayment = Payment.builder()
                .id(99L)
                .amount(requestAmount)
                .currency(Currency.EUR)
                .accountId(account.getId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentType(PaymentType.TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        PaymentHistoryEvent event = new PaymentHistoryEvent(/* fields */);
        String eventId = UUID.randomUUID().toString();

        given(accountRepository.findByUserId(userId)).willReturn(Optional.of(account));
        given(currencyService.resolveAmount(requestAmount, Currency.USD, Currency.EUR)).willReturn(finalAmount);
        given(accountRepository.save(account)).willReturn(account);
        given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);
        given(strategyResolver.resolve(PaymentType.TRANSFER)).willReturn(paymentStrategy);
        given(paymentMapper.toResponse(any(Payment.class))).willReturn(event);
        given(kafkaPublisherService.publish(event, "payment-result-topic")).willReturn(eventId);

        // When
        PaymentResponse response = paymentService.processPayment(request, userId);

        // Then
        assertThat(response.getBalance()).isEqualByComparingTo(expectedBalance);
//        assertThat(response.getPaymentId()).isEqualTo(99L);
        assertThat(response.getPayedAmount()).isEqualByComparingTo(finalAmount);
        assertThat(response.getCurrency()).isEqualTo(Currency.USD);
        assertThat(response.getStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
        assertThat(response.getEventId()).isEqualTo(eventId);

        verify(accountRepository).save(account);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentStrategy).doPayment(eq(account), any(Payment.class), eq(finalAmount));
        verify(kafkaPublisherService).publish(event, "payment-result-topic");
    }

    @Test
    void processPayment_ShouldThrowAccountNotFoundException_WhenAccountNotFound() {
        // Given
        Long userId = 99L;
        PaymentRequest request = new PaymentRequest(BigDecimal.valueOf(100), Currency.USD, PaymentType.TRANSFER, "Test payment");

        given(accountRepository.findByUserId(userId)).willReturn(Optional.empty());

        // When / Then
        assertThatThrownBy(() -> paymentService.processPayment(request, userId))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("No account found for user ID: " + userId);

        verifyNoInteractions(currencyService, paymentRepository, kafkaPublisherService, paymentStrategy);
    }

    @Test
    void processPayment_ShouldThrowInsufficientFundsException_WhenBalanceIsLow() {
        // Given
        Long userId = 1L;
        PaymentRequest request = new PaymentRequest(BigDecimal.valueOf(1000), Currency.USD, PaymentType.TRANSFER, "Test payment");

        Account account = Account.builder()
                .id(10L)
                .userId(userId)
                .balance(BigDecimal.valueOf(50))  // lower than finalAmount
                .currency(Currency.EUR)
                .build();

        given(accountRepository.findByUserId(userId)).willReturn(Optional.of(account));
        given(currencyService.resolveAmount(BigDecimal.valueOf(1000), Currency.USD, Currency.EUR))
                .willReturn(BigDecimal.valueOf(920));

        // When / Then
        assertThatThrownBy(() -> paymentService.processPayment(request, userId))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Insufficient balance");

        verifyNoInteractions(paymentRepository, kafkaPublisherService, strategyResolver);
    }

    @Test
    void processPayment_ShouldThrowRuntimeException_WhenKafkaPublishFails() {
        // Given
        Long userId = 1L;
        BigDecimal requestAmount  = BigDecimal.valueOf(100);
        BigDecimal finalAmount    = BigDecimal.valueOf(92);

        PaymentRequest request = new PaymentRequest(requestAmount, Currency.USD, PaymentType.TRANSFER, "Test payment");

        Account account = Account.builder()
                .id(10L)
                .userId(userId)
                .balance(BigDecimal.valueOf(500))
                .currency(Currency.EUR)
                .build();

        Payment savedPayment = Payment.builder()
                .id(99L)
                .amount(requestAmount)
                .currency(Currency.EUR)
                .accountId(account.getId())
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentType(PaymentType.TRANSFER)
                .createdAt(LocalDateTime.now())
                .build();

        PaymentHistoryEvent event = new PaymentHistoryEvent(/* fields */);

        given(accountRepository.findByUserId(userId)).willReturn(Optional.of(account));
        given(currencyService.resolveAmount(requestAmount, Currency.USD, Currency.EUR)).willReturn(finalAmount);
        given(accountRepository.save(account)).willReturn(account);
        given(paymentRepository.save(any(Payment.class))).willReturn(savedPayment);
        given(strategyResolver.resolve(PaymentType.TRANSFER)).willReturn(paymentStrategy);
        given(paymentMapper.toResponse(any(Payment.class))).willReturn(event);
        given(kafkaPublisherService.publish(event, "payment-result-topic"))
                .willThrow(new RuntimeException("Kafka unavailable"));

        // When / Then
        assertThatThrownBy(() -> paymentService.processPayment(request, userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Kafka unavailable");
    }
}