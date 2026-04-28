package com.anomalydetection.domain.anomalydetection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AnomalyDetectionLogicStatusTransitionTest {

  private CanAnomalyDetectionLogic newLogic() {
    return new CanAnomalyDetectionLogic(UUID.randomUUID(), "BrakePressureAnomalyDetection", "1.0.0");
  }

  @Test
  void newLogicStartsAsDraft() {
    assertThat(newLogic().getStatus()).isEqualTo(DetectionLogicStatus.DRAFT);
  }

  @Test
  void submitForApprovalTransitionsDraftToPending() {
    var logic = newLogic();
    logic.submitForApproval();
    assertThat(logic.getStatus()).isEqualTo(DetectionLogicStatus.PENDING_APPROVAL);
  }

  @Test
  void submitForApprovalThrowsWhenNotDraft() {
    var logic = newLogic();
    logic.submitForApproval();

    assertThatThrownBy(logic::submitForApproval)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("DRAFT");
  }

  @Test
  void approveTransitionsPendingToApproved() {
    var logic = newLogic();
    logic.submitForApproval();
    UUID reviewer = UUID.randomUUID();

    logic.approve(reviewer, "Looks good");

    assertThat(logic.getStatus()).isEqualTo(DetectionLogicStatus.APPROVED);
    assertThat(logic.getApprovedBy()).isEqualTo(reviewer);
    assertThat(logic.getApprovalNotes()).isEqualTo("Looks good");
    assertThat(logic.getApprovedAt()).isNotNull();
  }

  @Test
  void approveThrowsWhenNotPending() {
    var logic = newLogic();

    assertThatThrownBy(() -> logic.approve(null, null))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("PENDING_APPROVAL");
  }

  @Test
  void rejectTransitionsPendingToRejected() {
    var logic = newLogic();
    logic.submitForApproval();
    logic.reject("Safety requirements not met");

    assertThat(logic.getStatus()).isEqualTo(DetectionLogicStatus.REJECTED);
    assertThat(logic.getApprovalNotes()).isEqualTo("Safety requirements not met");
  }

  @Test
  void rejectThrowsWhenNotPending() {
    var logic = newLogic();

    assertThatThrownBy(() -> logic.reject("reason"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("PENDING_APPROVAL");
  }

  @Test
  void deprecateTransitionsAnyStatusToDeprecated() {
    var logic = newLogic();
    logic.deprecate("Superseded by v2");

    assertThat(logic.getStatus()).isEqualTo(DetectionLogicStatus.DEPRECATED);
    assertThat(logic.getApprovalNotes()).contains("Deprecated: Superseded by v2");
  }

  @Test
  void recordExecutionIncrementsCountAndSetsTimestamp() {
    var logic = newLogic();
    assertThat(logic.getExecutionCount()).isZero();

    logic.recordExecution(42.5);

    assertThat(logic.getExecutionCount()).isEqualTo(1);
    assertThat(logic.getLastExecutionTimeMs()).isEqualTo(42.5);
    assertThat(logic.getLastExecutedAt()).isNotNull();
  }
}
