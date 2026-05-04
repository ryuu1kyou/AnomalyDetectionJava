package com.anomalydetection.host.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
 * M9-A integration tests for the design-intent ledger (設計意図台帳).
 * Covers: create, read, update, activate, supersede, delete, duplicate decisionId guard.
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class DecisionLedgerApiTest {

  private static final String BASE = "/api/app/decision-ledger";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  private static SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }

  // ── CREATE ───────────────────────────────────────────────────────────────────

  @Test
  void createDecisionLedgerReturnsAllFields() throws Exception {
    String body = """
        {
          "decisionId": "DEC-M9A-001",
          "whatDecided": "Use brake-by-wire for AEB stopping distance",
          "whyDecided": "Meets ASIL-D latency requirement < 10ms",
          "assumptions": "Hydraulic backup always available",
          "constraintsText": "Max decel 12 m/s^2",
          "relatedFeatureIds": ["FEAT-AEB-001"],
          "relatedModuleIds": ["MOD-BRAKE-001"]
        }
        """;
    mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decisionId").value("DEC-M9A-001"))
        .andExpect(jsonPath("$.whatDecided").value("Use brake-by-wire for AEB stopping distance"))
        .andExpect(jsonPath("$.whyDecided").value("Meets ASIL-D latency requirement < 10ms"))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.relatedFeatureIds[0]").value("FEAT-AEB-001"))
        .andExpect(jsonPath("$.relatedModuleIds[0]").value("MOD-BRAKE-001"));
  }

  @Test
  void createWithoutDecisionIdReturnsBadRequest() throws Exception {
    String body = """
        {
          "whatDecided": "Some decision"
        }
        """;
    mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isBadRequest());
  }

  @Test
  void duplicateDecisionIdReturnsConflict() throws Exception {
    String body = """
        {
          "decisionId": "DEC-M9A-DUP",
          "whatDecided": "First entry"
        }
        """;
    mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
        .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isOk());

    String dup = """
        {
          "decisionId": "DEC-M9A-DUP",
          "whatDecided": "Duplicate entry"
        }
        """;
    mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(dup)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isBadRequest());
  }

  // ── READ ─────────────────────────────────────────────────────────────────────

  @Test
  void getByIdReturnsEntry() throws Exception {
    String body = """
        {
          "decisionId": "DEC-M9A-READ",
          "whatDecided": "Readable decision"
        }
        """;
    MvcResult created = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(get(BASE + "/" + id)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.decisionId").value("DEC-M9A-READ"));
  }

  // ── UPDATE ───────────────────────────────────────────────────────────────────

  @Test
  void updateDecisionLedgerChangesFields() throws Exception {
    String body = """
        {
          "decisionId": "DEC-M9A-UPD",
          "whatDecided": "Original decision"
        }
        """;
    MvcResult created = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

    String update = """
        {
          "whatDecided": "Updated decision",
          "whyDecided": "New rationale added"
        }
        """;
    mockMvc
        .perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(update)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Edit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.whatDecided").value("Updated decision"))
        .andExpect(jsonPath("$.whyDecided").value("New rationale added"));
  }

  // ── ACTIVATE ─────────────────────────────────────────────────────────────────

  @Test
  void activateTransitionsDraftToActive() throws Exception {
    String body = """
        {
          "decisionId": "DEC-M9A-ACT",
          "whatDecided": "Decision to be activated"
        }
        """;
    MvcResult created = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(post(BASE + "/" + id + "/activate")
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Approve"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  // ── SUPERSEDE ────────────────────────────────────────────────────────────────

  @Test
  void supersedeTransitionsToSuperseded() throws Exception {
    String body = """
        {
          "decisionId": "DEC-M9A-SUP",
          "whatDecided": "Decision to be superseded"
        }
        """;
    MvcResult created = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(post(BASE + "/" + id + "/supersede")
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Approve"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("SUPERSEDED"));
  }

  // ── DELETE ───────────────────────────────────────────────────────────────────

  @Test
  void deleteRemovesEntry() throws Exception {
    String body = """
        {
          "decisionId": "DEC-M9A-DEL",
          "whatDecided": "To be deleted"
        }
        """;
    MvcResult created = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(delete(BASE + "/" + id)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Delete"))))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get(BASE + "/" + id)
            .with(jwt().authorities(auth("SafetyTrace.DecisionLedger.Default"))))
        .andExpect(status().isNotFound());
  }

  // ── CrossRef: safety-trace with ext fields ────────────────────────────────────

  @Test
  void safetyTraceRecordWithExtFieldsRoundTrips() throws Exception {
    String body = """
        {
          "name": "AEB Ext Fields Record",
          "asilLevel": "ASIL-B",
          "featureId": "FEAT-EXT-001",
          "svnRev": "r12345",
          "moduleId": "MOD-AEB-BRAKE",
          "ifVersion": "IF-v2.3",
          "changeType": "INTEGRATION_EVENT"
        }
        """;
    mockMvc
        .perform(post("/api/app/safety-trace-records")
            .contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.Records.Create"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.svnRev").value("r12345"))
        .andExpect(jsonPath("$.moduleId").value("MOD-AEB-BRAKE"))
        .andExpect(jsonPath("$.ifVersion").value("IF-v2.3"))
        .andExpect(jsonPath("$.changeType").value("INTEGRATION_EVENT"));
  }
}
