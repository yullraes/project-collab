import { AppShell } from "../components/AppShell";
import { PageHeading } from "../components/PageHeading";
import { createShellProps } from "./composition/pageProps";
import { PageLink } from "./composition/PageLink";
import * as demo from "../data/demo";
import { ProjectsTable } from "../components/ProjectsTable";
import { EmptyState } from "../components/EmptyState";
import { Dialog } from "../components/Dialog";
import { createDialogProps } from "./composition/dialogProps";

export function ProjectsPage({ create = false }: { create?: boolean }) {
  const { project, viewers } = demo;
  const shell = createShellProps({
    global: true,
    active: "내 프로젝트",
    viewer: viewers.global,
  });
  const dialog = create ? createDialogProps("createProject", demo) : undefined;
  return (
    <>
      <div inert={create}>
        <AppShell {...shell}>
          <PageHeading
            title="내 프로젝트"
            description="참여 중인 프로젝트에서 작업을 이어가세요."
            action={
              <PageLink screen="02" tone="primary">
                프로젝트 만들기
              </PageLink>
            }
          />
          <ProjectsTable
            rows={[
              {
                id: String(project.id),
                name: project.name,
                description: project.description,
                updatedAt: project.updatedAt,
                href: "?screen=03",
              },
            ]}
            emptyContent={<EmptyState title="참여 중인 프로젝트가 없습니다" />}
          />
        </AppShell>
      </div>
      {dialog && <Dialog {...dialog} />}
    </>
  );
}
