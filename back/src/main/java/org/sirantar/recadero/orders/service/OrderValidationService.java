package org.sirantar.recadero.orders.service;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.shared.exception.ResourceConflictException;
import org.springframework.stereotype.Service;

/**
 * Status-transition and business-rule validation for orders.
 */
@Service
public class OrderValidationService {

  private static final int REFUND_WINDOW_DAYS = 30;

  private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

  static {
    ALLOWED_TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
    ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED));
    ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPED, EnumSet.of(OrderStatus.DELIVERED));
    ALLOWED_TRANSITIONS.put(OrderStatus.DELIVERED, EnumSet.of(OrderStatus.REFUNDED));
    ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    ALLOWED_TRANSITIONS.put(OrderStatus.REFUNDED, EnumSet.noneOf(OrderStatus.class));
  }

  public void validateTransition(OrderStatus current, OrderStatus requested) {
    if (current == requested) {
      return;
    }
    if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(requested)) {
      throw new ResourceConflictException(
          "INVALID_STATUS_TRANSITION",
          "Cannot transition order from " + current + " to " + requested,
          Map.of("current_status", current.name(), "requested_status", requested.name()));
    }
  }

  public void validateCancellable(OrderStatus current) {
    if (current != OrderStatus.PENDING && current != OrderStatus.CONFIRMED) {
      throw new ResourceConflictException(
          "CANNOT_CANCEL",
          "Order already " + current.name().toLowerCase() + "; cannot cancel",
          Map.of("current_status", current.name()));
    }
  }

  public void validateWithinRefundWindow(LocalDateTime deliveredAt) {
    if (deliveredAt == null) {
      return;
    }
    long daysSinceDelivery = java.time.Duration.between(deliveredAt, LocalDateTime.now()).toDays();
    if (daysSinceDelivery > REFUND_WINDOW_DAYS) {
      throw new ResourceConflictException(
          "REFUND_WINDOW_CLOSED",
          "Refunds only available within " + REFUND_WINDOW_DAYS + " days of delivery",
          Map.of("delivered_date", deliveredAt, "days_since_delivery", daysSinceDelivery));
    }
  }
}
