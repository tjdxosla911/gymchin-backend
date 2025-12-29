# API Contract Specification
# GymChin MVP

## 1. 기본 규칙

본 문서는 GymChin 서비스의 백엔드(Spring Boot)와 프론트엔드(Flutter)가
공통으로 준수해야 할 API 계약을 정의한다.

- API Base Path: `/api/v1`
- Data Format: JSON
- Encoding: UTF-8
- Authentication: JWT Bearer Token
- Authorization Header:
  - `Authorization: Bearer {accessToken}`

---

## 2. 공통 응답 포맷

### 2.1 성공 응답

{
  "success": true,
  "data": {},
  "error": null,
  "timestamp": "2025-01-01T12:00:00+09:00"
}

### 2.2 실패 응답

{
  "success": false,
  "data": null,
  "error": {
    "code": "ERROR_CODE",
    "message": "에러 메시지",
    "details": {}
  },
  "timestamp": "2025-01-01T12:00:00+09:00"
}

---

## 3. HTTP 상태 코드 정책

- 200: 조회/수정/삭제 성공
- 201: 생성 성공
- 400: 요청 유효성 실패
- 401: 인증 실패
- 403: 권한 없음
- 404: 리소스 없음
- 409: 상태 충돌
- 500: 서버 내부 오류

---

## 4. 에러 코드 규칙

AUTH_INVALID_CREDENTIALS
AUTH_TOKEN_EXPIRED
AUTH_UNAUTHORIZED
VALIDATION_FAILED
RESOURCE_NOT_FOUND
MATCH_ALREADY_REQUESTED
MATCH_ALREADY_CONNECTED
MATCH_INVALID_STATE
CONFLICT
INTERNAL_ERROR

---

## 5. 날짜 / 시간 규칙

모든 날짜/시간은 ISO-8601 문자열, 타임존 포함

예:
2025-01-01T12:00:00+09:00

---

## 6. 페이지네이션 규격

요청:
page (0부터)
size (기본 20, 최대 50)

응답:
items
page
size
totalElements
totalPages
hasNext

---

## 7. 도메인 모델 (요약)

### User
userId
email
nickname
gender
age
location(city, district)
fitnessLevel
goals
preferredDays
preferredTimeSlots
coachOption
gymName
createdAt
updatedAt

### Matching
matchingId
requesterUserId
targetUserId
status
message
createdAt
updatedAt

---

## 8. 인증 API

POST /auth/signup
POST /auth/login
POST /auth/refresh

---

## 9. 프로필 API

GET /me
PUT /me

---

## 10. 짐친 탐색 API

GET /gymmates

---

## 11. 매칭 API

POST /matchings
GET /matchings/sent
GET /matchings/received
GET /matchings/{matchingId}
PATCH /matchings/{matchingId}/status

---

## 12. 매칭 상태 전이 규칙

REQUESTED -> ACCEPTED / REJECTED
REQUESTED -> CANCELLED
ACCEPTED -> ENDED

---

## 13. 중복 매칭 방지 규칙

동일 사용자 쌍에 REQUESTED / ACCEPTED 존재 시 생성 불가

---

## 14. 보안 및 개인정보 원칙 (MVP)

이메일 비노출
위치는 시/구 단위
연락처 교환 제외

---

## 15. 구현 규칙

Backend: 공통 응답, GlobalExceptionHandler, Service 레이어 검증
Frontend: error.code 기준 분기, ISO-8601 파싱
