---
name: anomaly-appservice
description: Application service patterns for AnomalyDetectionJava — @PreAuthorize, permission constants, toDto() mapping, PagedResultDto, in-memory vs DB paging. Use when creating or reviewing AppService classes or DTOs.
---

# Application Service Patterns

## @PreAuthorize は全メソッドに必須

**すべての public メソッド** に `@PreAuthorize` を付与する。漏れがあると認証済みの任意ユーザが呼べてしまう。

```java
@Service
@Transactional
public class CanSignalAppService {

  @Transactional(readOnly = true)
  @PreAuthorize("hasAuthority('" + CanSignalPermissions.DEFAULT + "')")
  public List<CanSignalDto> getList(...) { ... }

  @PreAuthorize("hasAuthority('" + CanSignalPermissions.CREATE + "')")
  public CanSignalDto create(...) { ... }

  @PreAuthorize("hasAuthority('" + CanSignalPermissions.EDIT + "')")
  public Optional<CanSignalDto> update(...) { ... }

  @PreAuthorize("hasAuthority('" + CanSignalPermissions.DELETE + "')")
  public boolean delete(...) { ... }
}
```

## Permission 定数の場所

```
backend/application-contracts/src/main/java/com/anomalydetection/contracts/
├── cansignals/CanSignalPermissions.java
├── anomalydetection/AnomalyDetectionPermissions.java
├── detectiontemplates/DetectionTemplatePermissions.java
├── identity/IdentityPermissions.java
├── knowledgebase/KnowledgeBasePermissions.java
├── oemtraceability/OemTraceabilityPermissions.java
├── safety/SafetyTracePermissions.java
├── similarpatternsearch/SimilarPatternSearchPermissions.java
└── integration/IntegrationPermissions.java
```

Permission 定数は必ずここから `import` して使う。文字列のハードコードは禁止。

## toDto() パターン (MapStruct 導入前の暫定実装)

```java
private CanSignalDto toDto(CanSignal s) {
    return new CanSignalDto(
        s.getId(), s.getTenantId(), s.getSignalIdentifier(), s.getName(),
        s.getStatus(), s.getCategory(), s.isActive(),
        s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
}
```

MapStruct 導入後は `@Mapper` アノテーションを使うこと（現在は手書き）。

## ページングパターン

現状は in-memory フィルタリング（将来 DB ページングに置換予定）:

```java
public PagedResultDto<CanSignalDto> getList(GetCanSignalsInputDto input) {
    String filter = input != null && input.filter() != null ? input.filter().toLowerCase() : "";
    List<CanSignal> all = signalRepository.findAll();

    var filtered = all.stream()
        .filter(s -> filter.isBlank() || s.getName().toLowerCase().contains(filter))
        .toList();

    int skip = input != null && input.skipCount() != null ? Math.max(0, input.skipCount()) : 0;
    int take = input != null && input.maxResultCount() != null ? Math.max(1, input.maxResultCount()) : 10;

    var page = filtered.stream().skip(skip).limit(take).map(this::toDto).toList();
    return PagedResultDto.of(page, filtered.size());
}
```

## delete() の戻り値パターン

```java
// 存在しない場合 false、削除成功なら true
public boolean delete(UUID id) {
    if (!repository.existsById(id)) return false;
    repository.deleteById(id);  // @SQLDelete で UPDATE に変換される
    return true;
}
```

## アンチパターン

```java
// ❌ @PreAuthorize の漏れ
public boolean testConnection(UUID id) { ... }  // 誰でも呼べてしまう!

// ❌ getRecommendations のような broken ストリーム
.map(pair -> articleRepo.findAll().stream().findFirst()...)  // スコアと記事が対応していない

// ✅ 正しい: score と entity を Map.entry で対にする
.map(a -> Map.entry(computeScore(a), a))
.filter(e -> e.getKey() > 0)
.sorted(...)
.map(e -> toDto(e.getValue()))
```

## 新規 AppService 作成チェックリスト

- [ ] `@Service` + `@Transactional` がクラスに付与されている
- [ ] 読み取り専用メソッドに `@Transactional(readOnly = true)` がある
- [ ] すべての public メソッドに `@PreAuthorize` がある
- [ ] Permission 定数は `*Permissions.java` から import している
- [ ] `toDto()` プライベートヘルパーが実装されている
- [ ] delete() が `existsById` チェック後に削除している
