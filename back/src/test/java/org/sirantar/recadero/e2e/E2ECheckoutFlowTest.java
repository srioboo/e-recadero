package org.sirantar.recadero.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sirantar.recadero.cart.domain.Cart;
import org.sirantar.recadero.cart.domain.CartItem;
import org.sirantar.recadero.cart.domain.CartPromotion;
import org.sirantar.recadero.cart.domain.CartStatus;
import org.sirantar.recadero.cart.events.CartEventPublisher;
import org.sirantar.recadero.cart.events.CheckoutCompletedEvent;
import org.sirantar.recadero.cart.repository.CartItemRepository;
import org.sirantar.recadero.cart.repository.CartPromotionRepository;
import org.sirantar.recadero.cart.repository.CartRepository;
import org.sirantar.recadero.cart.repository.ReservationRepository;
import org.sirantar.recadero.cart.service.CartPromotionService;
import org.sirantar.recadero.cart.service.CartService;
import org.sirantar.recadero.cart.service.CartValidationService;
import org.sirantar.recadero.cart.service.CheckoutService;
import org.sirantar.recadero.cart.service.dto.AddItemRequest;
import org.sirantar.recadero.cart.service.dto.ConfirmCheckoutRequest;
import org.sirantar.recadero.cart.service.dto.PrepareCheckoutRequest;
import org.sirantar.recadero.catalog.domain.Inventory;
import org.sirantar.recadero.catalog.domain.Product;
import org.sirantar.recadero.catalog.domain.ProductVariant;
import org.sirantar.recadero.catalog.repository.InventoryRepository;
import org.sirantar.recadero.catalog.repository.ProductRepository;
import org.sirantar.recadero.catalog.repository.ProductVariantRepository;
import org.sirantar.recadero.catalog.service.InventoryService;
import org.sirantar.recadero.orders.domain.Order;
import org.sirantar.recadero.orders.domain.OrderItem;
import org.sirantar.recadero.orders.domain.OrderPayment;
import org.sirantar.recadero.orders.domain.OrderShipment;
import org.sirantar.recadero.orders.domain.OrderStatus;
import org.sirantar.recadero.orders.domain.ShipmentStatus;
import org.sirantar.recadero.orders.events.OrderConfirmedEvent;
import org.sirantar.recadero.orders.events.OrderEventPublisher;
import org.sirantar.recadero.orders.repository.OrderItemRepository;
import org.sirantar.recadero.orders.repository.OrderPaymentRepository;
import org.sirantar.recadero.orders.repository.OrderRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentEventRepository;
import org.sirantar.recadero.orders.repository.OrderShipmentRepository;
import org.sirantar.recadero.orders.repository.OrderTransactionRepository;
import org.sirantar.recadero.orders.service.OrderService;
import org.sirantar.recadero.orders.service.OrderShipmentService;
import org.sirantar.recadero.orders.service.OrderValidationService;
import org.sirantar.recadero.promotions.domain.CouponCode;
import org.sirantar.recadero.promotions.domain.Promotion;
import org.sirantar.recadero.promotions.domain.PromotionStatus;
import org.sirantar.recadero.promotions.domain.PromotionType;
import org.sirantar.recadero.promotions.events.PromotionEventPublisher;
import org.sirantar.recadero.promotions.repository.CouponCodeRepository;
import org.sirantar.recadero.promotions.repository.PromotionRepository;
import org.sirantar.recadero.promotions.repository.PromotionRuleRepository;
import org.sirantar.recadero.promotions.repository.PromotionUsageRepository;
import org.sirantar.recadero.promotions.service.CouponCodeService;
import org.sirantar.recadero.promotions.service.OrderConfirmedEventListener;
import org.sirantar.recadero.promotions.service.PromotionRulesEngine;
import org.sirantar.recadero.promotions.service.PromotionsCouponValidator;
import org.sirantar.recadero.users.domain.User;
import org.sirantar.recadero.users.domain.UserProfile;
import org.sirantar.recadero.users.domain.UserStatus;
import org.sirantar.recadero.users.events.UserEventPublisher;
import org.sirantar.recadero.users.repository.AddressRepository;
import org.sirantar.recadero.users.repository.EmailVerificationTokenRepository;
import org.sirantar.recadero.users.repository.PasswordResetTokenRepository;
import org.sirantar.recadero.users.repository.UserProfileRepository;
import org.sirantar.recadero.users.repository.UserRepository;
import org.sirantar.recadero.users.repository.UserRoleRepository;
import org.sirantar.recadero.users.service.UserAuthenticationService;
import org.sirantar.recadero.users.service.UserProfileService;
import org.sirantar.recadero.users.service.UserRoleService;
import org.sirantar.recadero.users.service.UserValidationService;
import org.sirantar.recadero.users.service.dto.RegisterRequest;
import org.sirantar.recadero.users.service.dto.RegisterResponse;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Full checkout journey across all six backend modules, wiring real service
 * instances together with mocked repositories (no Docker/Testcontainers
 * available in this environment — see per-module *IntegrationTest classes
 * for the same style). Simulates the event handoffs
 * (Cart→Orders via CheckoutCompletedEvent, Orders→Promotions via
 * OrderConfirmedEvent) explicitly, since there's no live Spring event bus here.
 *
 * Flow: register user → add item to cart → apply coupon → prepare checkout
 * → confirm checkout → order created & confirmed → coupon usage recorded →
 * shipment created → delivered → tracking available.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("E2E: full checkout flow across all modules")
