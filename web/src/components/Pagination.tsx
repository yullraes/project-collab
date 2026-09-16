import type { ReactNode } from "react";
import "./Pagination.css";

export interface PaginationProps {
  totalLabel: string;
  pageSizeLabel: string;
  pageLabel: string;
  previousAction: ReactNode;
  nextAction: ReactNode;
}
export function Pagination({
  totalLabel,
  pageSizeLabel,
  pageLabel,
  previousAction,
  nextAction,
}: PaginationProps) {
  return (
    <div className="pagination">
      <span>{totalLabel}</span>
      <div>
        <span>{pageSizeLabel}</span>
        <span>{pageLabel}</span>
        {previousAction}
        {nextAction}
      </div>
    </div>
  );
}
