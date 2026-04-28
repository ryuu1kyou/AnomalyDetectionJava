package com.anomalydetection.web.features;

import com.anomalydetection.application.features.FeatureManager;
import com.anomalydetection.contracts.features.FeatureDto;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/features")
public class FeaturesController {

  private final FeatureManager featureManager;

  public FeaturesController(FeatureManager featureManager) {
    this.featureManager = featureManager;
  }

  @GetMapping
  public List<FeatureDto> listAll() {
    return featureManager.listAll();
  }

  @PutMapping("/{name}")
  public ResponseEntity<Void> setGlobal(@PathVariable String name,
      @RequestBody Map<String, Boolean> body) {
    Boolean enabled = body.get("enabled");
    if (enabled == null) return ResponseEntity.badRequest().build();
    featureManager.setGlobal(name, enabled);
    return ResponseEntity.ok().build();
  }
}
