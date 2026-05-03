package com.anomalydetection.host.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
 * Phase A-5: Integration tests for Safety trace record traceability key fields.
 * Covers TOP1 (feature_id mandatory), TOP2/TOP3 (UNKNOWN if_impact constraints).
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class SafetyTraceApiTest {

  private static final String BASE = "/api/app/safety-trace-records";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  private static SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }

  // ── TOP1: feature_id mandatory ──────────────────────────────────────────────

  @Test
  void createWithoutFeatureIdReturnsBadRequest() throws Exception {
    String body = """
        {
          "name": "AEB Safety Record",
          "asilLevel": "ASIL-D"
        }
        """;
    mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.Records.Create"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("feature_id is required for safety trace records"));
  }

  // ── Create with traceability fields ─────────────────────────────────────────

  @Test
  void createWithTraceabilityFieldsReturnsAllFields() throws Exception {
    String body = """
        {
          "name": "AEB Brake Safety",
          "asilLevel": "ASIL-D",
          "featureId": "FEAT-AEB-001",
          "decisionId": "DEC-2026-042",
          "changeId": "CHG-2026-007",
          "ifImpact": "UNCHANGED",
          "designRationale": "AEB brake control boundary maintained",
          "assumption": "Brake pressure within spec range",
          "constraintText": "Max decel 12 m/s^2",
          "docSyncStatus": "NOT_REQUIRED",
          "scope": "PLATFORM",
          "applicability": "All OEMs"
        }
        """;
    mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.Records.Create"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.featureId").value("FEAT-AEB-001"))
        .andExpect(jsonPath("$.decisionId").value("DEC-2026-042"))
        .andExpect(jsonPath("$.changeId").value("CHG-2026-007"))
        .andExpect(jsonPath("$.ifImpact").value("UNCHANGED"))
        .andExpect(jsonPath("$.designRationale").value("AEB brake control boundary maintained"))
        .andExpect(jsonPath("$.scope").value("PLATFORM"))
        .andExpect(jsonPath("$.applicability").value("All OEMs"));
  }

  // ── TOP2/TOP3: UNKNOWN if_impact without deadline is blocked at submit ──────

  @Test
  void submitBlockedWhenIfImpactUnknownWithoutDeadline() throws Exception {
    // Create with UNKNOWN if_impact but no unknownUntil / unknownOwnerId
    String createBody = """
        {
          "name": "Radar Interference Record",
          "asilLevel": "ASIL-B",
          "featureId": "FEAT-RADAR-003",
          "ifImpact": "UNKNOWN"
        }
        """;
    MvcResult created = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody)
            .with(jwt().authorities(auth("SafetyTrace.Records.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

    // Submit should be blocked — 400 because deadline+owner are not set
    mockMvc
        .perform(post(BASE + "/" + id + "/submit")
            .with(jwt().authorities(auth("SafetyTrace.Records.Edit"))))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(
            "Cannot submit: if_impact is UNKNOWN but unknown_until or unknown_owner_id is not set"));
  }

  @Test
  void submitSucceedsWhenIfImpactUnknownWithDeadlineAndOwner() throws Exception {
    String ownerId = "11111111-1111-1111-1111-111111111111";
    String createBody = """
        {
          "name": "Lidar Boundary Record",
          "asilLevel": "ASIL-C",
          "featureId": "FEAT-LIDAR-007",
          "ifImpact": "UNKNOWN",
          "unknownUntil": "2026-12-31",
          "unknownOwnerId": "%s"
        }
        """.formatted(ownerId);

    MvcResult created = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody)
            .with(jwt().authorities(auth("SafetyTrace.Records.Create"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.unknownUntil").value("2026-12-31"))
        .andExpect(jsonPath("$.unknownOwnerId").value(ownerId))
        .andReturn();

    String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

    // Submit should succeed — deadline + owner are set
    mockMvc
        .perform(post(BASE + "/" + id + "/submit")
            .with(jwt().authorities(auth("SafetyTrace.Records.Edit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.approvalStatus").value("SUBMITTED"));
  }
}
