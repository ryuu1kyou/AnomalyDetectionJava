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

/**
 * Verifies that soft-deleted entities are invisible to subsequent read operations,
 * exercising the @SQLRestriction("is_deleted = false") guard.
 */
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class SoftDeleteApiTest {

  private static final String CAN_SIGNALS = "/api/app/can-signals";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  private SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }

  @Test
  void softDeletedSignalNotReturnedByGetById() throws Exception {
    String body = """
        {"frameId": 999, "name": "SoftDeleteTarget", "description": "",
         "startBit": 0, "length": 8, "byteOrder": "LITTLE_ENDIAN", "isSigned": false, "specificationId": null}
        """;

    MvcResult createResult = mockMvc
        .perform(post(CAN_SIGNALS).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("CanSignal.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(get(CAN_SIGNALS + "/" + id)
            .with(jwt().authorities(auth("CanSignal.Default"))))
        .andExpect(status().isOk());

    mockMvc
        .perform(delete(CAN_SIGNALS + "/" + id)
            .with(jwt().authorities(auth("CanSignal.Delete"))))
        .andExpect(status().isNoContent());

    // After soft-delete: GET by ID must return 404
    mockMvc
        .perform(get(CAN_SIGNALS + "/" + id)
            .with(jwt().authorities(auth("CanSignal.Default"))))
        .andExpect(status().isNotFound());
  }

  @Test
  void softDeletedSignalNotReturnedByList() throws Exception {
    String body = """
        {"frameId": 998, "name": "SoftDeleteInList", "description": "",
         "startBit": 0, "length": 8, "byteOrder": "LITTLE_ENDIAN", "isSigned": false, "specificationId": null}
        """;

    MvcResult createResult = mockMvc
        .perform(post(CAN_SIGNALS).contentType(MediaType.APPLICATION_JSON).content(body)
            .with(jwt().authorities(auth("CanSignal.Create"))))
        .andExpect(status().isOk())
        .andReturn();

    String id = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(delete(CAN_SIGNALS + "/" + id)
            .with(jwt().authorities(auth("CanSignal.Delete"))))
        .andExpect(status().isNoContent());

    // The deleted signal must not appear in the list
    mockMvc
        .perform(get(CAN_SIGNALS).with(jwt().authorities(auth("CanSignal.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == '" + id + "')]").isEmpty());
  }
}
