package com.mohamed.idempotency.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime timestamp;
    private String message;
}