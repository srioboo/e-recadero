package org.sirantar.recadero.orders.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.orders.domain.Order;
import org.sirantar.recadero.orders.domain.OrderShipment;
import org.sirantar.recadero.orders.domain.OrderShipmentEvent;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.domain.ShipmentStatus;
import org.sirantar.recadero.orders.events.OrderEventPublisher;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentEventRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentRepository;
import org.sirantar.recadero.orders.service.dto.ShipmentDetailResponse;
import org.sirantar.recadero.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Shipment creation and carrier-webhook status tracking.
 */
@Service
@RequiredArgsConstructor
public class OrderShipmentService {

  private final OrderShipmentRepository orderShipmentRepository;
  private final OrderShipmentEventRepository orderShipmentEventRepository;
  private final OrderRepository orderRepository;
  private final OrderEventPublisher eventPublisher;

  @Transactional
  public OrderShipment createShipment(
      Long orderId, String carrier, String trackingNumber, LocalDateTime estimatedDelivery) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));

    OrderShipment shipment = new OrderShipment();
    shipment.setOrderId(orderId);
    shipment.setCarrier(carrier);
    shipment.setTrackingNumber(trackingNumber);
    shipment.setStatus(ShipmentStatus.PENDING);
    if (estimatedDelivery != null) {
      shipment.setEstimatedDeliveryDate(estimatedDelivery.toLocalDate());
    }
    LocalDateTime now = LocalDateTime.now();
    shipment.setCreatedAt(now);
    shipment.setUpdatedAt(now);
    OrderShipment saved = orderShipmentRepository.save(shipment);

    order.setStatus(OrderStatus.SHIPPED);
    order.setShippedDate(now);
    order.setUpdatedAt(now);
    orderRepository.save(order);

    eventPublisher.publishShipped(order.getId(), order.getOrderNumber(), order.getUserId(), trackingNumber, carrier, estimatedDelivery);
    return saved;
  }

  @Transactional
  public void updateShipmentStatus(String trackingNumber, String status, String location) {
    OrderShipment shipment = orderShipmentRepository.findByTrackingNumber(trackingNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for tracking number: " + trackingNumber));

    ShipmentStatus newStatus = ShipmentStatus.valueOf(status);
    LocalDateTime now = LocalDateTime.now();
    shipment.setStatus(newStatus);
    shipment.setUpdatedAt(now);
    if (newStatus == ShipmentStatus.DELIVERED) {
      shipment.setDeliveredAt(now);
    }
    orderShipmentRepository.save(shipment);

    OrderShipmentEvent event = new OrderShipmentEvent();
    event.setShipmentId(shipment.getId());
    event.setStatus(status);
    event.setLocation(location);
    event.setOccurredAt(now);
    orderShipmentEventRepository.save(event);

    if (newStatus == ShipmentStatus.DELIVERED) {
      Order order = orderRepository.findById(shipment.getOrderId()).orElse(null);
      if (order != null) {
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredDate(now);
        order.setUpdatedAt(now);
        orderRepository.save(order);
        eventPublisher.publishDelivered(order.getId(), order.getOrderNumber(), order.getUserId(), now);
      }
    }
  }

  public ShipmentDetailResponse getShipmentByOrder(Long orderId) {
    OrderShipment shipment = orderShipmentRepository.findByOrderId(orderId)
        .orElseThrow(() -> new ResourceNotFoundException("No shipment found for order: " + orderId));
    return toDetail(shipment);
  }

  public ShipmentDetailResponse getShipmentByTrackingNumber(String trackingNumber) {
    OrderShipment shipment = orderShipmentRepository.findByTrackingNumber(trackingNumber)
        .orElseThrow(() -> new ResourceNotFoundException("Shipment not found for tracking number: " + trackingNumber));
    return toDetail(shipment);
  }

  private ShipmentDetailResponse toDetail(OrderShipment shipment) {
    List<ShipmentDetailResponse.TrackingEvent> history =
        orderShipmentEventRepository.findByShipmentIdOrderByOccurredAtAsc(shipment.getId()).stream()
            .map(e -> new ShipmentDetailResponse.TrackingEvent(e.getOccurredAt(), e.getStatus(), e.getLocation()))
            .toList();

    return new ShipmentDetailResponse(
        shipment.getId(),
        shipment.getOrderId(),
        shipment.getCarrier(),
        shipment.getTrackingNumber(),
        shipment.getStatus().name(),
        shipment.getShippedAt(),
        shipment.getEstimatedDeliveryDate() != null ? shipment.getEstimatedDeliveryDate().atTime(23, 59, 59) : null,
        shipment.getDeliveredAt(),
        history);
  }
}
