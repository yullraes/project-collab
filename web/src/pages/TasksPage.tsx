import { AppShell } from "../components/AppShell";
import { PageHeading } from "../components/PageHeading";
import { createShellProps } from "./composition/pageProps";
import { PageLink } from "./composition/PageLink";
import * as demo from "../data/demo";
import { Button } from "../components/Button";
import { ProjectSummary } from "../components/ProjectSummary";
import { TaskListSection } from "../components/TaskListSection";
import { TaskFilterBar } from "../components/TaskFilterBar";
import { Pagination } from "../components/Pagination";
import { EmptyState } from "../components/EmptyState";
import { DetailPanel } from "../components/DetailPanel";
import { TaskDetailContent } from "../components/TaskDetailContent";
import { Dialog } from "../components/Dialog";
import { createTaskDetailProps } from "./composition/taskDetailProps";
import { createDialogProps, type DialogId } from "./composition/dialogProps";
import type { DetailId } from "../data/demo";

type TaskDialogId = Extract<
  DialogId,
  | "proposeTask"
  | "registerTask"
  | "editTask"
  | "approveTask"
  | "rejectTask"
  | "requestChanges"
  | "conflict"
  | "assignTask"
>;
interface TasksPageProps {
  mode?: "default" | "empty" | "loading";
  detail?: DetailId;
  dialog?: TaskDialogId;
}

export function TasksPage({
  mode = "default",
  detail,
  dialog,
}: TasksPageProps) {
  const { project, tasks, stateLabels, viewers, details } = demo;
  const detailData = detail ? details[detail] : undefined;
  const viewer =
    detailData?.viewer ??
    (dialog === "registerTask" ? viewers.admin : viewers.member);
  const shell = createShellProps({ viewer, projectName: project.name });
  const detailProps = detailData
    ? createTaskDetailProps(detailData, project, stateLabels)
    : undefined;
  const dialogProps = dialog ? createDialogProps(dialog, demo) : undefined;
  const taskRows = tasks.map((task) => ({
    id: String(task.taskId),
    numberLabel: `#${task.taskId}`,
    title: task.title,
    href: `?screen=${task.detailScreen}`,
    status: { state: task.state, label: stateLabels[task.state] },
    assigneeLabel: task.assignee,
    assigneeMuted: task.assignee === "미할당",
    creatorLabel: task.creator,
    updatedAt: project.updatedAt,
  }));
  const statusOptions = [
    { value: "all", label: "전체" },
    ...Object.entries(stateLabels).map(([value, label]) => ({ value, label })),
  ];

  const loading = mode === "loading";
  const empty = mode === "empty";
  return (
    <>
      <div inert={Boolean(detail || dialog)}>
        <AppShell {...shell}>
          <ProjectSummary
            name={project.name}
            description={project.description}
            dates={[
              { label: "생성일", value: project.createdAt },
              { label: "수정일", value: project.updatedAt },
            ]}
          />
          <TaskListSection
            heading={
              <PageHeading
                title="작업"
                action={
                  <PageLink screen="10" tone="primary">
                    작업 만들기
                  </PageLink>
                }
              />
            }
            label="프로젝트 작업"
            rows={empty ? [] : taskRows}
            loading={loading}
            loadingRowCount={7}
            loadingLabel="작업 목록을 불러오는 중입니다."
            filters={
              <TaskFilterBar
                statusOptions={statusOptions}
                statusValue={empty ? demo.emptySearch.state : "all"}
                searchValue={empty ? demo.emptySearch.keyword : ""}
                disabled={loading}
                searchAction={<Button disabled={loading}>검색</Button>}
              />
            }
            emptyContent={
              <EmptyState
                title="검색 결과가 없습니다"
                description="검색어나 상태 조건을 변경해 보세요."
                action={<PageLink screen="03">조건 초기화</PageLink>}
              />
            }
            pagination={
              <Pagination
                totalLabel={
                  loading
                    ? "불러오는 중…"
                    : empty
                      ? "총 0개"
                      : `총 ${tasks.length}개`
                }
                pageSizeLabel={`${demo.pageSize}개씩 보기`}
                pageLabel={empty ? "0 / 0페이지" : "1 / 1페이지"}
                previousAction={
                  <Button disabled aria-label="이전 페이지">
                    ←
                  </Button>
                }
                nextAction={
                  <Button disabled aria-label="다음 페이지">
                    →
                  </Button>
                }
              />
            }
          />
        </AppShell>
      </div>
      {detailProps && (
        <div inert={Boolean(dialog)}>
          <DetailPanel
            label={detailProps.label}
            numberLabel={detailProps.numberLabel}
            menu={detailProps.menu}
            closeAction={
              <PageLink
                screen="03"
                tone="quiet"
                label="상세 닫기"
                className="icon-button"
              >
                ×
              </PageLink>
            }
          >
            <TaskDetailContent {...detailProps.content} />
          </DetailPanel>
        </div>
      )}
      {dialog === "conflict" && (
        <div inert>
          <Dialog {...createDialogProps("editTask", demo)} />
        </div>
      )}
      {dialogProps && <Dialog {...dialogProps} />}
    </>
  );
}
