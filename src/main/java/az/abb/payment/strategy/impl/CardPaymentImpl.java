package az.abb.payment.strategy.impl;

import az.abb.payment.entity.Account;
import az.abb.payment.entity.Payment;
import az.abb.payment.enums.PaymentType;
import az.abb.payment.strategy.Payable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@Slf4j
class CardPaymentImpl implements Payable {

    @Override
    public void doPayment(Account account, Payment payment, BigDecimal finalAmount) {
        log.info("Processing CARD payment of {}", finalAmount);
    }

    @Override
    public PaymentType supportedType() {
        return PaymentType.CARD;
    }
}
