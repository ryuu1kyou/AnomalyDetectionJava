package com.anomalydetection.web.settings;

import com.anomalydetection.application.settings.SettingManager;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app/settings")
public class SettingsController {

  private final SettingManager settingManager;

  public SettingsController(SettingManager settingManager) {
    this.settingManager = settingManager;
  }

  @GetMapping
  public ResponseEntity<Map<String, String>> get(@RequestParam String key) {
    Optional<String> value = settingManager.getGlobal(key);
    return value
        .map(v -> ResponseEntity.ok(Map.of("key", key, "value", v)))
        .orElse(ResponseEntity.notFound().build());
  }

  @PutMapping
  public ResponseEntity<Void> set(@RequestBody Map<String, String> body) {
    String key = body.get("key");
    String value = body.get("value");
    if (key == null || value == null) {
      return ResponseEntity.badRequest().build();
    }
    settingManager.setGlobal(key, value);
    return ResponseEntity.ok().build();
  }
}
