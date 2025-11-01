package com.mohamed.idempotency.service;

import com.mohamed.idempotency.dto.PaymentRequest;
import com.mohamed.idempotency.dto.PaymentResponse;
import com.mohamed.idempotency.entity.Payment;
import com.mohamed.idempotency.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRepository paymentRepository;

    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        log.info("Processing payment for user: {}, amount: {}", request.getUserId(), request.getAmount());

        var payment = Payment.builder()
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .description(request.getDescription())
                .status("COMPLETED")
                .build();

        var savedPayment = paymentRepository.save(payment);
        log.info("Payment processed successfully. Payment ID: {}", savedPayment.getId());

        return PaymentResponse.builder()
                .paymentId(savedPayment.getId())
                .userId(savedPayment.getUserId())
                .amount(savedPayment.getAmount())
                .currency(savedPayment.getCurrency())
                .status(savedPayment.getStatus())
                .timestamp(savedPayment.getCreatedAt())
                .message("Payment processed successfully")
                .build();
    }
}
