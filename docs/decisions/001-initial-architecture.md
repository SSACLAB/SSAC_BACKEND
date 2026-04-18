# ADR 001: 초기 아키텍처 결정

**상태**: 채택됨  
**날짜**: 2026-04-17  
**담당자**: 팀 전체

---

## 컨텍스트

프로젝트 초기 단계로, 3명이 AI 에이전트(Claude Code 등)와 함께 협업한다.
에이전트가 일관된 구조를 유지하려면 레이어 규칙을 사전에 명확히 정의해야 한다.

---

## 결정 사항

### 1. 패키지 구조

`com.ssac.ssacbackend` 하위를 기능(feature)이 아닌 **레이어(layer)** 기준으로 분리한다.

```
domain / repository / service / controller / dto / config / common
```

**채택 이유**: 팀 규모가 작고 도메인이 확정되지 않은 초기 단계에서 레이어 기반이 더 명확하다. 도메인 수가 5개 이상으로 늘어나면 feature-based 구조로 전환을 재검토한다.

**거절된 대안**: feature-based 패키지 (`user/`, `post/` 최상위)
- 초기에 레이어 경계 혼동 발생 우려

---

### 2. 의존성 방향

`controller → service → repository → domain` 단방향.
상위 레이어가 하위 레이어를 import하는 것만 허용.

**채택 이유**: Spring 표준 패턴. 에이전트 실수(controller에서 repository 직접 호출)를 ArchUnit으로 자동 감지.

---

### 3. 빌드 도구

Gradle 유지 (Spring Initializr 기본값).

---

### 4. Java 버전

Java 17 LTS 사용. Spring Boot 4.x 최소 요구사항.

---

### 5. 에러 응답 구조

전역 `ApiResponse<T>` 래퍼 + `GlobalExceptionHandler` 패턴.
모든 에러는 `BusinessException` 계층으로 표준화.

**채택 이유**: 클라이언트가 일관된 형식을 파싱할 수 있어야 함. Spring 기본 에러 응답은 구조가 달라 사용 불가.

---

## 결과 및 영향

- `docs/architecture.md`에 레이어 규칙 명문화
- `ArchitectureTest.java`로 기계적 강제 (미구현, DEBT-004)
- 향후 도메인 추가 시 이 ADR 참조하여 패키지 위치 결정

---

## 재검토 조건

- 도메인 수가 5개를 초과할 때 feature-based 전환 여부 논의
- 팀 규모 확장 시 마이크로서비스 분리 여부 논의
