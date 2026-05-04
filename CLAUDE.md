# AnomalyDetectionJava — Claude Code 向け開発コンテキスト

このファイルは **Claude Code (AI) が開発を引き継ぐ際の参照情報**です。  
プロジェクトの現状・設計判断・落とし穴・次の開発ポイントをまとめます。

---

## 1. プロジェクト概要

`.NET 10 + ABP vNext 9.3.5 + Angular` で実装された CAN 異常検出管理システムを  
**Java 21 + Spring Boot 3.3 + React 19** に完全機能パリティ移植するリポジトリ。

- 自動車業界 (OEM 複数社) 向けマルチテナント Web アプリ
- ISO 26262 機能安全トレーサビリティを維持
- 個人開発 (1 名)、Windows 11 + ホスト直接実行 (Docker 不使用)
- 将来的に独立 GitHub リポジトリとして公開予定

---

## 2. 現在の実装状態

**全マイルストーン完了 (M0〜M9)** — 2026-05-04 時点

| マイルストーン | 内容 | 状態 |
|--------------|------|------|
| M0 | Maven マルチモジュール基盤・Liquibase・ArchUnit | 完了 |
| M1 | Identity + Multi-Tenancy | 完了 |
| M2 | Spring Authorization Server (OAuth2/OIDC) | 完了 |
| M3 | 横断機能 (Permissions / Settings / Features / Audit / Jobs / BLOB / i18n) | 完了 |
| M4 | コアドメイン 10 モジュール REST API | 完了 |
| M5 | フロントエンド基盤 (Zustand / TanStack Query / react-oidc-context) | 完了 |
| M6 | フロントエンド全機能ページ (CRUD + 認可ガード) | 完了 |
| M7 | 統合テスト整備 (87 件全 PASS) | 完了 |
| M8 | トレーサビリティ強化 + GitHub Actions CI/CD | 完了 |
| M9 | Decision Ledger / V&V 記録 / CrossOEM レポート | 完了 |

テスト: **87 件** (バックエンド統合テスト、MariaDB4j 使用)

---

## 3. 技術スタック

### バックエンド

| 領域 | 採用技術 | ABP 対応 |
|------|---------|---------|
| 言語 | Java 21 LTS (Eclipse Temurin) | .NET 10 |
| ビルド | Maven 3.9 マルチモジュール (Wrapper 同梱) | .sln |
| Web | Spring Boot 3.3 + Spring Modulith | ABP モジュール |
| 認可サーバ | Spring Authorization Server (組み込み) | OpenIddict |
| リソースサーバ | Spring Security OAuth2 Resource Server | — |
| ORM | Spring Data JPA + Hibernate 6 | EF Core |
| DB マイグレーション | Liquibase (YAML 形式、001〜026 changeset) | EF Core Migrations |
| マッピング | MapStruct | AutoMapper |
| 監査 | Spring Data JPA Auditing + Hibernate Envers | IAuditedObject |
| マルチテナント | Hibernate `@Filter` + AOP で TenantId 自動注入 | IMultiTenant |
| イベント | Spring Modulith ApplicationEvents | Volo.Abp.EventBus |
| バックグラウンドジョブ | Quartz Scheduler + ShedLock | Volo.Abp.BackgroundJobs |
| 機能管理 | Togglz + feature_values テーブル | Volo.Abp.FeatureManagement |
| 設定管理 | settings テーブル + Caffeine キャッシュ | Volo.Abp.SettingManagement |
| テスト DB | MariaDB4j (組み込み MariaDB、Docker 不要) | xUnit + InMemory |
| テスト | JUnit 5 + AssertJ + MockMvc + ArchUnit | xUnit |

### フロントエンド

| 領域 | 採用技術 |
|------|---------|
| ビルド | Vite |
| 言語 | TypeScript strict |
| UI フレームワーク | React 19 |
| UI ライブラリ | Ant Design 6.x |
| 状態管理 | Zustand 5 |
| サーバ状態 | TanStack Query 5 |
| HTTP クライアント | openapi-fetch + 手書き型 (将来 api:generate で置換) |
| ルーティング | React Router |
| 認証 | react-oidc-context (PKCE) |
| フォーム | React Hook Form 7 + Zod 3 |
| テスト | Vitest + React Testing Library + MSW |

### データベース

- MySQL 8.x (開発: ホスト直接。接続: localhost:3306 / root / 123)
- テスト: MariaDB4j (JVM 内起動、テストごと独立)
- マルチテナント: Single DB + `tenant_id` カラムフィルタ方式

---

## 4. プロジェクト構造

