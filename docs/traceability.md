# ISO 26262 トレーサビリティ運用規則

本ドキュメントは **automotive-safety スキル** (ISO 26262 / Simulink MBD トレーサビリティ) の知見を  
本 Java/Spring Boot プロジェクトに適用した運用ルールです。

---

## 1. 3 台帳構成

Simulink MBD 現場で有効だった「承認台帳 / 実装接続台帳 / 設計意図台帳」の分離を  
本プロジェクトの JPA エンティティに対応させます。

| 台帳 | エンティティ | 主要カラム | 用途 |
|------|-----------|---------|------|
| 承認台帳 | `OemApproval` | `feature_id`, `decision_id`, `applicability` | OEM 承認フロー |
| 実装接続台帳 | `SafetyTraceRecord` | `feature_id`, `detection_logic_id`, `project_id` | 実装・テストの接続 |
| 設計意図台帳 | `SafetyTraceRecord` | `design_rationale`, `assumption`, `constraint_text` | なぜ変更したか |

---

## 2. TOP 3 — 絶対許容しない項目

| # | リスク | チェックポイント | 対策 |
|---|--------|----------------|------|
| 1 | **feature_id なし** | Safety / OEM レコードに `feature_id` が未設定 | API で `feature_id` を必須化 (400 Bad Request) |
| 2 | **IF 影響不明のままリリース** | `if_impact = UNKNOWN` のまま期限超過 | `unknown_until` + `unknown_owner_id` を必須化 |
| 3 | **期限なし UNKNOWN 放置** | `UNKNOWN` が期限切れのまま承認 / リリース | 提出時バリデーションでブロック |

---

## 3. フィールド定義

### SafetyTraceRecord の主要トレーサビリティフィールド

| フィールド | 型 | 説明 | 必須条件 |
|-----------|-----|------|---------|
| `featureId` | String | 変更のまとまり ID (例: FEAT-2026-001) | 常に必須 |
| `decisionId` | String | 設計判断 ID (例: DEC-2026-001) | 任意 |
| `changeId` | String | 変更単位 ID (例: CHG-20260430-001) | 任意 |
| `ifImpact` | Enum | IF 影響 (UNCHANGED/CHANGED/NEW_IF/DELETED_IF/UNKNOWN) | 常に必須 |
| `unknownUntil` | Date | UNKNOWN 回収期限 | `ifImpact=UNKNOWN` の場合必須 |
| `unknownOwnerId` | UUID | 回収責任者 | `ifImpact=UNKNOWN` の場合必須 |
| `designRationale` | String | 設計根拠・変更理由 | 任意 |
| `assumption` | String | 前提条件 | 任意 |
| `constraintText` | String | 制約・トレードオフ | 任意 |
| `docSyncStatus` | Enum | 文書同期状態 (NOT_REQUIRED/PENDING/UPDATED/REVIEWED) | 常に必須 |
| `scope` | Enum | 公開範囲 (PLATFORM/OEM_SPECIFIC/INTERNAL_ONLY) | 常に必須 |
| `applicability` | String | 適用範囲 (例: All OEMs, Toyota-only) | 任意 |
| `svnRev` | String | SVN リビジョン番号 | 任意 |
| `moduleId` | String | 対象モジュール ID | 任意 |
| `ifVersion` | String | CAN 信号 IF バージョン (例: 1.2.3) | 任意 |
| `changeType` | String | 変更種別 | 任意 |

### ID 採番ルール (推奨)

| ID 種別 | フォーマット | 例 |
|---------|------------|-----|
| `feature_id` | `FEAT-YYYYMM-NNN` | FEAT-202604-001 |
| `decision_id` | `DEC-YYYYMM-NNN` | DEC-202604-001 |
| `change_id` | `CHG-YYYYMMDD-NNN` | CHG-20260430-001 |

---

## 4. API バリデーション規則

### SafetyTraceRecord 作成 (POST /api/app/safety-trace-records)

```
必須フィールド:
  - name              (文字列)
  - asilLevel         (QM / ASIL_A / ASIL_B / ASIL_C / ASIL_D)
  - featureId         (文字列) ← TOP1

ifImpact = UNKNOWN の場合、追加で必須:
  - unknownUntil      (ISO 8601 日付)
  - unknownOwnerId    (UUID)
```

### 横断検索 (GET /api/app/traceability/feature/{featureId})

```
→ Safety レコード + OEM 承認を feature_id で突合して返す
権限: SafetyTrace.Records.Default + OemTraceability.Approvals.Default
```

### CrossOEM レポート (GET /api/app/oem-traceability-report/by-feature)

```
クエリパラメータ: featureId=xxx
→ feature_id に紐付く OEM 別承認状況サマリーを返す
{
  featureId: string
  totalOems: number
  approvedCount: number
  pendingCount: number
  byOem: [{ oemCode, dominantStatus, approvalCount }]
}
権限: OemTraceability.Approvals.Default
```

---

## 5. 状態遷移ガード

### SafetyTraceRecord

```
DRAFT → (submit) → SUBMITTED → (startReview) → UNDER_REVIEW → (approve) → APPROVED
                                                              → (reject)  → REJECTED
```

- `submit()`: `DRAFT` 以外からは `IllegalStateException` (→ 409 Conflict)
- `approve()`: `APPROVED` への二重承認不可 / `SUBMITTED` または `UNDER_REVIEW` からのみ可
- `reject()`: `REJECTED` への二重却下不可 / `SUBMITTED` または `UNDER_REVIEW` からのみ可

