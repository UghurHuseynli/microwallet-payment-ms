package az.abb.payment.dto.request;

public record DocumentRequest (
        String fileName,
        String description,
        Long fileSize,
        String contentType,
        String extractedText
) {
}
