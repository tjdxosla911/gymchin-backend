# PRD-T01 (Testing)
# Playwright 기반 API E2E 테스트 도입 (GymChin Backend)

## 1. 목적

본 문서는 GymChin 백엔드(Spring Boot)의 핵심 사용자 흐름이 배포/리팩토링 과정에서 깨지지 않도록,
Playwright를 이용한 API 중심 E2E(End-to-End) 테스트를 도입하는 기준과 구현 범위를 정의한다.

본 테스트는 “브라우저 UI 자동화”가 목적이 아니라,
브라우저 자동화 도구인 Playwright의 안정적인 HTTP 클라이언트/테스트 러너를 활용해
API 계약과 핵심 시나리오의 회귀를 빠르게 검증하는 것을 목적으로 한다.

## 2. 범위(이번 도입에서 포함/제외)

### 포함
- Playwright 테스트 프로젝트(e2e 폴더) 구성
- 로컬에서 한 줄로 실행 가능한 E2E 테스트 1세트
- 아래 시나리오의 “API 흐름 검증”
  - 회원 A/B 생성(또는 로그인) → A가 B에게 매칭 요청 → 목록/상태 전이 검증
  - 중복 매칭 요청 방지 검증
  - 자기 자신에게 매칭 요청 방지 검증(가능한 경우)

### 제외(MVP 범위 외)
- 실제 화면(UI) 요소 클릭/탐색 기반 테스트
- CI 파이프라인 연동(GitHub Actions 등)
- 성능 테스트/부하 테스트
- 모바일(Flutter) UI 자동화

## 3. 전제 조건

- 백엔드가 로컬에서 실행 중이어야 한다.
- 기본 Base URL은 아래로 가정한다.
  - http://localhost:8080
- API Base Path는 contract/api-spec.md 규격에 따라 /api/v1 를 사용한다.

## 4. 인증 처리 원칙

E2E 테스트는 실제 운영과 동일한 방식으로 인증을 수행하는 것을 원칙으로 한다.

- 우선 순위 1: /auth/signup, /auth/login 으로 토큰을 발급받아 Authorization: Bearer {token} 으로 호출한다.
- 만약 현재 구현에서 auth가 아직 미완성이라면, 아래 “대체 옵션” 중 1가지를 선택해 적용한다.
  - (대체 옵션 A) e2e 프로필에서 테스트 전용 인증 우회(특정 헤더/고정 사용자) 제공
  - (대체 옵션 B) SecurityConfig에서 로컬 프로필에서만 특정 API를 permitAll 처리

※ 본 레포에서는 우선 순위 1(정상 인증 흐름)을 기본으로 구현한다.

## 5. 테스트 시나리오(정의)

### 시나리오 1: 매칭 요청 생성 및 조회/상태 변경
- 사용자 A, 사용자 B를 생성(또는 로그인)한다.
- A 토큰으로 POST /api/v1/matchings 를 호출하여 B에게 요청한다.
- A 토큰으로 GET /api/v1/matchings/sent 에서 요청이 보이는지 확인한다.
- B 토큰으로 GET /api/v1/matchings/received 에서 요청이 보이는지 확인한다.
- B 토큰으로 PATCH /api/v1/matchings/{id}/status 를 호출하여 ACCEPTED로 변경한다.
- A 또는 B 토큰으로 GET /api/v1/matchings/{id} 를 조회해 status=ACCEPTED 인지 확인한다.

### 시나리오 2: 중복 요청 방지
- 시나리오 1에서 REQUESTED 또는 ACCEPTED 상태가 존재하는 사용자 쌍에 대해
  다시 POST /api/v1/matchings 를 호출한다.
- 409를 반환하고, error.code가 MATCH_ALREADY_REQUESTED 또는 MATCH_ALREADY_CONNECTED 인지 확인한다.

### 시나리오 3: 자기 자신에게 매칭 요청 방지(가능한 경우)
- A 토큰으로 자기 자신을 targetUserId로 지정하여 POST /api/v1/matchings 호출
- 400 또는 409를 반환하고, error.code가 CONFLICT 또는 정책에 맞는 코드인지 확인한다.

## 6. 구현 규칙

- 테스트는 “API 호출 중심”으로 작성한다.
  - Playwright의 request fixture(APIRequestContext)를 사용한다.
- 테스트 데이터는 실행마다 충돌을 피하기 위해 유니크한 email/nickname을 사용한다.
- 테스트는 로컬에서 아래 명령 한 줄로 실행 가능해야 한다.
  - npm test (또는 npx playwright test)
- 실패 시 원인이 빠르게 드러나도록, 응답 본문을 함께 출력(로그)할 수 있는 유틸을 둔다.

## 7. 완료 조건(Definition of Done)

- e2e 프로젝트가 구성되어 dependency 설치 후 바로 실행된다.
- 최소 2개 이상 테스트(시나리오 1, 2)가 통과한다.
- 백엔드가 실행 중이면, 별도 수작업 없이 로컬에서 재현 가능하다.
