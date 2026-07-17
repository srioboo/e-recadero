package org.sirantar.recadero.cart.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.sirantar.recadero.cart.domain.Cart;
import org.sirantar.recadero.cart.domain.CartItem;
import org.sirantar.recadero.cart.domain.CartStatus;
import org.sirantar.recadero.cart.events.CartEventPublisher;
import org.sirantar.recadero.cart.repository.CartItemRepository;
import org.sirantar.recadero.cart.repository.CartRepository;
import org.sirantar.recadero.cart.repository.ReservationRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Periodically abandons carts that have gone stale, releasing their held
 * inventory reservations back to availability.
 */
@Service
@RequiredArgsConstructor
public class CartExpirationService {

  private static final Logger log = LoggerFactory.getLogger(CartExpirationService.class);
  private static final long ONE_HOUR_MS = 3_600_000L;

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ReservationRepository reservationRepository;
  private final InventoryService inventoryService;
  private final CartEventPublisher eventPublisher;

  @Scheduled(fixedRate = ONE_HOUR_MS)
  @Transactional
  public void cleanupExpiredCarts() {
    LocalDateTime now = LocalDateTime.now();
    List<Cart> expired = cartRepository.findByStatusAndUpdatedAtBefore(CartStatus.ACTIVE, now.minusHours(24));

    for (Cart cart : expired) {
      List<CartItem> items = cartItemRepository.findByCartId(cart.getId());
      for (CartItem item : items) {
        inventoryService.releaseReservation(item.getProductVariantId(), item.getQuantity());
        reservationRepository.deleteByCartItemId(item.getId());
      }

      var grandTotal = items.stream()
          .map(i -> i.getLineTotal())
          .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

      cart.setStatus(CartStatus.ABANDONED);
      cart.setUpdatedAt(now);
      cartRepository.save(cart);

      eventPublisher.publishAbandoned(cart.getId(), cart.getUserId(), items.size(), grandTotal, now);
    }

    if (!expired.isEmpty()) {
      log.info("Marked {} cart(s) as abandoned and released their reservations", expired.size());
    }
  }
}
