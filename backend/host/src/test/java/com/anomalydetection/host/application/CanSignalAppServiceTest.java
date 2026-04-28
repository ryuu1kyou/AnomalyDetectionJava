package com.anomalydetection.host.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.anomalydetection.application.cansignals.CanSignalAppService;
import com.anomalydetection.contracts.cansignals.CreateUpdateCanSignalDto;
import com.anomalydetection.domain.cansignals.CanSignal;
import com.anomalydetection.domain.cansignals.CanSignalRepository;
import com.anomalydetection.domain.multitenancy.ICurrentTenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CanSignalAppServiceTest {

  private final CanSignalRepository repository = mock(CanSignalRepository.class);
  private final ICurrentTenant currentTenant = mock(ICurrentTenant.class);
  private CanSignalAppService service;

  @BeforeEach
  void setUp() {
    service = new CanSignalAppService(repository, currentTenant);
    when(currentTenant.getTenantId()).thenReturn(Optional.empty());
  }

  @Test
  void createPersistsAndReturnsDto() {
    when(repository.save(any(CanSignal.class))).thenAnswer(inv -> inv.getArgument(0));

    var input = new CreateUpdateCanSignalDto(0x100, "EngineSpeed", "Engine RPM", 0, 16,
        "LITTLE_ENDIAN", false, null);
    var dto = service.create(input);

    assertThat(dto.name()).isEqualTo("EngineSpeed");
    assertThat(dto.frameId()).isEqualTo(0x100);
    assertThat(dto.length()).isEqualTo(16);
    verify(repository).save(any(CanSignal.class));
  }

  @Test
  void getListReturnsAllSignals() {
    var signal = new CanSignal(UUID.randomUUID(), 0x100, "Speed", 0, 8);
    when(repository.findAll()).thenReturn(List.of(signal));

    var result = service.getList();

    assertThat(result).hasSize(1);
    assertThat(result.get(0).name()).isEqualTo("Speed");
  }

  @Test
  void updateReturnsUpdatedDto() {
    UUID id = UUID.randomUUID();
    var existing = new CanSignal(id, 0x100, "OldName", 0, 8);
    when(repository.findById(id)).thenReturn(Optional.of(existing));
    when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

    var input = new CreateUpdateCanSignalDto(0x200, "NewName", "Updated", 8, 16,
        "BIG_ENDIAN", true, null);
    var result = service.update(id, input);

    assertThat(result).isPresent();
    assertThat(result.get().name()).isEqualTo("NewName");
    assertThat(result.get().frameId()).isEqualTo(0x200);
  }

  @Test
  void updateReturnsEmptyWhenNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());

    var result = service.update(id,
        new CreateUpdateCanSignalDto(1, "X", "", 0, 8, "LITTLE_ENDIAN", false, null));

    assertThat(result).isEmpty();
  }

  @Test
  void deleteReturnsTrueWhenExists() {
    UUID id = UUID.randomUUID();
    when(repository.existsById(id)).thenReturn(true);

    assertThat(service.delete(id)).isTrue();
    verify(repository).deleteById(id);
  }

  @Test
  void deleteReturnsFalseWhenNotFound() {
    UUID id = UUID.randomUUID();
    when(repository.existsById(id)).thenReturn(false);

    assertThat(service.delete(id)).isFalse();
  }
}
