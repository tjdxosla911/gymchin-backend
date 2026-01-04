# PRD-T04: Flutter 붙이기 전 JWT 인증 “최소 형태” 정리
# GymChin MVP

## 0. 목적(Why)
Flutter(모바일) 클라이언트를 붙이기 전에, JWT 인증 흐름을 “한 번에” 고정한다.
목표는 아래 4가지를 일관되게 만들고, Swagger/E2E/Test-UI까지 동일한 규칙으로 검증 가능하게 하는 것이다.

- 토큰 정책(Access/Refresh, 만료, 재발급)
- 보호/공개 엔드포인트 범위
- 에러 코드/응답 계약(특히 401/403 케이스)
- 개발/테스트 편의(Swagger Authorize, E2E 자동 갱신, Test-UI 동일 동작)

## 1. 범위(In Scope)
- Access Token + Refresh Token 구조 확정 및 구현(최소)
- Refresh Token 저장 방식(최소: DB 저장)과 회전/폐기 규칙(최소: 1개 활성 토큰 유지)
- Spring Security 설정 정리(permitAll 경로, 인증 필터, 예외 처리)
- Swagger(OpenAPI) Bearer 인증 스키마 등록
- E2E(Playwright API 테스트)에서 만료/갱신 흐름 검증 1개 이상 추가
- Test-UI에서 토큰 표시/리프레시 버튼 제공(최소)

## 2. 비범위(Out of Scope)
- 소셜 로그인, 이메일 인증, 비밀번호 재설정
- 디바이스 바인딩, IP 제한, 고급 리스크 정책
- RBAC(권한 모델) 고도화(최소는 “인증됨/아님” 중심)
- 토큰 블랙리스트/로그아웃 고도화(최소는 refresh 폐기 정도)

## 3. 현재 전제(기존 프로젝트 기반)
- Base Path: /api/v1
- 공통 응답 포맷: { success, data, error, timestamp }
- 인증 API: /auth/signup, /auth/login, /auth/refresh
- 보호 API: /me, /gymmates, /matchings 등

## 4. 목표 계약(Definition of Done)
아래 항목이 모두 충족되면 완료로 본다.

- 로그인 응답이 accessToken + refreshToken을 모두 반환한다.
- Authorization: Bearer {accessToken}이 없으면 보호 API는 401(AUTH_UNAUTHORIZED)로 응답한다.
- accessToken 만료 시 보호 API는 401(AUTH_TOKEN_EXPIRED)로 응답한다.
- refresh 호출 성공 시 새 accessToken을 발급하고, 필요 시 refreshToken도 회전한다(최소: 회전 옵션은 구현하되 기본은 유지해도 됨).
- refreshToken이 유효하지 않으면 401(AUTH_UNAUTHORIZED)로 응답한다.
- Swagger UI에서 Authorize로 accessToken을 넣으면 보호 API 호출이 정상 동작한다.
- E2E에서 “만료 → refresh → 원요청 재시도” 시나리오가 최소 1개 이상 통과한다.
- Test-UI에서 현재 토큰 표시 + refresh 버튼 + 보호 API 호출이 가능하다.

## 5. 토큰 정책(최소 표준)
- Access Token TTL: 30분(환경변수로 조정 가능)
- Refresh Token TTL: 14일(환경변수로 조정 가능)
- 서명 알고리즘: HS256(대칭키, MVP)
- Claim 최소: sub(userId), iat, exp
- Refresh 저장: DB 테이블(refresh_tokens) 또는 users 테이블 컬럼(추천: 별도 테이블)
- “사용자당 활성 refresh 1개” 정책(최소): 로그인 시 기존 refresh 무효화 후 새로 저장(선택)
  - MVP에서는 “다중 디바이스”를 일단 포기하고 단순화

## 6. API 계약(변경/추가)

### 6.1 POST /api/v1/auth/login (응답 확장)
성공 시 data에 아래 포함:
- accessToken: string
- refreshToken: string
- tokenType: "Bearer"
- expiresIn: number (초)  # access 만료까지

### 6.2 POST /api/v1/auth/refresh (표준화)
요청 바디:
- refreshToken: string

