package com.anomalydetection.host.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class DetectionTemplateApiTest {

  private static final String BASE = "/api/app/detection-templates";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  private static SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }

  @Test
  void listRequiresAuthentication() throws Exception {
    mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
  }

  @Test
  void createAndListTemplate() throws Exception {
    String body = """
        {
          "name": "BrakeTimeoutTemplate",
          "description": "Detects brake timeout anomalies",
          "canSignalId": "00000000-0000-0000-0000-000000000001",
          "expression": "value > 5000",
          "threshold": 5000.0,
          "isActive": true
        }
        """;

    mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("DetectionTemplates.Create"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("BrakeTimeoutTemplate"))
        .andExpect(jsonPath("$.isActive").value(true));

    mockMvc
        .perform(get(BASE).with(jwt().authorities(auth("DetectionTemplates.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void deleteTemplateReturns204() throws Exception {
    String body = """
        {"name": "ToDelete", "description": "", "canSignalId": "00000000-0000-0000-0000-000000000002",
         "expression": "value < 0", "threshold": null, "isActive": true}
        """;

    MvcResult result = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("DetectionTemplates.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(delete(BASE + "/" + id).with(jwt().authorities(auth("DetectionTemplates.Delete"))))
        .andExpect(status().isNoContent());
  }
}
