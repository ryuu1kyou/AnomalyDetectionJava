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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class PermissionsApiTest {

  private static final String BASE = "/api/app/permissions";

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void definitionsRequiresAuthentication() throws Exception {
    mockMvc.perform(get(BASE + "/definitions")).andExpect(status().isUnauthorized());
  }

  @Test
  void definitionsReturnAllGroups() throws Exception {
    mockMvc.perform(get(BASE + "/definitions").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$[0].name").exists());
  }

  @Test
  void grantsReturnsArray() throws Exception {
    mockMvc.perform(get(BASE + "/grants").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void grantToRoleAddsGrant() throws Exception {
    String body = """
        {"permission": "CanSignal.Default"}
        """;
    mockMvc.perform(post(BASE + "/roles/viewer/grant").with(jwt())
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isOk());

    mockMvc.perform(get(BASE + "/grants").with(jwt()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.providerKey == 'viewer')].name").value("CanSignal.Default"));
  }
}