class E2ECheckoutFlowTest {

  // Matches InventoryService's package-private DEFAULT_WAREHOUSE_ID (not visible from this package).
  private static final java.util.UUID DEFAULT_WAREHOUSE_ID =
      java.util.UUID.nameUUIDFromBytes("DEFAULT_WAREHOUSE".getBytes(java.nio.charset.StandardCharsets.UTF_8));

  // Users
  @Mock private UserRepository userRepository;
  @Mock private UserProfileRepository userProfileRepository;
  @Mock private UserRoleRepository userRoleRepository;
  @Mock private EmailVerificationTokenRepository emailVerificationTokenRepository;
  @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private org.sirantar.recadero.shared.security.JwtTokenProvider jwtTokenProvider;
  @Mock private StringRedisTemplate redisTemplate;
  @Mock private UserEventPublisher userEventPublisher;
  @Mock private AddressRepository addressRepository;

  // Catalog / Inventory
  @Mock private InventoryRepository inventoryRepository;
  @Mock private ProductVariantRepository productVariantRepository;
  @Mock private ProductRepository productRepository;
  @Mock private ApplicationEventPublisher catalogEventPublisher;

  // Cart
  @Mock private CartRepository cartRepository;
  @Mock private CartItemRepository cartItemRepository;
  @Mock private CartPromotionRepository cartPromotionRepository;
  @Mock private ReservationRepository reservationRepository;
  @Mock private CartEventPublisher cartEventPublisher;

  // Promotions
  @Mock private PromotionRepository promotionRepository;
  @Mock private PromotionRuleRepository promotionRuleRepository;
  @Mock private CouponCodeRepository couponCodeRepository;
  @Mock private PromotionUsageRepository promotionUsageRepository;
  @Mock private org.sirantar.recadero.orders.repository.OrderRepository ordersRepoForRulesEngine;
  @Mock private PromotionEventPublisher promotionEventPublisher;

  // Orders
  @Mock private OrderRepository orderRepository;
  @Mock private OrderItemRepository orderItemRepository;
  @Mock private OrderPaymentRepository orderPaymentRepository;
  @Mock private OrderTransactionRepository orderTransactionRepository;
  @Mock private OrderShipmentRepository orderShipmentRepository;
  @Mock private OrderShipmentEventRepository orderShipmentEventRepository;
  @Mock private OrderEventPublisher orderEventPublisher;

