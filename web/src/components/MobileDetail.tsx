import type { ReactNode } from "react";
import "./TaskDetail.css";
import "./MobileDetail.css";

export interface MobileDetailProps {
  projectName: string;
  viewerLabel: string;
  backAction: ReactNode;
  menu?: ReactNode;
  children: ReactNode;
  actions: ReactNode;
}
export function MobileDetail({
  projectName,
  viewerLabel,
  backAction,
  menu,
  children,
  actions,
}: MobileDetailProps) {
  return (
    <main className="mobile-detail">
      <header className="mobile-header">
        {backAction}
        <strong>{projectName}</strong>
        {menu}
      </header>
      <div className="mobile-viewer">{viewerLabel}</div>
      <div className="detail-body">{children}</div>
      <footer className="detail-footer">{actions}</footer>
    </main>
  );
}
