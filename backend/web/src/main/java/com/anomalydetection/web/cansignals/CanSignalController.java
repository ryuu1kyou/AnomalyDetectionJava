package com.anomalydetection.web.cansignals;

import com.anomalydetection.application.cansignals.CanSignalAppService;
import com.anomalydetection.contracts.cansignals.CanSignalDto;
import com.anomalydetection.contracts.cansignals.CreateUpdateCanSignalDto;
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
@RequestMapping("/api/app/can-signals")
public class CanSignalController {

  private final CanSignalAppService appService;

  public CanSignalController(CanSignalAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<CanSignalDto> getList() {
    return appService.getList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CanSignalDto> get(@PathVariable UUID id) {
    return appService.getById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public CanSignalDto create(@RequestBody CreateUpdateCanSignalDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CanSignalDto> update(@PathVariable UUID id, @RequestBody CreateUpdateCanSignalDto input) {
    return appService.update(id, input)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id)
        ? ResponseEntity.noContent().build()
        : ResponseEntity.notFound().build();
  }
}