```
AnomalyDetectionJava/
├── backend/
│   ├── domain-shared/         # 列挙型・定数・共通例外 (Spring 依存なし)
│   ├── domain/                # エンティティ・ドメインサービス・Repository I/F
│   ├── application-contracts/ # DTO・AppService I/F・権限定数クラス
│   ├── application/           # AppService 実装・MapStruct Mapper・@PreAuthorize
│   ├── infrastructure/        # JPA 実装・Liquibase・SecurityConfiguration
│   ├── web/                   # @RestController・例外ハンドラ・OpenAPI
│   ├── auth-server/           # Spring Authorization Server
│   ├── host/                  # 起動アプリ・LocalDevSeeder (@Profile("local"))
│   └── db-migrator/           # SeedDataInitializer (独立 CLI)
└── frontend/
    └── src/
        ├── app/               # router.tsx・RootLayout・AuthGuard・HomePage
        ├── modules/           # 各ドメインの画面 (SafetyPage.tsx 等)
        ├── shared/
        │   ├── api/           # apiFetch.ts (openapi-fetch ラッパー)
        │   └── auth/          # usePermissions.ts・RequirePermission.tsx・permissions.ts
        └── api/               # schema.d.ts (api:generate で生成)
```

### モジュール依存方向

```
domain-shared → domain → application-contracts → application
                                                      ↓
                                            infrastructure → host
                                                  ↑         ↑
                                                 web ────────┘
```

`web → infrastructure` の直接参照は ArchUnit テストで禁止。

---

## 5. 重要な設計判断と落とし穴

### 5.1 権限シードの 2 箇所管理

**必須ルール**: 新しい権限定数を追加したら **必ず両方**を更新する。

| ファイル | プロファイル | 用途 |
|---------|------------|------|
| `host/LocalDevSeeder.java` | `@Profile("local")` のみ | ローカル開発起動時に毎回実行 |
| `db-migrator/SeedDataInitializer.java` | プロファイル制限なし | 独立 CLI として実行 |

`LocalDevSeeder` を更新し忘れると、ローカル起動後にドメインページが blank になる  
(`RequirePermission` が null を返すため — いわゆる「スケルトン画面」)。

**権限追加後のフロー:**
1. 権限定数クラス (`XxxPermissions.java`) に定数追加
2. `XxxPermissionDefinitionContributor.java` に追加
3. `LocalDevSeeder.seedAdminPermissions()` に追加
4. `SeedDataInitializer.seedAdminRolePermissions()` に追加
5. `.\mvnw.cmd clean install -DskipTests` (新クラス追加後は必ず clean)
6. バックエンド再起動
7. **ログアウト → 再ログイン** (JWT を更新するため必須)
8. `frontend/src/shared/auth/permissions.ts` にも定数を追加

### 5.2 Maven ビルドの注意点

```powershell
# 新しいクラスを追加した後は必ず clean
.\mvnw.cmd clean install -DskipTests

# clean なしだと stale な .class ファイルが残り ClassNotFoundException が発生
# 特に application-contracts に新クラスを追加した場合に頻発
# spring-boot:run は ~/.m2/ の JAR を使うため、install しただけでは不十分な場合がある
```

### 5.3 JWT と権限の関係

- `JwtTokenCustomizer` が JWT の `permissions` クレームにロールの権限一覧を埋め込む
- `SecurityConfiguration.jwtAuthConverter()` が `permissions` クレームを `GrantedAuthority` として抽出
- フロント `usePermissions.ts` が `access_token` の `permissions` クレームを直接パース
- **権限変更は次回ログイン時の JWT 発行まで反映されない**

### 5.4 フィールド名の注意

| エンティティ | 正しいフィールド | 間違えやすい名前 |
|------------|----------------|----------------|
| `SafetyTraceRecord` | `approvalStatus` | `status` |
| `OemApproval` | `status` | — |
| `AnomalyDetectionLogic` | `status` | — |

`SafetyTraceRecord` だけ `approvalStatus` であることに注意。

### 5.5 LocalDevSeeder vs SeedDataInitializer の分離

`db-migrator` モジュールは `host` の classpath に含まれない。  
`SeedDataInitializer` は `host` 起動時には **実行されない**。  
ローカル開発では `LocalDevSeeder` が全シードを担う。

### 5.6 apiFetch パターン (フロント)

```typescript
// GET
const data = await apiFetch<T[]>('/app/some-path')

// POST
const result = await apiFetch<T>('/app/some-path', {
  method: 'POST',
  body: JSON.stringify(input),
})
```

`/api/` プレフィックスは Vite のプロキシ設定で自動付与。  
`apiFetch` は 401 で自動ログアウト、他エラーは `Error` をスロー。

---

## 6. 認証・認可フロー

```
React SPA
  → /oauth2/authorize (PKCE challenge)
  → Spring Authorization Server (ログイン画面)
  → 認可コード → /oauth2/token
  → JWT access_token (permissions クレーム付き)
  → Bearer ヘッダで /api/app/** を呼び出し
  → Spring Security が JWT を検証、permissions を GrantedAuthority に変換
  → @PreAuthorize("hasAuthority('xxx')") でメソッドレベル認可
```

