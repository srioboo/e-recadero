package org.sirantar.recadero.orders.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.orders.domain.OrderItem;
import org.sirantar.recadero.orders.domain.OrderReturn;
import org.sirantar.recadero.orders.domain.ReturnStatus;
import org.sirantar.recadero.orders.repository.OrderItemRepository;
import org.sirantar.recadero.orders.repository.OrderReturnRepository;
import org.sirantar.recadero.orders.service.dto.InitiateReturnResponse;
import org.sirantar.recadero.orders.service.dto.ReturnListItem;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Return/RMA request lifecycle. Return shipping labels are a stub (a CDN
 * URL pattern) — no label-generation carrier integration exists yet.
 */
@Service
@RequiredArgsConstructor
public class OrderReturnService {

  private static final DateTimeFormatter RMA_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final OrderReturnRepository orderReturnRepository;
  private final OrderItemRepository orderItemRepository;

  @Transactional
  public InitiateReturnResponse initiateReturn(Long orderId, Long orderItemId, String reason, String description) {
    OrderItem item = orderItemRepository.findById(orderItemId)
        .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + orderItemId));
    if (!item.getOrderId().equals(orderId)) {
      throw new ResourceNotFoundException("Order item not found: " + orderItemId);
    }

    String returnNumber = generateReturnNumber();
    OrderReturn orderReturn = new OrderReturn();
    orderReturn.setOrderId(orderId);
    orderReturn.setOrderItemId(orderItemId);
    orderReturn.setReturnNumber(returnNumber);
    orderReturn.setReason(reason);
    orderReturn.setDescription(description);
    orderReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
    orderReturn.setRefundAmount(item.getLineTotal());
    orderReturn.setRequestedAt(LocalDateTime.now());
    OrderReturn saved = orderReturnRepository.save(orderReturn);

    return new InitiateReturnResponse(
        saved.getId(),
        orderId,
        saved.getStatus().name(),
        reason,
        returnNumber,
        1,
        saved.getRefundAmount(),
        "We'll review your return. Approval typically takes 1-2 business days.",
        "http://cdn.example.com/return-labels/" + returnNumber.toLowerCase());
  }

  @Transactional
  public OrderReturn approveReturn(Long returnId) {
    OrderReturn orderReturn = getReturn(returnId);
    orderReturn.setStatus(ReturnStatus.APPROVED);
    orderReturn.setApprovedAt(LocalDateTime.now());
    return orderReturnRepository.save(orderReturn);
  }

  @Transactional
  public OrderReturn processReturnRefund(Long returnId, BigDecimal amount) {
    OrderReturn orderReturn = getReturn(returnId);
    orderReturn.setRefundAmount(amount != null ? amount : orderReturn.getRefundAmount());
    orderReturn.setStatus(ReturnStatus.COMPLETED);
    orderReturn.setCompletedAt(LocalDateTime.now());
    return orderReturnRepository.save(orderReturn);
  }

  public List<ReturnListItem> listReturns(Long orderId) {
    return orderReturnRepository.findByOrderId(orderId).stream()
        .map(r -> new ReturnListItem(r.getId(), r.getOrderId(), r.getStatus().name(), r.getReason(), 1, r.getRefundAmount(), r.getRequestedAt()))
        .toList();
  }

  private OrderReturn getReturn(Long returnId) {
    return orderReturnRepository.findById(returnId)
        .orElseThrow(() -> new ResourceNotFoundException("Return not found: " + returnId));
  }

  private String generateReturnNumber() {
    String datePart = LocalDateTime.now().format(RMA_DATE);
    String randomPart = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    return "RMA-" + datePart + "-" + randomPart;
  }
}
