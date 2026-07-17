package org.sirantar.recadero.cart.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request payload for POST /api/v1/cart/apply-coupon.
 */
public record ApplyCouponRequest(@JsonProperty("coupon_code") String couponCode) {}