성공 응답 data:
- accessToken: string
- refreshToken: string (옵션: 회전 시만 새 값)
- tokenType: "Bearer"
- expiresIn: number

에러:
- refreshToken 만료/불일치: 401 / AUTH_UNAUTHORIZED
- 기타: 500 / INTERNAL_ERROR

### 6.3 POST /api/v1/auth/logout (추가, 최소)
요청:
- Authorization: Bearer accessToken (또는 refreshToken 바디 방식 중 택1)
권장 최소:
- 바디: refreshToken: string
동작:
- 해당 refreshToken을 DB에서 삭제/무효화

## 7. Spring Security 정리(구현 규칙)
- permitAll:
  - /api/v1/auth/**
  - /swagger-ui/**, /v3/api-docs/**
  - /test-ui/** (개발용 UI)
- 나머지 /api/v1/** 는 인증 필요

- 인증 필터:
  - Authorization 헤더에서 Bearer 토큰 파싱
  - 토큰 만료면 AUTH_TOKEN_EXPIRED로 401
  - 토큰 위조/형식 오류면 AUTH_UNAUTHORIZED로 401

- 예외 핸들링:
  - GlobalExceptionHandler(또는 Security EntryPoint/AccessDeniedHandler)에서 공통 응답 포맷 유지

## 8. Swagger(OpenAPI) 설정
- OpenAPI Security Scheme에 HTTP Bearer(jwt) 추가
- 보호 API에 기본 Security Requirement 적용
- 결과: Swagger UI에서 Authorize 버튼으로 accessToken 입력 후 실행 가능

## 9. E2E(Playwright) 확장 테스트(최소 1개)
- 시나리오: accessToken을 “짧은 TTL”로 발급(테스트 프로필에서 3초 등)
  - 보호 API 호출 → 401(AUTH_TOKEN_EXPIRED) 확인
  - refresh 호출 → 새 accessToken 수령
  - 동일 보호 API 재호출 → 200 성공
- 주의: 테스트에서 TTL을 줄이기 위한 방법
  - (권장) Spring profile: test-e2e 에서 access ttl을 3초로 설정
  - 또는 환경변수 JWT_ACCESS_TTL_SECONDS로 덮어쓰기

## 10. Test-UI 개선(최소)
- 토큰 영역:
  - accessToken / refreshToken 표시(마스킹 옵션)
  - Refresh 버튼: /auth/refresh 호출 후 accessToken 갱신
  - Logout 버튼: /auth/logout 호출 후 토큰 제거
- 보호 API 호출은 항상 accessToken을 Authorization에 넣어 호출하도록 통일

## 11. 작업 순서(권장)
1) DB 스키마 추가(Refresh 토큰 저장)
2) JwtService 유틸 정리(생성/검증/파싱/만료 판별)
3) AuthController 응답 스펙 수정(login에 refresh 포함)
4) Refresh API 표준화(재발급)
5) Logout API 추가(최소)
6) SecurityConfig permitAll/필터/EntryPoint/DeniedHandler 정리
7) Swagger Security Scheme 적용
8) E2E 시나리오 1개 추가(만료→refresh→재시도)
9) Test-UI에 Refresh/Logout 추가

## 12. 주의사항(함정 포인트)
- 401을 전부 같은 코드로 뭉개면 Flutter 자동 갱신 로직이 어려워진다.
  - 만료는 AUTH_TOKEN_EXPIRED, 그 외는 AUTH_UNAUTHORIZED로 구분 유지
- swagger-ui / v3/api-docs는 security에서 막히면 “Failed to load API definition”이 다시 나타난다.
- refreshToken을 클라이언트 저장소(Flutter Secure Storage)에 둘 계획이면, 서버는 refresh를 최소한 DB로 추적해야 회수/로그아웃이 된다.

---

## 13. 파일/폴더 배치 가이드
- 본 PRD 파일: `prd/security/PRD-T04-jwt-minimum.md`
- 구현 파일은 기존 컨벤션 유지(예: `src/main/java/.../security`, `.../auth` 등)
- e2e 테스트는 기존 `e2e/` 폴더 유지
