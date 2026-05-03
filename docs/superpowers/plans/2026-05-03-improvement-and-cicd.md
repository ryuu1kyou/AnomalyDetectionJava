# 改善 + CI/CD 整備 実施計画

**作成日**: 2026-05-03  
**対象リポジトリ**: AnomalyDetectionJava  
**ベース**: IMPROVEMENT_ADVICE.md + automotive-safety スキル (01〜06)

> 各フェーズは独立して別セッションで実行可能なよう前提条件と検証手順を明記しています。

---

## 共通前提

| 項目 | 値 |
|---|---|
| Java | 21 (Temurin) |
| ビルドツール | `./mvnw` (wrapper) |
| テスト DB | MariaDB4j (組み込み、Docker 不要) |
| ローカル MySQL (起動用) | root / `123` |
| フロントエンドテスト | `cd frontend && npm test` (Vitest) |
| バックエンド全検証 | `cd backend && ./mvnw -B verify` |

---

## フェーズ一覧

| フェーズ | 内容 | 依存フェーズ |
|---|---|---|
| A-1 | 状態遷移ガード + 409 例外ハンドラ | なし |
| A-2/3/4 | トレサビキー列追加 (エンティティ→DTO→Web→Liquibase) | A-1 |
| A-5 | テスト補完 | A-2/3/4 |
| B | Safety↔OEM feature_id 横断突合 API | A-5 |
| C-1 | フロントエンド権限認可 | なし (B と並行可) |
| C-2 | Safety 詳細画面トレサビ UI | C-1, B |
| D | GitHub Actions CI/CD | なし (A-5 完了後推奨) |
| E | ドキュメント反映 | D 完了後 |

---

## Phase A-1: 状態遷移ガード

### 目的
`SafetyTraceRecord`, `OemApproval`, `OemCustomization` の状態遷移を保護し、
不正な二重承認・逆遷移を `IllegalStateException` で防ぐ。
API 層では `@RestControllerAdvice` で 409 Conflict に変換する。

### 参考実装
`backend/domain/src/main/java/com/anomalydetection/domain/anomalydetection/AnomalyDetectionLogic.java`
のステータス遷移ガードが既に正しい実装になっているので同パターンを踏襲する。

### 変更ファイル

#### 1. `SafetyTraceRecord.java`
- `submit()`: `approvalStatus != DRAFT` → `throw IllegalStateException`
- `startReview()`: `approvalStatus != SUBMITTED` → `throw`
- `approve()`: `approvalStatus == APPROVED` → `throw`（二重承認防止）、`approvalStatus not in {SUBMITTED, UNDER_REVIEW}` → `throw`
- `reject()`: `approvalStatus == REJECTED` → `throw`、`approvalStatus not in {SUBMITTED, UNDER_REVIEW}` → `throw`

#### 2. `OemApproval.java`
- `approve()`: `status != PENDING` → `throw IllegalStateException("OemApproval can only be approved from PENDING status")`
- `reject()`: 同上
- `cancel()`: `status in {APPROVED, REJECTED, CANCELLED}` → `throw`

#### 3. `OemCustomization.java`
- `submitForApproval()`: `status != DRAFT` → `throw`
- `approve()`: `status != PENDING_APPROVAL` → `throw`
- `reject()`: `status != PENDING_APPROVAL` → `throw`
- `markObsolete()`: `status == OBSOLETE` → `throw`

#### 4. `web` モジュールに `GlobalExceptionHandler.java` 追加
```
backend/web/src/main/java/com/anomalydetection/web/exception/GlobalExceptionHandler.java
```
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String,String>> handleIllegalState(IllegalStateException ex) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(Map.of("message", ex.getMessage()));
  }
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String,String>> handleIllegalArg(IllegalArgumentException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Map.of("message", ex.getMessage()));
  }
}
```

#### 5. テスト追加（domain モジュール）
```
backend/domain/src/test/java/com/anomalydetection/domain/safety/SafetyTraceRecordStatusTransitionTest.java
backend/domain/src/test/java/com/anomalydetection/domain/oemtraceability/OemApprovalStatusTransitionTest.java
backend/domain/src/test/java/com/anomalydetection/domain/oemtraceability/OemCustomizationStatusTransitionTest.java
```

### 検証コマンド
```bash
cd backend
./mvnw -B -pl domain test
./mvnw -B verify
```

---

## Phase A-2/3/4: トレサビキー列追加

### 目的
automotive-safety スキルの TOP3 抜け道防止と 3 台帳モデルを本プロジェクトで実現する。

### 新規 Enum

```
backend/domain/src/main/java/com/anomalydetection/domain/safety/IfImpact.java
  → CHANGED, UNCHANGED, UNKNOWN

