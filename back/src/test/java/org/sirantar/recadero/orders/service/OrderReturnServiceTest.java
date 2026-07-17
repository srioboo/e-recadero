package org.sirantar.recadero.orders.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.orders.domain.OrderItem;
import org.sirantar.recadero.orders.domain.OrderReturn;
import org.sirantar.recadero.orders.domain.ReturnStatus;
import org.sirantar.recadero.orders.repository.OrderItemRepository;
import org.sirantar.recadero.orders.repository.OrderReturnRepository;
import org.sirantar.recadero.orders.service.dto.InitiateReturnResponse;

@ExtendWith(MockitoExtension.class)
class OrderReturnServiceTest {

  @Mock private OrderReturnRepository orderReturnRepository;
  @Mock private OrderItemRepository orderItemRepository;

  private OrderReturnService returnService;

  @BeforeEach
  void setUp() {
    returnService = new OrderReturnService(orderReturnRepository, orderItemRepository);
  }

  @Test
  void initiateReturnGeneratesRmaNumberAndPendingStatus() {
    OrderItem item = new OrderItem();
    item.setId(500L);
    item.setOrderId(1L);
    item.setLineTotal(BigDecimal.valueOf(19.99));
    when(orderItemRepository.findById(500L)).thenReturn(Optional.of(item));
    when(orderReturnRepository.save(any(OrderReturn.class))).thenAnswer(inv -> {
      OrderReturn r = inv.getArgument(0);
      if (r.getId() == null) r.setId(1L);
      return r;
    });

    InitiateReturnResponse response = returnService.initiateReturn(1L, 500L, "DEFECTIVE", "cracked screen");

    assertThat(response.status()).isEqualTo("PENDING_APPROVAL");
    assertThat(response.returnTrackingNumber()).startsWith("RMA-");
    assertThat(response.estimatedRefund()).isEqualByComparingTo("19.99");
    assertThat(response.returnShippingLabel()).contains(response.returnTrackingNumber().toLowerCase());
  }

  @Test
  void approveReturnTransitionsToApprovedAndStampsTimestamp() {
    OrderReturn orderReturn = new OrderReturn();
    orderReturn.setId(1L);
    orderReturn.setStatus(ReturnStatus.PENDING_APPROVAL);
    when(orderReturnRepository.findById(1L)).thenReturn(Optional.of(orderReturn));
    when(orderReturnRepository.save(any(OrderReturn.class))).thenAnswer(inv -> inv.getArgument(0));

    OrderReturn approved = returnService.approveReturn(1L);

    assertThat(approved.getStatus()).isEqualTo(ReturnStatus.APPROVED);
    assertThat(approved.getApprovedAt()).isNotNull();
  }

  @Test
  void processReturnRefundCompletesTheReturn() {
    OrderReturn orderReturn = new OrderReturn();
    orderReturn.setId(1L);
    orderReturn.setStatus(ReturnStatus.APPROVED);
    when(orderReturnRepository.findById(1L)).thenReturn(Optional.of(orderReturn));
    when(orderReturnRepository.save(any(OrderReturn.class))).thenAnswer(inv -> inv.getArgument(0));

    OrderReturn completed = returnService.processReturnRefund(1L, BigDecimal.valueOf(19.99));

    assertThat(completed.getStatus()).isEqualTo(ReturnStatus.COMPLETED);
    assertThat(completed.getRefundAmount()).isEqualByComparingTo("19.99");
    assertThat(completed.getCompletedAt()).isNotNull();
  }
}
