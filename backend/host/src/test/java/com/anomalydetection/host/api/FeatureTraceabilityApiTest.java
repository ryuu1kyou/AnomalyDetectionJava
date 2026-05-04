package com.anomalydetection.host.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.host.support.MariaDB4jExtension;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Phase B: Integration tests for cross-module feature_id traceability search.
 * Verifies GET /api/app/traceability/feature/{featureId} aggregates Safety + OEM data.
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class FeatureTraceabilityApiTest {

  private static final String SEARCH_BASE = "/api/app/traceability/feature";
  private static final String SAFETY_BASE = "/api/app/safety-trace-records";
  private static final String OEM_BASE = "/api/app/oem-traceability/approvals";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  private static SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }

  /**
   * Test 1: Create Safety record + OEM approval with same featureId,
   * then verify /traceability/feature/{featureId} returns both.
   */
  @Test
  void featureIdSearchReturnsBothSafetyAndOemRecords() throws Exception {
    String featureId = "FEAT-B-CROSS-001";

    // Create a Safety trace record with featureId
    String safetyBody = """
        {
          "name": "Cross-Module Safety Record",
          "asilLevel": "ASIL-B",
          "featureId": "%s",
          "ifImpact": "UNCHANGED",
          "designRationale": "Phase B cross-module test"
        }
        """.formatted(featureId);

    mockMvc.perform(post(SAFETY_BASE).contentType(MediaType.APPLICATION_JSON).content(safetyBody)
        .with(jwt().authorities(auth("SafetyTrace.Records.Create"))))
        .andExpect(status().isOk());

    // Create an OEM approval with same featureId
    String oemBody = """
        {
          "entityId": "AEB-MODULE-B",
          "entityType": "CanAnomalyDetectionLogic",
          "oemCode": "OEM-BETA",
          "type": "MODIFICATION",
          "featureId": "%s",
          "approvalReason": "Phase B cross-module OEM approval"
        }
        """.formatted(featureId);

    mockMvc.perform(post(OEM_BASE).contentType(MediaType.APPLICATION_JSON).content(oemBody)
        .with(jwt().authorities(auth("OemTraceability.Approvals.Create"))))
        .andExpect(status().isOk());

    // Cross-module search: expect both records in the response
    mockMvc.perform(get(SEARCH_BASE + "/" + featureId)
        .with(jwt().authorities(
            auth("SafetyTrace.Records.Default"),
            auth("OemTraceability.Approvals.Default"),
            auth("SafetyTrace.DecisionLedger.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.featureId").value(featureId))
        .andExpect(jsonPath("$.safetyRecords").isArray())
        .andExpect(jsonPath("$.safetyRecords[0].featureId").value(featureId))
        .andExpect(jsonPath("$.oemApprovals").isArray())
        .andExpect(jsonPath("$.oemApprovals[0].featureId").value(featureId));
  }

  /**
   * Test 2: featureId with no matching records returns empty lists (not 404).
   */
  @Test
  void featureIdSearchReturnsEmptyListsWhenNoMatch() throws Exception {
    String featureId = "FEAT-NONEXISTENT-999";

    mockMvc.perform(get(SEARCH_BASE + "/" + featureId)
        .with(jwt().authorities(
            auth("SafetyTrace.Records.Default"),
            auth("OemTraceability.Approvals.Default"),
            auth("SafetyTrace.DecisionLedger.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.featureId").value(featureId))
        .andExpect(jsonPath("$.safetyRecords").isArray())
        .andExpect(jsonPath("$.safetyRecords").isEmpty())
        .andExpect(jsonPath("$.oemApprovals").isArray())
        .andExpect(jsonPath("$.oemApprovals").isEmpty());
  }
}
