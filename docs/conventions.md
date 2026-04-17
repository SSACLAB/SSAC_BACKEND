# 코딩 컨벤션

## 네이밍

| 대상 | 규칙 | 예시 |
|------|------|------|
| 클래스 | PascalCase | `UserService`, `PostController` |
| 메서드/변수 | camelCase | `findUserById`, `userName` |
| 상수 | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE` |
| 패키지 | 소문자, 단수형 | `com.ssac.ssacbackend.domain.user` |
| DB 컬럼 | snake_case | `created_at`, `user_id` |
| API 경로 | kebab-case, 복수형 | `/api/v1/users`, `/api/v1/posts` |
| DTO | 접미사로 `Request` / `Response` | `CreateUserRequest`, `UserResponse` |

---

## 클래스 명명 접미사 규칙

| 접미사 | 역할 |
|--------|------|
| `Controller` | REST 엔드포인트 |
| `Service` | 비즈니스 로직 |
| `Repository` | 데이터 접근 (interface) |
| `Entity` 없음 | 엔티티는 도메인 이름만 사용 (`User`, `Post`) |
| `Request` | 인바운드 DTO |
| `Response` | 아웃바운드 DTO |
| `Exception` | 커스텀 예외 |
| `Config` | Spring 설정 빈 |

---

## API 응답 형식

모든 API는 `ApiResponse<T>` 래퍼를 사용한다.

```json
{
  "success": true,
  "data": { ... },
  "message": null
}
```

```json
{
  "success": false,
  "data": null,
  "message": "사용자를 찾을 수 없습니다."
}
```

- 성공: HTTP 200 / 201, `success: true`
- 실패: 적절한 4xx/5xx, `success: false`, `message`에 한국어 설명
- `GlobalExceptionHandler`에서 예외를 잡아 변환한다
- 컨트롤러에서 직접 에러 응답을 만들지 않는다

---

## 트랜잭션 규칙

- `@Transactional`은 **Service 레이어에만** 붙인다
- 읽기 전용 메서드: `@Transactional(readOnly = true)`
- Controller에 `@Transactional` 절대 금지

---

## 예외 처리

- 비즈니스 예외는 `BusinessException`(또는 하위 클래스)을 사용
- `RuntimeException`을 직접 던지지 않는다
- 예외 메시지는 한국어로 작성 (사용자에게 노출될 수 있음)

```java
throw new BusinessException("사용자를 찾을 수 없습니다.");  // OK
throw new RuntimeException("User not found");              // 금지
```

---

## 로깅

- `@Slf4j` (Lombok) 사용
- Controller 진입/종료: DEBUG 레벨
- 비즈니스 예외: WARN 레벨
- 시스템 예외/예상치 못한 오류: ERROR 레벨
- 로그에 비밀번호·토큰·개인정보 포함 금지

```java
log.debug("유저 조회 요청: userId={}", userId);  // OK
log.info("password={}", password);               // 절대 금지
```

---

## Lombok 사용 규칙

- `@Data` 대신 `@Getter` + 필요한 어노테이션만 사용 (엔티티에서 `@Data` 금지)
- 엔티티: `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 기본
- DTO: `@Getter`, `@Builder` 또는 `record` 사용 검토

---

## 파일 길이 제한

- 클래스 파일: 300줄 이내 권장 (초과 시 분리 검토)
- 메서드: 30줄 이내 권장
