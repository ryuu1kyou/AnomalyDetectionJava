---
name: anomaly-testing
description: Testing patterns for AnomalyDetectionJava — test module placement, MariaDB4j, MockMvc + jwt() with authorities, logicJwt() helper, AssertJ, test pyramid. Use when writing or reviewing any test class in the backend.
---

# Testing Patterns

## テストピラミッドとモジュール配置

仕様の目標 (現状は全て host に集中中 — 将来リファクタリング):

| テスト種別 | 配置先 (仕様) | 現在の配置 |
|---|---|---|
| 純粋ドメイン単体テスト (Spring 不要) | `domain/src/test/java/` | `host/.../domain/` |
| AppService / Application テスト | `application/src/test/java/` | `host/.../application/` |
| Repository / JPA テスト (@DataJpaTest) | `infrastructure/src/test/java/` | 未実装 |
| API 統合テスト (@SpringBootTest) | `host/src/test/java/` | `host/.../api/` |

新規テストを書く場合は**仕様の配置先**に作成すること。

## MariaDB4j セットアップ

すべての @SpringBootTest テストに必須:

```java
@SpringBootTest(classes = AnomalyDetectionApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@ExtendWith(MariaDB4jExtension.class)
class MyApiTest {

    @DynamicPropertySource
    static void registerMariaDb4j(DynamicPropertyRegistry registry) {
        MariaDB4jExtension.register(registry);
    }
}
```

## MockMvc + JWT 認証 (必須パターン)

`@PreAuthorize` が全メソッドに付いているため、テストの JWT には必要な permission authorities を含める必要がある。

### ❌ 間違い (403 になる)
```java
mockMvc.perform(post(BASE).with(jwt()))  // authorities なし → 403
```

### ✅ 正しい (authorities を明示)
```java
// 専用ヘルパーを定義してテスト全体で再利用
private static RequestPostProcessor logicJwt() {
    return jwt().authorities(
        new SimpleGrantedAuthority("AnomalyDetection.Logic.Default"),
        new SimpleGrantedAuthority("AnomalyDetection.Logic.Create"),
        new SimpleGrantedAuthority("AnomalyDetection.Logic.Edit"),
        new SimpleGrantedAuthority("AnomalyDetection.Logic.Approve"));
}

// テスト内で使用
mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(body)
    .with(logicJwt()))
    .andExpect(status().isOk());
```

### Permission 文字列一覧 (主要なもの)

| 権限クラス | 定数 | 値 |
|---|---|---|
| AnomalyDetectionPermissions | LOGIC_DEFAULT | `AnomalyDetection.Logic.Default` |
| AnomalyDetectionPermissions | LOGIC_CREATE | `AnomalyDetection.Logic.Create` |
| IdentityPermissions | TENANTS_VIEW | `AnomalyDetection.Identity.Tenants.View` |
| CanSignalPermissions | DEFAULT | (CanSignalPermissions.java を参照) |

## テスト命名規則

```java
// 日本語でも英語でも OK。意図が明確であること
void createLogicStartsAsDraft()
void submitForApprovalTransitionsToPending()
void listRequiresAuthentication()   // 401 テスト
```

## 認証なしテスト (401 確認)

```java
@Test
void listRequiresAuthentication() throws Exception {
    mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
}
```

## AssertJ スタイル

```java
// JUnit の assertEquals より AssertJ を優先
assertThat(result.name()).isEqualTo("BrakePressureTimeout");
assertThat(result.status()).isEqualTo("DRAFT");
assertThat(result).isNotNull();
```

## ドメイン単体テスト (Spring 不要)

```java
// @SpringBootTest は使わない — 純粋 Java
class CanSignalTest {

    @Test
    void should_start_as_active_when_created() {
        var signal = new CanSignal(UUID.randomUUID(), null, "SIG001", "BrakeSignal");
        assertThat(signal.isActive()).isTrue();
    }

    @Test
    void should_throw_when_invalid_state_transition() {
        // ...
    }
}
```

## テスト作成チェックリスト

- [ ] API テストは `@ExtendWith(MariaDB4jExtension.class)` + `@DynamicPropertySource` がある
- [ ] jwt() には必要な authorities が含まれている (`logicJwt()` パターン)
- [ ] 401 テスト (認証なし) が含まれている
- [ ] ドメイン単体テストは Spring コンテキストを起動していない
- [ ] 新規 AppService のテストは CRUD + 状態遷移を網羅している
