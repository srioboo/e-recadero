package org.sirantar.recadero.orders.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response payload for GET /api/v1/orders/{order_id}/payment.
 */
public record PaymentDetailResponse(
    @JsonProperty("payment_id") Long paymentId,
    @JsonProperty("order_id") Long orderId,
    String status,
    BigDecimal amount,
    String currency,
    String method,
    @JsonProperty("transaction_id") String transactionId,
    @JsonProperty("processed_at") LocalDateTime processedAt,
    @JsonProperty("refund_status") String refundStatus,
    @JsonProperty("refund_history") List<RefundHistoryEntry> refundHistory) {

  public record RefundHistoryEntry(
      BigDecimal amount, @JsonProperty("created_at") LocalDateTime createdAt, String reason) {}
}
