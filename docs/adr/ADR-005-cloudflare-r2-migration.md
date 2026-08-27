# ADR-005: 이미지 저장소를 Cloudinary에서 Cloudflare R2로 전환

## 맥락 (Context)
[[ADR-002-cloudinary-over-s3.md|ADR-002]]에서 Notion 이미지 URL 만료 문제를 해결하기 위해
Cloudinary를 이미지 영구 저장소로 채택했다. 당시 ADR-002의 "향후 검토 필요 항목"에
아래 트레이드오프가 명시되어 있었다.

- Cloudinary 외부 서비스 의존 추가
- 무료 플랜 스토리지/전송량 한도 존재 (트래픽 증가 시 유료 전환 필요)
- 마이그레이션 실패 시 원본 URL 유지 → 1시간 후 이미지 깨짐 가능

이 중 스토리지/전송량 한도 리스크에 대한 대안으로 아래 두 가지가 검토됐다.

| 대안 | 장점 | 단점 | 결론 |
|-----|-----|-----|-----|
| Cloudinary 유지 (유료 플랜 전환) | 코드 변경 없음, 이미지 변환 API(리사이즈 등) 계속 사용 가능 | 트래픽 증가 시 스토리지+전송량 과금이 함께 증가 | 미채택 |
| AWS S3 | 표준적인 S3 API, 생태계 풍부 | 스토리지 요금 외 **데이터 전송(egress) 요금**이 트래픽에 비례해 발생 | 미채택 |
| Cloudflare R2 | S3 호환 API(AWS SDK v2 그대로 재사용 가능), **egress 요금 없음**(무료) | Cloudinary의 온더플라이 이미지 변환 기능은 없음(저장/서빙만) | **채택** |

## 결정 (Decision)
**Cloudflare R2를 이미지 저장소로 채택한다.**

채택 이유:
- R2는 S3 호환 API를 제공하므로 AWS SDK v2의 `S3Client`를 그대로 사용하고 `endpointOverride`만
  R2 엔드포인트로 재정의하면 된다 — Cloudinary SDK 대비 마이그레이션 코드 변경 폭이 작다
  (`R2Config`, `NotionImageMigrator` 수정 수준)
- R2는 아웃바운드 데이터 전송(egress) 요금이 없어, 콘텐츠 이미지 트래픽이 늘어나도
  전송량 비용이 발생하지 않는다 (S3/Cloudinary 대비 구조적 이점)
- 커스텀 도메인(`img.ssac.io`)을 붙여 Cloudflare CDN을 그대로 활용 가능
- 이미지 온더플라이 변환(리사이즈 등)은 현재 사용하지 않고 있어 Cloudinary 전용 기능 손실이 없음

구현 방식 (전환 후):
- 실시간 마이그레이션: 콘텐츠 상세 조회(`GET /api/v1/contents/{id}`) 시마다
  `NotionBlockFetchService`가 이미지 블록의 URL을 `NotionImageMigrator.migrateIfNeeded()`로
  R2에 업로드하고 영구 URL로 교체
- 레거시 데이터 이관: 기존 `res.cloudinary.com` 썸네일 자산은 1회성 배치
  (`ThumbnailR2MigrationService`, `POST /admin/contents/thumbnails/migrate-legacy`)로
  R2에 재업로드하고, 원본 URL은 `content.thumbnail_url_legacy`에 백업
- 프론트엔드: `next.config.ts`의 `images.remotePatterns`와 CSP `img-src`를
  `res.cloudinary.com` → `img.ssac.io`(커스텀 도메인)로 교체

```
NotionBlockFetchService.parseImageBlock()
    → NotionImageMigrator.migrateIfNeeded(rawUrl)
        → (이미 R2/Cloudinary URL이면 스킵)
        → S3Client.putObject(bucket, key="content-thumbnails/{uuid}.ext")
        → publicBaseUrl + "/" + key 반환
```

## 결과 (Consequences)
**긍정적 영향:**
- 이미지 전송량에 비례한 비용 증가 리스크 제거 (egress 무료)
- AWS SDK v2 기반이라 향후 S3 등 다른 S3 호환 스토리지로도 전환 부담이 적음
- Cloudinary 계정/API 키 의존성 제거 (정리 대상)

**부정적 영향 / 트레이드오프:**
- Cloudinary의 온더플라이 이미지 변환(자동 리사이즈/포맷 변환) 기능을 잃음
  → 현재 구조는 원본을 그대로 저장/서빙하며, 프론트엔드 Next.js Image 최적화(`/_next/image`)로
    리사이즈를 대신 처리
- 레거시 배치 마이그레이션 실패 항목은 원본 Cloudinary URL을 그대로 유지하므로,
  Cloudinary 계정을 완전히 해지하기 전 배치 마이그레이션 성공률(현재 100%, 23/23건) 재확인 필요
- `R2Config`는 자격증명 미설정 시 `AnonymousCredentialsProvider`로 폴백하는데, 이 경우 실제 업로드는
  인증 오류로 실패하고 `NotionImageMigrator`가 원본 URL을 반환하는 폴백에 의존한다
  (로컬/테스트 환경에서는 의도된 동작이나, 운영 환경 자격증명 누락 시 조용히 실패할 수 있음)

**향후 검토 필요 항목:**
- Cloudinary 계정/API 키(`CLOUDINARY_CLOUD_NAME/API_KEY/API_SECRET`) 정리 완료 여부 추적
- 이미지 리사이즈/포맷 변환이 필요해질 경우 R2 앞단에 Cloudflare Images 또는 Workers 기반 변환 레이어 검토

## 프로토콜 반영 필요 여부
- [ ] self-diagnose.md → 해당 없음
- [ ] sc-structure-check.md → 해당 없음
- [ ] testing.md → 해당 없음
- [ ] CLAUDE.md → 해당 없음
- [ ] flyway.md → 해당 없음

## 작성일
2026-08-27

## 작성자
에이전트 (소급 작성, PR #187/#188 및 프로덕션 검증 결과 기반)
