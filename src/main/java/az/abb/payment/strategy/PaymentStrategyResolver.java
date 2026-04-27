package az.abb.payment.strategy;

import az.abb.linkedlist.DoublyLinkedList;
import az.abb.payment.enums.PaymentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.StreamSupport;


@Component
@Slf4j
public class PaymentStrategyResolver {

    private final DoublyLinkedList<Payable> strategies = new DoublyLinkedList<>();

    public PaymentStrategyResolver(List<Payable> payables) {
        payables.forEach(strategies::addLast);
    }

    public Payable resolve(PaymentType type) {
        return StreamSupport.stream(strategies.spliterator(), false)
                .filter(s -> s.supportedType() == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No strategy for type: " + type));
    }
}