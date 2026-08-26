# HTTP API 명세

실행 가능한 세부 스키마와 예시는 Swagger UI(`/swagger-ui/index.html`)가 정본이다. 모든 프로젝트 및 Task API의 `userId`는 인증이 완료된 요청자 ID라고 가정한다.

## 사용자와 프로젝트

| 메서드 | 경로 | 요청 | 성공 |
|---|---|---|---|
| `POST` | `/api/users` | `{name, email}` | `201 UserResponse` |
| `GET` | `/api/users/{userId}` | 경로 ID | `200 UserResponse` |
| `POST` | `/api/projects?userId=` | `{name, description}` | `201 ProjectResponse` |
| `GET` | `/api/projects?userId=` | 요청자 ID | `200 ProjectResponse[]` |
| `GET` | `/api/projects/{projectId}?userId=` | 프로젝트 멤버 | `200 ProjectResponse` |
| `PUT` | `/api/projects/{projectId}?userId=` | OWNER·ADMIN, `{name, description}` | `200 ProjectResponse` |
| `DELETE` | `/api/projects/{projectId}?userId=` | OWNER | `204` |
| `POST` | `/api/projects/{projectId}/members?userId=` | OWNER·ADMIN, `{userId}` | `201 ProjectMemberResponse` |
| `GET` | `/api/projects/{projectId}/members?userId=` | 프로젝트 멤버 | `200 ProjectMemberResponse[]` |
| `PUT` | `/api/projects/{projectId}/members/{targetUserId}/role?userId=` | OWNER·ADMIN, `{role}` | `200 ProjectMemberResponse` |
| `DELETE` | `/api/projects/{projectId}/members/{targetUserId}?userId=` | OWNER·ADMIN | `204` |

`role`은 `OWNER`, `ADMIN`, `MEMBER` 중 하나다. 마지막 OWNER의 강등과 제거는 거부한다.

## Task

기본 경로는 `/api/projects/{projectId}/tasks`다. 변경 요청은 직전 조회 응답의 `revision`을 본문 또는 쿼리 파라미터로 전달한다.

| 메서드 | 하위 경로 | 요청/조건 | 성공 |
|---|---|---|---|
| `POST` | `` | `{title, description, assigneeUserId?, unassigned?}` | `201 TaskResponse` |
| `GET` | `` | `userId`, `keyword?`, `state?`, `page=0`, `size=20` | `200 TaskPageResponse` |
| `GET` | `/{taskId}` | 프로젝트 멤버 | `200 TaskResponse` |
| `PUT` | `/{taskId}` | `{title, description, revision}` | `200 TaskResponse` |
| `DELETE` | `/{taskId}` | `userId`, `revision` | `204` |
| `POST` | `/{taskId}/assign` | 관리자, `{assigneeUserId, revision}` | `200 TaskResponse` |
| `DELETE` | `/{taskId}/assignee` | 관리자, `revision` | `200 TaskResponse` |
| `POST` | `/{taskId}/relinquish` | 담당자, `revision` | `200 TaskResponse` |
| `POST` | `/{taskId}/approve` | 관리자, `{assigneeUserId?, unassigned?, revision}` | `200 TaskResponse` |
| `POST` | `/{taskId}/reject` | 관리자, `{rejectionReason, revision}` | `200 TaskResponse` |
| `POST` | `/{taskId}/resubmit` | 생성자, `revision` | `200 TaskResponse` |
| `POST` | `/{taskId}/start` | 담당자, `revision` | `200 TaskResponse` |
| `POST` | `/{taskId}/request-review` | 담당자, `revision` | `200 TaskResponse` |
| `POST` | `/{taskId}/request-changes` | 관리자, `revision` | `200 TaskResponse` |
| `POST` | `/{taskId}/complete` | 관리자, `revision` | `200 TaskResponse` |

목록의 `state`는 `PENDING`, `REJECTED`, `ACCEPTED`, `IN_PROGRESS`, `IN_REVIEW`, `DONE`이며 `size`는 1~100이다.

## 상태 전이

| 전이 | 수행자 |
|---|---|
| `PENDING → ACCEPTED` | 생성자가 아닌 OWNER·ADMIN |
| `PENDING → REJECTED` | OWNER·ADMIN |
| `REJECTED → PENDING` | 생성자 |
| `ACCEPTED → IN_PROGRESS` | 담당자 |
| `IN_PROGRESS → IN_REVIEW` | 담당자 |
| `IN_REVIEW → IN_PROGRESS` | OWNER·ADMIN |
| `IN_REVIEW → DONE` | OWNER·ADMIN |

일반 멤버가 승인된 작업 내용을 수정하면 `PENDING`으로 돌아가 재승인을 받는다. 관리자가 `DONE` 이전에 내용을 수정해도 상태는 유지된다. `DONE`의 내용과 담당자 변경은 금지하지만 관리자는 삭제할 수 있다.

## 응답 예시

```json
{
  "taskId": 1,
  "projectId": 1,
  "revision": 1,
  "creatorUserId": 3,
  "assigneeUserId": 3,
  "title": "결제 오류 재현",
  "description": "실패 조건을 재현하고 원인을 기록합니다.",
  "state": "ACCEPTED",
  "rejectionReason": null,
  "createdAt": "2026-08-26T00:00:00Z",
  "updatedAt": "2026-08-26T00:01:00Z"
}
```

입력 형식 오류는 `400`, 권한 부족은 `403`, 없는 자원은 `404`, 중복이나 오래된 리비전은 `409`다.

```json
{
  "code": "task.revision.conflict",
  "message": "작업이 다른 요청에 의해 변경되었습니다.",
  "violations": []
}
```

검증 오류에서는 `violations`에 필드별 `{field, reason}`이 들어간다.
