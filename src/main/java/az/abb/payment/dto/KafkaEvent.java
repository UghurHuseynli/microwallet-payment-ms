package az.abb.payment.dto;

public interface KafkaEvent {
    void setEventId(String eventId);
    String getEventId();
}
