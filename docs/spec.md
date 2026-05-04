# AnomalyDetectionJava — アーキテクチャ仕様書

最終更新: 2026-05-04 (M9 完了時点での現状記述)

---

## 1. システム概要

`.NET 10 + ABP vNext 9.3.5` で実装された CAN 異常検出管理システムの  
**Java 21 + Spring Boot 3.3 + React 19** 完全機能パリティ移植版。

### 主要ドメイン

| ドメイン | 説明 |
|---------|------|
| CAN 信号管理 | 車載 CAN バス信号の登録・仕様・カテゴリ管理 |
| 異常検出ロジック | 検出ルールの CRUD・承認フロー・実行 |
| 検出テンプレート | 閾値等の再利用可能テンプレート |
| プロジェクト | 開発プロジェクト・マイルストーン・メンバー管理 |
| Safety (ISO 26262) | 安全トレース記録・ASIL レベル・状態遷移管理 |
| 決定台帳 (Decision Ledger) | 設計判断の記録・承認フロー |
| OEM トレーサビリティ | OEM 別承認・カスタマイズ・feature_id 横断レポート |
| ナレッジベース | 技術記事・ベストプラクティス管理 |
| 類似パターン検索 | 信号パターン類似度検索 |
| インテグレーション | 外部連携エンドポイント管理 |

### 横断機能

| 機能 | 実装 | ABP 対応 |
|------|------|---------|
| 認証・認可 | Spring Authorization Server + Spring Security | OpenIddict |
| マルチテナント | Hibernate @Filter + TenantId | IMultiTenant |
| 権限管理 | permission_grants テーブル + @PreAuthorize | Volo.Abp.PermissionManagement |
| 設定管理 | settings テーブル + Caffeine キャッシュ | Volo.Abp.SettingManagement |
| 機能フラグ | Togglz + feature_values テーブル | Volo.Abp.FeatureManagement |
| 監査ログ | Spring AOP + Hibernate Envers | AuditLog |
| バックグラウンドジョブ | Quartz + ShedLock | Volo.Abp.BackgroundJobs |
| BLOB ストレージ | Spring Content JPA | Volo.Abp.BlobStoring.Database |
| i18n | Spring MessageSource | Volo.Abp.Localization |
| ドメインイベント | Spring Modulith ApplicationEvents | Volo.Abp.EventBus |

---

## 2. 技術スタック

### バックエンド

| 領域 | バージョン |
|------|---------|
| Java | 21 LTS (Eclipse Temurin) |
| Spring Boot | 3.3.x |
| Spring Modulith | 1.x |
| Spring Authorization Server | 1.x |
| Hibernate | 6.x |
| Liquibase | 4.x |
| MapStruct | 1.5.x |
| Quartz | 2.3.x |
| ShedLock | 5.x |
| MariaDB4j (テスト用) | 3.x |

### フロントエンド

| ライブラリ | バージョン |
|---------|---------|
| React | 19.x |
| Ant Design | 6.x |
| TanStack Query | 5.x |
| Zustand | 5.x |
| react-oidc-context | 3.x |
| openapi-fetch | 0.13.x |
| Vite | 6.x |
| TypeScript | 5.x |

---

## 3. Maven マルチモジュール構成

### モジュール一覧と役割

| モジュール | 役割 | ABP 対応 |
|---------|------|---------|
| `domain-shared` | 列挙型・定数・共通例外・Localization key | Domain.Shared |
| `domain` | エンティティ・値オブジェクト・ドメインサービス・Repository I/F | Domain |
| `application-contracts` | DTO・AppService I/F・権限定数クラス | Application.Contracts |
| `application` | AppService 実装・MapStruct Mapper・@PreAuthorize | Application |
| `infrastructure` | JPA 実装・Liquibase changelog・SecurityConfiguration | EntityFrameworkCore |
| `web` | @RestController・例外ハンドラ・OpenAPI 設定 | HttpApi |
| `auth-server` | Spring Authorization Server 設定・JWT カスタマイザ | OpenIddict |
| `host` | 起動アプリ・application.yml・LocalDevSeeder | HttpApi.Host |
| `db-migrator` | 初期データ投入 CLI (SeedDataInitializer) | DbMigrator |

### 依存方向 (循環禁止 — ArchUnit で自動検証)

```
domain-shared
      ↑
   domain
      ↑
application-contracts
      ↑
  application ──────────────────────→ infrastructure
      ↑                                      ↓
      └──── web ──────────────────────────→ host
                                             ↑
                              auth-server, db-migrator も host で集約
```

