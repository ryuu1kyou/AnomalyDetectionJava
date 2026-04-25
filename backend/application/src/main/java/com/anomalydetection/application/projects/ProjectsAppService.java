package com.anomalydetection.application.projects;

import com.anomalydetection.contracts.projects.GetProjectsInputDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.contracts.projects.ProjectDto;
import com.anomalydetection.contracts.projects.ProjectPriority;
import com.anomalydetection.contracts.projects.ProjectStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * Temporary in-memory implementation.
 *
 * <p>M1 以降で repository + DB 実装に置き換える。
 */
@Service
public class ProjectsAppService {

  private final List<ProjectDto> mock =
      List.of(
          new ProjectDto(
              "p-001",
              "AD-001",
              "Brake Pressure Anomaly",
              "Brake pressure sensor anomaly detection baseline",
              "Model X",
              "2026",
              "PX-1",
              "Brake",
              "JP",
              ProjectStatus.Active,
              ProjectPriority.High,
              LocalDate.of(2026, 4, 1).toString(),
              LocalDate.of(2026, 6, 30).toString(),
              null,
              42,
              "OEM-A",
              "OEM Alpha",
              7,
              21,
              12,
              3),
          new ProjectDto(
              "p-002",
              "AD-002",
              "Battery Temp Drift",
              "Temperature drift detection for battery pack",
              "Model Y",
              "2025",
              "PY-2",
              "Battery",
              "US",
              ProjectStatus.Planning,
              ProjectPriority.Medium,
              LocalDate.of(2026, 5, 1).toString(),
              LocalDate.of(2026, 8, 31).toString(),
              null,
              5,
              "OEM-B",
              "OEM Beta",
              0,
              0,
              0,
              0));

  public PagedResultDto<ProjectDto> getList(GetProjectsInputDto input) {
    final String filter = input.filter() == null ? "" : input.filter().trim().toLowerCase();

    var filtered =
        mock.stream()
            .filter(
                p -> {
                  if (!filter.isBlank()) {
                    var hay = (p.projectCode() + " " + p.projectName()).toLowerCase();
                    if (!hay.contains(filter)) return false;
                  }
                  if (input.status() != null) {
                    if (p.status().code() != input.status()) return false;
                  }
                  if (input.priority() != null) {
                    if (p.priority().code() != input.priority()) return false;
                  }
                  if (input.vehicleModel() != null && !input.vehicleModel().isBlank()) {
                    if (!p.vehicleModel().toLowerCase().contains(input.vehicleModel().toLowerCase())) return false;
                  }
                  if (input.primarySystem() != null && !input.primarySystem().isBlank()) {
                    if (!p.primarySystem().toLowerCase().contains(input.primarySystem().toLowerCase())) return false;
                  }
                  return true;
                })
            .sorted((a, b) -> a.projectCode().compareToIgnoreCase(b.projectCode()))
            .toList();

    int skip = Optional.ofNullable(input.skipCount()).orElse(0);
    int take = Optional.ofNullable(input.maxResultCount()).orElse(10);
    if (skip < 0) skip = 0;
    if (take <= 0) take = 10;

    var page = filtered.stream().skip(skip).limit(take).toList();
    return PagedResultDto.of(page, filtered.size());
  }

  public Optional<ProjectDto> getById(String id) {
    return mock.stream().filter(p -> p.id().equals(id)).findFirst();
  }
}
