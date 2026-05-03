package com.anomalydetection.domain.safety;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.anomalydetection.shared.safety.IfImpact;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SafetyTraceRecordStatusTransitionTest {

  private SafetyTraceRecord newRecord() {
    return new SafetyTraceRecord(UUID.randomUUID(), "AEB Safety Trace", "ASIL-D", "SAFETY-FEAT-001");
  }

  /** Helper: move ifImpact out of UNKNOWN so submit() proceeds without deadline fields. */
  private void confirmIfImpact(SafetyTraceRecord r) {
    r.setIfImpact(IfImpact.UNCHANGED);
  }

  // ── Constructor validation ────────────────────────────────────────────────

  @Test
  void constructorThrowsWhenFeatureIdIsNull() {
    assertThatThrownBy(() -> new SafetyTraceRecord(UUID.randomUUID(), "name", "QM", null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("feature_id");
  }

  @Test
  void constructorThrowsWhenFeatureIdIsBlank() {
    assertThatThrownBy(() -> new SafetyTraceRecord(UUID.randomUUID(), "name", "QM", "  "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("feature_id");
  }

  @Test
  void newRecordStartsAsDraftWithUnknownIfImpact() {
    var r = newRecord();
    assertThat(r.getApprovalStatus()).isEqualTo(SafetyApprovalStatus.DRAFT);
    assertThat(r.getIfImpact()).isEqualTo(IfImpact.UNKNOWN);
  }

  // ── submit() guards ───────────────────────────────────────────────────────

  @Test
  void submitTransitionsDraftToSubmitted() {
    var record = newRecord();
    confirmIfImpact(record);
    record.submit(UUID.randomUUID());
    assertThat(record.getApprovalStatus()).isEqualTo(SafetyApprovalStatus.SUBMITTED);
    assertThat(record.getSubmittedAt()).isNotNull();
  }

  @Test
  void submitThrowsWhenNotDraft() {
    var record = newRecord();
    confirmIfImpact(record);
    record.submit(UUID.randomUUID());
    assertThatThrownBy(() -> record.submit(UUID.randomUUID()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("DRAFT");
  }

  @Test
  void submitThrowsWhenIfImpactUnknownAndDeadlineMissing() {
    var record = newRecord();
    // ifImpact defaults to UNKNOWN but no unknownUntil / unknownOwnerId set
    assertThatThrownBy(() -> record.submit(UUID.randomUUID()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("UNKNOWN");
  }

  @Test
  void submitSucceedsWhenIfImpactUnknownWithDeadlineAndOwner() {
    var record = newRecord();
    record.setUnknownUntil(LocalDate.now().plusDays(30));
    record.setUnknownOwnerId(UUID.randomUUID());
    record.submit(UUID.randomUUID());
    assertThat(record.getApprovalStatus()).isEqualTo(SafetyApprovalStatus.SUBMITTED);
  }

  // ── Review / Approve / Reject ─────────────────────────────────────────────

  @Test
  void startReviewTransitionsSubmittedToUnderReview() {
    var record = newRecord();
    confirmIfImpact(record);
    record.submit(UUID.randomUUID());
    record.startReview();
    assertThat(record.getApprovalStatus()).isEqualTo(SafetyApprovalStatus.UNDER_REVIEW);
  }

  @Test
  void startReviewThrowsWhenNotSubmitted() {
    var record = newRecord();
    assertThatThrownBy(record::startReview)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("SUBMITTED");
  }

  @Test
  void approveFromSubmittedTransitionsToApproved() {
    var record = newRecord();
    confirmIfImpact(record);
    UUID reviewer = UUID.randomUUID();
    record.submit(UUID.randomUUID());
    record.approve(reviewer, "LGTM");
    assertThat(record.getApprovalStatus()).isEqualTo(SafetyApprovalStatus.APPROVED);
    assertThat(record.getApprovedBy()).isEqualTo(reviewer);
    assertThat(record.getApprovalComments()).isEqualTo("LGTM");
    assertThat(record.getApprovedAt()).isNotNull();
  }

  @Test
  void approveFromUnderReviewTransitionsToApproved() {
    var record = newRecord();
    confirmIfImpact(record);
    record.submit(UUID.randomUUID());
    record.startReview();
    record.approve(UUID.randomUUID(), "Reviewed OK");
    assertThat(record.getApprovalStatus()).isEqualTo(SafetyApprovalStatus.APPROVED);
  }

  @Test
  void approveThrowsWhenDraft() {
    var record = newRecord();
    assertThatThrownBy(() -> record.approve(UUID.randomUUID(), "notes"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("SUBMITTED");
  }

  @Test
  void approveThrowsWhenAlreadyApproved() {
    var record = newRecord();
    confirmIfImpact(record);
    record.submit(UUID.randomUUID());
    record.approve(UUID.randomUUID(), "first approval");
    assertThatThrownBy(() -> record.approve(UUID.randomUUID(), "double approval"))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void rejectFromSubmittedTransitionsToRejected() {
    var record = newRecord();
    confirmIfImpact(record);
    record.submit(UUID.randomUUID());
    record.reject(UUID.randomUUID(), "Does not meet ASIL-D requirements");
    assertThat(record.getApprovalStatus()).isEqualTo(SafetyApprovalStatus.REJECTED);
    assertThat(record.getApprovalComments()).isEqualTo("Does not meet ASIL-D requirements");
  }

  @Test
  void rejectThrowsWhenDraft() {
    var record = newRecord();
    assertThatThrownBy(() -> record.reject(UUID.randomUUID(), "reason"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("SUBMITTED");
  }

  @Test
  void updateAsilLevelReopensApprovedRecordToDraft() {
    var record = newRecord();
    confirmIfImpact(record);
    record.submit(UUID.randomUUID());
    record.approve(UUID.randomUUID(), "ok");
    record.updateAsilLevel("ASIL-B");
    assertThat(record.getApprovalStatus()).isEqualTo(SafetyApprovalStatus.DRAFT);
    assertThat(record.getAsilLevel()).isEqualTo("ASIL-B");
  }
}
