import type { TaskState } from "../components/StatusBadge";
export type { TaskState } from "../components/StatusBadge";

export const stateLabels: Record<TaskState, string> = {
  PENDING: "승인 대기",
  REJECTED: "반려됨",
  ACCEPTED: "승인됨",
  IN_PROGRESS: "진행 중",
  IN_REVIEW: "검토 중",
  DONE: "완료",
};
export interface TaskFixture {
  taskId: number;
  title: string;
  description: string;
  state: TaskState;
  creator: string;
  assignee: string;
  detailScreen: string;
}
export const project = {
  id: 1,
  name: "결제 안정화",
  description: "결제 오류를 재현하고 수정 작업의 승인과 검토를 관리합니다.",
  createdAt: "2026.09.15",
  updatedAt: "2026.09.16",
};
export const members = [
  {
    id: 1,
    name: "김서연",
    email: "seoyeon@example.com",
    role: "소유자",
    joinedAt: "2026.09.01",
  },
  {
    id: 2,
    name: "박준호",
    email: "junho@example.com",
    role: "관리자",
    joinedAt: "2026.09.01",
  },
  {
    id: 3,
    name: "이민지",
    email: "minji@example.com",
    role: "멤버",
    joinedAt: "2026.09.01",
  },
];
export const tasks: TaskFixture[] = [
  {
    taskId: 107,
    title: "결제 취소 흐름 점검",
    description: "취소 요청과 처리 결과가 일치하는지 확인합니다.",
    state: "ACCEPTED",
    creator: "김서연",
    assignee: "미할당",
    detailScreen: "25",
  },
  {
    taskId: 106,
    title: "결제 로그 누락 수정",
    description: "누락된 결제 로그를 보완하고 기록 결과를 확인합니다.",
    state: "DONE",
    creator: "김서연",
    assignee: "이민지",
    detailScreen: "09",
  },
  {
    taskId: 105,
    title: "결제 오류 수정 검토",
    description: "오류 재현 조건에서 수정 결과와 회귀 여부를 확인합니다.",
    state: "IN_REVIEW",
    creator: "김서연",
    assignee: "이민지",
    detailScreen: "08",
  },
  {
    taskId: 104,
    title: "중복 결제 방지 처리",
    description: "같은 결제 요청이 반복되어도 한 번만 처리되도록 수정합니다.",
    state: "IN_PROGRESS",
    creator: "김서연",
    assignee: "이민지",
    detailScreen: "07",
  },
  {
    taskId: 103,
    title: "결제 재시도 조건 정리",
    description: "재시도 가능한 실패 조건과 처리 방법을 정리합니다.",
    state: "ACCEPTED",
    creator: "김서연",
    assignee: "이민지",
    detailScreen: "06",
  },
  {
    taskId: 102,
    title: "결제 실패 안내 개선",
    description: "실패 상황별 안내 문구와 재시도 방법을 정리합니다.",
    state: "REJECTED",
    creator: "이민지",
    assignee: "이민지",
    detailScreen: "05",
  },
  {
    taskId: 101,
    title: "결제 오류 재현",
    description: "실패 조건을 재현하고 원인을 기록합니다.",
    state: "PENDING",
    creator: "이민지",
    assignee: "이민지",
    detailScreen: "04",
  },
];
export interface DetailFixture {
  task: TaskFixture;
  viewer: string;
  instruction: string;
  primary?: string;
  secondary?: string;
  primaryDisabled?: boolean;
  note?: string;
  rejection?: string;
  assignment?: boolean;
  menu: string[];
}
export type DetailId =
  | "pending"
  | "rejected"
  | "accepted"
  | "inProgress"
  | "inReview"
  | "done"
  | "selfApproval"
  | "unassigned";
