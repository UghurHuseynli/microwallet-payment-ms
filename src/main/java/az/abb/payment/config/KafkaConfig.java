package az.abb.payment.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.payment-result}")
    private String paymentResultTopic;

    @Bean
    public NewTopic paymentResultTopic() {
        return TopicBuilder.name(paymentResultTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
