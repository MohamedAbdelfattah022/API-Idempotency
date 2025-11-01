package com.mohamed.idempotency.controller;

import com.mohamed.idempotency.dto.PaymentRequest;
import com.mohamed.idempotency.dto.PaymentResponse;
import com.mohamed.idempotency.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class UnsafePaymentController {
    private final PaymentService paymentService;

    @PostMapping("unsafe")
    public ResponseEntity<PaymentResponse> processPaymentUnsafe(@RequestBody PaymentRequest request) {
        log.warn("UNSAFE ENDPOINT - No idempotency protection");
        var response = paymentService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}
