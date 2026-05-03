package com.anomalydetection.domain.oemtraceability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class OemApprovalStatusTransitionTest {

  private OemApproval newApproval() {
    return new OemApproval(
        UUID.randomUUID(), "entity-001", "CanSignal", "OEM_TOYOTA", OemApprovalType.NEW_ENTITY);
  }

  @Test
  void newApprovalStartsAsPending() {
    assertThat(newApproval().getStatus()).isEqualTo(OemApprovalStatus.PENDING);
  }

  @Test
  void approveTransitionsPendingToApproved() {
    var approval = newApproval();
    UUID approver = UUID.randomUUID();
    approval.approve(approver, "Approved by OEM");
    assertThat(approval.getStatus()).isEqualTo(OemApprovalStatus.APPROVED);
    assertThat(approval.getApprovedBy()).isEqualTo(approver);
    assertThat(approval.getApprovalNotes()).isEqualTo("Approved by OEM");
    assertThat(approval.getApprovedAt()).isNotNull();
  }

  @Test
  void approveThrowsWhenAlreadyApproved() {
    var approval = newApproval();
    approval.approve(UUID.randomUUID(), "first");
    assertThatThrownBy(() -> approval.approve(UUID.randomUUID(), "second"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("PENDING");
  }

  @Test
  void approveThrowsWhenRejected() {
    var approval = newApproval();
    approval.reject(UUID.randomUUID(), "not ok");
    assertThatThrownBy(() -> approval.approve(UUID.randomUUID(), "override"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("PENDING");
  }

  @Test
  void rejectTransitionsPendingToRejected() {
    var approval = newApproval();
    approval.reject(UUID.randomUUID(), "Safety requirements not met");
    assertThat(approval.getStatus()).isEqualTo(OemApprovalStatus.REJECTED);
    assertThat(approval.getApprovalNotes()).isEqualTo("Safety requirements not met");
  }

  @Test
  void rejectThrowsWhenAlreadyRejected() {
    var approval = newApproval();
    approval.reject(UUID.randomUUID(), "first rejection");
    assertThatThrownBy(() -> approval.reject(UUID.randomUUID(), "double rejection"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("PENDING");
  }

  @Test
  void cancelTransitionsPendingToCancelled() {
    var approval = newApproval();
    approval.cancel(UUID.randomUUID(), "No longer needed");
    assertThat(approval.getStatus()).isEqualTo(OemApprovalStatus.CANCELLED);
    assertThat(approval.getApprovalNotes()).isEqualTo("No longer needed");
  }

  @Test
  void cancelThrowsWhenApproved() {
    var approval = newApproval();
    approval.approve(UUID.randomUUID(), "approved");
    assertThatThrownBy(() -> approval.cancel(UUID.randomUUID(), "try cancel"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("APPROVED");
  }

  @Test
  void cancelThrowsWhenAlreadyCancelled() {
    var approval = newApproval();
    approval.cancel(UUID.randomUUID(), "first cancel");
    assertThatThrownBy(() -> approval.cancel(UUID.randomUUID(), "second cancel"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("CANCELLED");
  }
}
