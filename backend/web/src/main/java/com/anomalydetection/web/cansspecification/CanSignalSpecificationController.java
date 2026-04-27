package com.anomalydetection.web.cansspecification;

import com.anomalydetection.application.cansspecification.CanSignalSpecificationAppService;
import com.anomalydetection.contracts.cansspecification.CanSignalSpecificationDto;
import com.anomalydetection.contracts.cansspecification.CreateUpdateCanSignalSpecificationDto;
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
@RequestMapping("/api/app/can-signal-specifications")
public class CanSignalSpecificationController {

  private final CanSignalSpecificationAppService appService;

  public CanSignalSpecificationController(CanSignalSpecificationAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<CanSignalSpecificationDto> getList() {
    return appService.getList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CanSignalSpecificationDto> get(@PathVariable UUID id) {
    return appService.getById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public CanSignalSpecificationDto create(@RequestBody CreateUpdateCanSignalSpecificationDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CanSignalSpecificationDto> update(@PathVariable UUID id, @RequestBody CreateUpdateCanSignalSpecificationDto input) {
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