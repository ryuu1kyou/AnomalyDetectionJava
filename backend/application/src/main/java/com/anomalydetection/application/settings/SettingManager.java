package com.anomalydetection.application.settings;

import com.anomalydetection.contracts.settings.SettingProvider;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class SettingManager {

  private final SettingProvider settingProvider;

  public SettingManager(SettingProvider settingProvider) {
    this.settingProvider = settingProvider;
  }

  public Optional<String> getGlobal(String name) {
    return settingProvider.getGlobal(name);
  }

  public void setGlobal(String name, String value) {
    settingProvider.setGlobal(name, value);
  }
}