**禁止されている依存:**
- `web → infrastructure` 直接参照
- `domain` からの Spring 注入 (`@Autowired` 禁止)
- 機能モジュールの `internal` パッケージへの他モジュールからのアクセス
- JPA エンティティの `infrastructure` 以外での定義
- DTO の `application-contracts` 以外での定義

---

## 4. データベース設計

### 命名規則

| 項目 | 規則 |
|------|------|
| テーブル名 | `snake_case` 複数形 (例: `can_signals`) |
| カラム名 | `snake_case` (例: `tenant_id`, `created_at`) |
| 主キー | `id` (BINARY(16)、UUID v7) |
| 外部キー | `<相手テーブル単数形>_id` |
| 文字セット | `utf8mb4` / `utf8mb4_unicode_ci` |
| 監査カラム | `created_at`, `created_by`, `last_modified_at`, `last_modified_by`, `is_deleted`, `deleted_at`, `deleted_by` |
| マルチテナント | `tenant_id BINARY(16) NULL` (NULL は OEM 共通) |

### Liquibase Changelog 一覧 (001〜026)

`infrastructure/src/main/resources/db/changelog/`

| ファイル | テーブル / 変更 |
|---------|--------------|
| 001-identity.yaml | users, roles, user_roles, user_claims, role_claims, user_logins, user_tokens |
| 002-multitenancy.yaml | tenants, tenant_connection_strings |
| 003-oauth2-authorization-server.yaml | oauth2_registered_client, oauth2_authorization, oauth2_authorization_consent |
| 004-permissions.yaml | permission_grants, permission_definitions |
| 005-settings.yaml | settings |
| 006-features.yaml | feature_values |
| 007-audit.yaml | audit_logs, audit_log_actions, entity_changes, entity_property_changes |
| 008-modulith-events.yaml | event_publication (Spring Modulith) |
| 009-shedlock.yaml | shedlock |
| 010-blob.yaml | blob_containers, blobs |
| 011-cansspecification.yaml | can_signal_specifications, can_system_categories |
| 012-cansignals.yaml | can_signals |
| 013-detectiontemplates.yaml | detection_templates |
| 014-outbox.yaml | アウトボックスパターン用テーブル |
| 015-audit-extra.yaml | 監査ログ追加カラム |
| 016-blob-storage.yaml | BLOB ストレージ拡張 |
| 017-anomalydetection.yaml | can_anomaly_detection_logics, anomaly_detection_results |
| 018-projects.yaml | projects, vehicle_phases, project_members, project_milestones |
| 019-safety.yaml | safety_trace_records |
| 020-knowledgebase.yaml | knowledge_articles |
| 021-oemtraceability.yaml | oem_approvals, oem_customizations |
| 022-integration.yaml | integration_endpoints |
| 023-traceability-keys.yaml | feature_id / decision_id / change_id 等のトレーサビリティキー列追加 |
| 024-rejected-fields.yaml | 却下理由・rejected_by 等の拡張 |
| 025-decision-ledger.yaml | decision_ledgers テーブル |
| 026-safety-trace-ext-fields.yaml | svn_rev, module_id, if_version, change_type 等 M9-A 拡張 |

### ソフトデリート

全ドメインエンティティは物理削除なし。`is_deleted = 1` + `@SQLDelete` + Hibernate `@Filter` でソフトデリート。

### マルチテナント実装

1. `TenantResolutionFilter`: `__tenant` ヘッダ → サブドメイン → JWT `tenantid` クレームの順で解決
2. Hibernate `@Filter(name="tenantFilter", condition="tenant_id = :tenantId OR tenant_id IS NULL")` を全エンティティに付与
3. `@PrePersist` フックで `tenant_id` を自動注入
4. `tenant_id IS NULL` = OEM 共通ナレッジ (全テナントが参照可)

---

## 5. 認証・認可

### OAuth2 / OIDC フロー

```
React SPA ─→ /oauth2/authorize (PKCE + code_challenge)
          ← ログイン画面 (Spring Security FormLogin)
          ─→ 認可コード
          ─→ /oauth2/token (code + code_verifier)
          ← JWT { access_token, refresh_token }
          ─→ /api/app/** (Bearer access_token)
          ← JSON レスポンス
```

- 認可サーバとリソースサーバは **同一 host モジュール内に同居** (将来切り出し可能)
- 開発時 redirect_uri: `http://localhost:5173/callback`

### JWT 構造 (access_token)

