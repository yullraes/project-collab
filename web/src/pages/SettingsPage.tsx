import { AppShell } from "../components/AppShell";
import { PageHeading } from "../components/PageHeading";
import { createShellProps } from "./composition/pageProps";
import { PageLink } from "./composition/PageLink";
import * as demo from "../data/demo";
import { Button } from "../components/Button";
import { Metadata } from "../components/Metadata";
import { ProjectSettings } from "../components/ProjectSettings";
import { Dialog } from "../components/Dialog";
import { createProjectFields } from "./composition/pageProps";
import { createDialogProps } from "./composition/dialogProps";

export function SettingsPage({
  confirmDelete = false,
}: {
  confirmDelete?: boolean;
}) {
  const { project, viewers } = demo;
  const shell = createShellProps({
    active: "설정",
    viewer: viewers.owner,
    projectName: project.name,
  });
  const projectFields = createProjectFields(project);
  const dialog = confirmDelete
    ? createDialogProps("deleteProject", demo)
    : undefined;
  return (
    <>
      <div inert={confirmDelete}>
        <AppShell {...shell}>
          <PageHeading title="프로젝트 설정" />
          <ProjectSettings
            fields={projectFields}
            metadata={
              <Metadata
                items={[
                  ["생성일", project.createdAt],
                  ["최근 수정일", project.updatedAt],
                ]}
              />
            }
            saveAction={<Button tone="primary">변경사항 저장</Button>}
            danger={{
              title: "프로젝트 삭제",
              description:
                "프로젝트의 모든 작업과 멤버 정보가 함께 삭제됩니다.",
              action: (
                <PageLink screen="27" tone="danger">
                  프로젝트 삭제
                </PageLink>
              ),
            }}
          />
        </AppShell>
      </div>
      {dialog && <Dialog {...dialog} />}
    </>
  );
}
