# PRD-T03: 매칭 상태 전이(UI + E2E) 마감

## 목적
기존 매칭 기능(REQUESTED/ACCEPTED)을 서비스 완성 단계로 끌어올리기 위해
REJECTED / CANCELLED / ENDED 상태 전이를 UI + Playwright E2E까지 포함하여 마감한다.

본 작업 완료 시, 매칭은 요청 → 수락/거절/취소 → 종료까지 전 구간이
브라우저 UI와 자동화 테스트로 검증 가능해야 한다.

---

## 상태 정의
- REQUESTED: 매칭 요청 생성 직후
- ACCEPTED: 대상자가 요청 수락
- REJECTED: 대상자가 요청 거절
- CANCELLED: 요청자가 요청 취소
- ENDED: 수락 이후 관계 종료

---

## 상태 전이 규칙 (Single Source of Truth)

- REQUESTED → ACCEPTED (TARGET만 가능)
- REQUESTED → REJECTED (TARGET만 가능)
- REQUESTED → CANCELLED (REQUESTER만 가능)
- ACCEPTED → ENDED (REQUESTER / TARGET 모두 가능)

❌ 그 외 모든 전이는 불가

---

## 권한 규칙
- requesterUserId == currentUserId → REQUESTER
- targetUserId == currentUserId → TARGET
- 둘 다 아니면 403

---

## API 규칙
- PATCH /api/v1/matchings/{matchingId}/status
- Body: { "status": "REJECTED | CANCELLED | ENDED | ACCEPTED" }
- 응답: 공통 응답 포맷

### 에러 코드
- MATCH_INVALID_STATE
- AUTH_UNAUTHORIZED / AUTH_FORBIDDEN
- RESOURCE_NOT_FOUND
- VALIDATION_FAILED

---

## 백엔드 구현 지시
- MatchingStatus enum 확장
- validateTransition(currentStatus, nextStatus, actorRole) 함수 구현
- 중복 매칭 방지 조건은 REQUESTED / ACCEPTED 만 차단
- REJECTED / CANCELLED / ENDED 는 재요청 허용
- Service 레이어에서 모든 검증 수행
- GlobalExceptionHandler로 공통 응답 유지

---

## 테스트 UI 구현 규칙

### 표시 정보
- matchingId, status
- requesterUserId / targetUserId
- 내 역할 뱃지 (REQUESTER / TARGET)

### 버튼 노출 규칙
- REQUESTED + TARGET → [Accept] [Reject]
- REQUESTED + REQUESTER → [Cancel]
- ACCEPTED → [End]
- REJECTED / CANCELLED / ENDED → 버튼 없음

버튼 클릭 → API 호출 → 성공 시 리스트 재조회

---

## Playwright E2E 시나리오

### 시나리오 A: 요청 취소
- userA 요청 → CANCELLED
- 동일 쌍 재요청 가능

### 시나리오 B: 요청 거절
- userA 요청 → userB REJECT
- 동일 쌍 재요청 가능

### 시나리오 C: 수락 후 종료
- userA 요청 → userB ACCEPT → END

### 시나리오 D: 금지 전이
- requester가 ACCEPT 시도 → 실패
- target이 CANCEL 시도 → 실패
- REJECTED 상태에서 END 시도 → 실패

---

## 완료 조건
- UI에서 모든 버튼 동작 확인
- Swagger 정상
- Playwright 전 시나리오 green
- DB 상태 정확
