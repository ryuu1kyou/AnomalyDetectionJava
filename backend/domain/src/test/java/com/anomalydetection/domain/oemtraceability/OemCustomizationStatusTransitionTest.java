package com.anomalydetection.domain.oemtraceability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class OemCustomizationStatusTransitionTest {

  private OemCustomization newCustomization() {
    return new OemCustomization(
        UUID.randomUUID(), "entity-001", "DetectionTemplate", "OEM_BMW",
        OemCustomizationType.PARAMETER_ADJUSTMENT);
  }

  @Test
  void newCustomizationStartsAsDraft() {
    assertThat(newCustomization().getStatus()).isEqualTo(OemCustomizationStatus.DRAFT);
  }

  @Test
  void submitForApprovalTransitionsDraftToPending() {
    var custom = newCustomization();
    custom.submitForApproval();
    assertThat(custom.getStatus()).isEqualTo(OemCustomizationStatus.PENDING_APPROVAL);
  }

  @Test
  void submitForApprovalThrowsWhenNotDraft() {
    var custom = newCustomization();
    custom.submitForApproval();
    assertThatThrownBy(custom::submitForApproval)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("DRAFT");
  }

  @Test
  void approveTransitionsPendingToApproved() {
    var custom = newCustomization();
    UUID approver = UUID.randomUUID();
    custom.submitForApproval();
    custom.approve(approver, "Parameter change accepted");
    assertThat(custom.getStatus()).isEqualTo(OemCustomizationStatus.APPROVED);
    assertThat(custom.getApprovedBy()).isEqualTo(approver);
    assertThat(custom.getApprovalNotes()).isEqualTo("Parameter change accepted");
    assertThat(custom.getApprovedAt()).isNotNull();
  }

  @Test
  void approveThrowsWhenDraft() {
    var custom = newCustomization();
    assertThatThrownBy(() -> custom.approve(UUID.randomUUID(), "notes"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("PENDING_APPROVAL");
  }

  @Test
  void rejectTransitionsPendingToRejected() {
    var custom = newCustomization();
    custom.submitForApproval();
    custom.reject(UUID.randomUUID(), "Does not comply with spec");
    assertThat(custom.getStatus()).isEqualTo(OemCustomizationStatus.REJECTED);
    assertThat(custom.getApprovalNotes()).isEqualTo("Does not comply with spec");
  }

  @Test
  void rejectThrowsWhenDraft() {
    var custom = newCustomization();
    assertThatThrownBy(() -> custom.reject(UUID.randomUUID(), "reason"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("PENDING_APPROVAL");
  }

  @Test
  void markObsoleteTransitionsAnyNonObsoleteToObsolete() {
    var custom = newCustomization();
    custom.markObsolete();
    assertThat(custom.getStatus()).isEqualTo(OemCustomizationStatus.OBSOLETE);
  }

  @Test
  void markObsoleteThrowsWhenAlreadyObsolete() {
    var custom = newCustomization();
    custom.markObsolete();
    assertThatThrownBy(custom::markObsolete)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("OBSOLETE");
  }

  @Test
  void updateCustomParametersReopensApprovedCustomizationToDraft() {
    var custom = newCustomization();
    custom.submitForApproval();
    // updateCustomParameters from PENDING_APPROVAL reverts to DRAFT
    custom.updateCustomParameters("{\"threshold\": 0.9}");
    assertThat(custom.getStatus()).isEqualTo(OemCustomizationStatus.DRAFT);
  }
}