export const details: Record<DetailId, DetailFixture> = {
  unassigned: {
    task: tasks[0],
    viewer: "박준호 · 관리자",
    instruction: "담당자를 지정하면 작업을 시작할 수 있습니다.",
    primary: "담당자 지정",
    menu: ["내용 수정", "작업 삭제"],
  },
  pending: {
    task: tasks[6],
    viewer: "박준호 · 관리자",
    instruction: "내용과 담당자를 확인한 뒤 승인하거나 반려하세요.",
    primary: "승인",
    secondary: "반려",
    note: "담당자 변경은 승인 단계에서 진행할 수 있습니다.",
    menu: ["내용 수정", "작업 삭제"],
  },
  rejected: {
    task: tasks[5],
    viewer: "이민지 · 멤버",
    instruction:
      "반려 사유를 확인하고 필요한 내용을 수정한 뒤 다시 제출하세요.",
    primary: "다시 제출",
    secondary: "내용 수정",
    rejection: "완료 조건을 더 구체적으로 작성해 주세요.",
    menu: ["제안 철회"],
  },
  accepted: {
    task: tasks[4],
    viewer: "이민지 · 멤버",
    instruction: "담당한 작업을 시작할 수 있습니다.",
    primary: "작업 시작",
    secondary: "내용 수정",
    note: "내용을 변경하면 승인 대기로 돌아갑니다.",
    menu: ["담당 포기"],
  },
  inProgress: {
    task: tasks[3],
    viewer: "이민지 · 멤버",
    instruction: "작업을 마치면 검토를 요청하세요.",
    primary: "검토 요청",
    secondary: "내용 수정",
    note: "내용을 변경하면 승인 대기로 돌아갑니다.",
    menu: ["담당 포기"],
  },
  inReview: {
    task: tasks[2],
    viewer: "박준호 · 관리자",
    instruction: "작업 내용을 확인하고 완료하거나 보완을 요청하세요.",
    primary: "완료 처리",
    secondary: "보완 요청",
    assignment: true,
    menu: ["내용 수정", "담당자 해제", "작업 삭제"],
  },
  done: {
    task: tasks[1],
    viewer: "박준호 · 관리자",
    instruction: "완료된 작업입니다. 내용과 담당자를 변경할 수 없습니다.",
    menu: ["작업 삭제"],
  },
  selfApproval: {
    task: tasks[6],
    viewer: "이민지 · 관리자",
    instruction:
      "자신이 생성한 작업은 승인할 수 없습니다. 다른 관리자 또는 소유자의 승인이 필요합니다.",
    primary: "승인",
    primaryDisabled: true,
    secondary: "반려",
    menu: ["내용 수정", "작업 삭제"],
  },
};
export const screenNames = [
  "내 프로젝트",
  "프로젝트 만들기",
  "프로젝트 작업 목록",
  "승인 대기 상세 · 관리자",
  "반려 작업 상세 · 생성자",
  "승인된 작업 상세 · 담당자",
  "진행 중 상세 · 담당자",
  "검토 중 상세 · 관리자",
  "완료 작업 상세 · 관리자",
  "작업 제안 만들기 · 멤버",
  "작업 등록 · 관리자",
  "내용 수정과 재승인",
  "작업 승인",
  "작업 반려 · 입력 오류",
  "보완 요청 확인",
  "프로젝트 멤버",
  "기존 사용자 추가",
  "마지막 소유자 역할 보호",
  "프로젝트 설정 · 소유자",
  "작업 변경 충돌",
  "작업 목록 · 검색 결과 없음",
  "작업 목록 · 로딩",
  "프로젝트 접근 권한 부족",
  "자기 작업 승인 제한",
  "미할당 작업 담당자 지정",
  "프로젝트 멤버 제거 확인",
  "프로젝트 삭제 확인",
  "반려 작업 상세 · 모바일",
];

export const viewers = {
  member: "이민지 · 멤버",
  admin: "박준호 · 관리자",
  owner: "김서연 · 소유자",
  global: "이민지",
};
export const memberManagement = {
  currentUserId: 2,
  protectedOwnerId: 1,
  selectedAssigneeId: 3,
  removedMemberId: 3,
};
export const lookupUser = { id: 4, name: "최유진", email: "yujin@example.com" };
export const taskForms = {
  proposal: { title: tasks[6].title, description: tasks[6].description },
  register: { title: tasks[0].title, description: tasks[0].description },
  revise: {
    title: tasks[3].title,
    description:
      "중복 요청 식별 조건을 추가하고 반복 결제 요청이 한 번만 처리되는지 확인합니다.",
  },
};
export const emptySearch = { state: "IN_REVIEW", keyword: "정산" };
export const pageSize = 20;
export type ProjectData = typeof project;
export type MemberData = (typeof members)[number];