  private UserAuthenticationService userAuthenticationService;
  private InventoryService inventoryService;
  private CartService cartService;
  private CartPromotionService cartPromotionService;
  private CheckoutService checkoutService;
  private CouponCodeService couponCodeService;
  private OrderService orderService;
  private OrderShipmentService orderShipmentService;
  private OrderConfirmedEventListener orderConfirmedEventListener;

  private ProductVariant variant;
  private Order createdOrder;

  @BeforeEach
  void setUp() {
    ObjectMapper objectMapper = new ObjectMapper();

    UserProfileService userProfileService =
        new UserProfileService(userRepository, userProfileRepository, passwordEncoder, userEventPublisher);
    UserRoleService userRoleService = new UserRoleService(userRoleRepository);
    userAuthenticationService = new UserAuthenticationService(
        userRepository, userProfileService, userRoleService, new UserValidationService(),
        emailVerificationTokenRepository, passwordResetTokenRepository, passwordEncoder,
        jwtTokenProvider, redisTemplate, userEventPublisher);

    inventoryService = new InventoryService(inventoryRepository, productVariantRepository, productRepository, catalogEventPublisher);

    CartValidationService cartValidationService = new CartValidationService(productVariantRepository);
    cartService = new CartService(
        cartRepository, cartItemRepository, cartPromotionRepository, reservationRepository,
        productVariantRepository, inventoryService, cartValidationService, cartEventPublisher, objectMapper);

    PromotionRulesEngine rulesEngine =
        new PromotionRulesEngine(promotionRuleRepository, productVariantRepository, ordersRepoForRulesEngine, objectMapper);
    couponCodeService = new CouponCodeService(
        couponCodeRepository, promotionRepository, promotionUsageRepository, rulesEngine, promotionEventPublisher);
    PromotionsCouponValidator couponValidator = new PromotionsCouponValidator(couponCodeService);
    cartPromotionService = new CartPromotionService(cartPromotionRepository, cartService, couponValidator);

    checkoutService = new CheckoutService(
        cartRepository, cartItemRepository, cartPromotionRepository, reservationRepository,
        productVariantRepository, cartService, cartEventPublisher);

    orderService = new OrderService(
        orderRepository, orderItemRepository, orderPaymentRepository, orderTransactionRepository,
        orderShipmentRepository, productVariantRepository, inventoryService, addressRepository,
        new OrderValidationService(), orderEventPublisher);
    orderShipmentService = new OrderShipmentService(orderShipmentRepository, orderShipmentEventRepository, orderRepository, orderEventPublisher);
    orderConfirmedEventListener = new OrderConfirmedEventListener(couponCodeService);

    Product product = new Product();
    product.setId(200L);
    product.setName("Wireless Mouse");
    variant = new ProductVariant();
    variant.setId(50L);
    variant.setProduct(product);
    variant.setSku("MOUSE-1");
    variant.setPrice(BigDecimal.valueOf(40.00));
  }

