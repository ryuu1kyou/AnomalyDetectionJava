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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class CanSignalApiTest {

  private static final String BASE = "/api/app/can-signals";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void listRequiresAuthentication() throws Exception {
    mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
  }

  @Test
  void listReturnsEmptyArrayWithJwt() throws Exception {
    mockMvc
        .perform(get(BASE).with(jwt().authorities(
            new org.springframework.security.core.authority.SimpleGrantedAuthority("CanSignal.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void createAndRetrieveSignal() throws Exception {
    String body = """
        {
          "frameId": 512,
          "name": "TestEngineSpeed",
          "description": "Engine RPM signal",
          "startBit": 0,
          "length": 16,
          "byteOrder": "LITTLE_ENDIAN",
          "isSigned": false,
          "specificationId": null
        }
        """;

    MvcResult createResult = mockMvc
        .perform(post(BASE)
            .contentType(MediaType.APPLICATION_JSON)
            .content(body)
            .with(jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CanSignal.Create"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("TestEngineSpeed"))
        .andExpect(jsonPath("$.frameId").value(512))
        .andReturn();

    String responseBody = createResult.getResponse().getContentAsString();
    String id = objectMapper.readTree(responseBody).get("id").asText();

    mockMvc
        .perform(get(BASE + "/" + id)
            .with(jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CanSignal.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(id))
        .andExpect(jsonPath("$.name").value("TestEngineSpeed"));
  }

  @Test
  void updateSignal() throws Exception {
    String createBody = """
        {"frameId": 600, "name": "UpdateMe", "description": "",
         "startBit": 0, "length": 8, "byteOrder": "LITTLE_ENDIAN", "isSigned": false, "specificationId": null}
        """;

    MvcResult createResult = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody)
            .with(jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CanSignal.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    String updateBody = """
        {"frameId": 601, "name": "UpdatedName", "description": "Updated",
         "startBit": 8, "length": 16, "byteOrder": "BIG_ENDIAN", "isSigned": true, "specificationId": null}
        """;

    mockMvc
        .perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content(updateBody)
            .with(jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CanSignal.Edit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("UpdatedName"))
        .andExpect(jsonPath("$.frameId").value(601));
  }

  @Test
  void deleteSignalReturns204ThenGetReturns404() throws Exception {
    String createBody = """
        {"frameId": 700, "name": "DeleteMe", "description": "",
         "startBit": 0, "length": 8, "byteOrder": "LITTLE_ENDIAN", "isSigned": false, "specificationId": null}
        """;

    MvcResult createResult = mockMvc
        .perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(createBody)
            .with(jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CanSignal.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(delete(BASE + "/" + id)
            .with(jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CanSignal.Delete"))))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get(BASE + "/" + id)
            .with(jwt().authorities(
                new org.springframework.security.core.authority.SimpleGrantedAuthority("CanSignal.Default"))))
        .andExpect(status().isNotFound());
  }
}
