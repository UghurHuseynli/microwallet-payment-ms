package az.abb.payment.controller;

import az.abb.payment.dto.request.ConversionRequest;
import az.abb.payment.dto.request.DocumentRequest;
import az.abb.payment.dto.request.PaymentRequest;
import az.abb.payment.dto.response.ConversionResponse;
import az.abb.payment.dto.response.DocumentResponse;
import az.abb.payment.dto.response.PaymentResponse;
import az.abb.payment.service.ConversionService;
import az.abb.payment.service.DocumentService;
import az.abb.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final DocumentService documentService;
    private final ConversionService conversionService;
    private final PaymentService paymentService;

    @PostMapping(value = "/accept-doc", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestHeader("X-Account-Id") Long userId,
            @RequestBody DocumentRequest request
            ) {
        DocumentResponse response = documentService.acceptDocument(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/convert")
    public ResponseEntity<ConversionResponse> convertCurrency(
            @RequestHeader("X-Account-Id") Long  userId,
            @RequestBody ConversionRequest request
            ) {
        return ResponseEntity.ok(conversionService.convertAmount(userId, request));
    }

    @PostMapping("/pay")
    public ResponseEntity<PaymentResponse> pay(
            @RequestHeader("X-Account-Id") Long userId,
            @RequestBody PaymentRequest request
    ) {
        PaymentResponse response = paymentService.processPayment(request, userId);
        return ResponseEntity.ok(response);
    }

}
