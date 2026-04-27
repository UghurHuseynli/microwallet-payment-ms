package az.abb.payment.strategy;

import az.abb.payment.entity.Account;
import az.abb.payment.entity.Payment;
import az.abb.payment.enums.PaymentType;

import java.math.BigDecimal;

public interface Payable {
    void doPayment(Account account, Payment payment, BigDecimal finalAmount);
    PaymentType supportedType();
}