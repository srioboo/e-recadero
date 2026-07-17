package org.sirantar.recadero.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.orders.domain.Order;
import org.sirantar.recadero.orders.domain.OrderShipment;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.domain.ShipmentStatus;
import org.sirantar.recadero.orders.events.OrderEventPublisher;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentEventRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentRepository;

@ExtendWith(MockitoExtension.class)
class OrderShipmentServiceTest {

  @Mock private OrderShipmentRepository orderShipmentRepository;
  @Mock private OrderShipmentEventRepository orderShipmentEventRepository;
  @Mock private OrderRepository orderRepository;
  @Mock private OrderEventPublisher eventPublisher;

  private OrderShipmentService shipmentService;

  @BeforeEach
  void setUp() {
    shipmentService = new OrderShipmentService(orderShipmentRepository, orderShipmentEventRepository, orderRepository, eventPublisher);
  }

  @Test
  void updateShipmentStatusRecordsHistoryAndEmitsNoDeliveryEventForInTransit() {
    OrderShipment shipment = new OrderShipment();
    shipment.setId(5L);
    shipment.setOrderId(1L);
    shipment.setTrackingNumber("TRACK-1");
    shipment.setStatus(ShipmentStatus.PENDING);
    when(orderShipmentRepository.findByTrackingNumber("TRACK-1")).thenReturn(Optional.of(shipment));
    when(orderShipmentRepository.save(any(OrderShipment.class))).thenAnswer(inv -> inv.getArgument(0));

    shipmentService.updateShipmentStatus("TRACK-1", "IN_TRANSIT", "Chicago Hub");

    assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.IN_TRANSIT);
    org.mockito.Mockito.verify(orderShipmentEventRepository).save(any());
    org.mockito.Mockito.verify(eventPublisher, org.mockito.Mockito.never()).publishDelivered(any(), any(), any(), any());
  }

  @Test
  void updateShipmentStatusMarksOrderDeliveredAndEmitsEvent() {
    OrderShipment shipment = new OrderShipment();
    shipment.setId(5L);
    shipment.setOrderId(1L);
    shipment.setTrackingNumber("TRACK-1");
    shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
    when(orderShipmentRepository.findByTrackingNumber("TRACK-1")).thenReturn(Optional.of(shipment));
    when(orderShipmentRepository.save(any(OrderShipment.class))).thenAnswer(inv -> inv.getArgument(0));

    Order order = new Order();
    order.setId(1L);
    order.setUserId(10L);
    order.setOrderNumber("ORD-1");
    order.setStatus(OrderStatus.SHIPPED);
    when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

    shipmentService.updateShipmentStatus("TRACK-1", "DELIVERED", "Front door");

    assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.DELIVERED);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    org.mockito.Mockito.verify(eventPublisher).publishDelivered(
        org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq("ORD-1"),
        org.mockito.ArgumentMatchers.eq(10L), any());
  }
}
