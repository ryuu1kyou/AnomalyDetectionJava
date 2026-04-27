package com.anomalydetection.web.detectiontemplates;

import com.anomalydetection.application.detectiontemplates.DetectionTemplateAppService;
import com.anomalydetection.contracts.detectiontemplates.CreateUpdateDetectionTemplateDto;
import com.anomalydetection.contracts.detectiontemplates.DetectionTemplateDto;
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
@RequestMapping("/api/app/detection-templates")
public class DetectionTemplatesController {

  private final DetectionTemplateAppService appService;

  public DetectionTemplatesController(DetectionTemplateAppService appService) {
    this.appService = appService;
  }

  @GetMapping
  public List<DetectionTemplateDto> getList() {
    return appService.getList();
  }

  @GetMapping("/{id}")
  public ResponseEntity<DetectionTemplateDto> get(@PathVariable UUID id) {
    return appService.getById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping
  public DetectionTemplateDto create(@RequestBody CreateUpdateDetectionTemplateDto input) {
    return appService.create(input);
  }

  @PutMapping("/{id}")
  public ResponseEntity<DetectionTemplateDto> update(
      @PathVariable UUID id, @RequestBody CreateUpdateDetectionTemplateDto input) {
    return appService.update(id, input).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable UUID id) {
    return appService.delete(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
  }
}
