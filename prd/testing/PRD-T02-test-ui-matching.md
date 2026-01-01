# PRD-T02 (Testing UI)
# 매칭 기능 확인용 테스트 화면(Web) 추가 (Spring Boot 정적 페이지)

## 1. 목적

현재 GymChin 백엔드는 매칭 기능이 구현되어 있으나, 확인 수단이 Swagger/DB/테스트 로그에 치우쳐 있어
기능 체감과 디버깅 속도가 떨어진다.

본 문서는 백엔드 프로젝트 내부에 “테스트용 웹 화면(정적 페이지)”을 추가하여,
로그인 → 짐친 탐색 → 매칭 요청 → 보낸/받은 요청 확인 → 수락/거절(상태 변경)
흐름을 브라우저에서 버튼 클릭으로 검증할 수 있게 만드는 것을 목표로 한다.

이 화면은 운영 기능이 아니라 개발/검증 목적이며,
제품 UI(Flutter) 개발과 무관하게 빠르게 유즈케이스를 확인하는 용도다.

---

## 2. 범위

### 포함
- Spring Boot 정적 리소스 기반 테스트 UI 제공
- JWT 토큰 기반 API 호출(Authorization 헤더)
- 아래 기능을 한 페이지에서 수행 가능
  - 로그인(토큰 발급)
  - 내 정보 조회(/me)
  - 짐친 탐색(/gymmates)
  - 매칭 요청 생성(/matchings)
  - 보낸 요청 목록(/matchings/sent)
  - 받은 요청 목록(/matchings/received)
  - 받은 요청 상태 변경(/matchings/{id}/status) : ACCEPTED/REJECTED
  - 로그아웃(토큰 제거)

### 제외
- 실제 제품 UI/디자인(Flutter) 구현
- 회원가입 UI(필요 시 버튼으로 호출만 제공)
- 거리 기반 매칭/지도 기능
- 복잡한 상태관리(React/Vue 등 프레임워크 도입)
- CI 연동

---

## 3. 화면 접근 경로

정적 페이지로 제공한다.

- 경로(권장): `http://localhost:8080/test-ui/`
- 파일 위치(권장): `src/main/resources/static/test-ui/index.html`
- 스크립트: `src/main/resources/static/test-ui/app.js`

---

## 4. 보안 정책(중요)

테스트 UI 페이지 자체는 로컬 개발 편의를 위해 접근을 허용한다.

- PermitAll 대상:
  - `/test-ui/**` (정적 리소스)
  - (이미 적용되어 있다면) `/swagger-ui/**`, `/v3/api-docs/**`

단, 실제 API는 기존 규칙대로 JWT 인증이 필요하다.
테스트 UI는 로그인으로 토큰을 받고, 그 토큰으로 API 호출을 수행한다.

---

## 5. API 전제(계약)

계약 기준은 `contract/api-spec.md`를 따른다.

- Base URL: `http://localhost:8080`
- API Base Path: `/api/v1`
- Authorization: `Authorization: Bearer {accessToken}`
- 공통 응답 포맷: `success/data/error/timestamp`

---

## 6. 사용자 흐름(시나리오)

### 시나리오 A: 로그인 → 내 정보 확인
1) 이메일/비밀번호 입력 후 로그인 버튼 클릭
2) 성공 시 accessToken을 localStorage에 저장
3) /me 호출하여 현재 로그인 사용자 정보를 화면 상단에 표시

### 시나리오 B: 짐친 탐색 → 매칭 요청 생성
1) “짐친 탐색” 버튼으로 /gymmates 호출
2) 결과 리스트를 테이블로 표시
3) 각 row에 “매칭 요청” 버튼 제공
4) 클릭 시 targetUserId를 해당 유저의 userId로 넣어 /matchings 호출
5) 생성된 matchingId 및 결과 메시지를 로그/토스트 영역에 표시

### 시나리오 C: 보낸/받은 요청 확인
1) “보낸 요청” 버튼으로 /matchings/sent 호출 → 테이블 표시
2) “받은 요청” 버튼으로 /matchings/received 호출 → 테이블 표시

### 시나리오 D: 받은 요청 수락/거절
1) 받은 요청 리스트에서 REQUESTED 상태의 건에 대해
   - “수락” 버튼: PATCH /matchings/{id}/status with status=ACCEPTED
   - “거절” 버튼: PATCH /matchings/{id}/status with status=REJECTED
2) 성공 후 받은 요청/보낸 요청 리스트를 자동 새로고침(또는 수동 버튼 안내)

### 시나리오 E: 로그아웃
1) localStorage 토큰 제거
2) 화면 상단 상태를 “로그아웃”으로 표시

---

## 7. UI 요구사항(최소)

하나의 단일 페이지로 구성한다.

- 상단 상태 바
  - 현재 Base URL 표시
  - 로그인 상태(로그인/로그아웃)
  - accessToken 존재 여부 표시(마스킹)
- 로그인 영역
  - email, password input
  - Login 버튼
  - (선택) Signup 버튼
- 기능 버튼 영역
  - Me 조회
  - 짐친 탐색
  - 보낸 요청
  - 받은 요청
  - 로그아웃
- 결과 출력 영역
  - 테이블(리스트)
  - 요청/응답 로그(최근 20줄 정도)
  - 에러 발생 시 error.code, error.message를 눈에 보이게 출력

---

## 8. 데이터 요구사항(중요)

테스트 UI에서 “매칭 요청”을 만들려면 짐친 탐색 결과에 targetUserId가 있어야 한다.

- /gymmates 응답 item에는 최소 다음 필드가 필요하다.
  - userId (필수)
  - nickname (표시용)
  - gender/age 등은 선택

만약 현재 /gymmates 응답에 userId가 없다면,
탐색 응답 DTO에 userId를 추가하는 것이 본 티켓 범위에 포함된다.

---

## 9. 기술 요구사항(구현 규칙)

- 프레임워크 없이 순수 HTML/JS로 구현한다.
- fetch() 사용
- Authorization 헤더는 localStorage의 token으로 자동 주입한다.
- 공통 응답 포맷을 가정하고, 다음을 표준으로 처리한다.
  - success=true → data 렌더링
  - success=false → error.code, error.message 출력
- 상태 전이 enum은 서버 구현과 동일한 문자열 사용:
  - REQUESTED / ACCEPTED / REJECTED / CANCELLED / ENDED

---

## 10. 완료 조건(Definition of Done)

아래 조건을 로컬에서 브라우저로 직접 확인할 수 있어야 한다.

1) `http://localhost:8080/test-ui/` 접속 시 페이지가 열린다.
2) 로그인 후 accessToken이 저장되고, /me 결과가 화면에 표시된다.
3) /gymmates 결과가 리스트로 표시되고, 각 항목에 “매칭 요청” 버튼이 있다.
4) 매칭 요청 생성 후, /matchings/sent 에 해당 요청이 보인다.
5) /matchings/received 에 요청이 보이고, 수락/거절 버튼으로 status 변경이 된다.
6) 에러 발생 시 error.code와 message가 화면 로그에 남는다.

---

## 11. 작업 산출물

- 정적 파일
  - `src/main/resources/static/test-ui/index.html`
  - `src/main/resources/static/test-ui/app.js`
  - (선택) `src/main/resources/static/test-ui/styles.css`
- 백엔드 설정(필요 시)
  - SecurityConfig에서 `/test-ui/**` permitAll
  - /gymmates 응답에 userId 포함(필요 시 DTO 수정)
