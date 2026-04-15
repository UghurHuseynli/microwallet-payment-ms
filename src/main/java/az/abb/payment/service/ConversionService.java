package az.abb.payment.service;

import az.abb.payment.dto.ConversionRequest;
import az.abb.payment.dto.ConversionResponse;
import az.abb.payment.dto.PaymentResultEvent;
import az.abb.payment.entity.Account;
import az.abb.payment.entity.Payment;
import az.abb.payment.repository.AccountRepository;
import az.abb.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversionService {

    @Value("${kafka.topics.convert-result}")
    private String paymentResultTopic;
    private final AccountRepository accountRepository;
    private final PaymentRepository paymentRepository;
    private final KafkaPublisherService kafkaPublisherService;
    private final Environment env;

    @Value("${payment.fee.percentage}")
    private BigDecimal feePercentage;

    @Transactional
    public ConversionResponse convert(Long accountId, ConversionRequest request) {
         Account account = accountRepository.findByUserId(accountId)
                 .orElseGet(() -> {
                     Long uniqueCif = accountRepository.getNextCifValue();
                     Account newAccount = Account.builder()
                             .userId(accountId)
                             .balance(new BigDecimal("1000.00"))
                             .cif(uniqueCif)
                             .build();
                     return accountRepository.save(newAccount);
                 });

//        Fetch exchange fee from config
         String rateKey = "currency.rates." + request.getFromCurrency() + "_" + request.getToCurrency();
         String rateStr = env.getProperty(rateKey);
         if (rateStr == null) {
             throw new RuntimeException("Exchange rate not configured for: " + rateKey);
         }

         BigDecimal rate = new BigDecimal(rateStr);
         BigDecimal convertedRaw = request.getAmount().multiply(rate);

//         Calculate fee
        BigDecimal feeAmount = request.getAmount()
                .multiply(feePercentage)
                .divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

        BigDecimal finalConverted = convertedRaw.subtract(feeAmount.multiply(rate))
                .setScale(4, RoundingMode.HALF_UP);

//        Persist payment record
//        Payment payment  = Payment.builder()
//                .accountId(account.getId())
//                .amount(request.getAmount())
//                .currency(request.getFromCurrency())
//                .build();
//        paymentRepository.save(payment);
//
////        Publish to Kafka
//        PaymentResultEvent event = PaymentResultEvent.builder()
//                .eventType("CONVERSION")
//                .accountId(account.getId())
//                .amount(request.getAmount())
//                .currency(request.getFromCurrency())
//                .convertedAmount(finalConverted)
//                .targetCurrency(request.getToCurrency())
//                .feeAmount(feeAmount)
//                .status("SUCCESS")
//                .message("Conversion completed successfully")
//                .processedAt(LocalDateTime.now())
//                .build();
//
//        String eventId = kafkaPublisherService.publishPaymentResult(event, paymentResultTopic);

        return ConversionResponse.builder()
                .accountId(account.getId())
                .originalAmount(request.getAmount())
                .fromCurrency(request.getFromCurrency())
                .convertedAmount(finalConverted)
                .toCurrency(request.getToCurrency())
                .feeAmount(feeAmount)
                .feePercentage(feePercentage)
                .status("SUCCESS")
//                .kafkaEventId(eventId)
                .build();
    }
}
