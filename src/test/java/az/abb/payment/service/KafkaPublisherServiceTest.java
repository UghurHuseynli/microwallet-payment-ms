package az.abb.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KafkaPublisherServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private KafkaPublisherService kafkaPublisherService;

    private static final String TOPIC = "test-topic";

    @Test
    void publish_ShouldReturnEventId_WhenPublishedSuccessfully() throws JsonProcessingException {
        TestEvent event = new TestEvent("data");
        String payload = "{\"data\":\"data\"}";

        SendResult<String, Object> sendResult = mockSendResult(TOPIC, 0, 1L);
        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(sendResult);

        given(objectMapper.writeValueAsString(event)).willReturn(payload);
        given(kafkaTemplate.send(eq(TOPIC), anyString(), eq(payload))).willReturn(future);

        String eventId = kafkaPublisherService.publish(event, TOPIC);

        assertThat(eventId).isNotNull();
        assertThat(eventId).matches("[0-9a-f\\-]{36}"); // UUID format

        verify(objectMapper).writeValueAsString(event);
        verify(kafkaTemplate).send(eq(TOPIC), anyString(), eq(payload));
    }

    @Test
    void publish_ShouldReturnEventId_WhenKafkaSendFails() throws JsonProcessingException {
        TestEvent event = new TestEvent("data");
        String payload = "{\"data\":\"data\"}";

        CompletableFuture<SendResult<String, Object>> future = CompletableFuture.failedFuture(
                new RuntimeException("Broker unavailable")
        );

        given(objectMapper.writeValueAsString(event)).willReturn(payload);
        given(kafkaTemplate.send(eq(TOPIC), anyString(), eq(payload))).willReturn(future);

        String eventId = kafkaPublisherService.publish(event, TOPIC);

        assertThat(eventId).isNotNull();
        assertThat(eventId).matches("[0-9a-f\\-]{36}");
    }

    @Test
    void publish_ShouldThrowRuntimeException_WhenSerializationFails() throws JsonProcessingException {
        // Given
        TestEvent event = new TestEvent("data");

        given(objectMapper.writeValueAsString(event))
                .willThrow(new JsonProcessingException("Serialization error") {});

        assertThatThrownBy(() -> kafkaPublisherService.publish(event, TOPIC))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Event serialization failed")
                .hasCauseInstanceOf(JsonProcessingException.class);

        verifyNoInteractions(kafkaTemplate);
    }

    @SuppressWarnings("unchecked")
    private SendResult<String, Object> mockSendResult(String topic, int partition, long offset) {
        SendResult<String, Object> sendResult = mock(SendResult.class);
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(topic, partition), offset, 0, 0L, 0, 0
        );
        given(sendResult.getRecordMetadata()).willReturn(metadata);
        return sendResult;
    }

    record TestEvent(String data) {}
}