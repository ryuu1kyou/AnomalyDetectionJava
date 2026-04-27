package com.anomalydetection.application.projects;

import com.anomalydetection.contracts.projects.CreateProjectDto;
import com.anomalydetection.contracts.projects.CreateProjectMemberDto;
import com.anomalydetection.contracts.projects.CreateProjectMilestoneDto;
import com.anomalydetection.contracts.projects.GetProjectsInputDto;
import com.anomalydetection.contracts.projects.MilestoneStatus;
import com.anomalydetection.contracts.projects.PagedResultDto;
import com.anomalydetection.contracts.projects.ProjectDto;
import com.anomalydetection.contracts.projects.ProjectMemberDto;
import com.anomalydetection.contracts.projects.ProjectMilestoneDto;
import com.anomalydetection.contracts.projects.ProjectPriority;
import com.anomalydetection.contracts.projects.ProjectStatus;
import com.anomalydetection.contracts.projects.UpdateProjectDto;
import com.anomalydetection.contracts.projects.UpdateProjectMemberDto;
import com.anomalydetection.contracts.projects.UpdateProjectMilestoneDto;
import com.anomalydetection.domain.projects.AnomalyDetectionProject;
import com.anomalydetection.domain.projects.AnomalyDetectionProjectRepository;
import com.anomalydetection.domain.projects.ProjectMember;
import com.anomalydetection.domain.projects.ProjectMemberRepository;
import com.anomalydetection.domain.projects.ProjectMilestone;
import com.anomalydetection.domain.projects.ProjectMilestoneRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProjectsAppService {

  private final AnomalyDetectionProjectRepository projectRepo;
  private final ProjectMilestoneRepository milestoneRepo;
  private final ProjectMemberRepository memberRepo;
  private final ObjectMapper objectMapper;

  public ProjectsAppService(
      AnomalyDetectionProjectRepository projectRepo,
      ProjectMilestoneRepository milestoneRepo,
      ProjectMemberRepository memberRepo,
      ObjectMapper objectMapper) {
    this.projectRepo = projectRepo;
    this.milestoneRepo = milestoneRepo;
    this.memberRepo = memberRepo;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  public PagedResultDto<ProjectDto> getList(GetProjectsInputDto input) {
    final String filter = input.filter() == null ? "" : input.filter().trim().toLowerCase();

    var filtered =
        projectRepo.findAll().stream()
            .filter(
                p -> {
                  if (!filter.isBlank()) {
                    var hay = (p.getProjectCode() + " " + p.getName()).toLowerCase();
                    if (!hay.contains(filter)) return false;
                  }
                  if (input.status() != null) {
                    var s = ProjectStatus.fromCode(input.status());
                    if (!p.getStatus().equals(s.name())) return false;
                  }
                  if (input.priority() != null) {
                    var pr = ProjectPriority.fromCode(input.priority());
                    if (!p.getPriority().equals(pr.name())) return false;
                  }
                  if (input.vehicleModel() != null && !input.vehicleModel().isBlank()) {
                    if (p.getVehicleModel() == null
                        || !p.getVehicleModel()
                            .toLowerCase()
                            .contains(input.vehicleModel().toLowerCase())) return false;
                  }
                  if (input.primarySystem() != null && !input.primarySystem().isBlank()) {
                    if (p.getPrimarySystem() == null
                        || !p.getPrimarySystem()
                            .toLowerCase()
                            .contains(input.primarySystem().toLowerCase())) return false;
                  }
                  return true;
                })
            .sorted(Comparator.comparing(AnomalyDetectionProject::getProjectCode,
                String.CASE_INSENSITIVE_ORDER))
            .toList();

    int skip = Optional.ofNullable(input.skipCount()).orElse(0);
    int take = Optional.ofNullable(input.maxResultCount()).orElse(10);
    if (skip < 0) skip = 0;
    if (take <= 0) take = 10;

    var page = filtered.stream().skip(skip).limit(take).map(this::toDto).toList();
    return PagedResultDto.of(page, filtered.size());
  }

  @Transactional(readOnly = true)
  public Optional<ProjectDto> getById(String id) {
    try {
      return projectRepo.findById(UUID.fromString(id)).map(this::toDto);
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public ProjectDto create(CreateProjectDto input) {
    var project = new AnomalyDetectionProject(
        UUID.randomUUID(),
        input.projectCode(),
        input.projectName(),
        input.status() != null ? input.status().name() : ProjectStatus.Planning.name(),
        input.priority() != null ? input.priority().name() : ProjectPriority.Medium.name());
    project.setDescription(input.description());
    project.setVehicleModel(input.vehicleModel());
    project.setModelYear(input.modelYear());
    project.setPlatform(input.platform());
    project.setPrimarySystem(input.primarySystem());
    project.setTargetMarket(input.targetMarket());
    project.setOemCode(input.oemCode());
    project.setOemName(input.oemName());
    project.setNotes(input.notes());
    if (input.startDate() != null) project.setStartDate(LocalDate.parse(input.startDate()));
    if (input.plannedEndDate() != null)
      project.setPlannedEndDate(LocalDate.parse(input.plannedEndDate()));
    return toDto(projectRepo.save(project));
  }

  public Optional<ProjectDto> update(String id, UpdateProjectDto input) {
    return projectRepo.findById(UUID.fromString(id)).map(p -> {
      p.setName(input.projectName());
      p.setDescription(input.description());
      p.setVehicleModel(input.vehicleModel());
      p.setModelYear(input.modelYear());
      p.setPlatform(input.platform());
      p.setPrimarySystem(input.primarySystem());
      p.setTargetMarket(input.targetMarket());
      p.setOemCode(input.oemCode());
      p.setOemName(input.oemName());
      p.setNotes(input.notes());
      p.setProgressPercentage(input.progressPercentage());
      if (input.status() != null) p.setStatus(input.status().name());
      if (input.priority() != null) p.setPriority(input.priority().name());
      if (input.startDate() != null) p.setStartDate(LocalDate.parse(input.startDate()));
      if (input.plannedEndDate() != null) p.setPlannedEndDate(LocalDate.parse(input.plannedEndDate()));
      if (input.actualEndDate() != null) p.setActualEndDate(LocalDate.parse(input.actualEndDate()));
      return toDto(projectRepo.save(p));
    });
  }

  public boolean delete(String id) {
    return projectRepo.findById(UUID.fromString(id)).map(p -> {
      p.softDelete(null);
      projectRepo.save(p);
      return true;
    }).orElse(false);
  }

  // --- Milestones ---

  @Transactional(readOnly = true)
  public List<ProjectMilestoneDto> getMilestones(String projectId) {
    UUID pid = UUID.fromString(projectId);
    return milestoneRepo.findAllByProjectId(pid).stream()
        .sorted(Comparator.comparing(m -> m.getPlannedDate() != null ? m.getPlannedDate().toString() : ""))
        .map(this::toMilestoneDto)
        .toList();
  }

  public ProjectMilestoneDto createMilestone(CreateProjectMilestoneDto input) {
    var milestone = new ProjectMilestone(
        UUID.randomUUID(), UUID.fromString(input.projectId()), input.name());
    milestone.setDescription(input.description());
    if (input.plannedDate() != null) milestone.setPlannedDate(LocalDate.parse(input.plannedDate()));
    milestone.setDependencies(toJsonList(input.dependencies()));
    milestone.setDeliverables(toJsonList(input.deliverables()));
    return toMilestoneDto(milestoneRepo.save(milestone));
  }

  public Optional<ProjectMilestoneDto> updateMilestone(String milestoneId, UpdateProjectMilestoneDto input) {
    return milestoneRepo.findById(UUID.fromString(milestoneId)).map(m -> {
      m.setName(input.name());
      m.setDescription(input.description());
      if (input.plannedDate() != null) m.setPlannedDate(LocalDate.parse(input.plannedDate()));
      if (input.actualDate() != null) m.setActualDate(LocalDate.parse(input.actualDate()));
      if (input.status() != null) m.setStatus(input.status().name());
      m.setProgressPercentage(input.progressPercentage());
      m.setDependencies(toJsonList(input.dependencies()));
      m.setDeliverables(toJsonList(input.deliverables()));
      return toMilestoneDto(milestoneRepo.save(m));
    });
  }

  public boolean deleteMilestone(String milestoneId) {
    return milestoneRepo.findById(UUID.fromString(milestoneId)).map(m -> {
      m.softDelete(null);
      milestoneRepo.save(m);
      return true;
    }).orElse(false);
  }

  public Optional<ProjectMilestoneDto> completeMilestone(String milestoneId) {
    return milestoneRepo.findById(UUID.fromString(milestoneId)).map(m -> {
      m.setStatus(MilestoneStatus.Completed.name());
      m.setActualDate(LocalDate.now());
      m.setProgressPercentage(100);
      return toMilestoneDto(milestoneRepo.save(m));
    });
  }

  // --- Members ---

  @Transactional(readOnly = true)
  public List<ProjectMemberDto> getMembers(String projectId) {
    UUID pid = UUID.fromString(projectId);
    return memberRepo.findAllByProjectId(pid).stream()
        .sorted(Comparator.comparing(m -> m.getUserName() != null ? m.getUserName() : ""))
        .map(this::toMemberDto)
        .toList();
  }

  public ProjectMemberDto addMember(CreateProjectMemberDto input) {
    UUID userId;
    try {
      userId = UUID.fromString(input.userId());
    } catch (IllegalArgumentException e) {
      userId = UUID.randomUUID();
    }
    var member = new ProjectMember(
        UUID.randomUUID(), UUID.fromString(input.projectId()), userId, input.role());
    member.setUserName(input.userId());
    member.setEmail(input.userId() + "@example.com");
    member.setResponsibilities(toJsonList(input.responsibilities()));
    member.setCanEdit(input.canEdit());
    member.setCanDelete(input.canDelete());
    member.setCanManageMembers(input.canManageMembers());
    return toMemberDto(memberRepo.save(member));
  }

  public Optional<ProjectMemberDto> updateMember(String memberId, UpdateProjectMemberDto input) {
    return memberRepo.findById(UUID.fromString(memberId)).map(m -> {
      m.setRole(input.role());
      m.setResponsibilities(toJsonList(input.responsibilities()));
      m.setCanEdit(input.canEdit());
      m.setCanDelete(input.canDelete());
      m.setCanManageMembers(input.canManageMembers());
      m.setActive(input.isActive());
      if (!input.isActive()) m.setLeftDate(LocalDate.now());
      return toMemberDto(memberRepo.save(m));
    });
  }

  public boolean removeMember(String memberId) {
    return memberRepo.findById(UUID.fromString(memberId)).map(m -> {
      m.softDelete(null);
      memberRepo.save(m);
      return true;
    }).orElse(false);
  }

  // --- Mappers ---

  private ProjectDto toDto(AnomalyDetectionProject p) {
    return new ProjectDto(
        p.getId().toString(),
        p.getProjectCode(),
        p.getName(),
        p.getDescription(),
        p.getVehicleModel(),
        p.getModelYear(),
        p.getPlatform(),
        p.getPrimarySystem(),
        p.getTargetMarket(),
        parseStatus(p.getStatus()),
        parsePriority(p.getPriority()),
        p.getStartDate() != null ? p.getStartDate().toString() : null,
        p.getPlannedEndDate() != null ? p.getPlannedEndDate().toString() : null,
        p.getActualEndDate() != null ? p.getActualEndDate().toString() : null,
        p.getProgressPercentage(),
        p.getOemCode(),
        p.getOemName(),
        p.getTotalDetectionLogics(),
        p.getTotalCanSignals(),
        p.getTotalAnomalies(),
        p.getResolvedAnomalies());
  }

  private ProjectMilestoneDto toMilestoneDto(ProjectMilestone m) {
    return new ProjectMilestoneDto(
        m.getId().toString(),
        m.getProjectId().toString(),
        m.getName(),
        m.getDescription(),
        m.getPlannedDate() != null ? m.getPlannedDate().toString() : null,
        m.getActualDate() != null ? m.getActualDate().toString() : null,
        parseMilestoneStatus(m.getStatus()),
        m.getProgressPercentage(),
        parseJsonList(m.getDependencies()),
        parseJsonList(m.getDeliverables()));
  }

  private ProjectMemberDto toMemberDto(ProjectMember m) {
    return new ProjectMemberDto(
        m.getId().toString(),
        m.getProjectId().toString(),
        m.getUserId() != null ? m.getUserId().toString() : null,
        m.getUserName(),
        m.getEmail(),
        m.getRole(),
        parseJsonList(m.getResponsibilities()),
        m.getJoinedDate() != null ? m.getJoinedDate().toString() : null,
        m.getLeftDate() != null ? m.getLeftDate().toString() : null,
        m.isActive(),
        m.isCanEdit(),
        m.isCanDelete(),
        m.isCanManageMembers());
  }

  private ProjectStatus parseStatus(String name) {
    if (name == null) return ProjectStatus.Planning;
    try { return ProjectStatus.valueOf(name); } catch (Exception e) { return ProjectStatus.Planning; }
  }

  private ProjectPriority parsePriority(String name) {
    if (name == null) return ProjectPriority.Medium;
    try { return ProjectPriority.valueOf(name); } catch (Exception e) { return ProjectPriority.Medium; }
  }

  private MilestoneStatus parseMilestoneStatus(String name) {
    if (name == null) return MilestoneStatus.NotStarted;
    try { return MilestoneStatus.valueOf(name); } catch (Exception e) { return MilestoneStatus.NotStarted; }
  }

  private List<String> parseJsonList(String json) {
    if (json == null || json.isBlank()) return List.of();
    try {
      return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (Exception e) {
      return List.of();
    }
  }

  private String toJsonList(List<String> list) {
    if (list == null || list.isEmpty()) return "[]";
    try {
      return objectMapper.writeValueAsString(list);
    } catch (Exception e) {
      return "[]";
    }
  }
}
