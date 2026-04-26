package az.abb.payment.service;

import az.abb.payment.dto.request.DocumentRequest;
import az.abb.payment.dto.response.DocumentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    public DocumentResponse acceptDocument(DocumentRequest request, Long userId) {

      log.info("Received request to accept {} document(file size: {} byte) with account id {}", request.fileName(), request.fileSize(), userId);
      return DocumentResponse.builder()
              .message("Document accepted for processing")
              .build();
    }
}