フロントの権限チェック:
- `usePermissions()` → JWT の `permissions` クレームをパース
- `RequirePermission` → 権限なしの場合 `null` を返す (ページが空になる)
- `RootLayout.filterMenu()` → 権限のないメニュー項目を非表示

---

## 7. Liquibase Changelog 構成 (現在: 001〜026)

`infrastructure/src/main/resources/db/changelog/`

| ファイル | 内容 |
|---------|------|
| 001〜010 | Identity / Multi-Tenancy / OAuth2 / Permissions / Settings / Features / Audit / Events / ShedLock / BLOB |
| 011〜022 | ドメインテーブル (CAN信号・検出・テンプレート・異常検出・プロジェクト・Safety・KB・OEM・Integration) |
| 023 | トレーサビリティキー列追加 (feature_id, decision_id 等) |
| 024 | 却下理由等拡張フィールド |
| 025 | decision_ledgers テーブル |
| 026 | Safety 拡張フィールド (M9-A: svn_rev, module_id 等) |

次の changeset は `027-xxx.yaml`、`db.changelog-master.yaml` に include を追加。

---

## 8. テスト構成

```java
// 統合テストの標準パターン
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class SomeApiTest {
  @DynamicPropertySource
  static void registerMariaDb4j(DynamicPropertyRegistry registry) {
    MariaDB4jExtension.register(registry);
  }

  // JWT モック
  private static SimpleGrantedAuthority auth(String perm) {
    return new SimpleGrantedAuthority(perm);
  }
  // .with(jwt().authorities(auth("SomePermission.String")))
}
```

---

## 9. フロントエンドの権限定数同期

`frontend/src/shared/auth/permissions.ts` — バックエンドの権限定数と手動同期が必要。

| Java クラス | フロント定数オブジェクト | 同期状態 |
|------------|----------------------|---------|
| `IdentityPermissions` | `AdminPermissions` | 同期済 |
| `CanSignalPermissions` | `CanSignalPermissions` | 同期済 |
| `SafetyTracePermissions` | `SafetyTracePermissions` | 同期済 |
| `DecisionLedgerPermissions` | (未追加) | **要対応** |
| `OemTraceabilityPermissions` | `OemTraceabilityPermissions` | 同期済 |

---

## 10. 次の開発エントリポイント

### フロントエンド未実装ページ (バックエンド実装済み)

1. **Decision Ledger ページ**
   - API: `GET/POST /api/app/decision-ledger`
   - 権限: `SafetyTrace.DecisionLedger.Default`
   - 実装場所: `/safety` タブ追加 または `/safety/decision-ledger` ルート追加

2. **OEM トレーサビリティレポートページ**
   - API: `GET /api/app/oem-traceability-report/by-feature?featureId=xxx`
   - 権限: `OemTraceability.Approvals.Default`

3. **V&V 記録 UI**
   - API: `GET/POST /api/app/safety-trace-records/{id}/verifications`
   - API: `GET/POST /api/app/safety-trace-records/{id}/validations`
   - 実装場所: Safety 詳細 Drawer 内タブ

### 型定義の本番化

```powershell
cd frontend
npm run api:generate   # backend 起動中に実行 → src/api/schema.d.ts 更新
```

各ページの手書き `interface` 型を `schema.d.ts` の型で置換する。

---

## 11. 将来課題

| 区分 | 内容 |
|------|------|
| インフラ | Docker / docker-compose 化 |
| インフラ | 本番デプロイ (Nginx + TLS + サービス化 + 監視) |
| 性能 | Redis 分散キャッシュ (Spring Cache 差替で対応可) |
| ストレージ | S3 互換 BLOB ストレージ (Spring Content 差替で対応可) |
| 連携 | Kafka / RabbitMQ (Spring Modulith Externalization で対応可) |
| テスト | Playwright E2E テスト整備 |
| テスト | JMH パフォーマンスベンチマーク |
| アーキテクチャ | auth-server 別プロセス分離 |
| 監視 | Prometheus + Grafana + Alertmanager (Micrometer 経由) |

---

## 12. 参照ドキュメント

| ドキュメント | 内容 |
|------------|------|
| [docs/spec.md](docs/spec.md) | アーキテクチャ詳細・DB 設計・認証フロー |
| [docs/history.md](docs/history.md) | M0〜M9 マイルストーン遷移記録・設計決定ログ |
| [docs/traceability.md](docs/traceability.md) | ISO 26262 トレーサビリティ運用規則 |
| [README.md](README.md) | 起動手順・フォルダ構成・API 概要 |
| [移植元 .NET 版](../AnomalyDetection/README.md) | 機能仕様の参照元 |
