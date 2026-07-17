package org.sirantar.recadero.orders.service;

import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.cart.events.CheckoutCompletedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Subscribes to Cart's {@link CheckoutCompletedEvent} to create the actual
 * order — this is the cross-module link described in cart-contract.md's
 * "Orders Module: Creates order from confirmed cart", implemented as an
 * event listener (rather than Cart calling Orders synchronously) per
 * Spring Modulith's module-boundary conventions. Runs after the cart's
 * checkout transaction commits, so order creation never races a rollback.
 */
@Component
@RequiredArgsConstructor
public class CartCheckoutEventListener {

  private final OrderService orderService;

  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onCheckoutCompleted(CheckoutCompletedEvent event) {
    orderService.createOrderFromCart(event);
  }
}
