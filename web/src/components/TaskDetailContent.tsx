import type { ReactNode } from "react";
import { Metadata, type MetadataProps } from "./Metadata";
import { StatusBadge, type StatusBadgeProps } from "./StatusBadge";
import "./TaskDetail.css";

export interface TaskDetailContentProps {
  title: string;
  status: StatusBadgeProps;
  description: string;
  metadata: MetadataProps;
  notice?: ReactNode;
  guidance?: string;
  actions?: ReactNode;
  note?: string;
}
export function TaskDetailContent({
  title,
  status,
  description,
  metadata,
  notice,
  guidance,
  actions,
  note,
}: TaskDetailContentProps) {
  return (
    <>
      <div className="detail-title">
        <StatusBadge {...status} />
        <h1>{title}</h1>
      </div>
      {notice}
      {(guidance || actions) && (
        <div className="detail-guidance">
          {guidance && <p>{guidance}</p>}
          {actions && <div className="detail-actions">{actions}</div>}
        </div>
      )}
      <section className="content-section">
        <h2>작업 내용</h2>
        <p className="task-description">{description}</p>
      </section>
      <section className="content-section">
        <h2>작업 정보</h2>
        <Metadata {...metadata} />
      </section>
      {note && <p className="footnote">{note}</p>}
    </>
  );
}
