package org.sirantar.recadero.users.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Extended profile information and preferences for a user.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_profile", schema = "users")
public class UserProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false, unique = true)
  private Long userId;

  @Column(name = "first_name", length = 100)
  private String firstName;

  @Column(name = "last_name", length = 100)
  private String lastName;

  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  @Column(name = "profile_picture_url", length = 1000)
  private String avatarUrl;

  @Column(columnDefinition = "TEXT")
  private String bio;

  @Column(name = "preferred_language", length = 10)
  private String preferredLocale = "en";

  @Column(name = "preferred_currency", length = 10)
  private String preferredCurrency = "USD";

  @Column(name = "notification_preferences", columnDefinition = "jsonb")
  private String notificationPreferences;

  @Column(name = "newsletter_subscribed", nullable = false)
  private Boolean newsletterSubscribed = Boolean.FALSE;

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
