package com.anomalydetection.host.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class SettingsApiTest {

  private static final String BASE = "/api/app/settings";

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void getRequiresAuthentication() throws Exception {
    mockMvc.perform(get(BASE).param("key", "test.key")).andExpect(status().isUnauthorized());
  }

  @Test
  void getMissingKeyReturns404() throws Exception {
    mockMvc.perform(get(BASE).param("key", "nonexistent.key").with(jwt()))
        .andExpect(status().isNotFound());
  }

  @Test
  void setAndGetGlobalSetting() throws Exception {
    String body = """
        {"key": "app.maxItems", "value": "50"}
        """;
    mockMvc.perform(put(BASE).with(jwt())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());

    mockMvc.perform(get(BASE).param("key", "app.maxItems").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.value").value("50"));
  }
}
