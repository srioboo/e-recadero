package org.sirantar.recadero.orders.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.service.OrderService;
import org.sirantar.recadero.orders.service.OrderShipmentService;
import org.sirantar.recadero.orders.service.dto.AdminCreateShipmentRequest;
import org.sirantar.recadero.orders.service.dto.AdminOrderListItem;
import org.sirantar.recadero.orders.service.dto.AdminStatusChangeRequest;
import org.sirantar.recadero.orders.service.dto.OrderDetailResponse;
import org.sirantar.recadero.orders.service.dto.ShipmentDetailResponse;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.security.AdminOnly;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Admin-only order oversight: cross-user listing, forced status changes,
 * and manual shipment creation.
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
@AdminOnly
public class AdminOrderController {

  private final OrderService orderService;
  private final OrderShipmentService orderShipmentService;

  @GetMapping
  public PaginationResponse<AdminOrderListItem> listOrders(
      @RequestParam(required = false) String status,
      @RequestParam(name = "user_id", required = false) Long userId,
      @RequestParam(name = "from_date", required = false) LocalDateTime fromDate,
      @RequestParam(name = "to_date", required = false) LocalDateTime toDate,
      @RequestParam(name = "min_total", required = false) BigDecimal minTotal,
      @RequestParam(name = "max_total", required = false) BigDecimal maxTotal,
      Pageable pageable) {
    return orderService.listOrdersAdmin(
        userId, status != null ? OrderStatus.valueOf(status) : null, fromDate, toDate, minTotal, maxTotal, pageable);
  }

  @PatchMapping("/{id}/status")
  public OrderDetailResponse changeStatus(@PathVariable Long id, @RequestBody AdminStatusChangeRequest request) {
    return orderService.adminChangeStatus(id, OrderStatus.valueOf(request.status()), request.notes());
  }

  @PostMapping("/{id}/shipment")
  @ResponseStatus(HttpStatus.CREATED)
  public ShipmentDetailResponse createShipment(@PathVariable Long id, @RequestBody AdminCreateShipmentRequest request) {
    orderShipmentService.createShipment(id, request.carrier(), request.trackingNumber(), request.estimatedDelivery());
    return orderShipmentService.getShipmentByOrder(id);
  }
}
