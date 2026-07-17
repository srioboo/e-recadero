package org.sirantar.recadero.users.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A billing/shipping/other address belonging to a user.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "address", schema = "users")
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "address_type", nullable = false, length = 50)
  private AddressType type;

  @Column(name = "street_address", nullable = false, length = 255)
  private String streetAddress;

  @Column(name = "apartment_suite", length = 100)
  private String streetAddress2;

  @Column(nullable = false, length = 100)
  private String city;

  @Column(name = "state_province", length = 100)
  private String stateProvince;

  @Column(name = "postal_code", nullable = false, length = 20)
  private String postalCode;

  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  @Column(name = "is_default", nullable = false)
  private Boolean isPrimary = Boolean.FALSE;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
