package com.anomalydetection.web.cansspecification;

import com.anomalydetection.application.cansspecification.CanSystemCategoryAppService;
import com.anomalydetection.contracts.cansspecification.CanSystemCategoryDto;
import com.anomalydetection.contracts.cansspecification.CreateUpdateCanSystemCategoryDto;
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
@RequestMapping("/api/app/can-system-categories")
public class CanSystemCategoryController {

  private final CanSystemCategoryAppService appService;

  public CanSystemCategoryController(CanSystemCategoryAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<CanSystemCategoryDto> getList() {
    return appService.getList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<CanSystemCategoryDto> get(@PathVariable UUID id) {
    return appService.getById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping
  public CanSystemCategoryDto create(@RequestBody CreateUpdateCanSystemCategoryDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<CanSystemCategoryDto> update(@PathVariable UUID id, @RequestBody CreateUpdateCanSystemCategoryDto input) {
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