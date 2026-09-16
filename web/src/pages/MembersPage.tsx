import { AppShell } from "../components/AppShell";
import { PageHeading } from "../components/PageHeading";
import { createShellProps } from "./composition/pageProps";
import { PageLink } from "./composition/PageLink";
import * as demo from "../data/demo";
import { Button } from "../components/Button";
import { MembersTable } from "../components/MembersTable";
import { ProjectSummary } from "../components/ProjectSummary";
import { EmptyState } from "../components/EmptyState";
import { Dialog } from "../components/Dialog";
import { createDialogProps } from "./composition/dialogProps";

export function MembersPage({
  dialog,
}: {
  dialog?: "addMember" | "changeRole" | "removeMember";
}) {
  const { members, project, viewers } = demo;
  const shell = createShellProps({
    active: "멤버",
    viewer: viewers.admin,
    projectName: project.name,
  });
  const dialogProps = dialog ? createDialogProps(dialog, demo) : undefined;
  const memberRows = members.map((member) => ({
    id: String(member.id),
    name: member.name,
    annotation:
      member.id === demo.memberManagement.currentUserId ? "(나)" : undefined,
    userId: String(member.id),
    email: member.email,
    roleLabel: member.role,
    joinedAt: member.joinedAt,
    actions: (
      <>
        <PageLink screen="18" tone="quiet">
          역할 변경
        </PageLink>
        {member.id === demo.memberManagement.protectedOwnerId ? (
          <Button tone="quiet" disabled aria-describedby="owner-rule">
            멤버 제거
          </Button>
        ) : (
          <PageLink screen="26" tone="quiet">
            멤버 제거
          </PageLink>
        )}
      </>
    ),
  }));

  return (
    <>
      <div inert={Boolean(dialog)}>
        <AppShell {...shell}>
          <ProjectSummary
            name={project.name}
            description={project.description}
          />
          <PageHeading
            title="프로젝트 멤버"
            description="함께 작업하는 멤버와 프로젝트 역할을 관리합니다."
            action={
              <PageLink screen="17" tone="primary">
                멤버 추가
              </PageLink>
            }
          />
          <MembersTable
            rows={memberRows}
            totalLabel={`총 ${members.length}명`}
            emptyContent={<EmptyState title="멤버가 없습니다" />}
          />
          <p className="footnote" id="owner-rule">
            프로젝트에는 소유자가 최소 한 명 필요합니다. 마지막 소유자는
            제거하거나 다른 역할로 변경할 수 없습니다.
          </p>
        </AppShell>
      </div>
      {dialogProps && <Dialog {...dialogProps} />}
    </>
  );
}
