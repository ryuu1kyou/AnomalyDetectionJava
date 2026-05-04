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
 * M9-B: Integration tests for V&V records and lifecycle events on safety trace records.
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class SafetyTraceVandVApiTest {

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

  private String createRecord(String featureId) throws Exception {
    String body = """
        {
          "name": "VandV Test Record",
          "asilLevel": "ASIL-B",
          "featureId": "%s"
        }
        """.formatted(featureId);
    MvcResult result = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.Records.Create"))))
        .andExpect(status().isOk())
        .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
  }

  // ── Verification records ──────────────────────────────────────────────────

  @Test
  void addVerificationAndRetrieve() throws Exception {
    String id = createRecord("FEAT-VV-001");

    String body = """
        {
          "stage": "VERIFICATION",
          "method": "Code Review + Static Analysis",
          "result": "PASS",
          "toolRef": "Polyspace-v2024",
          "notes": "No MISRA violations found"
        }
        """;
    mockMvc
        .perform(post(BASE + "/" + id + "/verifications")
            .contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.Records.Edit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stage").value("VERIFICATION"))
        .andExpect(jsonPath("$.method").value("Code Review + Static Analysis"))
        .andExpect(jsonPath("$.result").value("PASS"))
        .andExpect(jsonPath("$.toolRef").value("Polyspace-v2024"))
        .andExpect(jsonPath("$.id").isNotEmpty());

    mockMvc
        .perform(get(BASE + "/" + id + "/verifications")
            .with(jwt().authorities(auth("SafetyTrace.Records.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].stage").value("VERIFICATION"));
  }

  // ── Validation records ───────────────────────────────────────────────────

  @Test
  void addValidationAndRetrieve() throws Exception {
    String id = createRecord("FEAT-VV-002");

    String body = """
        {
          "stage": "VALIDATION",
          "scenario": "AEB braking in wet road conditions",
          "outcome": "PASS — 9.8ms response time",
          "testRef": "HIL-TEST-2026-042",
          "notes": "HIL test with real brake ECU"
        }
        """;
    mockMvc
        .perform(post(BASE + "/" + id + "/validations")
            .contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.Records.Edit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stage").value("VALIDATION"))
        .andExpect(jsonPath("$.scenario").value("AEB braking in wet road conditions"))
        .andExpect(jsonPath("$.testRef").value("HIL-TEST-2026-042"))
        .andExpect(jsonPath("$.id").isNotEmpty());

    mockMvc
        .perform(get(BASE + "/" + id + "/validations")
            .with(jwt().authorities(auth("SafetyTrace.Records.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].outcome").value("PASS — 9.8ms response time"));
  }

  // ── Lifecycle events ──────────────────────────────────────────────────────

  @Test
  void recordLifecycleEventAndRetrieve() throws Exception {
    String id = createRecord("FEAT-VV-003");

    String body = """
        {
          "stage": "IMPLEMENTATION",
          "eventType": "BASELINE_SET",
          "description": "Branch v2.3 frozen for integration test"
        }
        """;
    mockMvc
        .perform(post(BASE + "/" + id + "/lifecycle-events")
            .contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.Records.Edit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stage").value("IMPLEMENTATION"))
        .andExpect(jsonPath("$.eventType").value("BASELINE_SET"))
        .andExpect(jsonPath("$.id").isNotEmpty());

    mockMvc
        .perform(get(BASE + "/" + id + "/lifecycle-events")
            .with(jwt().authorities(auth("SafetyTrace.Records.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1));
  }

  // ── Change requests ───────────────────────────────────────────────────────

  @Test
  void addChangeRequestAndRetrieve() throws Exception {
    String id = createRecord("FEAT-VV-004");

    String body = """
        {
          "changeId": "CHG-2026-099",
          "changeType": "INTEGRATION_EVENT",
          "description": "Increase max decel threshold to 13 m/s^2",
          "rationale": "New OEM requirement from Tier1 supplier"
        }
        """;
    mockMvc
        .perform(post(BASE + "/" + id + "/change-requests")
            .contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("SafetyTrace.Records.Edit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.changeId").value("CHG-2026-099"))
        .andExpect(jsonPath("$.changeType").value("INTEGRATION_EVENT"))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.id").isNotEmpty());

    mockMvc
        .perform(get(BASE + "/" + id + "/change-requests")
            .with(jwt().authorities(auth("SafetyTrace.Records.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[0].rationale").value("New OEM requirement from Tier1 supplier"));
  }

  // ── Multiple entries accumulate ───────────────────────────────────────────

  @Test
  void multipleVerificationsAccumulate() throws Exception {
    String id = createRecord("FEAT-VV-005");

    for (int i = 1; i <= 3; i++) {
      String body = """
          {
            "stage": "UNIT_TESTING",
            "method": "JUnit test #%d",
            "result": "PASS"
          }
          """.formatted(i);
      mockMvc
          .perform(post(BASE + "/" + id + "/verifications")
              .contentType(MediaType.APPLICATION_JSON).content(body)
              .with(jwt().authorities(auth("SafetyTrace.Records.Edit"))))
          .andExpect(status().isOk());
    }

    mockMvc
        .perform(get(BASE + "/" + id + "/verifications")
            .with(jwt().authorities(auth("SafetyTrace.Records.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(3));
  }
}
