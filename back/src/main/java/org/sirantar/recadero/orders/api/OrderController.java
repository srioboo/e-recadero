package org.sirantar.recadero.orders.api;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.service.OrderReturnService;
import org.sirantar.recadero.orders.service.OrderService;
import org.sirantar.recadero.orders.service.OrderShipmentService;
import org.sirantar.recadero.orders.service.dto.CancelOrderRequest;
import org.sirantar.recadero.orders.service.dto.CancelOrderResponse;
import org.sirantar.recadero.orders.service.dto.InitiateReturnRequest;
import org.sirantar.recadero.orders.service.dto.InitiateReturnResponse;
import org.sirantar.recadero.orders.service.dto.OrderDetailResponse;
import org.sirantar.recadero.orders.service.dto.OrderListItem;
import org.sirantar.recadero.orders.service.dto.PaymentDetailResponse;
import org.sirantar.recadero.orders.service.dto.RefundRequest;
import org.sirantar.recadero.orders.service.dto.RefundResponse;
import org.sirantar.recadero.orders.service.dto.ReturnListItem;
import org.sirantar.recadero.orders.service.dto.ShipmentDetailResponse;
import org.sirantar.recadero.orders.service.dto.ShipmentWebhookRequest;
import org.sirantar.recadero.orders.service.dto.WebhookResponse;
import org.sirantar.recadero.shared.dto.PaginationResponse;
import org.sirantar.recadero.shared.security.Authenticated;
import org.sirantar.recadero.shared.security.SecurityUser;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
 * Authenticated customer's own orders: history, detail, cancellation,
 * refunds, shipment tracking, and returns.
 * See specs/002-backend-ecommerce/contracts/orders-contract.md.
 */
@RestController
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;
  private final OrderShipmentService orderShipmentService;
  private final OrderReturnService orderReturnService;

  @Authenticated
  @GetMapping("/api/v1/orders")
  public PaginationResponse<OrderListItem> listOrders(
      @AuthenticationPrincipal SecurityUser user, @RequestParam(required = false) String status, Pageable pageable) {
    return orderService.listOrders(userId(user), status != null ? OrderStatus.valueOf(status) : null, pageable);
  }

  @Authenticated
  @GetMapping("/api/v1/orders/{id}")
  public OrderDetailResponse getOrder(@AuthenticationPrincipal SecurityUser user, @PathVariable Long id) {
    return orderService.getOrder(id, userId(user));
  }

  @Authenticated
  @PatchMapping("/api/v1/orders/{id}/cancel")
  public CancelOrderResponse cancelOrder(
      @AuthenticationPrincipal SecurityUser user, @PathVariable Long id, @RequestBody CancelOrderRequest request) {
    return orderService.cancelOrder(id, userId(user), request.reason());
  }

  @Authenticated
  @PostMapping("/api/v1/orders/{id}/refund")
  public RefundResponse refund(
      @AuthenticationPrincipal SecurityUser user, @PathVariable Long id, @RequestBody RefundRequest request) {
    return orderService.refundOrder(id, userId(user), request.amount(), request.reason());
  }

  @Authenticated
  @GetMapping("/api/v1/orders/{id}/payment")
  public PaymentDetailResponse getPayment(@AuthenticationPrincipal SecurityUser user, @PathVariable Long id) {
    return orderService.getPaymentDetail(id, userId(user));
  }

  @Authenticated
  @GetMapping("/api/v1/orders/{id}/shipment")
  public ShipmentDetailResponse getShipment(@AuthenticationPrincipal SecurityUser user, @PathVariable Long id) {
    orderService.getOrder(id, userId(user));
    return orderShipmentService.getShipmentByOrder(id);
  }

  @Authenticated
  @PostMapping("/api/v1/orders/{id}/return")
  @ResponseStatus(HttpStatus.CREATED)
  public InitiateReturnResponse initiateReturn(
      @AuthenticationPrincipal SecurityUser user, @PathVariable Long id, @RequestBody InitiateReturnRequest request) {
    orderService.getOrder(id, userId(user));
    return orderReturnService.initiateReturn(id, request.orderItemId(), request.reason(), request.description());
  }

  @Authenticated
  @GetMapping("/api/v1/orders/{id}/returns")
  public java.util.List<ReturnListItem> listReturns(@AuthenticationPrincipal SecurityUser user, @PathVariable Long id) {
    orderService.getOrder(id, userId(user));
    return orderReturnService.listReturns(id);
  }

  // Carrier webhook: no end-user JWT available, so this endpoint is not
  // gated by @Authenticated. A production deployment needs a proper
  // carrier API-key/HMAC verification scheme here before going live.
  @PostMapping("/api/v1/orders/{id}/shipment/webhook")
  public WebhookResponse shipmentWebhook(@PathVariable Long id, @RequestBody ShipmentWebhookRequest request) {
    orderShipmentService.updateShipmentStatus(request.trackingNumber(), request.status(), request.location());
    return new WebhookResponse(true, "Shipment update recorded");
  }

  @GetMapping("/api/tracking/{trackingNumber}")
  public ShipmentDetailResponse publicTracking(@PathVariable String trackingNumber) {
    return orderShipmentService.getShipmentByTrackingNumber(trackingNumber);
  }

  private Long userId(SecurityUser user) {
    return Long.valueOf(user.getUserId());
  }
}
