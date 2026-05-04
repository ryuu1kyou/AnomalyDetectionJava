# AnomalyDetection Java 版

[![Backend CI](https://github.com/ryuu1kyou/AnomalyDetectionJava/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/ryuu1kyou/AnomalyDetectionJava/actions/workflows/backend-ci.yml)
[![Frontend CI](https://github.com/ryuu1kyou/AnomalyDetectionJava/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/ryuu1kyou/AnomalyDetectionJava/actions/workflows/frontend-ci.yml)
[![CodeQL](https://github.com/ryuu1kyou/AnomalyDetectionJava/actions/workflows/codeql.yml/badge.svg)](https://github.com/ryuu1kyou/AnomalyDetectionJava/actions/workflows/codeql.yml)

`.NET 10 + ABP vNext 9.3.5` で実装された CAN 異常検出管理システムを  
**Java 21 + Spring Boot 3.3 + React 19 + MySQL 8** へ完全機能パリティ移植したリポジトリ。

---

## 前提条件

| ツール | バージョン | 備考 |
|-------|-----------|------|
| Java JDK | 21 LTS (Eclipse Temurin) | `JAVA_HOME` 設定必須 |
| Maven | 3.9.x | Wrapper `mvnw` / `mvnw.cmd` 同梱 |
| Node.js | 20.x LTS | フロントエンド開発時 |
| MySQL | 8.x | ホスト OS に直接インストール (Docker 不使用) |

---

## クイックスタート

### 1. データベース作成 (初回のみ)

```sql
CREATE DATABASE anomaly_detection
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

接続先: `localhost:3306` / ユーザ: `root` / パスワード: `123` (デフォルト)

### 2. バックエンド起動

```powershell
cd backend

# 初回 or 新クラス追加後は必ず clean install
.\mvnw.cmd clean install -DskipTests

# local プロファイルで起動
.\mvnw.cmd -pl host spring-boot:run "-Dspring-boot.run.profiles=local"
```

- ヘルスチェック: `http://localhost:44397/actuator/health`
- Swagger UI: `http://localhost:44397/swagger-ui.html`

### 3. フロントエンド起動

```powershell
cd frontend
npm install        # 初回のみ
npm run dev
```

ブラウザ: `http://localhost:5173`  
初期ログイン: **admin** / **Admin@1234**

> **重要**: バックエンド再起動後は必ず **ログアウト → 再ログイン** してください。  
> 権限情報は JWT に含まれるため、古いトークンでは新規権限が反映されません。

---

## フォルダ構成

```
AnomalyDetectionJava/
├── backend/
│   ├── pom.xml                      # 親 POM (Java 21, Spring Boot 3.3 BOM)
│   ├── domain-shared/               # 列挙型・定数・共通例外
│   ├── domain/                      # エンティティ・値オブジェクト・ドメインサービス
│   ├── application-contracts/       # DTO・AppService I/F・権限定数
│   ├── application/                 # ユースケース実装・MapStruct・認可
│   ├── infrastructure/              # JPA・Liquibase・Hibernate Filter・セキュリティ設定
│   │   └── resources/db/changelog/  # Liquibase 差分 (001〜026)
│   ├── web/                         # REST コントローラ・例外ハンドラ・OpenAPI
│   ├── auth-server/                 # Spring Authorization Server (OAuth2/OIDC)
│   ├── host/                        # 起動アプリ・プロファイル設定・LocalDevSeeder
│   └── db-migrator/                 # 初期データ投入 CLI (SeedDataInitializer)
├── frontend/
│   └── src/
│       ├── app/                     # ルーティング・レイアウト・AuthGuard
│       ├── modules/                 # 機能ページ (各ドメイン)
│       ├── shared/                  # 共通 hook・apiFetch・権限ユーティリティ
│       └── api/                     # OpenAPI 自動生成型定義 (schema.d.ts)
├── docs/
│   ├── spec.md                      # 詳細アーキテクチャ仕様
│   ├── history.md                   # マイルストーン遷移記録 (M0〜M9)
│   └── traceability.md              # ISO 26262 トレーサビリティ運用規則
├── CLAUDE.md                        # AI (Claude Code) 向け開発コンテキスト
└── README.md                        # 本ファイル
```

### モジュール依存方向 (循環禁止)

```
domain-shared
      ↑
   domain
      ↑
application-contracts
      ↑
  application ──→ infrastructure ──→ host
      ↑                                ↑
      └──── web ────────────────────────┘
                                       ↑
                        auth-server, db-migrator も host が集約
```

ArchUnit テストでビルド時に自動検証。

---

## 機能モジュール一覧

| ページ | パス | 主要機能 |
|--------|------|---------|
| CAN 信号管理 | `/can-signals` | 信号 CRUD・仕様管理 |
| 異常検出ロジック | `/anomaly-detection` | ロジック CRUD・承認フロー |
| 検出テンプレート | `/detection-templates` | 閾値テンプレート管理 |
| プロジェクト管理 | `/projects` | プロジェクト・マイルストーン管理 |
| Safety (ISO 26262) | `/safety` | 安全トレース記録・ASIL 管理・OEM 突合 |
| OEM トレーサビリティ | `/oem-traceability` | OEM 別承認・カスタマイズ管理 |
| ナレッジベース | `/knowledge-base` | 技術記事・ベストプラクティス |
| 類似パターン検索 | `/similar-pattern-search` | 信号パターン類似度検索 |
| インテグレーション | `/integration` | 外部連携エンドポイント管理 |
| 監査ログ | `/audit-log` | 操作ログ閲覧 |
| 設定 | `/settings` | システム設定管理 |
| 機能フラグ | `/features` | Togglz ベースの機能管理 |
| 権限管理 | `/permissions` | ロール・権限付与管理 |

---

## API エンドポイント概要

ベース URL: `http://localhost:44397/api/app/`  
認証: Bearer JWT (Authorization Code + PKCE フロー)

| ドメイン | パス | 備考 |
|---------|------|------|
| CAN 信号 | `can-signals` | CRUD |
| CAN 信号仕様 | `can-signal-specs` | CRUD |
| 検出テンプレート | `detection-templates` | CRUD |
| 異常検出ロジック | `anomaly-detection/logics` | CRUD + submit / approve |
| プロジェクト | `projects` | CRUD + メンバー / マイルストーン |
| Safety レコード | `safety-trace-records` | CRUD + submit |
| 決定台帳 | `decision-ledger` | CRUD + approve |
| V&V 検証 | `safety-trace-records/{id}/verifications` | 追加・一覧 |
| V&V 妥当性確認 | `safety-trace-records/{id}/validations` | 追加・一覧 |
| OEM 承認 | `oem-traceability/approvals` | CRUD + approve / reject |
| OEM カスタマイズ | `oem-traceability/customizations` | CRUD |
| OEM レポート | `oem-traceability-report/by-feature` | feature_id 別横断レポート |
| トレーサビリティ突合 | `traceability/feature/{featureId}` | Safety + OEM 横断 |
| ナレッジベース | `knowledge-base` | CRUD + publish |
| 類似パターン検索 | `similar-pattern-search` | 検索 |
| インテグレーション | `integration` | CRUD |
| 監査ログ | `audit-logs` | 一覧 |
| 設定 | `settings` | 読取 / 更新 |
| 機能フラグ | `features` | 読取 / 更新 |
| 権限 | `permissions` | 定義 / 付与 / 剥奪 |

完全な仕様は Swagger UI (`/swagger-ui.html`) を参照。

---

## テスト実行

```powershell
# バックエンド全テスト (MariaDB4j 組み込み、Docker 不要)
cd backend
.\mvnw.cmd -B verify

# フロントエンドユニットテスト
cd frontend
npm test

# API 型定義再生成 (バックエンド起動中に実行)
npm run api:generate
```

テスト数: **87 件** (2026-05-04 時点)  
テストは MariaDB4j (組み込み MariaDB) で実行されるため、ローカル MySQL は不要。

---

## 環境変数

ローカルは `backend/host/src/main/resources/application-local.yml` で設定済み。  
変更が必要な場合のみ以下の環境変数で上書きする。

| 変数 | デフォルト | 用途 |
|------|-----------|------|
| `ANOMALY_DB_PASSWORD` | `123` | MySQL 接続パスワード |
| `ANOMALY_DB_URL` | `jdbc:mysql://localhost:3306/anomaly_detection` | JDBC URL |
| `ADMIN_PASSWORD` | `Admin@1234` | 初期管理者パスワード (LocalDevSeeder) |

---

## Todo / ロードマップ

### 近期対応候補

- [ ] フロントエンド: Decision Ledger ページ実装 (`/safety` タブ追加)
- [ ] フロントエンド: OEM トレーサビリティレポートページ
- [ ] フロントエンド: V&V 記録 UI (検証・妥当性確認)
- [ ] OpenAPI 自動生成クライアント本番化 (`npm run api:generate` → `src/api/schema.d.ts` 利用)

### 将来課題

- [ ] Docker / docker-compose 化
- [ ] 本番デプロイ構成 (Nginx + TLS + サービス化)
- [ ] Redis 分散キャッシュ切替 (Spring Cache 抽象で差替可)
- [ ] S3 互換 BLOB ストレージ切替
- [ ] Kafka / RabbitMQ メッセージブローカー連携
- [ ] Playwright E2E テスト整備
- [ ] JMH パフォーマンスベンチマーク整備
- [ ] auth-server の別プロセス分離 (現状: host に同居)

---

## 参照ドキュメント

| ドキュメント | 内容 |
|------------|------|
| [docs/spec.md](docs/spec.md) | アーキテクチャ詳細・DB 設計・認証フロー |
| [docs/history.md](docs/history.md) | M0〜M9 マイルストーン遷移記録 |
| [docs/traceability.md](docs/traceability.md) | ISO 26262 トレーサビリティ運用規則 |
| [CLAUDE.md](CLAUDE.md) | Claude Code 向け開発コンテキスト・引き継ぎ情報 |
| [移植元 .NET 版](../AnomalyDetection/README.md) | 参照元システム |
