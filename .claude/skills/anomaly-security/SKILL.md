---
name: anomaly-security
description: Security patterns for AnomalyDetectionJava — @PreAuthorize conventions, Permission constants structure, JWT customization, multi-tenancy security, common vulnerabilities to avoid. Use when adding authorization, reviewing security-sensitive code, or writing authenticated tests.
---

# Security Patterns

## @PreAuthorize の原則

すべての AppService public メソッドに付与。Controller レベルではなく AppService レベルで制御。

```java
// 閲覧系
@Transactional(readOnly = true)
@PreAuthorize("hasAuthority('" + XxxPermissions.DEFAULT + "')")
public List<XxxDto> getList(...) { ... }

// 作成
@PreAuthorize("hasAuthority('" + XxxPermissions.CREATE + "')")
public XxxDto create(...) { ... }

// 更新
@PreAuthorize("hasAuthority('" + XxxPermissions.EDIT + "')")
public Optional<XxxDto> update(...) { ... }

// 削除
@PreAuthorize("hasAuthority('" + XxxPermissions.DELETE + "')")
public boolean delete(...) { ... }

// 承認 (ワークフロー系)
@PreAuthorize("hasAuthority('" + XxxPermissions.APPROVE + "')")
public Optional<XxxDto> approve(...) { ... }
```

## Permission 定数の構造

各モジュールの `*Permissions.java` は `application-contracts` モジュールに置く:

```java
public final class CanSignalPermissions {
    private CanSignalPermissions() {}
    public static final String GROUP   = "AnomalyDetection.CanSignals";
    public static final String DEFAULT = GROUP + ".Default";  // 一覧/閲覧
    public static final String CREATE  = GROUP + ".Create";
    public static final String EDIT    = GROUP + ".Edit";
    public static final String DELETE  = GROUP + ".Delete";
}
```

命名規則: `AnomalyDetection.<Module>.<Action>`

## JWT クレーム構造

アクセストークンに含まれる権限クレーム:

```json
{
  "sub": "<userId>",
  "tenantid": "<tenantId or null>",
  "roles": ["admin", "engineer"],
  "oi_prst": ["AnomalyDetection.CanSignals.Default", "AnomalyDetection.Logic.Create"]
}
```

- `tenantid` — テナント識別。null は Host 管理者
- `oi_prst` — 解決済み権限リスト (DB 再アクセス不要)
- リソースサーバは JWT の `oi_prst` を `SimpleGrantedAuthority` にマッピング

## テナントセキュリティ

```java
// TenantResolutionFilter の優先順位:
// 1. JWT の tenantid クレーム (改ざん不可・最優先)
// 2. __tenant ヘッダ (JWT と不一致なら 403)

// Hibernate Filter は自動で有効化される
// condition: "tenant_id = :tenantId OR tenant_id IS NULL"
// → OEM 共通データ (tenant_id IS NULL) は全テナントから参照可
```

## よくある漏れ・バグ

```java
// ❌ @PreAuthorize なし → 認証済みなら誰でも実行可
public boolean testConnection(UUID id) { ... }

// ❌ 権限文字列のハードコード → 定数変更時に不整合
@PreAuthorize("hasAuthority('AnomalyDetection.Logic.Create')")  // NG

// ✅ 定数 import で統一
@PreAuthorize("hasAuthority('" + AnomalyDetectionPermissions.LOGIC_CREATE + "')")  // OK

// ❌ findAll() + Java フィルタでテナント分離 → Hibernate Filter が効いているので
//    findAll() 自体はテナントフィルタ済みだが、全件 DB ロードはパフォーマンス問題
```

## CORS / CSRF

- REST API は JWT ベースのため CSRF 無効 (`http.csrf().disable()`)
- CORS 許可オリジンは `settings` テーブルで管理 (将来)
- 認可サーバの FormLogin のみ CSRF 有効

## セキュリティレビューチェックリスト

- [ ] 新規 AppService の全 public メソッドに `@PreAuthorize` がある
- [ ] Permission 定数は `*Permissions.java` の定数を使っている
- [ ] テスト JWT には対応する authorities が含まれている
- [ ] `delete()` は `existsById` 後に削除 (TOCTOU は許容範囲)
- [ ] ユーザ入力の UUID は `UUID.fromString()` でパースしている (例外で 400)
