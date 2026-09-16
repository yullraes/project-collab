# Project Collab

사용자, 프로젝트 멤버십과 역할, 승인 기반 Task 흐름을 다루는 협업 서비스다. Task는 Jira 또는 GitHub Issue와 같은 협업 티켓으로 해석했다. Spring Boot 백엔드는 `api/`, Vite 기반 React + TypeScript 프런트엔드는 `web/`에 둔다. 프런트엔드는 현재 기본 프로젝트만 생성한 상태다.

## 프로젝트 구조

```text
api/     Spring Boot 소스, 테스트, Gradle 빌드 설정
web/     Vite + React + TypeScript 프로젝트
docs/    API 명세와 설계 문서
```

## 문서 경로

| 확인할 내용 | 정본 문서 |
|---|---|
| 빌드, 실행, 즉시 확인 | 이 README의 [실행과 확인](#실행과-확인) |
| 전체 엔드포인트, 파라미터, 상태 전이, 응답 예시 | [HTTP API 명세](docs/api-spec.md) |
| 주요 설계 결정, 이유, 비용과 대안 | [설계 결정](docs/design-decisions.md) |
| 회사별 데이터 분리, 미완성 및 제외 범위 | [회사별 데이터 분리와 미완성 항목](docs/scaling-and-gaps.md) |

문서가 충돌하면 실행 계약은 코드와 Swagger UI를, 설계 의도는 `docs/design-decisions.md`를 기준으로 한다.

## 실행과 확인

### 요구 환경

- Java 17
- Node.js 22.12 이상과 npm (프런트엔드, 초기 검증 환경: Node.js 24.14.0)
- 별도 데이터베이스 불필요: H2 인메모리 사용

### 백엔드

저장소 루트에서 `api/`로 이동한 뒤 실행한다.

```bash
cd api

# macOS/Linux
./gradlew bootRun

# Windows
./gradlew.bat bootRun
```

- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- 테스트: `api/`에서 `./gradlew test` 또는 `./gradlew.bat test`

### 프런트엔드

별도 터미널에서 저장소 루트를 기준으로 실행한다.

```bash
cd web
npm ci
npm run dev
```

- 개발 화면: [http://localhost:5173](http://localhost:5173)
- 빌드: `web/`에서 `npm run build`
- 정적 검사: `web/`에서 `npm run lint`
- 개발 서버는 `/api` 요청을 `http://localhost:8080`으로 전달한다. API를 사용하려면 백엔드를 함께 실행한다. 이 설정은 개발 서버에만 적용된다.
- 아직 Stitch 화면 변환과 API 연동은 진행하지 않았다.

### 백엔드 예제 데이터

기본 실행 시 사용자, 프로젝트, 상태별 Task 예제가 자동 생성된다. 실제 ID와 리비전은 시작 로그를 기준으로 한다.

```text
Swagger demo data initialized: projectId=...
Swagger demo users: OWNER userId=..., ADMIN userId=..., MEMBER userId=...
Swagger demo tasks: PENDING taskId=... revision=..., ...
```

초기 데이터를 끄려면 `APP_DEMO_DATA_ENABLED=false` 환경 변수를 지정한다.

### Swagger 확인 절차

Swagger UI의 `Try it out`은 앞선 응답을 다음 요청에 자동 전달하지 않는다. 각 변경 요청에는 직전 Task 응답의 `revision`을 사용한다.

1. `GET /api/users/{userId}`로 로그에 나온 OWNER, ADMIN, MEMBER를 조회한다.
2. `GET /api/projects/{projectId}/tasks?userId={memberUserId}`로 상태별 예제와 리비전을 확인한다.
3. MEMBER가 `POST /api/projects/{projectId}/tasks?userId={memberUserId}`로 Task를 만들고 `PENDING`을 확인한다.
4. ADMIN이 `POST /api/projects/{projectId}/tasks/{taskId}/approve?userId={adminUserId}`와 본문 `{"revision": 0}`으로 승인한다.
5. 담당자가 최신 리비전으로 `start`, `request-review`를 순서대로 호출하고 ADMIN이 `complete`를 호출한다.
6. 이미 사용한 오래된 리비전으로 변경을 다시 요청해 `409 Conflict`와 `task.revision.conflict`를 확인한다.

반려와 보완 요청은 로그에 표시된 `PENDING`, `IN_REVIEW` 예제 Task로 각각 확인할 수 있다.

## API 요약

모든 프로젝트 및 Task API의 `userId`는 인증이 완료된 요청자의 식별자라고 가정한다.

| 영역 | 엔드포인트 요약 |
|---|---|
| 사용자 | `POST /api/users`, `GET /api/users/{userId}` |
| 프로젝트 | `POST/GET /api/projects`, `GET/PUT/DELETE /api/projects/{projectId}` |
| 멤버 | `POST/GET /api/projects/{projectId}/members`, `PUT .../members/{targetUserId}/role`, `DELETE .../members/{targetUserId}` |
| Task | `POST/GET /api/projects/{projectId}/tasks`, `GET/PUT/DELETE .../tasks/{taskId}` |
| 담당자 | `POST .../{taskId}/assign`, `DELETE .../{taskId}/assignee`, `POST .../{taskId}/relinquish` |
| 승인 | `POST .../{taskId}/approve`, `reject`, `resubmit` |
| 수행·검토 | `POST .../{taskId}/start`, `request-review`, `request-changes`, `complete` |

목록 조회는 `keyword`, `state`, `page`, `size`를 지원한다. Task 변경은 최신 `revision`이 필요하다. 요청 본문, 역할·상태별 선행 조건과 성공·오류 응답 예시는 [HTTP API 명세](docs/api-spec.md)와 Swagger UI에 있다.

## 주요 설계 결정

- User와 Project는 행위를 도출할 근거가 부족해 서비스 중심으로 구현하고, 상태와 전이 규칙이 있는 Task만 도메인 모델로 만들었다.
- Task를 협업 티켓으로 해석해 제안 승인, 실행, 검토, 완료 흐름을 추가했다.
- 순수 Task 모델과 JPA 엔티티를 분리했다. 명시적 매핑과 인지 비용을 감수하고 영속성 기술이 도메인 모델을 규정하지 않게 했다.
- JPA 연관관계와 물리 외래 키 대신 ID 값 참조를 사용한다. 참조 확인, 삭제 순서와 담당자 정규화는 애플리케이션 서비스가 책임진다.
- 조회 당시 내용을 승인한다는 의미를 보존하기 위해 요청 `revision`과 JPA `@Version`을 함께 사용한다. 비관적 잠금은 요청 사이의 사용자 고민 시간을 보호하지 못하므로 사용하지 않았다.
- 상태 전이는 허용 주체와 부수 효과가 달라 `approve`, `start`, `complete` 같은 명령형 경로로 분리했다.
- `TaskReadService`와 `TaskWriteService` 분리는 책임과 트랜잭션을 위한 것이며 별도 읽기 모델이 없으므로 CQRS가 아니다.
- 완료된 Task의 내용과 담당자는 바꿀 수 없지만 OWNER·ADMIN의 삭제는 허용한다.

각 결정의 대안과 비용은 [설계 결정](docs/design-decisions.md)에 기록했다.

## 사용 기술과 선택 이유

| 기술 | 선택 이유 |
|---|---|
| Java 17, Spring Boot 3.3 | 과제 실행 환경에 맞추고 웹·검증·영속성 구성을 한 틀에서 다룬다. |
| Spring Web MVC | JSON 기반 HTTP API와 예외 응답을 구현한다. |
| Spring Data JPA, Hibernate | 단순 저장과 JPQL, 트랜잭션 충돌 감지를 위한 `@Version`을 사용한다. |
| H2 | 별도 설치 없이 평가 환경을 재현하고 초기 데이터를 즉시 확인한다. |
| Bean Validation | HTTP 경계의 필수값과 형식을 선언적으로 검사한다. 서비스·도메인 정책 검증은 별도로 유지한다. |
| springdoc-openapi | 실행 중인 API 계약과 예시를 Swagger UI에서 직접 확인한다. |
| JUnit 5, Spring Boot Test | JPA 매핑, 트랜잭션, 권한과 HTTP 계약을 함께 검증하는 통합 테스트를 우선한다. |

현재 규모에서는 정적 JPQL로 조회 조건을 충분히 표현할 수 있어 QueryDSL을 추가하지 않았다. Spring Security는 인증 제외 조건에 따라 도입하지 않았고, Lombok은 반복 코드 절감 효과보다 명시적인 코드의 이점이 커 제거했다.

## 회사별 데이터 분리

일반 SaaS라면 공유 데이터베이스와 스키마에 `Company` 및 `companyId`를 추가하고 모든 조회·변경을 회사 범위로 강제한다. 고객의 직접 DB 접근, 계약, 감사 또는 보안 조건이 물리 분리를 요구할 때만 회사별 스키마나 데이터베이스를 선택한다. 이는 기술 취향보다 사업 및 운영 조건의 결정이며, 구체적인 변경 지점과 운영 비용은 [회사별 데이터 분리와 미완성 항목](docs/scaling-and-gaps.md)에 정리했다.

## 구현하지 못했거나 제외한 부분

- 담당자 재할당과 해당 사용자 멤버 제거의 동시 실행 충돌은 완전히 해결하지 못했다.
- 실제 화면을 기준으로 한 조회 전용 모델과 역할별 대시보드는 설계하지 못했다.
- 인증은 과제 범위에 따라 제외했다. 프런트엔드는 후속 작업으로 기본 프로젝트를 생성했으며, 업무 화면과 API 연동은 아직 구현하지 않았다.

후속 접근은 [회사별 데이터 분리와 미완성 항목](docs/scaling-and-gaps.md)에 기록했다.
