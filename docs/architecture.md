# 아키텍처 — 레이어 구조 및 의존성 지도

## 레이어 정의

```
com.ssac.ssacbackend
├── domain/          # 엔티티, VO, 도메인 예외, Repository 인터페이스
├── repository/      # JPA Repository 구현체, QueryDSL
├── service/         # 비즈니스 로직, @Transactional 경계
├── controller/      # REST 컨트롤러, 요청/응답 매핑
├── dto/             # Request / Response DTO (레이어 공유 허용)
├── config/          # Spring 설정 클래스 (Security, JPA 등)
└── common/          # 공통 유틸, 전역 예외 핸들러, 상수
```

---

## 의존성 방향 규칙 (단방향, 위반 불가)

```
controller
    ↓ (service만 호출 가능)
service
    ↓ (domain, repository만 호출 가능)
repository
    ↓ (domain만 참조 가능)
domain
    (외부 레이어 import 금지)

dto       ← controller, service 양쪽에서 import 가능
config    ← 모든 레이어에서 import 가능 (단, 설정 빈만)
common    ← 모든 레이어에서 import 가능
```

### 명시적 금지 관계

| 출발 레이어 | 금지된 import 대상 | 이유 |
|------------|-------------------|------|
| `domain` | `service`, `repository`, `controller`, `dto` | 도메인은 순수해야 함 |
| `controller` | `repository` | 서비스 레이어 우회 금지 |
| `repository` | `service`, `controller` | 상위 레이어 역참조 금지 |
| `service` | `controller` | 순환 의존 방지 |

---

## 기계적 강제: ArchUnit 테스트

의존성 규칙은 `src/test/java/.../ArchitectureTest.java` 에서 자동 검증된다.
`./gradlew test` 실행 시 위반이 있으면 빌드가 실패한다.

의존성 규칙을 변경하려면:
1. 이 파일(`docs/architecture.md`)을 먼저 수정
2. `ArchitectureTest.java` 규칙을 동기화
3. ADR을 `docs/decisions/` 에 추가

---

## 패키지 구조 예시

```
com.ssac.ssacbackend
├── domain/
│   ├── user/
│   │   ├── User.java              # @Entity
│   │   └── UserRepository.java    # interface (JPA)
│   └── post/
│       ├── Post.java
│       └── PostRepository.java
├── service/
│   ├── UserService.java
│   └── PostService.java
├── controller/
│   ├── UserController.java
│   └── PostController.java
├── dto/
│   ├── request/
│   └── response/
├── config/
│   ├── SecurityConfig.java
│   └── JpaConfig.java
└── common/
    ├── exception/
    │   ├── GlobalExceptionHandler.java
    │   └── BusinessException.java
    └── response/
        └── ApiResponse.java
```

> 도메인이 추가될 때마다 이 문서를 업데이트하라.
