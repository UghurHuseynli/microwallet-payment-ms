package az.abb.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaPublisherService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public <T> String publish(T event, String topic) {
        String eventId = UUID.randomUUID().toString();

        try {
            String payload = objectMapper.writeValueAsString(event);
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(topic, eventId, payload);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event [{}]: {}", eventId, ex.getMessage());
                } else {
                    log.info("Successfully result event [{}] published to topic [{}] partition [{}] offset [{}]",
                            eventId,
                            result.getRecordMetadata().topic(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                }
            });
        } catch (JsonProcessingException e) {
            log.error("Failed to publish event: {}", e.getMessage());
            throw new RuntimeException("Event serialization failed", e);
        }
        return eventId;
    }

}
