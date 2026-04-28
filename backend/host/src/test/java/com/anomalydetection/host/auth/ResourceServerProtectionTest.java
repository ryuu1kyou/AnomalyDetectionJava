package com.anomalydetection.host.auth;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.anomalydetection.AnomalyDetectionApplication;
import com.anomalydetection.host.support.MariaDB4jExtension;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class ResourceServerProtectionTest {

  @Autowired private MockMvc mockMvc;

  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  @Test
  void apiEndpointReturns401WithoutToken() throws Exception {
    mockMvc
        .perform(get("/api/app/tenants"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void apiEndpointReturns200WithValidJwt() throws Exception {
    mockMvc
        .perform(get("/api/app/tenants").with(
            jwt().authorities(new SimpleGrantedAuthority("AnomalyDetection.Identity.Tenants.View"))))
        .andExpect(status().isOk());
  }

  @Test
  void actuatorHealthIsPublic() throws Exception {
    mockMvc
        .perform(get("/actuator/health"))
        .andExpect(status().isOk());
  }
}
