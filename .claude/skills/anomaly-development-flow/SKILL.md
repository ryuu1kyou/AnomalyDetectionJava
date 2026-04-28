---
name: anomaly-development-flow
description: End-to-end development workflow for AnomalyDetectionJava — full checklist for adding a new domain feature from entity to REST API to test. Use when starting any new feature, module, or entity.
---

# Development Workflow

## 新規ドメイン機能の追加フロー

### 1. domain-shared — 列挙型・定数

```java
// backend/domain-shared/src/main/java/com/anomalydetection/domainshared/<module>/
public enum MyStatus { DRAFT, ACTIVE, ARCHIVED }
```

### 2. domain — Entity + Repository インターフェース

```java
// backend/domain/src/main/java/com/anomalydetection/domain/<module>/
@Entity
@Table(name = "my_entities")
@SQLDelete(sql = "UPDATE my_entities SET is_deleted = true, deleted_at = CURRENT_TIMESTAMP(6) WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId OR tenant_id IS NULL")
public class MyEntity extends FullAuditedEntity<UUID> implements MultiTenant {
    // フィールド, コンストラクタ, ドメインメソッド
}

public interface MyEntityRepository extends JpaRepository<MyEntity, UUID> {
    // カスタムクエリのみ (単純 CRUD は JpaRepository に任せる)
}
```

### 3. infrastructure — Liquibase changelog

```yaml
# backend/infrastructure/src/main/resources/db/changelog/0000-baseline/0XX-domain-my.yaml
databaseChangeLog:
  - changeSet:
      id: 0XX-create-my-entities
      author: ryuu1kyou
      changes:
        - createTable:
            tableName: my_entities
            columns:
              - column: { name: id, type: BINARY(16), constraints: { primaryKey: true } }
              - column: { name: tenant_id, type: BINARY(16) }
              - column: { name: name, type: VARCHAR(255), constraints: { nullable: false } }
              # 監査カラム (必須)
              - column: { name: created_at, type: DATETIME(6) }
              - column: { name: created_by, type: VARCHAR(255) }
              - column: { name: last_modified_at, type: DATETIME(6) }
              - column: { name: last_modified_by, type: VARCHAR(255) }
              - column: { name: is_deleted, type: BOOLEAN, defaultValueBoolean: false }
              - column: { name: deleted_at, type: DATETIME(6) }
              - column: { name: deleted_by, type: VARCHAR(255) }
```

### 4. application-contracts — DTO + Permission 定数

```java
// backend/application-contracts/src/main/java/com/anomalydetection/contracts/<module>/
public record MyEntityDto(String id, String name, ...) {}
public record CreateMyEntityDto(String name, ...) {}

public final class MyEntityPermissions {
    public static final String GROUP   = "AnomalyDetection.MyEntity";
    public static final String DEFAULT = GROUP + ".Default";
    public static final String CREATE  = GROUP + ".Create";
    public static final String EDIT    = GROUP + ".Edit";
    public static final String DELETE  = GROUP + ".Delete";
}
```

### 5. application — AppService

```java
// backend/application/src/main/java/com/anomalydetection/application/<module>/
@Service
@Transactional
public class MyEntityAppService {

    @Transactional(readOnly = true)
    @PreAuthorize("hasAuthority('" + MyEntityPermissions.DEFAULT + "')")
    public List<MyEntityDto> getList() { ... }

    @PreAuthorize("hasAuthority('" + MyEntityPermissions.CREATE + "')")
    public MyEntityDto create(CreateMyEntityDto input) { ... }

    @PreAuthorize("hasAuthority('" + MyEntityPermissions.EDIT + "')")
    public Optional<MyEntityDto> update(UUID id, ...) { ... }

    @PreAuthorize("hasAuthority('" + MyEntityPermissions.DELETE + "')")
    public boolean delete(UUID id) { ... }
}
```

### 6. web — REST Controller

```java
// backend/web/src/main/java/com/anomalydetection/web/<module>/
@RestController
@RequestMapping("/api/app/my-entities")
@Tag(name = "MyEntity")
public class MyEntityController {
    // GET / POST / PUT / DELETE エンドポイント
}
```

### 7. テスト

```java
// API テスト: host/src/test/java/com/anomalydetection/host/api/
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc @ActiveProfiles("test") @ExtendWith(MariaDB4jExtension.class)
class MyEntityApiTest {
    @DynamicPropertySource
    static void registerMariaDb4j(DynamicPropertyRegistry r) { MariaDB4jExtension.register(r); }

    private static RequestPostProcessor myJwt() {
        return jwt().authorities(
            new SimpleGrantedAuthority(MyEntityPermissions.DEFAULT),
            new SimpleGrantedAuthority(MyEntityPermissions.CREATE));
    }
}
```

## モジュール依存方向 (違反禁止)

```
domain-shared ← domain ← application-contracts ← application → infrastructure → host
                                                      ↑
                                                     web
```

- `web` → `infrastructure` の直接参照禁止 (ArchUnit で検証)
- `domain` → Spring 注入禁止 (`@Autowired` など)
- `application` → `web` の参照禁止

## 新機能追加チェックリスト

- [ ] domain-shared: 列挙型・定数
- [ ] domain: Entity (`@SQLDelete` + `@SQLRestriction` + `@Filter`)
- [ ] domain: Repository インターフェース
- [ ] infrastructure: Liquibase changelog (監査カラム含む)
- [ ] application-contracts: DTO (record), Permission 定数
- [ ] application: AppService (全メソッドに `@PreAuthorize`)
- [ ] web: REST Controller + OpenAPI `@Tag`
- [ ] test: API テスト (CRUD + 認証なし 401 + ワークフロー)
- [ ] test: ドメイン単体テスト (状態遷移)
- [ ] `mvn test` が全 PASS

## ビルド確認コマンド

```bash
# プロジェクトルートの backend/ で実行
mvn test -pl host -am

# 特定テストのみ
mvn test -pl host -Dtest=MyEntityApiTest
```
