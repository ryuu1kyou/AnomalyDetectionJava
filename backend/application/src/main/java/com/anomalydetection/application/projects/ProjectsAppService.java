package com.anomalydetection.application.projects;

import com.anomalydetection.contracts.projects.GetProjectsInputDto;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.contracts.projects.ProjectMemberDto;
import com.anomalydetection.contracts.projects.ProjectDto;
import com.anomalydetection.contracts.projects.ProjectMilestoneDto;
import com.anomalydetection.contracts.projects.ProjectPriority;
import com.anomalydetection.contracts.projects.ProjectStatus;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
// NOTE: keep imports minimal; this is a temporary in-memory implementation.
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
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

  // In-memory child aggregates (for UI integration). Keyed by projectId.
  private final Map<String, List<ProjectMilestoneDto>> milestonesByProjectId = new HashMap<>();
  private final Map<String, List<ProjectMemberDto>> membersByProjectId = new HashMap<>();

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

  public List<ProjectMilestoneDto> getMilestones(String projectId) {
    return milestonesByProjectId.getOrDefault(projectId, List.of()).stream()
        .sorted(Comparator.comparing(ProjectMilestoneDto::plannedDate))
        .toList();
  }

  public ProjectMilestoneDto createMilestone(com.anomalydetection.contracts.projects.CreateProjectMilestoneDto input) {
    var id = "ms-" + UUID.randomUUID();
    var milestone =
        new ProjectMilestoneDto(
            id,
            input.projectId(),
            input.name(),
            input.description(),
            input.plannedDate(),
            null,
            com.anomalydetection.contracts.projects.MilestoneStatus.NotStarted,
            0,
            input.dependencies() == null ? List.of() : List.copyOf(input.dependencies()),
            input.deliverables() == null ? List.of() : List.copyOf(input.deliverables()));

    milestonesByProjectId.computeIfAbsent(input.projectId(), __ -> new ArrayList<>()).add(milestone);
    return milestone;
  }

  public Optional<ProjectMilestoneDto> updateMilestone(
      String milestoneId, com.anomalydetection.contracts.projects.UpdateProjectMilestoneDto input) {
    for (var entry : milestonesByProjectId.entrySet()) {
      var list = entry.getValue();
      for (int i = 0; i < list.size(); i++) {
        var cur = list.get(i);
        if (!cur.id().equals(milestoneId)) continue;

        var updated =
            new ProjectMilestoneDto(
                cur.id(),
                cur.projectId(),
                input.name(),
                input.description(),
                input.plannedDate(),
                input.actualDate(),
                input.status(),
                input.progressPercentage(),
                input.dependencies() == null ? List.of() : List.copyOf(input.dependencies()),
                input.deliverables() == null ? List.of() : List.copyOf(input.deliverables()));
        list.set(i, updated);
        return Optional.of(updated);
      }
    }
    return Optional.empty();
  }

  public boolean deleteMilestone(String milestoneId) {
    for (var entry : milestonesByProjectId.entrySet()) {
      var list = entry.getValue();
      if (list.removeIf(m -> m.id().equals(milestoneId))) {
        return true;
      }
    }
    return false;
  }

  public Optional<ProjectMilestoneDto> completeMilestone(String milestoneId) {
    for (var entry : milestonesByProjectId.entrySet()) {
      var list = entry.getValue();
      for (int i = 0; i < list.size(); i++) {
        var cur = list.get(i);
        if (!cur.id().equals(milestoneId)) continue;
        var updated =
            new ProjectMilestoneDto(
                cur.id(),
                cur.projectId(),
                cur.name(),
                cur.description(),
                cur.plannedDate(),
                LocalDate.now().toString(),
                com.anomalydetection.contracts.projects.MilestoneStatus.Completed,
                100,
                cur.dependencies(),
                cur.deliverables());
        list.set(i, updated);
        return Optional.of(updated);
      }
    }
    return Optional.empty();
  }

  public List<ProjectMemberDto> getMembers(String projectId) {
    return membersByProjectId.getOrDefault(projectId, List.of()).stream()
        .sorted(Comparator.comparing(ProjectMemberDto::userName))
        .toList();
  }

  public ProjectMemberDto addMember(com.anomalydetection.contracts.projects.CreateProjectMemberDto input) {
    var id = "mb-" + UUID.randomUUID();
    // NOTE: userName/email are unknown in this baseline. Use userId as placeholder.
    var member =
        new ProjectMemberDto(
            id,
            input.projectId(),
            input.userId(),
            input.userId(),
            input.userId() + "@example.com",
            input.role(),
            input.responsibilities() == null ? List.of() : List.copyOf(input.responsibilities()),
            LocalDate.now().toString(),
            null,
            true,
            input.canEdit(),
            input.canDelete(),
            input.canManageMembers());
    membersByProjectId.computeIfAbsent(input.projectId(), __ -> new ArrayList<>()).add(member);
    return member;
  }

  public Optional<ProjectMemberDto> updateMember(
      String memberId, com.anomalydetection.contracts.projects.UpdateProjectMemberDto input) {
    for (var entry : membersByProjectId.entrySet()) {
      var list = entry.getValue();
      for (int i = 0; i < list.size(); i++) {
        var cur = list.get(i);
        if (!cur.id().equals(memberId)) continue;

        var updated =
            new ProjectMemberDto(
                cur.id(),
                cur.projectId(),
                cur.userId(),
                cur.userName(),
                cur.email(),
                input.role(),
                input.responsibilities() == null ? List.of() : List.copyOf(input.responsibilities()),
                cur.joinedDate(),
                cur.leftDate(),
                input.isActive(),
                input.canEdit(),
                input.canDelete(),
                input.canManageMembers());
        list.set(i, updated);
        return Optional.of(updated);
      }
    }
    return Optional.empty();
  }

  public boolean removeMember(String memberId) {
    for (var entry : membersByProjectId.entrySet()) {
      var list = entry.getValue();
      if (list.removeIf(m -> m.id().equals(memberId))) {
        return true;
      }
    }
    return false;
  }
}