backend/domain/src/main/java/com/anomalydetection/domain/safety/DocSyncStatus.java
  → NOT_REQUIRED, PENDING, UPDATED, REVIEWED

backend/domain/src/main/java/com/anomalydetection/domain/safety/TraceabilityScope.java
  → PLATFORM, OEM_SPECIFIC, INTERNAL_ONLY
```

### `SafetyTraceRecord.java` に追加するフィールド

| フィールド | 型 | DB列名 | 必須 | 説明 |
|---|---|---|---|---|
| `featureId` | String(64) | `feature_id` | ✓ (バリデーション) | 機能まとまりID |
| `decisionId` | String(64) | `decision_id` | - | DR/経緯番号 |
| `changeId` | String(64) | `change_id` | - | 変更単位ID |
| `ifImpact` | IfImpact(ENUM) | `if_impact` | ✓ default UNKNOWN | IF 影響有無 |
| `unknownUntil` | LocalDate | `unknown_until` | if_impact=UNKNOWN 時 | 解消期限 |
| `unknownOwnerId` | UUID | `unknown_owner_id` | if_impact=UNKNOWN 時 | 回収責任者 |
| `designRationale` | LONGTEXT | `design_rationale` | - | 設計理由 |
| `assumption` | LONGTEXT | `assumption` | - | 前提条件 |
| `constraint` | LONGTEXT | `constraint_note` | - | 制約 |
| `docSyncStatus` | DocSyncStatus | `doc_sync_status` | default NOT_REQUIRED | 文書反映状況 |
| `scope` | TraceabilityScope | `scope` | default PLATFORM | 適用範囲 |
| `applicability` | String(255) | `applicability` | - | 適用OEM/車両コード |

**コンストラクタバリデーション**（TOP1/TOP2/TOP3 強制）:
```java
public SafetyTraceRecord(UUID id, String name, String asilLevel, String featureId) {
  if (featureId == null || featureId.isBlank())
    throw new IllegalArgumentException("feature_id is required");
  this.featureId = featureId;
  this.ifImpact = IfImpact.UNKNOWN;
  this.docSyncStatus = DocSyncStatus.NOT_REQUIRED;
  this.scope = TraceabilityScope.PLATFORM;
  // ...既存フィールド...
}

public void submit(UUID byUserId) {
  if (ifImpact == IfImpact.UNKNOWN
      && (unknownUntil == null || unknownOwnerId == null)) {
    throw new IllegalStateException(
        "if_impact=UNKNOWN requires unknown_until and unknown_owner_id before submit");
  }
  // ...既存ガード...
}
```

### `OemApproval.java` に追加するフィールド

| フィールド | 型 | DB列名 |
|---|---|---|
| `featureId` | String(64) | `feature_id` |
| `decisionId` | String(64) | `decision_id` |
| `applicability` | String(255) | `applicability` |
| `confidentialityLevel` | String(32) | `confidentiality_level` |

### DTO 更新

```
application-contracts/safety/SafetyTraceRecordDto.java     ← featureId, decisionId, ifImpact, docSyncStatus, scope, applicability, designRationale を追加
application-contracts/safety/CreateSafetyTraceRecordDto.java  ← 同上 (入力用)
application-contracts/safety/UpdateSafetyTraceRecordDto.java  ← 確認・追加
application-contracts/oemtraceability/OemApprovalDto.java  ← featureId, decisionId, applicability 追加
application-contracts/oemtraceability/CreateOemApprovalDto.java ← 同上
```

### AppService 更新

```
application/safety/SafetyTraceAppService.java
  → create() で featureId を SafetyTraceRecord コンストラクタへ渡す
  → toDto() に新フィールドを追加
  → update() に新フィールドを追加

application/oemtraceability/OemTraceabilityAppService.java
  → create/toDto に featureId, decisionId を追加
```

### Liquibase changeset

```
backend/infrastructure/src/main/resources/db/changelog/changes/2026-05-03-safety-traceability-keys.xml
```

```xml
<changeSet id="2026-05-03-safety-traceability-keys" author="dev">
  <addColumn tableName="safety_trace_records">
    <column name="feature_id" type="VARCHAR(64)"/>
    <column name="decision_id" type="VARCHAR(64)"/>
    <column name="change_id" type="VARCHAR(64)"/>
    <column name="if_impact" type="VARCHAR(16)" defaultValue="UNKNOWN"/>
    <column name="unknown_until" type="DATE"/>
    <column name="unknown_owner_id" type="BINARY(16)"/>
    <column name="design_rationale" type="LONGTEXT"/>
    <column name="assumption" type="LONGTEXT"/>
    <column name="constraint_note" type="LONGTEXT"/>
    <column name="doc_sync_status" type="VARCHAR(16)" defaultValue="NOT_REQUIRED"/>
    <column name="scope" type="VARCHAR(16)" defaultValue="PLATFORM"/>
    <column name="applicability" type="VARCHAR(255)"/>
  </addColumn>

  <addColumn tableName="oem_approvals">
    <column name="feature_id" type="VARCHAR(64)"/>
    <column name="decision_id" type="VARCHAR(64)"/>
    <column name="applicability" type="VARCHAR(255)"/>
    <column name="confidentiality_level" type="VARCHAR(32)"/>
  </addColumn>

  <!-- 既存レコードのマイグレーション (feature_id を LEGACY-{id} で埋める) -->
  <update tableName="safety_trace_records">
    <column name="feature_id" valueComputed="CONCAT('LEGACY-', LOWER(HEX(id)))"/>
    <where>feature_id IS NULL</where>
  </update>
