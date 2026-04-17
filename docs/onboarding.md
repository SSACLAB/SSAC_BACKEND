# 온보딩 가이드

> 새로운 팀원(또는 에이전트)이 처음 읽는 문서.

---

## 이 프로젝트는 무엇인가

SSAC 서비스의 Spring Boot 백엔드 API 서버.
현재 초기 세팅 단계이며, 팀원 3명이 협업 중이다.

---

## 기술 스택

| 항목 | 버전/선택 |
|------|----------|
| Java | 17 |
| Spring Boot | 4.0.5 |
| Spring Security | Boot 내장 |
| Spring Data JPA | Boot 내장 |
| DB | MySQL |
| 빌드 | Gradle 9.x |
| Lombok | 최신 |

---

## 로컬 환경 설정

### 1. 사전 요구사항
- Java 17 설치 (`java -version` 확인)
- MySQL 8.x 설치 및 실행
- (선택) IntelliJ IDEA

### 2. 저장소 클론 후 설정

```bash
git clone <repo-url>
cd ssac-backend
```

### 3. 환경변수 설정

`src/main/resources/application-local.properties` 파일을 생성하고 아래를 채운다.
(이 파일은 `.gitignore`에 포함되어 있다. 절대 커밋하지 않는다.)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ssac_db
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
spring.jpa.hibernate.ddl-auto=update
```

### 4. 빌드 및 실행

```bash
./gradlew build
./gradlew bootRun --args='--spring.profiles.active=local'
```

### 5. 테스트 실행

```bash
./gradlew test
```

---

## 브랜치 전략

```
master          ← 배포 가능한 상태만 병합
  └── feature/SSACBE-{이슈번호}-{설명}   ← 기능 개발
  └── fix/SSACBE-{이슈번호}-{설명}       ← 버그 수정
```

- `master` 직접 커밋 금지
- PR을 통해서만 병합, 최소 1인 리뷰 필요

---

## 커밋 메시지 형식

```
{type}: [{이슈번호}] {한국어 설명}

예시:
feat: [SSACBE-12] 유저 로그인 API 구현
fix: [SSACBE-15] 토큰 만료 오류 수정
refactor: [SSACBE-20] UserService 책임 분리
docs: [SSACBE-5] architecture.md 레이어 다이어그램 추가
```

| type | 의미 |
|------|------|
| `feat` | 새 기능 |
| `fix` | 버그 수정 |
| `refactor` | 동작 변경 없는 리팩토링 |
| `docs` | 문서만 변경 |
| `test` | 테스트만 추가/변경 |
| `chore` | 빌드, 설정 변경 |

---

## 코드 작성 전 필독

1. `docs/architecture.md` — 레이어와 의존성 방향 확인
2. `docs/conventions.md` — 네이밍, 응답 형식, 트랜잭션 규칙 확인
3. `docs/decisions/` — 이미 결정된 사항 확인

---

## 자주 묻는 질문

**Q: 새 도메인(엔티티)을 추가할 때 무엇을 만들어야 하는가?**
A: `domain/{name}/{Name}.java` (Entity) → `domain/{name}/{Name}Repository.java` (Interface) → `service/{Name}Service.java` → `controller/{Name}Controller.java` → `dto/request/{Create|Update}{Name}Request.java` + `dto/response/{Name}Response.java` 순서로 생성. architecture.md의 패키지 구조 예시 참고.

**Q: 테스트는 어디에 어떻게 작성하는가?**
A: `src/test/java/com/ssac/ssacbackend/` 하위에 동일한 패키지 구조로. Service 단위 테스트는 Mockito, 통합 테스트는 `@SpringBootTest`. 자세한 내용은 추후 `docs/testing.md`로 분리 예정.

**Q: 설계 결정을 내렸을 때 어떻게 기록하는가?**
A: `docs/decisions/NNN-{설명}.md` ADR 형식으로 추가. 템플릿은 `docs/decisions/001-initial-architecture.md` 참고.
