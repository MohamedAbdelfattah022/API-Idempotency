package com.mohamed.idempotency.controller;

import com.mohamed.idempotency.dto.PaymentRequest;
import com.mohamed.idempotency.dto.PaymentResponse;
import com.mohamed.idempotency.entity.IdempotencyRecord;
import com.mohamed.idempotency.service.IdempotencyService;
import com.mohamed.idempotency.service.LockService;
import com.mohamed.idempotency.service.PaymentService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class IdempotentPaymentController {
    private final PaymentService paymentService;
    private final IdempotencyService idempotencyService;
    private final LockService lockService;


    @PostMapping("idempotent")
    public ResponseEntity<PaymentResponse> processPaymentIdempotent(
            @RequestHeader(value = "Idempotency-Key")
            @NotBlank
            String idempotencyKey,
            @RequestBody PaymentRequest request
    ) {
        log.info("IDEMPOTENT ENDPOINT - Using idempotency key: {}", idempotencyKey);

        String requestHash = idempotencyService.generateHash(idempotencyKey, request);

        Optional<IdempotencyRecord> existingRecord = idempotencyService.findExistingRecord(idempotencyKey, requestHash);

        if (existingRecord.isPresent()) {
            var record = existingRecord.get();
            var cachedResponse = idempotencyService.deserializeResponse(
                    record.getResponseBody(),
                    PaymentResponse.class
            );

            return ResponseEntity.status(record.getHttpStatus()).body(cachedResponse);
        }

        log.info("Processing new payment request");
        var response = paymentService.processPayment(request);
        idempotencyService.saveRecord(idempotencyKey, requestHash, response, HttpStatus.OK.value());

        return ResponseEntity.ok(response);
    }

    @PostMapping("idempotent/lock")
    public ResponseEntity<PaymentResponse> processPaymentIdempotentWithLock(
            @RequestHeader(value = "Idempotency-Key")
            @NotBlank
            String idempotencyKey,
            @RequestBody PaymentRequest request
    ) {
        log.info("IDEMPOTENT WITH LOCK ENDPOINT - Using idempotency key: {}", idempotencyKey);

        var lock = lockService.acquireLock("idempotency:" + idempotencyKey);
        try {
            String requestHash = idempotencyService.generateHash(idempotencyKey, request);

            Optional<IdempotencyRecord> existingRecord = idempotencyService.findExistingRecord(idempotencyKey, requestHash);

            if (existingRecord.isPresent()) {
                var record = existingRecord.get();
                var cachedResponse = idempotencyService.deserializeResponse(
                        record.getResponseBody(),
                        PaymentResponse.class
                );

                return ResponseEntity.status(record.getHttpStatus()).body(cachedResponse);
            }

            log.info("Processing new payment request");
            var response = paymentService.processPayment(request);
            idempotencyService.saveRecord(idempotencyKey, requestHash, response, HttpStatus.OK.value());

            return ResponseEntity.ok(response);
        } finally {
            lockService.releaseLock("idempotency:" + idempotencyKey, lock);
        }
    }
}
