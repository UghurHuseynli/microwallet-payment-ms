package az.abb.payment.controller;

import az.abb.payment.dto.ConversionRequest;
import az.abb.payment.dto.ConversionResponse;
import az.abb.payment.dto.PaymentDocResponse;
import az.abb.payment.service.ConversionService;
import az.abb.payment.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final DocumentService documentService;
    private final ConversionService conversionService;

    @PostMapping(value = "/proccess-doc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PaymentDocResponse> uploadDocument(
            @RequestPart("accountId") Long  accountId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("description") String description) {
        return ResponseEntity.ok(documentService.processDocument(accountId, file, description));
    }

    @PostMapping("/convert")
    public ResponseEntity<ConversionResponse> convertCurrency(
            @RequestParam("accountId") Long  accountId,
            @RequestBody ConversionRequest request
            ) {
        return ResponseEntity.ok(conversionService.convert(accountId, request));
    }

}
