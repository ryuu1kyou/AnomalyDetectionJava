# AnomalyDetection (Java 移植版)

`.NET 10 + ABP vNext 9.3.5` で実装された CAN 異常検出管理システムを
**Java 21 + Spring Boot 3.3 + React** に完全機能パリティ移植したリポジトリ。

詳細は以下を参照:

- プロジェクトメモ (Claude Code 用): [CLAUDE.md](CLAUDE.md)
- 設計書: [docs/superpowers/specs/2026-04-25-anomaly-detection-java-port-design.md](docs/superpowers/specs/2026-04-25-anomaly-detection-java-port-design.md)
- 実装計画: [docs/superpowers/plans/](docs/superpowers/plans/)

## 開発前提

- Java 21 (Eclipse Temurin 推奨)
- Maven 3.9.x (Wrapper `./mvnw` 同梱、別途インストール不要)
- Node.js 20.x (フロントエンド開発時)
- MySQL 8.x (PC に直接インストール、Docker 不使用)

## ローカル起動

```bash
cd backend

# 1. ローカル MySQL に DB を作成 (初回のみ)
mysql -u root -p -e "CREATE DATABASE anomaly_detection CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;"

# 2. バックエンド起動
./mvnw -pl host -am spring-boot:run -Dspring-boot.run.profiles=local
```

`http://localhost:44397/actuator/health` でヘルスチェックを確認。

## テスト

```bash
cd backend
./mvnw -B verify
```
