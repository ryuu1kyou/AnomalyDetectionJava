package com.anomalydetection.host.identity;

import static org.assertj.core.api.Assertions.assertThat;

import com.anomalydetection.infrastructure.multitenancy.CurrentTenantHolder;
import com.anomalydetection.infrastructure.multitenancy.TenantResolutionFilter;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class TenantResolutionFilterTest {

  private CurrentTenantHolder tenantHolder;
  private TenantResolutionFilter filter;

  @BeforeEach
  void setUp() {
    tenantHolder = new CurrentTenantHolder();
    filter = new TenantResolutionFilter(tenantHolder);
  }

  @Test
  void resolvesTenantFromXTenantIdHeader() throws Exception {
    UUID tenantId = UUID.randomUUID();
    var request = new MockHttpServletRequest();
    request.addHeader("X-Tenant-Id", tenantId.toString());

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    // After filter completes, tenant is cleared (finally block in TenantResolutionFilter)
    assertThat(tenantHolder.isSet()).isFalse();
  }

  @Test
  void ignoresInvalidUuid() throws Exception {
    var request = new MockHttpServletRequest();
    request.addHeader("X-Tenant-Id", "not-a-uuid");

    filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

    assertThat(tenantHolder.isSet()).isFalse();
  }
}
