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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class AnomalyDetectionLogicApiTest {

  private static final String BASE = "/api/app/can-anomaly-detection-logics";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  private static SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }

  private static RequestPostProcessor logicJwt() {
    return jwt().authorities(
        auth("AnomalyDetection.Logic.Default"),
        auth("AnomalyDetection.Logic.Create"),
        auth("AnomalyDetection.Logic.Edit"),
        auth("AnomalyDetection.Logic.Delete"),
        auth("AnomalyDetection.Logic.Approve"));
  }

  private static final String CREATE_BODY = """
      {
        "name": "BrakePressureTimeout",
        "version": "1.0.0",
        "oemCode": "BMW",
        "anomalyType": "TIMEOUT",
        "description": "Detects brake pressure timeout",
        "targetSystemType": "BrakeSystem",
        "complexity": "SIMPLE",
        "requirements": "ISO 26262 ASIL-B",
        "implementationType": "CONFIGURATION",
        "implementationContent": "",
        "implementationLanguage": "",
        "implementationEntryPoint": "",
        "asilLevel": "B",
        "safetyRequirementId": "",
        "safetyGoalId": "",
        "hazardAnalysisId": "",
        "sharingLevel": "PRIVATE",
        "vehiclePhaseId": null
      }
      """;

  @Test
  void listRequiresAuthentication() throws Exception {
    mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
  }

  @Test
  void createLogicStartsAsDraft() throws Exception {
    mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY)
            .with(logicJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("BrakePressureTimeout"))
        .andExpect(jsonPath("$.status").value("DRAFT"))
        .andExpect(jsonPath("$.asilLevel").value("B"));
  }

  @Test
  void submitForApprovalTransitionsToPending() throws Exception {
    MvcResult createResult = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY)
            .with(logicJwt()))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(post(BASE + "/" + id + "/submit-for-approval").with(logicJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PENDING_APPROVAL"));
  }

  @Test
  void approveWorkflow() throws Exception {
    MvcResult createResult = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY)
            .with(logicJwt()))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(post(BASE + "/" + id + "/submit-for-approval").with(logicJwt()))
        .andExpect(status().isOk());

    mockMvc
        .perform(post(BASE + "/" + id + "/approve")
            .param("notes", "Reviewed and approved")
            .with(logicJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("APPROVED"))
        .andExpect(jsonPath("$.approvalNotes").value("Reviewed and approved"));
  }

  @Test
  void rejectWorkflow() throws Exception {
    MvcResult createResult = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY)
            .with(logicJwt()))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc.perform(post(BASE + "/" + id + "/submit-for-approval").with(logicJwt()))
        .andExpect(status().isOk());

    mockMvc
        .perform(post(BASE + "/" + id + "/reject")
            .param("reason", "Missing test evidence")
            .with(logicJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("REJECTED"));
  }

  @Test
  void filterByStatus() throws Exception {
    mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(CREATE_BODY)
            .with(logicJwt()))
        .andExpect(status().isOk());

    mockMvc
        .perform(get(BASE).param("status", "DRAFT").with(logicJwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }
}
