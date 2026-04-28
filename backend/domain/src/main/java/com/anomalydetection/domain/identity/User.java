package com.anomalydetection.domain.identity;

import com.anomalydetection.domain.base.FullAuditedEntity;
import com.anomalydetection.domain.base.MultiTenant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * Application user — matching ABP's {@code IdentityUser}.
 *
 * <p>Password hashing and authentication are handled by Spring Security.
 * This entity only stores the identity attributes.
 *
 * <p>Multi-tenant tenant_id is injected by {@code MultiTenantEntityListener}
 * registered globally via JPA {@code orm.xml} or {@code HibernateProperties}.
 */
@Entity
@Table(name = "users")
@Filter(name = "tenantFilter", condition = "tenant_id = UNHEX(REPLACE(:tenantId, '-', ''))")
@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class User extends FullAuditedEntity<UUID> implements MultiTenant {

  @Id
  @Column(name = "id", columnDefinition = "BINARY(16)")
  private UUID id;

  @Column(name = "tenant_id", columnDefinition = "BINARY(16)")
  private UUID tenantId;

  @Column(name = "user_name", nullable = false, length = 256)
  private String userName;

  @Column(name = "normalized_user_name", nullable = false, length = 256)
  private String normalizedUserName;

  @Column(name = "email", length = 256)
  private String email;

  @Column(name = "normalized_email", length = 256)
  private String normalizedEmail;

  @Column(name = "email_confirmed", nullable = false)
  private boolean emailConfirmed;

  @Column(name = "password_hash", length = 512)
  private String passwordHash;

  @Column(name = "phone_number", length = 64)
  private String phoneNumber;

  @Column(name = "phone_number_confirmed", nullable = false)
  private boolean phoneNumberConfirmed;

  @Column(name = "two_factor_enabled", nullable = false)
  private boolean twoFactorEnabled;

  @Column(name = "lockout_enabled", nullable = false)
  private boolean lockoutEnabled;

  @Column(name = "lockout_end")
  private java.time.Instant lockoutEnd;

  @Column(name = "access_failed_count", nullable = false)
  private int accessFailedCount;

  @Column(name = "is_active", nullable = false)
  private boolean isActive;

  protected User() {}

  public User(UUID id, String userName, String normalizedUserName) {
    this.id = id;
    this.userName = userName;
    this.normalizedUserName = normalizedUserName;
    this.isActive = true;
  }

  @Override
  public UUID getId() {
    return id;
  }

  @Override
  public UUID getTenantId() {
    return tenantId;
  }

  @Override
  public void setTenantId(UUID tenantId) {
    this.tenantId = tenantId;
  }

  public String getUserName() {
    return userName;
  }

  public String getNormalizedUserName() {
    return normalizedUserName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getNormalizedEmail() {
    return normalizedEmail;
  }

  public void setNormalizedEmail(String normalizedEmail) {
    this.normalizedEmail = normalizedEmail;
  }

  public boolean isEmailConfirmed() {
    return emailConfirmed;
  }

  public void setEmailConfirmed(boolean emailConfirmed) {
    this.emailConfirmed = emailConfirmed;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public boolean isPhoneNumberConfirmed() {
    return phoneNumberConfirmed;
  }

  public void setPhoneNumberConfirmed(boolean phoneNumberConfirmed) {
    this.phoneNumberConfirmed = phoneNumberConfirmed;
  }

  public boolean isTwoFactorEnabled() {
    return twoFactorEnabled;
  }

  public void setTwoFactorEnabled(boolean twoFactorEnabled) {
    this.twoFactorEnabled = twoFactorEnabled;
  }

  public boolean isLockoutEnabled() {
    return lockoutEnabled;
  }

  public void setLockoutEnabled(boolean lockoutEnabled) {
    this.lockoutEnabled = lockoutEnabled;
  }

  public java.time.Instant getLockoutEnd() {
    return lockoutEnd;
  }

  public void setLockoutEnd(java.time.Instant lockoutEnd) {
    this.lockoutEnd = lockoutEnd;
  }

  public int getAccessFailedCount() {
    return accessFailedCount;
  }

  public void setAccessFailedCount(int accessFailedCount) {
    this.accessFailedCount = accessFailedCount;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean active) {
    isActive = active;
  }
}