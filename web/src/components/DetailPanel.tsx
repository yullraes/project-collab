import type { ReactNode } from "react";
import "./TaskDetail.css";

export interface DetailPanelProps {
  label: string;
  numberLabel: string;
  menu?: ReactNode;
  closeAction: ReactNode;
  children: ReactNode;
  footer?: ReactNode;
}
export function DetailPanel({
  label,
  numberLabel,
  menu,
  closeAction,
  children,
  footer,
}: DetailPanelProps) {
  return (
    <div className="drawer-backdrop">
      <section
        className="detail-drawer"
        role="dialog"
        aria-modal="true"
        aria-label={label}
      >
        <header className="drawer-header">
          <span>
            {numberLabel} <span className="muted">작업 상세</span>
          </span>
          <div>
            {menu}
            {closeAction}
          </div>
        </header>
        <div className="detail-body">{children}</div>
        {footer && <footer className="detail-footer">{footer}</footer>}
      </section>
    </div>
  );
}
