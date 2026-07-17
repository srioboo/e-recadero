package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Abbreviated payment info embedded in an order detail response.
 */
public record PaymentSummary(
    String status,
    String method,
    @JsonProperty("transaction_id") String transactionId,
    BigDecimal amount,
    @JsonProperty("processed_at") LocalDateTime processedAt) {}
