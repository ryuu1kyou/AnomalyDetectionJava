## 変更概要

<!-- この PR で何を変更したかを簡潔に説明してください -->

## 変更の種類

- [ ] バグ修正
- [ ] 新機能
- [ ] リファクタリング (動作変更なし)
- [ ] テスト追加/修正
- [ ] ドキュメント更新
- [ ] CI/CD 設定変更

---

## Automotive Safety トレサビリティ チェックリスト

> automotive-safety スキル TOP3 + 1枚チェックリスト準拠

### 必須記入欄

| 項目 | 値 |
|------|----|
| `feature_id` | <!-- 例: AEB-FEAT-017 → 粒度: 機能まとまり単位で必ず記入 --> |
| `decision_id` | <!-- 例: DR-2026-0418-03 → 該当DRがなければ "N/A" と明記 --> |
| `if_impact` | <!-- CHANGED / UNCHANGED / UNKNOWN (UNKNOWNの場合は下記期限を記入) --> |

### `if_impact = UNKNOWN` の場合のみ記入

| 項目 | 値 |
|------|----|
| 解消期限 (`unknown_until`) | <!-- 例: 2026-06-30 --> |
| 回収責任者 (`unknown_owner`) | <!-- @GitHubユーザ名 --> |

### 台帳状態チェック

- [ ] `feature_id` を記入した (空文字・"TODO" は **不可**)
- [ ] `if_impact` を記入した (`UNKNOWN` の場合は期限と責任者を記入)
- [ ] `UNKNOWN` を期限なしで放置していない
- [ ] 状態遷移を伴う変更は `IllegalStateException` ガードを確認した
- [ ] `doc_sync_status` を更新した (`PENDING` / `UPDATED` / `NOT_REQUIRED`)

### 影響範囲確認

- [ ] Safety ドメイン (`SafetyTraceRecord`) への影響を確認
- [ ] OEM トレサビリティ (`OemApproval` / `OemCustomization`) への影響を確認
- [ ] マルチテナント (TenantId フィルタ) への影響を確認
- [ ] 既存のテストが全て PASS している (`mvn -B verify` / `npm test`)

---

## テスト確認

- [ ] 単体テスト追加/更新済み
- [ ] 統合テスト確認済み
- [ ] `cd backend; ./mvnw -B verify` → BUILD SUCCESS 確認
- [ ] `cd frontend; npm test` → 全 PASS 確認 (フロント変更がある場合)

## レビュー依頼事項

<!-- レビュアーに特に確認してほしい点があれば記載 -->
