package com.anomalydetection.domain.cansignals;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CanSignalTest {

  @Test
  void constructorSetsRequiredFields() {
    UUID id = UUID.randomUUID();
    var signal = new CanSignal(id, 0x100, "EngineSpeed", 0, 16);

    assertThat(signal.getId()).isEqualTo(id);
    assertThat(signal.getFrameId()).isEqualTo(0x100);
    assertThat(signal.getName()).isEqualTo("EngineSpeed");
    assertThat(signal.getStartBit()).isEqualTo(0);
    assertThat(signal.getLength()).isEqualTo(16);
  }

  @Test
  void settersMutateCorrectly() {
    var signal = new CanSignal(UUID.randomUUID(), 0x100, "Speed", 0, 8);
    UUID tenantId = UUID.randomUUID();

    signal.setTenantId(tenantId);
    signal.setDescription("Vehicle speed signal");
    signal.setByteOrder("LITTLE_ENDIAN");
    signal.setSigned(true);

    assertThat(signal.getTenantId()).isEqualTo(tenantId);
    assertThat(signal.getDescription()).isEqualTo("Vehicle speed signal");
    assertThat(signal.getByteOrder()).isEqualTo("LITTLE_ENDIAN");
    assertThat(signal.isSigned()).isTrue();
  }

  @Test
  void specificationIdIsNullByDefault() {
    var signal = new CanSignal(UUID.randomUUID(), 1, "TestSignal", 0, 8);
    assertThat(signal.getSpecificationId()).isNull();
  }

  @Test
  void specificationIdCanBeSet() {
    var signal = new CanSignal(UUID.randomUUID(), 1, "TestSignal", 0, 8);
    UUID specId = UUID.randomUUID();
    signal.setSpecificationId(specId);
    assertThat(signal.getSpecificationId()).isEqualTo(specId);
  }
}
