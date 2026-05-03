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
 * Phase A-5: Integration tests for OEM approval traceability key fields.
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class OemTraceabilityApiTest {

  private static final String APPROVALS = "/api/app/oem-traceability/approvals";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  private static SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }

  // ── Auth guard ──────────────────────────────────────────────────────────────

  @Test
  void listApprovalsRequiresAuthentication() throws Exception {
    mockMvc.perform(get(APPROVALS)).andExpect(status().isUnauthorized());
  }

  // ── Create with traceability fields ─────────────────────────────────────────

  @Test
  void createApprovalWithTraceabilityFieldsReturnsAllFields() throws Exception {
    String body = """
        {
          "entityId": "entity-aeb-001",
          "entityType": "CanSignal",
          "oemCode": "OEM_TOYOTA",
          "type": "NEW_ENTITY",
          "approvalReason": "Initial OEM sign-off for AEB signal",
          "priority": 1,
          "featureId": "FEAT-AEB-001",
          "decisionId": "DEC-2026-042",
          "applicability": "Toyota Gr86 2026",
          "confidentialityLevel": "OEM_INTERNAL"
        }
        """;
    mockMvc
        .perform(post(APPROVALS).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("OemTraceability.Approvals.Create"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.featureId").value("FEAT-AEB-001"))
        .andExpect(jsonPath("$.decisionId").value("DEC-2026-042"))
        .andExpect(jsonPath("$.applicability").value("Toyota Gr86 2026"))
        .andExpect(jsonPath("$.confidentialityLevel").value("OEM_INTERNAL"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  // ── Create without traceability fields (optional) succeeds ──────────────────

  @Test
  void createApprovalWithoutTraceabilityFieldsSucceeds() throws Exception {
    String body = """
        {
          "entityId": "entity-radar-002",
          "entityType": "DetectionTemplate",
          "oemCode": "OEM_BMW",
          "type": "MODIFICATION",
          "approvalReason": "Radar threshold adjustment",
          "priority": 2
        }
        """;
    mockMvc
        .perform(post(APPROVALS).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("OemTraceability.Approvals.Create"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.featureId").isEmpty())
        .andExpect(jsonPath("$.decisionId").isEmpty())
        .andExpect(jsonPath("$.status").value("PENDING"));
  }

  // ── Retrieve approval by ID verifies persisted traceability fields ───────────

  @Test
  void createAndGetApprovalPreservesTraceabilityFields() throws Exception {
    String body = """
        {
          "entityId": "entity-lidar-003",
          "entityType": "CanSignal",
          "oemCode": "OEM_HONDA",
          "type": "NEW_ENTITY",
          "priority": 1,
          "featureId": "FEAT-LIDAR-009",
          "decisionId": "DEC-2026-099",
          "applicability": "Honda e-Series 2026",
          "confidentialityLevel": "PLATFORM_PUBLIC"
        }
        """;
    MvcResult created = mockMvc
        .perform(post(APPROVALS).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("OemTraceability.Approvals.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(get(APPROVALS + "/" + id)
            .with(jwt().authorities(auth("OemTraceability.Approvals.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.featureId").value("FEAT-LIDAR-009"))
        .andExpect(jsonPath("$.decisionId").value("DEC-2026-099"))
        .andExpect(jsonPath("$.applicability").value("Honda e-Series 2026"))
        .andExpect(jsonPath("$.confidentialityLevel").value("PLATFORM_PUBLIC"));
  }
}
