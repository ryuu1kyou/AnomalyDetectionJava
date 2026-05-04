package com.anomalydetection.web.safety;

import com.anomalydetection.application.safety.DecisionLedgerAppService;
import com.anomalydetection.contracts.safety.CreateDecisionLedgerDto;
import com.anomalydetection.contracts.safety.DecisionLedgerDto;
import com.anomalydetection.contracts.safety.UpdateDecisionLedgerDto;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/decision-ledger")
public class DecisionLedgerController {

  private final DecisionLedgerAppService appService;

  public DecisionLedgerController(DecisionLedgerAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<DecisionLedgerDto> getAll() {
    return appService.getAll();
  }

  @GetMapping("/{id}")
  public ResponseEntity<DecisionLedgerDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public DecisionLedgerDto create(@RequestBody CreateDecisionLedgerDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<DecisionLedgerDto> update(
      @PathVariable UUID id, @RequestBody UpdateDecisionLedgerDto input) {
    return appService.update(id, input).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/activate")
  public ResponseEntity<DecisionLedgerDto> activate(@PathVariable UUID id) {
    return appService.activate(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/{id}/supersede")
  public ResponseEntity<DecisionLedgerDto> supersede(@PathVariable UUID id) {
    return appService.supersede(id).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id) ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}