```json
{
  "iss": "http://localhost:44397",
  "sub": "<userId(UUID)>",
  "exp": 1714060000,
  "scope": "openid profile email",
  "tenantid": "<tenantId(UUID) または null>",
  "preferred_username": "admin",
  "roles": ["admin"],
  "permissions": [
    "CanSignal.Default",
    "SafetyTrace.Records.Default",
    "..."
  ]
}
```

- `permissions` クレームに **解決済み権限一覧**を含める (リソースサーバ側で DB 不要)
- 権限の DB 集計は **トークン発行時のみ** (`JwtTokenCustomizer` が実装)
- access_token 有効期間: 1 時間 / refresh_token: 30 日

### 権限システム

```
[権限定数クラス]                   [定義登録]
XxxPermissions.java           ←→  XxxPermissionDefinitionContributor.java
  └ "ModuleName.Action.Verb"        → permission_definitions テーブルに起動時同期

[権限付与]                         [チェック]
LocalDevSeeder (local profile)     @PreAuthorize("hasAuthority('...')")
SeedDataInitializer (migrator)  ←→ usePermissions() / RequirePermission (フロント)
  └ permission_grants テーブル
```

権限文字列の命名規則: `{Module}.{Resource}.{Action}`  
例: `SafetyTrace.Records.Default`, `OemTraceability.Approvals.Create`

---

## 6. Spring プロファイル

| プロファイル | 用途 | 主な差分 |
|------------|------|---------|
| `local` | ローカル開発 | LocalDevSeeder 実行、SQL ログ ON、CORS 寛容 |
| `test` | 自動テスト | MariaDB4j 接続、Liquibase 自動実行、ログ DEBUG |
| `prod` | 本番 (将来) | TLS 必須、SQL ログ OFF、機密は環境変数 |

`application.yml` ベース + `application-{profile}.yml` で差分のみ記述。

---

## 7. フロントエンドアーキテクチャ

### ルーティングと認可

```typescript
// router.tsx の基本パターン
{ 
  path: '/some-domain',
  element: (
    <RequirePermission permission={SomeDomainPermissions.DEFAULT}>
      <SomeDomainPage />
    </RequirePermission>
  )
}
```

`RequirePermission` は権限なしの場合 `null` を返す (ページが空になる)。  
権限が JWT に含まれていない場合も同様 → LocalDevSeeder の更新が必要。

### データフェッチパターン

```typescript
// TanStack Query + apiFetch の標準パターン
const KEY = ['domain-records']
const BASE = '/app/domain-records'

function useDomainRecords() {
  return useQuery({ queryKey: KEY, queryFn: () => apiFetch<T[]>(BASE) })
}

function useCreateRecord() {
  const qc = useQueryClient()
  return useMutation({
    mutationFn: (input: CreateInput) =>
      apiFetch<T>(BASE, { method: 'POST', body: JSON.stringify(input) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: KEY }),
  })
}
```

### 権限定数の同期

`frontend/src/shared/auth/permissions.ts` はバックエンドの権限定数クラスと手動同期。  
`api:generate` を実行しても権限定数は更新されないため、別途手動で追加が必要。

---

## 8. CI/CD (GitHub Actions)

`.github/workflows/` に以下が整備済み:

| ワークフロー | トリガー | 内容 |
|------------|---------|------|
| `backend-ci.yml` | push/PR | `./mvnw -B verify` (全テスト + ArchUnit) |
| `frontend-ci.yml` | push/PR | `npm run lint` + `npm test` + `npm run build` |
| `codeql.yml` | push/PR/schedule | CodeQL セキュリティ解析 |

Dependabot: `dependabot.yml` で Maven + npm の依存更新を自動 PR。

---

## 9. ローカル開発フロー

```powershell
# 1. MySQL 起動 (Windows サービス)
# Start-Service MySQL80

# 2. DB 作成 (初回のみ)
# CREATE DATABASE anomaly_detection CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 3. バックエンドビルド (新クラス追加後は必ず clean)
cd backend
.\mvnw.cmd clean install -DskipTests

# 4. バックエンド起動
.\mvnw.cmd -pl host spring-boot:run "-Dspring-boot.run.profiles=local"

# 5. フロントエンド起動 (別ターミナル)
cd frontend
npm install    # 初回のみ
npm run dev

# 6. テスト
cd backend
.\mvnw.cmd -B verify

cd frontend
npm test
```

起動後: `http://localhost:5173` → admin / Admin@1234 でログイン  
Swagger: `http://localhost:44397/swagger-ui.html`
