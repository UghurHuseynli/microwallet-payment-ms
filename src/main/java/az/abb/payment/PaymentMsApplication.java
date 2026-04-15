package az.abb.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@AutoConfiguration
public class PaymentMsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentMsApplication.class, args);
    }
}
