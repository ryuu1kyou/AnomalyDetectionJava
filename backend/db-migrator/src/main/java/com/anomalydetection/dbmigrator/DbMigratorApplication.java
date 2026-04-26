package com.anomalydetection.dbmigrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.anomalydetection")
public class DbMigratorApplication {

  private static final Logger log = LoggerFactory.getLogger(DbMigratorApplication.class);

  public static void main(String[] args) {
    log.info("Starting DB Migrator...");
    var ctx = SpringApplication.run(DbMigratorApplication.class, args);
    log.info("DB migration and seeding complete.");
    ctx.close();
  }
}
