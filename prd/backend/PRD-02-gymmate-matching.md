# Backend PRD-02
# 기능명: 짐친 탐색 및 매칭

## 1. 목적

이 기능의 목적은 사용자가 “실제로 함께 운동할 수 있는 사람”을 탐색하고,
매칭 요청을 통해 관계를 형성할 수 있도록 하는 것이다.

이 기능은 짐친 서비스 MVP의 핵심 가치 검증 대상이며,
PT·결제·콘텐츠보다 우선적으로 구현된다.

## 2. MVP 검증 질문

- 운동 파트너를 리스트로 보여주고
- 간단한 기준으로 필터링하고
- 매칭 요청/수락 흐름을 제공하는 것만으로
- 실제 매칭 관계가 형성되는가?

본 PRD는 위 질문에 답하기 위한 최소 기능만 포함한다.

## 3. 매칭 기준 (MVP 고정 규칙)

### 3.1 필수 기준 (탐색 가능성)

MVP에서는 “거리/GPS 계산”을 하지 않는다.
실제 만남 가능성을 높이기 위해 다음을 기본 기준으로 사용한다.

- 같은 시/구 단위 지역
- 운동 가능한 요일/시간대의 교집합 존재

※ 단, 초기 유저풀이 적을 수 있으므로 “필수 기준”은 서버 기본 필터/정렬에 반영하되,
API 파라미터로 강제하지 않는다(클라이언트 필터로 선택 가능).

### 3.2 선택 기준 (필터)

사용자는 탐색 시 다음 조건을 선택적으로 사용할 수 있다.

- 운동 레벨 (BEGINNER / INTERMEDIATE / ADVANCED)
- 운동 목적 (DIET / HEALTH / STRENGTH 등)
- 코칭 가능 여부 (coachOption)

### 3.3 정렬 기준 (서버 기본 정렬)

탐색 결과는 서버에서 다음 우선순위로 정렬한다.

1. 같은 헬스장 여부(있다면 최우선)
2. 시간대 일치 개수
3. 최근 활동 사용자(※ MVP에서는 updatedAt 기준으로 대체 가능)
4. 최근 가입 사용자(createdAt)

※ MVP에서는 정렬 옵션을 클라이언트에 노출하지 않는다.

## 4. 기능 범위

### 4.1 포함 기능

- 짐친 탐색 리스트 조회
- 필터 조건 기반 검색
- 매칭 요청 생성
- 매칭 상태 관리
- 내가 보낸 / 받은 매칭 목록 조회

### 4.2 제외 기능 (MVP 범위 외)

- 거리(GPS) 기반 계산
- 추천 알고리즘
- 헬스장 인증
- 메시징 / 채팅
- 결제 / 정산

## 5. 도메인 모델 (요약)

본 기능은 다음 도메인을 중심으로 구현한다.

### User
- userId
- location (city, district)
- fitnessLevel
- goals
- preferredDays
- preferredTimeSlots
- coachOption
- gymName (optional, 문자열)
- createdAt, updatedAt

### Matching
- matchingId
- requesterUserId
- targetUserId
- status
- message
- createdAt
- updatedAt

## 6. API 구현 범위

본 PRD는 다음 API의 구현을 요구한다.
(API 계약 문서 `contract/api-spec.md`의 규격을 반드시 준수한다.)

### 6.1 짐친 탐색

- GET `/api/v1/gymmates`

필터(Query):
- page, size
- city (optional)
- district (optional)
- fitnessLevel (optional)
- goal (optional, 단일)
- day (optional, 단일)
- timeSlot (optional, 단일)
- coachOption (optional)

처리 규칙:
- 자기 자신은 결과에서 제외
- 이미 ACCEPTED 상태 매칭이 있는 유저는 제외
- REQUESTED 상태로 “서로” 매칭이 진행 중인 유저는 제외(요청 중복/혼선 방지)

### 6.2 매칭 요청 생성

- POST `/api/v1/matchings`

Request:
- targetUserId: number
- message: string|null

처리 규칙:
- requesterUserId는 토큰(인증 컨텍스트) 기준으로 설정
- 자기 자신에게 요청 금지(409 또는 400 정책 중 택1, MVP에서는 409+CONFLICT 권장)
- 동일 사용자 쌍에 REQUESTED / ACCEPTED 상태 존재 시 생성 불가(409)
- 생성 시 status는 REQUESTED

### 6.3 내가 보낸 매칭 목록

- GET `/api/v1/matchings/sent`
- Query: page, size, status(optional)

### 6.4 내가 받은 매칭 목록

- GET `/api/v1/matchings/received`
- Query: page, size, status(optional)

### 6.5 매칭 상세 조회

- GET `/api/v1/matchings/{matchingId}`

### 6.6 매칭 상태 변경

- PATCH `/api/v1/matchings/{matchingId}/status`

Request:
- status: ACCEPTED | REJECTED | CANCELLED | ENDED

상태 전이 규칙:
- REQUESTED → ACCEPTED / REJECTED (받은 사람만 가능)
- REQUESTED → CANCELLED (요청자만 가능)
- ACCEPTED → ENDED (양쪽 가능)
- 종료 상태(REJECTED/CANCELLED/ENDED)에서는 변경 불가(409)

## 7. 예외 및 오류 처리

다음 경우는 반드시 예외로 처리한다.

- 이미 매칭 요청이 존재하는 경우 (409, MATCH_ALREADY_REQUESTED)
- 이미 매칭(ACCEPTED) 상태가 존재하는 경우 (409, MATCH_ALREADY_CONNECTED)
- 상태 전이가 허용되지 않는 경우 (409, MATCH_INVALID_STATE)
- 권한 없는 사용자의 상태 변경 시도 (403)
- 존재하지 않는 매칭 조회 (404, RESOURCE_NOT_FOUND)

에러 응답은 API 계약의 실패 포맷을 따른다.

## 8. 구현 제약 및 규칙

### 8.1 아키텍처 규칙

- Controller / Service / Repository 계층 분리
- 상태 전이 및 중복 검증은 Service 레이어에서 처리
- Repository는 조회 조건을 명확히 하고(예: status in ...), 비즈니스 규칙은 넣지 않는다

### 8.2 성능 기준 (MVP)

- 탐색 API 응답 시간: 1초 이내 목표(로컬 개발 기준)
- 필터 조건은 DB WHERE 절 기반
- 복잡한 추천 로직은 구현하지 않음

### 8.3 테스트 기준

- 매칭 생성 중복 방지 테스트
- 상태 전이 정상/비정상 케이스 테스트
- 자기 자신 요청 방지 테스트

## 9. 완료 조건 (Definition of Done)

다음 조건을 모두 만족하면 이 PRD는 완료로 판단한다.

- 조건에 맞는 짐친 리스트가 조회된다
- 매칭 요청이 생성된다
- 요청/수락/취소/종료 흐름이 정상 동작한다
- 중복 매칭 및 잘못된 상태 전이가 차단된다
- API 계약 문서와 응답 구조가 일치한다
