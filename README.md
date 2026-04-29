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

### Backend (Spring Boot)

#### 1. ローカル MySQL/MariaDB に DB を作成 (初回のみ)

```sql
CREATE DATABASE anomaly_detection
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

#### 2. バックエンド起動

PowerShell では `./mvnw` ではなく `mvnw.cmd` を推奨します。
また multi-module 構成のため、初回は install を挟むのが確実です。

```powershell
cd .\backend

# 2-1) host の依存モジュールをローカル repo に install
.\mvnw.cmd -f .\pom.xml -pl host -am -DskipTests install

# 2-2) host を起動
.\mvnw.cmd -f .\host\pom.xml spring-boot:run "-Dspring-boot.run.profiles=local"
```

DB パスワードを `123` 以外にしたい場合は、環境変数で上書きできます。

```powershell
$env:ANOMALY_DB_PASSWORD = "your_password"
```

`http://localhost:44397/actuator/health` でヘルスチェックを確認。

## テスト

```bash
cd backend
./mvnw -B verify
```
