package az.abb.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDocResponse {
    private String fileName;
    private String description;
    private Long fileSize;
    private String contentType;
    private String extractedText;
    private String status;
}