  @Test
  @DisplayName("register -> add to cart -> apply coupon -> checkout -> order confirmed -> ship -> deliver")
  void fullCheckoutJourney() {
    // 1. Register user
    when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
    when(userRepository.existsByUsername("janedoe")).thenReturn(false);
    when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenAnswer(inv -> {
      User u = inv.getArgument(0);
      if (u.getId() == null) u.setId(10L);
      return u;
    });
    when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(inv -> inv.getArgument(0));

    RegisterResponse registered = userAuthenticationService.registerUser(
        new RegisterRequest("Jane@Example.com", "janedoe", "SecurePass123!", "Jane", "Doe"));
    assertThat(registered.userId()).isEqualTo(10L);
    Long userId = registered.userId();

    // 2. Add item to cart (reserves inventory)
    Inventory inventory = new Inventory();
    inventory.setId(1L);
    inventory.setProductVariant(variant);
    inventory.setProduct(variant.getProduct());
    inventory.setWarehouseId(DEFAULT_WAREHOUSE_ID);
    inventory.setQuantityAvailable(20);
    inventory.setQuantityReserved(0);
    when(inventoryRepository.findByProductVariantIdAndWarehouseId(50L, DEFAULT_WAREHOUSE_ID))
        .thenReturn(Optional.of(inventory));
    when(inventoryRepository.save(any(Inventory.class))).thenAnswer(inv -> inv.getArgument(0));

    Cart cart = new Cart();
    cart.setId(1L);
    cart.setUserId(userId);
    cart.setStatus(CartStatus.ACTIVE);
    cart.setCreatedAt(LocalDateTime.now());
    cart.setUpdatedAt(LocalDateTime.now());
    when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE)).thenReturn(Optional.of(cart));
    when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));
    when(productVariantRepository.findById(50L)).thenReturn(Optional.of(variant));
    when(cartItemRepository.findByCartIdAndProductVariantId(1L, 50L)).thenReturn(Optional.empty());

    java.util.List<CartItem> storedItems = new java.util.ArrayList<>();
    when(cartItemRepository.save(any(CartItem.class))).thenAnswer(inv -> {
      CartItem item = inv.getArgument(0);
      if (item.getId() == null) {
        item.setId(500L);
        storedItems.add(item);
      }
      return item;
    });
    when(cartItemRepository.findByCartId(1L)).thenAnswer(inv -> new java.util.ArrayList<>(storedItems));

    cartService.addItem(userId, new AddItemRequest(50L, 2));
    assertThat(inventory.getQuantityAvailable()).isEqualTo(18);
    assertThat(inventory.getQuantityReserved()).isEqualTo(2);

    // 3. Apply a real coupon through Promotions
    Promotion promotion = new Promotion();
    promotion.setId(5L);
    promotion.setName("Welcome10");
    promotion.setType(PromotionType.PERCENTAGE_DISCOUNT);
    promotion.setDiscountValue(BigDecimal.TEN);
    promotion.setStatus(PromotionStatus.ACTIVE);
    promotion.setStartDate(LocalDateTime.now().minusDays(1));
    promotion.setEndDate(LocalDateTime.now().plusDays(30));
    promotion.setCurrentUsageCount(0);

    CouponCode coupon = new CouponCode();
    coupon.setId(1L);
    coupon.setCode("WELCOME10");
    coupon.setPromotionId(5L);
    coupon.setIsActive(true);
    coupon.setCurrentUsage(0);
    coupon.setCreatedAt(LocalDateTime.now());

    when(couponCodeRepository.findByCode("WELCOME10")).thenReturn(Optional.of(coupon));
    when(promotionRepository.findById(5L)).thenReturn(Optional.of(promotion));
    when(promotionRuleRepository.findByPromotionId(5L)).thenReturn(List.of());
    java.util.List<CartPromotion> storedPromotions = new java.util.ArrayList<>();
    when(cartPromotionRepository.save(any(CartPromotion.class))).thenAnswer(inv -> {
      CartPromotion p = inv.getArgument(0);
      storedPromotions.add(p);
      return p;
    });
    when(cartPromotionRepository.findByCartId(1L)).thenAnswer(inv -> new java.util.ArrayList<>(storedPromotions));

    var couponResponse = cartPromotionService.applyCoupon(userId, "WELCOME10");
    assertThat(couponResponse.discountAmount()).isEqualByComparingTo("8.00"); // 10% of 80.00 subtotal

    // 4. Prepare checkout
    var prepared = checkoutService.prepareCheckout(userId, new PrepareCheckoutRequest(null, 1L, 2L));
    assertThat(prepared.status()).isEqualTo("LOCKED_FOR_CHECKOUT");

    // 5. Confirm checkout -> capture the CheckoutCompletedEvent Cart would publish
    ArgumentCaptor<List<CheckoutCompletedEvent.LineItem>> itemsCaptor = ArgumentCaptor.forClass(List.class);
    checkoutService.confirmCheckout(userId, new ConfirmCheckoutRequest(prepared.checkoutToken(), "CREDIT_CARD", "txn-999"));

    var eventCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
    org.mockito.Mockito.verify(cartEventPublisher).publishCheckoutCompleted(
        org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(userId), itemsCaptor.capture(),
        org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(2L), any(),
        org.mockito.ArgumentMatchers.eq("CREDIT_CARD"), org.mockito.ArgumentMatchers.eq("txn-999"), any(),
        org.mockito.ArgumentMatchers.eq(prepared.checkoutToken()), eventCaptor.capture(), any());

    assertThat(eventCaptor.getValue()).isEqualTo("WELCOME10");

    // 6. Feed the captured event into Orders (simulating CartCheckoutEventListener)
    CheckoutCompletedEvent event = new CheckoutCompletedEvent(
        1L, userId, itemsCaptor.getValue(), 1L, 2L, null, "CREDIT_CARD", "txn-999",
        BigDecimal.valueOf(72.00), prepared.checkoutToken(), "WELCOME10", BigDecimal.valueOf(8.00));

    when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
      Order o = inv.getArgument(0);
      if (o.getId() == null) {
        o.setId(900L);
        createdOrder = o;
      }
      return o;
    });
    when(orderPaymentRepository.save(any(OrderPayment.class))).thenAnswer(inv -> {
      OrderPayment p = inv.getArgument(0);
      if (p.getId() == null) p.setId(1L);
      return p;
    });
    // inventoryService is a real instance backed by the already-stubbed
    // inventoryRepository, so createOrderFromCart's availability check
    // resolves naturally from the same `inventory` object mutated in step 2.

    Order order = orderService.createOrderFromCart(event);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    assertThat(order.getGrandTotal()).isEqualByComparingTo("72.00");

    var confirmedCaptor = org.mockito.ArgumentCaptor.forClass(OrderConfirmedEvent.class);
    // 7. Feed OrderConfirmedEvent into Promotions to record real usage
    when(couponCodeRepository.save(any(CouponCode.class))).thenAnswer(inv -> inv.getArgument(0));
    when(promotionRepository.save(any(Promotion.class))).thenAnswer(inv -> inv.getArgument(0));
    when(promotionUsageRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    orderConfirmedEventListener.onOrderConfirmed(
        new OrderConfirmedEvent(order.getId(), order.getOrderNumber(), userId, order.getGrandTotal(), "WELCOME10", BigDecimal.valueOf(8.00)));

    assertThat(coupon.getCurrentUsage()).isEqualTo(1);
    assertThat(promotion.getCurrentUsageCount()).isEqualTo(1);

    // 8. Ship the order
    when(orderRepository.findById(900L)).thenReturn(Optional.of(order));
    when(orderShipmentRepository.save(any(OrderShipment.class))).thenAnswer(inv -> {
      OrderShipment s = inv.getArgument(0);
      if (s.getId() == null) s.setId(7L);
      return s;
    });
    orderShipmentService.createShipment(900L, "FEDEX", "TRACK-777", null);
    assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

    // 9. Carrier webhook marks it delivered
    OrderShipment shipment = new OrderShipment();
    shipment.setId(7L);
    shipment.setOrderId(900L);
    shipment.setTrackingNumber("TRACK-777");
    shipment.setCarrier("FEDEX");
    shipment.setStatus(ShipmentStatus.OUT_FOR_DELIVERY);
    when(orderShipmentRepository.findByTrackingNumber("TRACK-777")).thenReturn(Optional.of(shipment));
    when(orderShipmentRepository.findByOrderId(900L)).thenReturn(Optional.of(shipment));
    when(orderShipmentEventRepository.findByShipmentIdOrderByOccurredAtAsc(7L)).thenReturn(List.of());

    orderShipmentService.updateShipmentStatus("TRACK-777", "DELIVERED", "Front door");

    assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    assertThat(order.getDeliveredDate()).isNotNull();

    // 10. Tracking is available
    var tracking = orderShipmentService.getShipmentByOrder(900L);
    assertThat(tracking.status()).isEqualTo("DELIVERED");
    assertThat(tracking.trackingNumber()).isEqualTo("TRACK-777");
  }
}
