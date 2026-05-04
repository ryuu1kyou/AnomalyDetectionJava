package com.anomalydetection.application.safety;

import com.anomalydetection.contracts.safety.CreateDecisionLedgerDto;
import com.anomalydetection.contracts.safety.DecisionLedgerDto;
import com.anomalydetection.contracts.safety.DecisionLedgerPermissions;
import com.anomalydetection.contracts.safety.UpdateDecisionLedgerDto;
import com.anomalydetection.domain.safety.DecisionLedger;
import com.anomalydetection.domain.safety.DecisionLedgerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DecisionLedgerAppService {

  private final DecisionLedgerRepository repo;
  private final ObjectMapper objectMapper;

  public DecisionLedgerAppService(DecisionLedgerRepository repo, ObjectMapper objectMapper) {
    this.repo = repo;
    this.objectMapper = objectMapper;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.DEFAULT + "')")
  public List<DecisionLedgerDto> getAll() {
    return repo.findAll().stream().map(this::toDto).toList();
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.DEFAULT + "')")
  public Optional<DecisionLedgerDto> getById(UUID id) {
    return repo.findById(id).map(this::toDto);
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.DEFAULT + "')")
  public Optional<DecisionLedgerDto> getByDecisionId(String decisionId) {
    return repo.findByDecisionId(decisionId).map(this::toDto);
  }

  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.CREATE + "')")
  public DecisionLedgerDto create(CreateDecisionLedgerDto input) {
    if (repo.existsByDecisionId(input.decisionId()))
      throw new IllegalArgumentException(
          "A decision ledger entry already exists for decision_id: " + input.decisionId());
    var entry = new DecisionLedger(UUID.randomUUID(), input.decisionId(), input.whatDecided());
    entry.setWhyDecided(input.whyDecided());
    entry.setAssumptions(input.assumptions());
    entry.setConstraintsText(input.constraintsText());
    entry.setRelatedFeatureIds(toJson(input.relatedFeatureIds()));
    entry.setRelatedModuleIds(toJson(input.relatedModuleIds()));
    return toDto(repo.save(entry));
  }

  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.EDIT + "')")
  public Optional<DecisionLedgerDto> update(UUID id, UpdateDecisionLedgerDto input) {
    return repo.findById(id).map(e -> {
      if (input.whatDecided() != null) e.setWhatDecided(input.whatDecided());
      if (input.whyDecided() != null) e.setWhyDecided(input.whyDecided());
      if (input.assumptions() != null) e.setAssumptions(input.assumptions());
      if (input.constraintsText() != null) e.setConstraintsText(input.constraintsText());
      if (input.relatedFeatureIds() != null) e.setRelatedFeatureIds(toJson(input.relatedFeatureIds()));
      if (input.relatedModuleIds() != null) e.setRelatedModuleIds(toJson(input.relatedModuleIds()));
      return toDto(repo.save(e));
    });
  }

  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.APPROVE + "')")
  public Optional<DecisionLedgerDto> activate(UUID id) {
    return repo.findById(id).map(e -> {
      e.activate(null);
      return toDto(repo.save(e));
    });
  }

  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.APPROVE + "')")
  public Optional<DecisionLedgerDto> supersede(UUID id) {
    return repo.findById(id).map(e -> {
      e.supersede();
      return toDto(repo.save(e));
    });
  }

  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.DELETE + "')")
  public boolean delete(UUID id) {
    if (!repo.existsById(id)) return false;
    repo.deleteById(id);
    return true;
  }

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + DecisionLedgerPermissions.DEFAULT + "')")
  public List<DecisionLedgerDto> findByFeatureId(String featureId) {
    return repo.findAll().stream()
        .filter(e -> {
          var ids = fromJson(e.getRelatedFeatureIds());
          return ids.contains(featureId);
        })
        .map(this::toDto)
        .toList();
  }

  DecisionLedgerDto toDto(DecisionLedger e) {
    return new DecisionLedgerDto(
        e.getId().toString(),
        e.getDecisionId(),
        e.getWhatDecided(),
        e.getWhyDecided(),
        e.getAssumptions(),
        e.getConstraintsText(),
        fromJson(e.getRelatedFeatureIds()),
        fromJson(e.getRelatedModuleIds()),
        e.getStatus(),
        e.getApprovedBy() != null ? e.getApprovedBy().toString() : null,
        e.getApprovedAt() != null ? e.getApprovedAt().toString() : null);
  }

  private List<String> fromJson(String json) {
    if (json == null || json.isBlank()) return List.of();
    try {
      return objectMapper.readValue(json, new TypeReference<List<String>>() {});
    } catch (Exception ex) {
      return List.of();
    }
  }

  private String toJson(List<String> list) {
    if (list == null || list.isEmpty()) return "[]";
    try {
      return objectMapper.writeValueAsString(list);
    } catch (Exception ex) {
      return "[]";
    }
  }
}
