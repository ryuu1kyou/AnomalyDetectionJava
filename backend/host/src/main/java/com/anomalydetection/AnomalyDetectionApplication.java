package com.anomalydetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulithic;

/**
 * AnomalyDetection (Java port) — application entry point.
 *
 * <p>9 つの Maven サブモジュールに分割された機能を本クラスから組み立てる。
 * Spring Modulith の {@link Modulithic} で {@code com.anomalydetection} 配下を
 * モジュラーモノリスのルートとして宣言する。
 */
@SpringBootApplication
@Modulithic(systemName = "AnomalyDetection")
public class AnomalyDetectionApplication {

  public static void main(String[] args) {
    SpringApplication.run(AnomalyDetectionApplication.class, args);
  }
}