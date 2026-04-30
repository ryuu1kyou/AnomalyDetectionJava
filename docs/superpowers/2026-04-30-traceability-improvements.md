# トレーサビリティ改善ガイド — プロジェクト適用編

## この文書の目的

自動車業界 ADAS Simulink MBD トレーサビリティ運用の知見（6文書: 基盤編／最小セット／1枚チェックリスト／非常口／抜け道シナリオ集／レーティング）を、
本 Java/Spring Boot プロジェクト（CAN 異常検出管理システム）の OemTraceability / Safety / KnowledgeBase / CanSignal 各ドメインに
適用可能な形に翻訳・圧縮したものです。

---

## 1. 3台帳構成（エンティティ設計へのマッピング）

Simulink MBD 現場で有効だった「承認台帳／実装接続台帳／設計意図台帳」の分離を、
本プロジェクトの JPA エンティティに落とし込む。

### 1.1 承認台帳 → OemApproval

**既存**: `OemApproval`（oem_approvals テーブル）
**改善**: 以下を追加すると現場の承認フローと合致する
- `approval_type`（APPROVAL / ACKNOWLEDGE / REVIEW）
- `target_entity_type` + `target_entity_id`（どのエンティティに対する承認か）
- `test_result_link`（テスト結果へのリンク）
- `spo_link`（SPO文書へのリンク）
- `approved_by`, `approved_at`

### 1.2 実装接続台帳 → OemCustomization / 新規エンティティ候補

**既存**: `OemCustomization`（oem_customizations テーブル）
**改善**: 接続台帳として以下のフィールドを追加
- `change_id` — 変更単位のID
- `feature_id` — 機能/案件のまとまりID
- `decision_id` — 設計判断ID
- `module_id` — 対象モジュール
- `if_version` — CAN信号IFバージョン
- `svn_rev` / `git_commit_hash` — 実装リビジョン
- `doc_sync_status` — 文書反映状態（SYNCED / PENDING / UNKNOWN）

#### 新規エンティティ: ImplementationConnection（実装接続台帳）

```java
@Entity
@Table(name = "implementation_connections")
public class ImplementationConnection extends AuditableEntity {
    private String changeId;
    private String featureId;
    private String decisionId;
    private String moduleId;
    private String ifVersion;
    private String gitCommitHash;
    @Enumerated(EnumType.STRING)
    private DocSyncStatus docSyncStatus;  // SYNCED / PENDING / UNKNOWN
    private LocalDate unknownDeadline;    // unknown の回収期限
    private String unknownOwner;          // 回収責任者
}
```

### 1.3 設計意図台帳 → 新規エンティティ: DesignIntent

```java
@Entity
@Table(name = "design_intents")
public class DesignIntent extends AuditableEntity {
    private String decisionId;       // 設計判断ID
    private String featureId;        // 関連機能ID
    @Column(columnDefinition = "TEXT")
    private String whatWasDecided;   // 何を決めたか
    @Column(columnDefinition = "TEXT")
    private String rationale;        // なぜそうしたか
    @Column(columnDefinition = "TEXT")
    private String assumptions;      // 前提条件
    @Column(columnDefinition = "TEXT")
    private String constraints;      // 制約・トレードオフ
    private String sourceType;       // DR / 議事録 / Slack / レビュー
    private String sourceLink;       // 出典へのリンク
}
```

---

## 2. CanSignal IF バージョントラッキング

**根拠**: 抜け道シナリオ4（IF変更なし判定の見落とし）+ TOP2リスク（IF影響不明のままインテグ）

### CanSignal に追加推奨フィールド

```java
public class CanSignal extends AuditableEntity {
    // 既存フィールドに加えて:
    private String ifVersion;           // "1.2.3" セマンティックバージョン
    private String changeReason;        // このバージョンでの変更理由
    @Column(columnDefinition = "TEXT")
    private String signalMeaning;       // 信号の意味（実質的なIF定義、単なる値範囲ではない）
}
```

### 変更検知ルール（アプリケーションサービス側）

- **形の変更**: port名、datatype、unit、range、enum値の追加/削除 → `if_version` のMINOR更新
- **意味の変更**: 出力条件、解釈ロジックの変更 → `signalMeaning` 更新 + `if_version` のMAJOR更新
- 意味変更を見逃さないために、CanSignal 更新APIに `changeReason` を必須化する

---

## 3. decision_id / feature_id 体系

### 全ドメインへの横断付与

各 Aggregate に `feature_id` を追加し、`DesignIntent` と `ImplementationConnection` で横断追跡する：

| エンティティ | 追加フィールド |
|-------------|---------------|
| `CanSignal` | `featureId` |
| `CanAnomalyDetectionLogic` | `featureId` |
| `DetectionTemplate` | `featureId` |
| `KnowledgeBaseEntry` | `featureId`, `decisionId` |
| `SafetyTraceRecord` | `featureId`, `decisionId` |

### 採番ルール（推奨）

- `feature_id`: `F-YYYYMM-NNN`（例: F-202604-001）
- `decision_id`: `D-YYYYMM-NNN`（例: D-202604-001）
- `change_id`: `C-YYYYMMDD-NNN`（例: C-20260430-001）

---

## 4. unknown 状態の明示的管理

**根拠**: TOP3リスク（unknown を期限なしで放置）+ 抜け道シナリオ3

### 全エンティティに適用可能な共通 enum + フィールド

```java
public enum ClassificationStatus {
    CONFIRMED,      // 確定
    UNKNOWN,        // 未確定だが許容
    TEMPORARY       // 暫定で進める（期限付き）
}
```

各エンティティに以下を追加可能：
- `oemScope`（共通/OEM固有/社内限定の区分）
- `asilLevel`（QM/ASIL_A/ASIL_B/ASIL_C/ASIL_D/UNKNOWN）
- `unknownDeadline`（UNKNOWN/TEMPORARY の回収期限）
- `unknownOwner`（回収責任者 UUID）

### 運用ルール

- `UNKNOWN` は許容してよいが **無期限放置は禁止**
- 承認前またはインテグ（リリース）前に回収必須
- 回収責任者を必ず1名アサイン

---

## 5. 絶対許容しない TOP3（運用判断基準）

これらの3つはプロジェクトとして「そのまま閉じない」対象とする：

| # | リスク | チェックポイント | 対策 |
|---|--------|----------------|------|
| 1 | **feature_id なし/粒度崩壊** | 変更のまとまりが不明 | APIパラメータに feature_id を必須化 |
| 2 | **IF影響不明のままリリース** | CanSignal の変更理由/意味が未記録 | CanSignal更新APIで changeReason を必須化 |
| 3 | **unknown を期限なしで放置** | UNKNOWN のまま deadline 超過 | 定時バッチで期限切れUNKNOWNを警告 |

---

## 6. リーダ向け確認3問

複雑なチェックは不要。以下の3問だけで危険信号を検出する：

1. **この変更は何のまとまりか？** → `feature_id` で回答できるか
2. **IFに意味変更が入っていないか？** → `signalMeaning` / `ifVersion` で回答できるか
3. **unknown が放置されていないか？** → `unknownDeadline` が設定され期限切れでないか

---

## 関連ファイル

- `domain/oemtraceability/` — OemApproval, OemCustomization エンティティ
- `domain/cansignals/CanSignal.java` — CAN信号エンティティ
- `domain/safety/SafetyTraceRecord.java` / `SafetyTraceLink.java` — 安全トレース
- `domain/knowledgebase/KnowledgeArticle.java` — ナレッジ記事