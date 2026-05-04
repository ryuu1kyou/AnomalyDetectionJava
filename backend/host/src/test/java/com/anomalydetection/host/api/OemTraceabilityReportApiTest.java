package com.anomalydetection.host.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.host.support.MariaDB4jExtension;
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

/**
 * M9-C: Integration tests for CrossOEM traceability report.
 * Covers: empty feature, single-OEM, multi-OEM with divergent status.
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class OemTraceabilityReportApiTest {

  private static final String REPORT = "/api/app/oem-traceability-report/by-feature";
  private static final String APPROVALS = "/api/app/oem-traceability/approvals";

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  private static SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }

  private void createApproval(String featureId, String oemCode) throws Exception {
    String body = """
        {
          "entityId": "ent-001",
          "entityType": "SafetyTraceRecord",
          "oemCode": "%s",
          "type": "MODIFICATION",
          "featureId": "%s",
          "priority": 2,
          "dueDate": "2026-12-31T00:00:00Z"
        }
        """.formatted(oemCode, featureId);
    mockMvc.perform(post(APPROVALS).contentType(MediaType.APPLICATION_JSON).content(body)
        .with(jwt().authorities(auth("OemTraceability.Approvals.Create"))))
        .andExpect(status().isOk());
  }

  @Test
  void reportForUnknownFeatureReturnsEmpty() throws Exception {
    mockMvc
        .perform(get(REPORT).param("featureId", "FEAT-M9C-NONE")
            .with(jwt().authorities(auth("OemTraceability.Approvals.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.featureId").value("FEAT-M9C-NONE"))
        .andExpect(jsonPath("$.totalOems").value(0))
        .andExpect(jsonPath("$.approvedCount").value(0))
        .andExpect(jsonPath("$.byOem").isArray())
        .andExpect(jsonPath("$.byOem.length()").value(0));
  }

  @Test
  void reportGroupsByOemCode() throws Exception {
    String fid = "FEAT-M9C-GRP";
    createApproval(fid, "OEM-ALPHA");
    createApproval(fid, "OEM-BETA");
    createApproval(fid, "OEM-ALPHA");

    mockMvc
        .perform(get(REPORT).param("featureId", fid)
            .with(jwt().authorities(auth("OemTraceability.Approvals.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.featureId").value(fid))
        .andExpect(jsonPath("$.totalOems").value(2))
        .andExpect(jsonPath("$.byOem.length()").value(2));
  }

  @Test
  void reportPendingCountsCorrectly() throws Exception {
    String fid = "FEAT-M9C-CNT";
    createApproval(fid, "OEM-GAMMA");
    createApproval(fid, "OEM-GAMMA");

    mockMvc
        .perform(get(REPORT).param("featureId", fid)
            .with(jwt().authorities(auth("OemTraceability.Approvals.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalOems").value(1))
        .andExpect(jsonPath("$.pendingCount").value(2))
        .andExpect(jsonPath("$.byOem[0].oemCode").value("OEM-GAMMA"))
        .andExpect(jsonPath("$.byOem[0].dominantStatus").value("PENDING"));
  }
}
