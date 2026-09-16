import { AppShell } from "../components/AppShell";
import { createShellProps } from "./composition/pageProps";
import { PageLink } from "./composition/PageLink";
import * as demo from "../data/demo";
import { AccessState } from "../components/AccessState";
import { Notice } from "../components/Notice";

export function ForbiddenPage() {
  const shell = createShellProps({ global: true, viewer: demo.viewers.global });
  return (
    <AppShell {...shell}>
      <AccessState
        message={
          <Notice title="프로젝트에 접근할 수 없습니다" tone="warning">
            프로젝트 멤버인지 확인해 주세요. 접근 권한이 변경되었을 수 있습니다.
          </Notice>
        }
        action={
          <PageLink screen="01" tone="primary">
            내 프로젝트로
          </PageLink>
        }
      />
    </AppShell>
  );
}