### OemApproval

```
PENDING → (approve) → APPROVED
        → (reject)  → REJECTED
```

- `approve()` / `reject()`: `PENDING` 以外からは `IllegalStateException`

### OemCustomization

```
DRAFT → (submit) → PENDING → (approve) → APPROVED
                            → (reject)  → REJECTED
                            → (obsolete)→ OBSOLETE
```

---

## 6. フロントエンド UI ガイド

### バッジ配色

| フィールド | 値 | Ant Design 色 | 意味 |
|-----------|-----|-------------|------|
| `approvalStatus` | `DRAFT` | `default` | 下書き |
| `approvalStatus` | `SUBMITTED` | `processing` | 提出済み |
| `approvalStatus` | `UNDER_REVIEW` | `processing` | レビュー中 |
| `approvalStatus` | `APPROVED` | `success` | 承認済み |
| `approvalStatus` | `REJECTED` | `error` | 却下 |
| `ifImpact` | `UNCHANGED` | 緑 | IF 変更なし |
| `ifImpact` | `CHANGED` | 青 | IF 変更あり |
| `ifImpact` | `UNKNOWN` | オレンジ | 影響不明 (期限要) |
| `docSyncStatus` | `NOT_REQUIRED` | `default` | 文書更新不要 |
| `docSyncStatus` | `PENDING` | オレンジ | 文書更新待ち |
| `docSyncStatus` | `UPDATED` | `processing` | 文書更新済み |
| `docSyncStatus` | `REVIEWED` | `success` | 文書レビュー済み |

### 詳細 Drawer のナビゲーション

Safety 一覧の `name` をクリック  
→ **詳細 Drawer** でトレーサビリティキーを閲覧  
→ `feature_id` タグをクリック  
→ **横断突合 Drawer** で同一 `feature_id` の Safety レコード + OEM 承認を表示

---

## 7. CanSignal IF バージョントラッキング

CAN 信号の意味変更を見落とさないための補助ルール。

### 変更の分類

| 変更種別 | 例 | `if_version` の更新 |
|---------|-----|------------------|
| 形の変更 | ポート名・データ型・単位・範囲・Enum 値の追加/削除 | MINOR 更新 |
| 意味の変更 | 出力条件・解釈ロジックの変更 | MAJOR 更新 |

意味変更は形の変更より発見が難しい。  
CAN 信号更新 API に `changeReason` を必須化し、記録を残すことを推奨。

---

## 8. リーダ向け確認 3 問

複雑なチェックは不要。以下の 3 問だけで危険信号を検出できる。

1. **この変更は何のまとまりか？** → `feature_id` で回答できるか
2. **IF に意味変更が入っていないか？** → `signalMeaning` / `ifVersion` で回答できるか
3. **unknown が放置されていないか？** → `unknownUntil` が設定され期限切れでないか

---

## 9. PR チェックリスト

`.github/PULL_REQUEST_TEMPLATE.md` に自動挿入:

- [ ] `feature_id` は設定されているか
- [ ] `if_impact` は `UNKNOWN` のままでないか
- [ ] `UNKNOWN` の場合、`unknown_until` + `unknown_owner_id` は設定されているか

---

## 10. テスト網羅表

| フェーズ | テストファイル | テスト数 | 検証内容 |
|---------|-------------|---------|---------|
| M8 A-1 | `SafetyTraceRecordStatusTransitionTest` | 12 | DRAFT→SUBMITTED→UNDER_REVIEW→APPROVED の遷移ガード |
| M8 A-1 | `OemApprovalStatusTransitionTest` | 9 | PENDING→APPROVED/REJECTED の二重承認防止 |
| M8 A-1 | `OemCustomizationStatusTransitionTest` | 10 | DRAFT→PENDING→APPROVED/REJECTED/OBSOLETE の遷移 |
| M8 A-5 | `SafetyTraceApiTest` | 4 | `feature_id` 必須・UNKNOWN 期限チェック・フィールド保存 |
| M8 A-5 | `OemTraceabilityApiTest` | 4 | OEM 承認のトレサビキー保存・検証 |
| M8 B | `FeatureTraceabilityApiTest` | 2 | feature_id 横断検索で Safety + OEM を両方取得 |
| M9 C | `OemTraceabilityReportApiTest` | 3 | OEM 別レポートの集計・グループ化検証 |

---

## 11. 導入初期でやらなくてよいこと

- 全エンティティの棚卸しを一気にやる
- 過去履歴を完全復元する
- すべてのテーブルを最初から完璧に設計する

**まずはこれから入る変更に対して最低限の接続を残すことを優先する。**

---

## 関連ファイル

| ファイル | 内容 |
|---------|------|
| `domain/safety/SafetyTraceRecord.java` | 安全トレース記録エンティティ |
| `domain/safety/DecisionLedger.java` | 決定台帳エンティティ |
| `domain/oemtraceability/OemApproval.java` | OEM 承認エンティティ |
| `domain/oemtraceability/OemCustomization.java` | OEM カスタマイズエンティティ |
| `web/traceability/TraceabilitySearchController.java` | feature_id 横断検索 API |
| `web/oemtraceability/OemTraceabilityReportController.java` | CrossOEM レポート API |
| `frontend/src/modules/safety/SafetyPage.tsx` | Safety 画面 (詳細 Drawer + 横断突合 Drawer) |
| `frontend/src/modules/oemtraceability/OemTraceabilityPage.tsx` | OEM トレーサビリティ画面 |