</changeSet>
```

changelog ルート (`db-changelog-master.xml`) に include を追加する。

### 検証コマンド
```bash
cd backend
./mvnw -B verify
```

---

## Phase A-5: テスト補完

### 目的
Phase A-2/3/4 で追加したフィールドを既存/新規 API テストで網羅する。

### 変更ファイル
- `SafetyTraceRecordValidationTest.java` (domain): feature_id 空文字でコンストラクタが例外を投げることを確認
- 既存 `SafetyTraceApiTest.java` (host/test): create リクエストに featureId を追加、レスポンスで検証
- 既存 `OemTraceabilityApiTest.java` (host/test): featureId を追加

### 検証コマンド
```bash
cd backend && ./mvnw -B verify
```

---

## Phase B: Safety↔OEM feature_id 横断突合

### 目的
`feature_id` を共通キーとして Safety レコードと OEM 承認を横断検索できるようにする。

### 変更ファイル

#### Domain
```
OemApprovalRepository.java
  + findAllByEntityTypeAndEntityId(String, String): List<OemApproval>
  + findAllByFeatureId(String): List<OemApproval>
```

#### Application
```
SafetyTraceAppService.java
  + getRelatedOemApprovals(UUID recordId): List<OemApprovalDto>
  + getByFeatureId(String featureId): List<SafetyTraceRecordDto>
```

#### Web
```
SafetyController.java
  + GET /api/app/safety-trace-records/{id}/oem-approvals
  + GET /api/app/safety-trace-records/by-feature/{featureId}
```

#### Test
```
host/test: SafetyOemLinkApiTest.java (新規 2 テスト)
  1. Safety作成 → OEM承認発行(同featureId) → /by-feature で両方取得できる
  2. /oem-approvals エンドポイントが関連OEM承認を返す
```

### 検証コマンド
```bash
cd backend && ./mvnw -B verify
```

---

## Phase C-1: フロントエンド権限認可

### 目的
JWT の `permissions` claim に基づいてメニュー・ルートをフィルタリングする。

### 新規ファイル

```
frontend/src/shared/auth/permissions.ts
  → バックエンドの Permission 定数文字列を列挙した定数オブジェクト

frontend/src/shared/auth/usePermissions.ts
  → useAuth() から JWT をデコードし permissions claim を返すフック
  → hasPermission(name: string): boolean

frontend/src/app/components/RequirePermission.tsx
  → <RequirePermission permission="..."> でラップするルートガード
  → 権限なしは <Navigate to="/" /> または 403 ページへ
```

### 変更ファイル

```
frontend/src/app/router.tsx (または相当のルート定義)
  → /audit-log, /settings, /features, /permissions を RequirePermission でラップ

frontend/src/app/components/RootLayout.tsx (またはサイドバー相当)
  → MENU_ITEMS を hasPermission でフィルタ

frontend/src/shared/auth/usePermissions.test.ts (新規 Vitest)
  → JWTモック → hasPermission 結果を検証
```

### 検証コマンド
```bash
cd frontend && npm test
cd frontend && npm run build
```

---

## Phase C-2: Safety 詳細画面 トレサビ UI

### 目的
`feature_id`, `if_impact`, `doc_sync_status` を可視化し、
同 feature_id の OEM 承認を突合表示して承認儀式化を防止する。

### 変更ファイル

```
frontend/src/modules/safety/SafetyTraceRecordDetailPage.tsx (または相当)
  → feature_id バッジ（未設定なら警告色 orange）
  → if_impact バッジ（UNKNOWN → 赤、CHANGED → 青、UNCHANGED → グレー）
  → doc_sync_status チップ
  → 関連 OEM 承認テーブル（GET /by-feature/{featureId}）

frontend/src/modules/safety/SafetyTraceListPage.tsx
  → フィルタに if_impact=UNKNOWN, doc_sync_status=PENDING を追加
