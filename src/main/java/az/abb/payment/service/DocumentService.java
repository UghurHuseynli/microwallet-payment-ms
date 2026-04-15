package az.abb.payment.service;

import az.abb.payment.dto.PaymentDocResponse;
import az.abb.payment.dto.PaymentResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    @Value("${kafka.topics.document-result}")
    private String documentResultTopic;

    private final KafkaPublisherService kafkaPublisherService;

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "application/pdf", "image/jpeg", "image/png",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    public PaymentDocResponse processDocument(Long accountId, MultipartFile file, String description) {
        if (file.isEmpty()) {
            throw new RuntimeException("Uploaded file is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new RuntimeException("Unsupported file type" + contentType);
        }

        String extractedText = extractText(file);

        PaymentResultEvent event = PaymentResultEvent.builder()
                .eventType("DOCUMENT_PROCESSED")
                .status("STATUS")
                .message("Document '" + file.getOriginalFilename() + "' proccessed")
                .build();

        kafkaPublisherService.publishPaymentResult(event, documentResultTopic);

        return PaymentDocResponse.builder()
                .fileName(file.getOriginalFilename())
                .description(description)
                .fileSize(file.getSize())
                .contentType(contentType)
                .extractedText(extractedText)
                .status("SUCCESS")
                .build();
    }

    private String extractText(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            return "Extracted " + bytes.length + " bytes from " + file.getOriginalFilename();
        } catch (Exception e) {
            log.error("Failed to read file bytes", e);
            return "Extraction failed";
        }
    }

}
