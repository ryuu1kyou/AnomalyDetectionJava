# トレーサビリティ運用ルール — AnomalyDetectionJava

> automotive-safety スキル (ISO 26262 / Simulink MBD トレーサビリティ) の知見を、本 Java/Spring Boot プロジェクトに適用した運用ルールです。

---

## 1. 3台帳構成 (実装対応)

| 台帳 | エンティティ | カラム | 用途 |
|------|------------|--------|------|
| 承認台帳 | `OemApproval` | `feature_id`, `decision_id`, `applicability` | OEM 承認フロー |
| 実装接続台帳 | `SafetyTraceRecord` | `feature_id`, `detection_logic_id`, `project_id` | 実装・テストの接続 |
| 設計意図台帳 | `SafetyTraceRecord` | `design_rationale`, `assumption`, `constraint_text` | なぜ変更したか |

---

## 2. TOP 3 — 絶対許容しない項目

| # | リスク | チェックポイント | 対策 |
|---|--------|----------------|------|
| 1 | **feature_id なし** | Safety/OEM レコードに `feature_id` が未設定 | API で `feature_id` を必須化 (400 Bad Request) |
| 2 | **IF 影響不明のままリリース** | `if_impact = UNKNOWN` のまま期限超過 | `unknown_until` + `unknown_owner_id` を必須化 |
| 3 | **期限なし UNKNOWN 放置** | `UNKNOWN` が期限切れのまま承認/リリース | 提出時にバリデーションでブロック |

---

## 3. 必須フィールド (API バリデーション)

### SafetyTraceRecord 作成

```
POST /api/app/safety-trace-records
必須:
  - name              (文字列)
  - asilLevel         (QM / ASIL_A / ASIL_B / ASIL_C / ASIL_D)
  - featureId         (文字列)  ← TOP1

if_impact = UNKNOWN の場合、追加必須:
  - unknownUntil      (ISO 8601 日付)
  - unknownOwnerId    (UUID)
```

### 横断検索

```
GET /api/app/traceability/feature/{featureId}
→ Safety レコード + OEM 承認を feature_id で突合
権限: SafetyTrace.Records.Default + OemTraceability.Approvals.Default
```

---

## 4. フロントエンド運用ガイド

### メニュー表示制御

`usePermissions()` フックで JWT の `permissions` claim を解析し、
各メニュー項目の表示/非表示を制御する。

| ページ | 必要権限 |
|--------|---------|
| Safety | `SafetyTrace.Records.Default` |
| OEM トレーサビリティ | `OemTraceability.Approvals.Default` |
| CAN 信号 | `CanSignal.Default` |

### 詳細 Drawer

Safety 一覧の `name` をクリック → **詳細 Drawer** でトレーサビリティキーを閲覧
→ `feature_id` をクリック → **横突合 Drawer** で同一 feature_id の OEM 承認を表示

### バッジ配色

| フィールド | 値 | 色 | 意味 |
|-----------|-----|-----|------|
| `ifImpact` | `UNCHANGED` | 🟢 緑 | インターフェース変更なし |
| `ifImpact` | `CHANGED` | 🔴 赤 | インターフェース変更あり |
| `ifImpact` | `UNKNOWN` | 🟠 オレンジ | 影響不明 (期限要) |
| `docSyncStatus` | `SYNCED` | 🟢 緑 | 設計文書と同期済 |
| `docSyncStatus` | `OUT_OF_SYNC` | 🔴 赤 | 設計文書と不一致 |

---

## 5. CI/CD チェックリスト (PR Template)

`.github/PULL_REQUEST_TEMPLATE.md` に自動挿入される項目:

- [ ] `feature_id` は設定されているか
- [ ] `if_impact` は `UNKNOWN` になっていないか
- [ ] `UNKNOWN` の場合、`unknown_until` + `unknown_owner_id` は設定されているか

---

## 6. テスト網羅表

| フェーズ | テストファイル | テスト数 | 検証内容 |
|---------|-------------|---------|---------|
| A-1 | `SafetyTraceRecordStatusTransitionTest` | 12 | DRAFT→SUBMITTED→UNDER_REVIEW→APPROVED の遷移ガード |
| A-1 | `OemApprovalStatusTransitionTest` | 9 | PENDING→APPROVED/REJECTED の二重承認防止 |
| A-1 | `OemCustomizationStatusTransitionTest` | 10 | DRAFT→PENDING→APPROVED/REJECTED/OBSOLETE の遷移 |
| A-5 | `SafetyTraceApiTest` | 4 | `feature_id` 必須・UNKNOWN 期限チェック・トレサビフィールド保存 |
| A-5 | `OemTraceabilityApiTest` | 4 | OEM 承認のトレサビキー保存・検証 |
| B | `FeatureTraceabilityApiTest` | 2 | feature_id 横断検索で Safety + OEM を両方取得 |
