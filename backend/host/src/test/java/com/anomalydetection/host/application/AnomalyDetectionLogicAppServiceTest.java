package com.anomalydetection.host.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.anomalydetection.application.anomalydetection.CanAnomalyDetectionLogicAppService;
import com.anomalydetection.contracts.anomalydetection.CreateUpdateCanAnomalyDetectionLogicDto;
import com.anomalydetection.domain.anomalydetection.AnomalyType;
import com.anomalydetection.domain.anomalydetection.AsilLevel;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogic;
import com.anomalydetection.domain.anomalydetection.CanAnomalyDetectionLogicRepository;
import com.anomalydetection.domain.anomalydetection.DetectionLogicStatus;
import com.anomalydetection.domain.anomalydetection.ImplementationType;
import com.anomalydetection.domain.anomalydetection.LogicComplexity;
import com.anomalydetection.domain.anomalydetection.SharingLevel;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AnomalyDetectionLogicAppServiceTest {

  private final CanAnomalyDetectionLogicRepository repository =
      mock(CanAnomalyDetectionLogicRepository.class);
  private final ICurrentTenant currentTenant = mock(ICurrentTenant.class);
  private CanAnomalyDetectionLogicAppService service;

  @BeforeEach
  void setUp() {
    service = new CanAnomalyDetectionLogicAppService(repository, currentTenant);
    when(currentTenant.getTenantId()).thenReturn(Optional.empty());
  }

  private CreateUpdateCanAnomalyDetectionLogicDto buildInput(String name) {
    return new CreateUpdateCanAnomalyDetectionLogicDto(
        name, "1.0.0", "OEM-A", AnomalyType.TIMEOUT,
        "Test description", "BrakeSystem", LogicComplexity.SIMPLE,
        "ISO 26262", ImplementationType.CONFIGURATION,
        "", "Python", "", AsilLevel.QM,
        "", "", "", SharingLevel.PRIVATE, null);
  }

  @Test
  void createReturnsDtoWithDraftStatus() {
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var dto = service.create(buildInput("BrakeAnomalyDetector"));

    assertThat(dto.name()).isEqualTo("BrakeAnomalyDetector");
    assertThat(dto.status()).isEqualTo(DetectionLogicStatus.DRAFT);
    assertThat(dto.version()).isEqualTo("1.0.0");
  }

  @Test
  void getListReturnsAll() {
    var entity = new CanAnomalyDetectionLogic(UUID.randomUUID(), "SpeedAnomaly", "1.0.0");
    when(repository.findAll()).thenReturn(List.of(entity));

    var result = service.getList();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("SpeedAnomaly");
  }

  @Test
  void submitForApprovalTransitionsStatus() {
    UUID id = UUID.randomUUID();
    var entity = new CanAnomalyDetectionLogic(id, "Logic", "1.0.0");
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result = service.submitForApproval(id);

    assertThat(result).isPresent();
    assertThat(result.get().status()).isEqualTo(DetectionLogicStatus.PENDING_APPROVAL);
  }

  @Test
  void approveTransitionsToApproved() {
    UUID id = UUID.randomUUID();
    var entity = new CanAnomalyDetectionLogic(id, "Logic", "1.0.0");
    entity.submitForApproval();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result = service.approve(id, null, "All clear");

    assertThat(result).isPresent();
    assertThat(result.get().status()).isEqualTo(DetectionLogicStatus.APPROVED);
    assertThat(result.get().approvalNotes()).isEqualTo("All clear");
  }

  @Test
  void rejectTransitionsToRejected() {
    UUID id = UUID.randomUUID();
    var entity = new CanAnomalyDetectionLogic(id, "Logic", "1.0.0");
    entity.submitForApproval();
    when(repository.findById(id)).thenReturn(Optional.of(entity));
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var result = service.reject(id, "Missing safety documentation");

    assertThat(result).isPresent();
    assertThat(result.get().status()).isEqualTo(DetectionLogicStatus.REJECTED);
  }

  @Test
  void getByStatusFiltersCorrectly() {
    var draft = new CanAnomalyDetectionLogic(UUID.randomUUID(), "Draft", "1.0.0");
    when(repository.findAllByStatus(DetectionLogicStatus.DRAFT)).thenReturn(List.of(draft));

    var result = service.getByStatus(DetectionLogicStatus.DRAFT);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).status()).isEqualTo(DetectionLogicStatus.DRAFT);
  }
}
