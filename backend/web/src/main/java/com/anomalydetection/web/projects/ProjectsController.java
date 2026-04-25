package com.anomalydetection.web.projects;

import com.anomalydetection.application.projects.ProjectsAppService;
import com.anomalydetection.contracts.projects.CreateProjectMemberDto;
import com.anomalydetection.contracts.projects.CreateProjectMilestoneDto;
import com.anomalydetection.contracts.projects.GetProjectsInputDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.contracts.projects.ProjectMemberDto;
import com.anomalydetection.contracts.projects.ProjectDto;
import com.anomalydetection.contracts.projects.ProjectMilestoneDto;
import com.anomalydetection.contracts.projects.UpdateProjectMemberDto;
import com.anomalydetection.contracts.projects.UpdateProjectMilestoneDto;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/app/anomaly-detection-project")
public class ProjectsController {

  private final ProjectsAppService appService;

  public ProjectsController(ProjectsAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public PagedResultDto<ProjectDto> getList(
      @RequestParam(required = false) String filter,
      @RequestParam(required = false) Integer status,
      @RequestParam(required = false) Integer priority,
      @RequestParam(required = false) String vehicleModel,
      @RequestParam(required = false) String primarySystem,
      @RequestParam(required = false) Integer skipCount,
      @RequestParam(required = false) Integer maxResultCount,
      @RequestParam(required = false) String sorting) {
    return appService.getList(
        new GetProjectsInputDto(
            filter,
            status,
            priority,
            vehicleModel,
            primarySystem,
            skipCount,
            maxResultCount,
            sorting));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProjectDto> get(@PathVariable String id) {
    return appService.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{projectId}/milestones")
  public List<ProjectMilestoneDto> getMilestones(@PathVariable String projectId) {
    return appService.getMilestones(projectId);
  }

  @PostMapping("/milestones")
  public ProjectMilestoneDto createMilestone(@RequestBody CreateProjectMilestoneDto input) {
    return appService.createMilestone(input);
  }

  @PutMapping("/milestones/{id}")
  public ResponseEntity<ProjectMilestoneDto> updateMilestone(
      @PathVariable String id, @RequestBody UpdateProjectMilestoneDto input) {
    return appService.updateMilestone(id, input).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/milestones/{id}")
  public ResponseEntity<Void> deleteMilestone(@PathVariable String id) {
    return appService.deleteMilestone(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }

  @PostMapping("/milestones/{id}/complete")
  public ResponseEntity<ProjectMilestoneDto> completeMilestone(@PathVariable String id) {
    return appService.completeMilestone(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{projectId}/members")
  public List<ProjectMemberDto> getMembers(@PathVariable String projectId) {
    return appService.getMembers(projectId);
  }

  @PostMapping("/members")
  public ProjectMemberDto addMember(@RequestBody CreateProjectMemberDto input) {
    return appService.addMember(input);
  }

  @PutMapping("/members/{id}")
  public ResponseEntity<ProjectMemberDto> updateMember(
      @PathVariable String id, @RequestBody UpdateProjectMemberDto input) {
    return appService.updateMember(id, input).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/members/{id}")
  public ResponseEntity<Void> removeMember(@PathVariable String id) {
    return appService.removeMember(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }
}