```

### 検証コマンド
```bash
cd frontend && npm test
cd frontend && npm run build
```

---

## Phase D: GitHub Actions CI/CD

### 目的
PRマージ前に Java ビルド・テスト・フロントエンドビルド・CodeQL を自動実行する。

### 新規ファイル

#### `.github/workflows/backend-ci.yml`
```yaml
name: Backend CI
on:
  push:
    branches: [main, develop]
    paths: ['backend/**']
  pull_request:
    branches: [main]
    paths: ['backend/**']

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: 'temurin' }
      - uses: actions/cache@v4
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('backend/**/pom.xml') }}
      - run: cd backend && ./mvnw -B verify
      - uses: actions/upload-artifact@v4
        if: always()
        with:
          name: jacoco-report
          path: backend/host/target/site/jacoco/
```

#### `.github/workflows/frontend-ci.yml`
```yaml
name: Frontend CI
on:
  push:
    branches: [main, develop]
    paths: ['frontend/**']
  pull_request:
    branches: [main]
    paths: ['frontend/**']

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with: { node-version: '20', cache: 'npm', cache-dependency-path: frontend/package-lock.json }
      - run: cd frontend && npm ci
      - run: cd frontend && npm run lint
      - run: cd frontend && npm run build
      - run: cd frontend && npm test
```

#### `.github/workflows/codeql.yml`
```yaml
name: CodeQL
on:
  schedule: [{ cron: '0 2 * * 1' }]
  pull_request: { branches: [main] }

jobs:
  analyze:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        language: [java, javascript]
    steps:
      - uses: actions/checkout@v4
      - uses: github/codeql-action/init@v3
        with: { languages: ${{ matrix.language }} }
      - uses: github/codeql-action/autobuild@v3
      - uses: github/codeql-action/analyze@v3
```

#### `.github/dependabot.yml`
```yaml
version: 2
updates:
  - package-ecosystem: maven
    directory: /backend
    schedule: { interval: weekly }
  - package-ecosystem: npm
    directory: /frontend
    schedule: { interval: weekly }
  - package-ecosystem: github-actions
    directory: /
    schedule: { interval: weekly }
```

#### `.github/pull_request_template.md`
automotive-safety スキルの 1 枚チェックリスト (#03) を PR テンプレとして組み込む。
```markdown
## 変更概要

## トレサビ確認チェックリスト

- [ ] `feature_id` が設定されている (例: `AEB-FEAT-017`)
- [ ] `decision_id` または DR メモがある
- [ ] IF 影響有無が明確 (`if_impact`: CHANGED / UNCHANGED / UNKNOWN+期限)
- [ ] `UNKNOWN` の場合、`unknown_until` と `unknown_owner_id` を設定済み
- [ ] `doc_sync_status` を適切に更新した
- [ ] OEM 適用範囲 (`applicability`) が設定されている
- [ ] 関連する Safety / OEM 承認レコードが存在する

## テスト
- [ ] `cd backend && ./mvnw -B verify` PASS
- [ ] `cd frontend && npm test` PASS
```

### 検証
GitHub にプッシュしてワークフローが緑になることを確認。

---

## Phase E: ドキュメント反映

### 変更ファイル

#### `CLAUDE.md`
- 5.1 マイルストーン進捗に M8 行を追加:
  ```
  | **M8** (改善 + CI/CD) | **完了** (Safety/OEM 状態遷移ガード, トレサビキー列, 権限認可, GitHub Actions) |
  ```
- 5.4 将来課題から「CI/CD パイプライン (GitHub Actions)」を削除

#### `docs/traceability-rules.md` (新規)
automotive-safety スキル要点をこのプロジェクト用に圧縮した運用ルール。
内容:
- 3 台帳モデル (承認台帳 / 実装接続台帳 / 設計意図台帳)
- 必須キー: `feature_id`, `decision_id`, `if_impact`, `svn_rev` 相当 (`change_id`)
- TOP3 絶対許容しない (feature_id 粒度崩壊 / IF 影響不明 / unknown 放置)
- `feature_id` 命名規則: `{DOMAIN}-FEAT-{連番3桁}` 例: `SAFETY-FEAT-001`
- `doc_sync_status` ライフサイクル

#### `README.md`
- CI バッジ (Backend CI / Frontend CI) を先頭に追加
- `docs/traceability-rules.md` へのリンクを追加

---

## 完了定義

全フェーズ完了時に以下がすべて満たされていること:

1. `cd backend && ./mvnw -B verify` → 全テスト GREEN
2. `cd frontend && npm run build && npm test` → GREEN
3. GitHub Actions のすべてのワークフローが GREEN
4. Safety 詳細画面で `feature_id` / `if_impact` / `doc_sync_status` が表示される
5. `/api/app/safety-trace-records/by-feature/{featureId}` が動作する
6. 権限なしユーザに管理メニューが非表示になる
