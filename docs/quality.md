# 품질 현황 및 기술 부채

> 마지막 업데이트: 2026-04-17

---

## 현재 품질 등급

| 영역 | 등급 | 설명 |
|------|------|------|
| 아키텍처 구조 | 🟡 초기 | 레이어 정의 완료, 실제 코드 없음 |
| 테스트 커버리지 | 🔴 없음 | ArchUnit 설정 예정, 비즈니스 테스트 0% |
| API 문서화 | 🔴 없음 | Swagger/SpringDoc 미설치 |
| 보안 설정 | 🟡 기본 | Spring Security 의존성만 추가, 실제 설정 미완 |
| 에러 처리 | 🔴 없음 | GlobalExceptionHandler 미구현 |
| 로깅 | 🔴 없음 | 구조화 로깅 미설정 |
| DB 마이그레이션 | 🔴 없음 | Flyway/Liquibase 미설치 |

등급 기준: 🟢 완료 | 🟡 부분 완료 | 🔴 없음

---

## 알려진 기술 부채

### [DEBT-001] DB 마이그레이션 도구 부재
- **영향**: `spring.jpa.hibernate.ddl-auto`로 스키마 관리 중, 운영 환경에서 위험
- **해결 방향**: Flyway 도입 (예정)
- **우선순위**: 높음

### [DEBT-002] API 문서화 없음
- **영향**: 프론트엔드 팀과 API 계약 불명확
- **해결 방향**: SpringDoc OpenAPI(Swagger) 추가
- **우선순위**: 높음

### [DEBT-003] 전역 예외 핸들러 미구현
- **영향**: 에러 응답 형식이 Spring 기본값, 클라이언트 파싱 불가
- **해결 방향**: `GlobalExceptionHandler` + `BusinessException` 구현
- **우선순위**: 최우선

### [DEBT-004] ArchUnit 의존성 미설치
- **영향**: 레이어 의존성 규칙 기계적 강제 불가
- **해결 방향**: `build.gradle`에 ArchUnit 추가 + `ArchitectureTest.java` 작성
- **우선순위**: 높음

### [DEBT-005] 환경별 설정 분리 없음
- **영향**: `application.properties` 단일 파일, dev/prod 구분 없음
- **해결 방향**: `application-dev.properties`, `application-prod.properties` 분리
- **우선순위**: 중간

---

## 부채 해소 기록

| ID | 해소일 | 담당자 | 메모 |
|----|--------|--------|------|
| (없음) | - | - | - |

---

## 다음 스프린트 품질 목표

- [ ] GlobalExceptionHandler + BusinessException 구현 (DEBT-003)
- [ ] ArchUnit 의존성 추가 및 기본 규칙 테스트 작성 (DEBT-004)
- [ ] SpringDoc 추가 및 기본 API 문서화 (DEBT-002)
