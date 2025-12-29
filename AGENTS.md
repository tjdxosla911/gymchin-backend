# AGENTS.md (GymChin Backend - Cursor/Codex 작업 규칙)

이 문서는 Cursor/Codex가 GymChin 백엔드 작업을 수행할 때 반드시 지켜야 하는 실행 규칙이다.
PRD와 API 계약을 “정답”으로 간주하고, 추측으로 기능을 확장하지 않는다.

## 0. 최우선 참조 문서
- 루트 `PRD.md` (제품 방향/범위)
- `contract/api-spec.md` (응답 래퍼/에러/페이지네이션/도메인 필드/상태 전이 계약)
- `prd/backend/PRD-02-gymmate-matching.md` (이번 작업 티켓)

## 1. 작업 방식 규칙
- 기존 코드/파일을 먼저 검색하고, 중복 구현을 금지한다.
- 한 번에 너무 많은 파일을 바꾸지 말고, “작게 만들고 실행/테스트로 검증”한다.
- PRD에 없는 기능은 추가하지 않는다(추천 알고리즘, GPS 거리 계산, 채팅, 결제 등).
- DTO/Entity는 분리한다. Entity를 API 응답으로 직접 노출하지 않는다.
- 모든 API는 공통 응답 래퍼(success/data/error/timestamp)를 사용한다.

## 2. 패키지/아키텍처 규칙
- 기능 중심 패키지 + 레이어 분리:
  - `com.gymchin.api.common`
  - `com.gymchin.api.user`
  - `com.gymchin.api.matching`
- 각 기능 패키지 내부 구조(권장):
  - `controller`, `service`, `repository`, `dto`, `entity`
- 도메인 규칙(상태 전이/중복 방지/권한 검증)은 Service 레이어에 응집한다.
- Repository는 조회/저장 역할에 집중하고, 비즈니스 규칙을 넣지 않는다.

## 3. 보안/인증 규칙(MVP 개발 단계)
- 인증이 필요한 API는 Spring Security 보호 하에 둔다.
- 개발 단계에서는 최소한의 인증 경로를 제공한다(예: 임시 사용자 컨텍스트 주입 또는 간단한 JWT).
- 단, 어떤 방식이든 “requesterUserId는 토큰 기준” 원칙을 지킨다.
- Swagger(OpenAPI)를 붙이는 경우, `/swagger-ui/**`, `/v3/api-docs/**`는 인증 예외로 둘 수 있다.

## 4. 데이터/시간 규칙
- 날짜/시간은 API 계약에 따라 ISO-8601 문자열(타임존 포함)로 응답한다.
- DB 저장은 UTC 권장을 따르되, 응답은 타임존 포함 문자열로 반환한다.

## 5. 에러 처리 규칙
- 예외는 `GlobalExceptionHandler`에서 잡아 API 계약의 실패 포맷으로 변환한다.
- 유효성 오류는 `VALIDATION_FAILED` + details.fieldErrors 형태를 사용한다.
- 매칭 중복/상태 전이 불가 등 충돌은 409로 처리하고, 계약된 에러 코드를 사용한다.

## 6. 테스트 규칙(최소)
- PRD-02 범위에서 최소 3개 테스트를 작성한다:
  - 중복 매칭 생성 방지
  - 상태 전이 정상/비정상
  - 자기 자신에게 매칭 요청 방지
- 테스트는 “서비스 레벨” 또는 “컨트롤러 통합 테스트” 중 하나로 통일한다(혼용 최소화).

## 7. 커밋/변경 규칙(권장)
- 문서 추가/구조 세팅/기능 구현/리팩토링은 가능한 분리해서 커밋한다.
- 커밋 메시지 예:
  - `docs: add PRD-02 matching ticket`
  - `chore: add common response wrapper and exception handler`
  - `feat: implement matching create and status transition`
