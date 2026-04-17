# AGENTS.md — SSAC Backend 에이전트 진입점

## 이 저장소는 무엇인가

SSAC 서비스의 Spring Boot 백엔드 API 서버다.
Java 17, Spring Boot 4.x, Spring Security, JPA(MySQL) 스택으로 구성된다.

---

## 코드 작성 전 반드시 읽어야 할 파일

| 파일 | 읽어야 하는 이유 |
|------|----------------|
| `docs/architecture.md` | 레이어 구조와 의존성 방향 — 위반 시 빌드 실패 |
| `docs/conventions.md` | 네이밍, 패키지, 응답 형식 컨벤션 |
| `docs/decisions/` | 이미 결정된 사항을 다시 논의하지 않기 위해 |

> 위 파일을 읽지 않고 코드를 작성하면 PR이 거절된다.

---

## 커밋 전 체크리스트

```
[ ] ./gradlew build -x test   # 컴파일 오류 없음
[ ] ./gradlew test             # 테스트 전체 통과 (ArchUnit 포함)
[ ] ./gradlew checkstyleMain   # 스타일 위반 없음
[ ] 새 레이어 의존성 추가 시 docs/architecture.md 업데이트
[ ] 새 설계 결정 시 docs/decisions/ 에 ADR 추가
```

---

## 금지 행동

- `master` 브랜치 직접 커밋 — PR을 통해서만 병합
- `controller` → `repository` 직접 의존 — `service` 레이어를 반드시 경유
- `domain` 패키지에서 외부 레이어 클래스 import
- 환경변수·비밀키를 소스코드에 하드코딩
- `@Transactional`을 `controller`에 붙이는 것
- docs/ 없이 아키텍처 결정 구두로만 처리

---

## 모르는 것이 있을 때

- 레이어/의존성 → `docs/architecture.md`
- 네이밍/포맷 → `docs/conventions.md`
- 이미 내린 결정 → `docs/decisions/`
- 온보딩/환경 설정 → `docs/onboarding.md`
- 현재 기술 부채 → `docs/quality.md`

답을 찾지 못했다면 임의로 결정하지 말고 PR 설명란에 `[QUESTION]` 태그로 질문을 남겨라.
