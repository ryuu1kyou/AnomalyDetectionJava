package com.anomalydetection.web.projects;

import com.anomalydetection.application.projects.ProjectsAppService;
import com.anomalydetection.contracts.projects.GetProjectsInputDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.contracts.projects.ProjectDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
