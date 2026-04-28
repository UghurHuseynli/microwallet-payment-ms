package az.abb.payment.service;

import az.abb.payment.dto.request.DocumentRequest;
import az.abb.payment.dto.response.DocumentResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @InjectMocks
    private DocumentService documentService;

    @Test
    void acceptDocument_ShouldReturnDocumentResponse_WithCorrectMessage() {
        Long userId = 1L;
        DocumentRequest request = new DocumentRequest("invoice.pdf", "Invoice document", 2048L, "application/pdf", "extracted text");

        DocumentResponse response = documentService.acceptDocument(request, userId);

        assertThat(response.getMessage()).isEqualTo("Document accepted for processing");
    }

    @Test
    void acceptDocument_ShouldHandleZeroFileSize() {
        DocumentRequest request = new DocumentRequest("empty.pdf", "Empty file", 0L, "application/pdf", null);

        DocumentResponse response = documentService.acceptDocument(request, 1L);

        assertThat(response.getMessage()).isEqualTo("Document accepted for processing");
    }

    @Test
    void acceptDocument_ShouldHandleNullUserId() {
        DocumentRequest request = new DocumentRequest("file.pdf", "Some doc", 1024L, "application/pdf", "some text");

        DocumentResponse response = documentService.acceptDocument(request, null);

        assertThat(response.getMessage()).isEqualTo("Document accepted for processing");
    }
}