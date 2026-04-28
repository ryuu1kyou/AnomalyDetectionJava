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
import java.util.Map;
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
class ProjectsApiTest {

  private static final String BASE = "/api/app/anomaly-detection-project";

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
  void listReturnsPagedResultWithJwt() throws Exception {
    mockMvc
        .perform(
            get(BASE)
                .with(
                    jwt().authorities(
                        new SimpleGrantedAuthority("Projects.Projects.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.totalCount").isNumber());
  }

  @Test
  void createAndGetProject() throws Exception {
    String body =
        objectMapper.writeValueAsString(
            Map.of(
                "projectCode", "PRJ-TEST-001",
                "projectName", "Test Project",
                "description", "Integration test project",
                "status", 0,
                "priority", 2));

    MvcResult createResult =
        mockMvc
            .perform(
                post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(body)
                    .with(
                        jwt().authorities(
                            new SimpleGrantedAuthority("Projects.Projects.Create"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.projectCode").value("PRJ-TEST-001"))
            .andExpect(jsonPath("$.projectName").value("Test Project"))
            .andReturn();

    String projectId =
        objectMapper
            .readTree(createResult.getResponse().getContentAsString())
            .get("id")
            .asText();

    mockMvc
        .perform(
            get(BASE + "/" + projectId)
                .with(
                    jwt().authorities(
                        new SimpleGrantedAuthority("Projects.Projects.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(projectId));
  }

  @Test
  void updateProject() throws Exception {
    String createBody =
        objectMapper.writeValueAsString(
            Map.of("projectCode", "PRJ-UPD-001", "projectName", "Before Update", "status", 0,
                "priority", 2));

    MvcResult createResult =
        mockMvc
            .perform(
                post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createBody)
                    .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.Create"))))
            .andExpect(status().isOk())
            .andReturn();

    String projectId =
        objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    String updateBody =
        objectMapper.writeValueAsString(
            Map.of("projectName", "After Update", "status", 1, "priority", 3,
                "progressPercentage", 50));

    mockMvc
        .perform(
            put(BASE + "/" + projectId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody)
                .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.Edit"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.projectName").value("After Update"));
  }

  @Test
  void deleteProject() throws Exception {
    String createBody =
        objectMapper.writeValueAsString(
            Map.of("projectCode", "PRJ-DEL-001", "projectName", "To Delete", "status", 0,
                "priority", 2));

    MvcResult createResult =
        mockMvc
            .perform(
                post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createBody)
                    .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.Create"))))
            .andExpect(status().isOk())
            .andReturn();

    String projectId =
        objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(
            delete(BASE + "/" + projectId)
                .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.Delete"))))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(
            get(BASE + "/" + projectId)
                .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.Default"))))
        .andExpect(status().isNotFound());
  }

  @Test
  void milestoneLifecycle() throws Exception {
    String createBody =
        objectMapper.writeValueAsString(
            Map.of("projectCode", "PRJ-MS-001", "projectName", "Milestone Project", "status", 0,
                "priority", 2));

    MvcResult createResult =
        mockMvc
            .perform(
                post(BASE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createBody)
                    .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.Create"))))
            .andExpect(status().isOk())
            .andReturn();

    String projectId =
        objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asText();

    String msBody =
        objectMapper.writeValueAsString(
            Map.of("projectId", projectId, "name", "M1 Design Review",
                "plannedDate", "2025-06-30", "dependencies", new String[]{},
                "deliverables", new String[]{}));

    MvcResult msResult =
        mockMvc
            .perform(
                post(BASE + "/milestones")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(msBody)
                    .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.ManageMilestones"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("M1 Design Review"))
            .andReturn();

    String msId =
        objectMapper.readTree(msResult.getResponse().getContentAsString()).get("id").asText();

    mockMvc
        .perform(
            get(BASE + "/" + projectId + "/milestones")
                .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.Default"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(msId));

    mockMvc
        .perform(
            post(BASE + "/milestones/" + msId + "/complete")
                .with(jwt().authorities(new SimpleGrantedAuthority("Projects.Projects.ManageMilestones"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.progressPercentage").value(100));
  }
}
